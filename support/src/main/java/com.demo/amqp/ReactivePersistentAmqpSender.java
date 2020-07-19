package com.demo.amqp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.demo.JacksonBuilder;
import com.demo.PersistentAmqpMessageResult;
import com.demo.ReactorUtils;
import com.lanmaoly.util.lang.JdbcUtils;
import com.lanmaoly.util.lang.TimeUtils;
import com.rabbitmq.client.AMQP;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;


@SuppressWarnings("WeakerAccess")
public class ReactivePersistentAmqpSender {

    private static final ObjectMapper MAPPER = JacksonBuilder.build().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static void ddl(DataSource dataSource) throws SQLException {
        String ddl = "create table T_SYS_AMQP_MSG\n" +
                "(\n" +
                "    ID bigint auto_increment\n" +
                "        primary key,\n" +
                "    EXCHANGE varchar(200) null,\n" +
                "    ROUTING_KEY varchar(200) null,\n" +
                "    PAYLOAD blob null,\n" +
                "    CREATE_TIME datetime not null,\n" +
                "    COMPLETE_TIME datetime null,\n" +
                "    FAIL_COUNT int null,\n" +
                "    LAST_FAIL_TIME datetime null,\n" +
                "    PROPERTIES blob null\n" +
                ")";
        JdbcUtils.execute(dataSource, ddl);
    }

    private final Logger logger = LoggerFactory.getLogger(ReactivePersistentAmqpSender.class);

    private DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    public ReactivePersistentAmqpSender(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 持久化一个Amqp消息
     */
    public PersistentAmqpMessage persistent(String exchange, String routingKey, byte[] body, AMQP.BasicProperties properties) {
        try {
            byte[] propsBytes = toByteArray(properties);

            String sql = "insert into T_SYS_AMQP_MSG (EXCHANGE, ROUTING_KEY, PAYLOAD, CREATE_TIME, FAIL_COUNT, PROPERTIES) values (?, ?, ?, ?, ?, ?)";

            long now = System.currentTimeMillis();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rows = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, exchange);
                ps.setString(2, routingKey);
                ps.setBlob(3, new ByteArrayInputStream(body));
                ps.setTimestamp(4, new Timestamp(now));
                ps.setInt(5, 0);
                if (propsBytes != null) {
                    ps.setBytes(6, propsBytes);
                } else {
                    ps.setNull(6, Types.VARBINARY);
                }
                return ps;
            }, keyHolder);
            if (rows != 1) {
                throw new IllegalStateException("update返回不是1");
            }

            if (keyHolder.getKey() == null) {
                throw new IllegalStateException("未返回ID");
            }

            long id = keyHolder.getKey().longValue();
            return new PersistentAmqpMessage(id, routingKey, properties, body, exchange);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 发送PersistentAmqpMessage
     */
    public Flux<PersistentAmqpMessageResult> send(Sender sender, Publisher<PersistentAmqpMessage> messages) {
        return sender.sendWithPublishConfirms(Flux.from(messages).cast(OutboundMessage.class)).map(result -> {
            if (logger.isTraceEnabled()) {
                logger.trace("发送完成: {}", result.getOutboundMessage());
            }
            PersistentAmqpMessage msg = (PersistentAmqpMessage) result.getOutboundMessage();
            if (result.isAck()) {
                delete(msg.getId());
            } else {
                logger.warn("publish ack失败, id={}, exchange={}, routingKey={}", msg.getId(), msg.getExchange(), msg.getRoutingKey());
                // 更新重试次数
                updateFail(msg.getId());
            }
            return result;
        }).map(PersistentAmqpMessageResult::new);
    }

    public Mono<PersistentAmqpMessage> getById(long id) {
        String sql = "select * from T_SYS_AMQP_MSG where ID = ?";
        return ReactorUtils.query(dataSource, sql, (rs, i) -> load(rs), id).singleOrEmpty();
    }

    public Flux<PersistentAmqpMessage> query(LocalDateTime start, LocalDateTime end, int maxFailCount) {
        String sql = "select * from T_SYS_AMQP_MSG where CREATE_TIME between ? and ? and FAIL_COUNT <= ?";
        return ReactorUtils.query(dataSource, sql, (rs, i) -> load(rs), TimeUtils.asDate(start), TimeUtils.asDate(end), maxFailCount);
    }

    /**
     * 立即删除一个消息
     */
    public void delete(long id) {
        String sql;
        sql = "delete from T_SYS_AMQP_MSG where ID = ?";
        jdbcTemplate.update(sql, id);
    }

    private void updateFail(long id) {
        String sql = "update T_SYS_AMQP_MSG set FAIL_COUNT = FAIL_COUNT+1, LAST_FAIL_TIME=? where ID = ?";
        if (1 != jdbcTemplate.update(sql, new Date(), id)) {
            logger.warn("updateFail id={} 失败", id);
        }
    }

    private PersistentAmqpMessage load(ResultSet rs) throws SQLException {
        try {
            long id = rs.getLong("ID");
            String exchange = rs.getString("EXCHANGE");
            String routingKey = rs.getString("ROUTING_KEY");
            byte[] properties = rs.getBytes("PROPERTIES");
            byte[] data = Utils.getBytes(rs, "PAYLOAD");

            AMQP.BasicProperties basicProperties = fromByteArray(properties);
            return new PersistentAmqpMessage(id, routingKey, basicProperties, data, exchange);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static byte[] toByteArray(AMQP.BasicProperties properties) throws IOException {
        if (properties != null) {
            return MAPPER.writeValueAsBytes(properties);
        } else {
            return null;
        }
    }

    static AMQP.BasicProperties fromByteArray(byte[] data) throws IOException {
        return MAPPER.readValue(data, AMQP.BasicProperties.class);
    }
}
