package com.lanmaoly.cloud.support.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lanmaoly.util.lang.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("WeakerAccess")
public class AmqpCorePublisher {

    public static void ddl(DataSource dataSource) throws SQLException {
        String sql = "CREATE TABLE\n" +
                "    T_SYS_MQ_LOG\n" +
                "    (\n" +
                "        MOST_BITS BIGINT NOT NULL,\n" +
                "        LEAST_BITS BIGINT NOT NULL,\n" +
                "        CREATE_TIME DATETIME NOT NULL,\n" +
                "        EXCHANGE VARCHAR(50),\n" +
                "        ROUTING_KEY VARCHAR(50),\n" +
                "        PAYLOAD BLOB,\n" +
                "        PRIMARY KEY (MOST_BITS, LEAST_BITS)\n" +
                "    );\n" +
                "CREATE INDEX I_SYS_MQ_LOG_TIME ON T_SYS_MQ_LOG\n" +
                "    (\n" +
                "        CREATE_TIME ASC\n" +
                "    )";
        JdbcUtils.execute(dataSource, sql);
    }

    private final Logger logger = LoggerFactory.getLogger(AmqpCorePublisher.class);

    private final AmqpTemplate amqpTemplate;

    private final JdbcTemplate jdbcTemplate;

    private volatile boolean debugSkipSend;

    public AmqpCorePublisher(AmqpTemplate amqpTemplate, DataSource dataSource) {
        this.amqpTemplate = amqpTemplate;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void send(String exchange, String routingKey, Envelope envelope, Executor executor) {
        send(exchange, routingKey, envelope, executor, Collections.emptyMap());
    }

    public void send(String exchange, String routingKey, Envelope envelope, Executor executor, Map<String, String> header) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("必须在事务内发送消息!");
        }

        Message message = new Message(UUID.randomUUID(), exchange, routingKey, new Payload(envelope.getPayload(), header));
        // 存库
        save(message);

        // 事务提交后发送消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    executor.execute(() -> {
                        if (debugSkipSend) {
                            throw new AmqpException("debug");
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("受理消息成功, exchange={}, routingKey={}", exchange, routingKey);
                        }
                        convertAndSend(exchange, routingKey, envelope, header);
                        delete(message);
                    });
                } catch (RejectedExecutionException e) {
                    // 忽略此错误，等待retry
                    logger.warn("提交任务时被拒绝,丢弃消息. exchange={}, routingKey={}, 原因: {}", exchange, routingKey, e.getMessage());
                }
            }
        });
    }

    public void retry(Duration retryBefore) {

        OffsetDateTime time = OffsetDateTime.now().minus(retryBefore);
        String sql = "select * from T_SYS_MQ_LOG where CREATE_TIME <= ?";
        AtomicLong count = new AtomicLong();
        AtomicBoolean hasData = new AtomicBoolean();
        long start = System.currentTimeMillis();
        jdbcTemplate.query(sql, rs -> {
            try {
                if (!hasData.get()) {
                    logger.info("检测到需要补发的消息，开始处理...");
                    hasData.set(true);
                }
                Message message = load(rs);

                Envelope envelope = new Envelope(message.getPayload().getData());
                convertAndSend(message.getExchange(), message.getRoutingKey(), envelope, message.getPayload().getHeader());

                delete(message);

                count.incrementAndGet();
            } catch (ClassNotFoundException | IOException e) {
                throw new IllegalStateException(e);
            }
        }, Date.from(time.toInstant()));
        if (hasData.get()) {
            long end = System.currentTimeMillis();
            logger.info("共补发{}笔消息, 耗时{}毫秒", count, end - start);
        }
    }

    public void debugSkipSend(boolean value) {
        debugSkipSend = value;
    }

    private void convertAndSend(String exchange, String routingKey, Envelope envelope, Map<String, String> header) {
        amqpTemplate.convertAndSend(exchange, routingKey, envelope, msg -> {
            header.forEach((key, value) -> msg.getMessageProperties().setHeader(key, value));
            return msg;
        });
    }

    private void delete(Message message) {
        jdbcTemplate.update("delete from T_SYS_MQ_LOG where MOST_BITS = ? and LEAST_BITS = ?", message.getMostBits(), message.getLeastBits());
    }

    private void save(Message message) {
        try {
            byte[] data = Utils.toBytes(message.getPayload());
            String sql = "insert into T_SYS_MQ_LOG (MOST_BITS, LEAST_BITS, CREATE_TIME, EXCHANGE, ROUTING_KEY, PAYLOAD) values (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, message.getMostBits(), message.getLeastBits(), new Date(), message.getExchange(), message.getRoutingKey(), data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Message load(ResultSet rs) throws SQLException, IOException, ClassNotFoundException {
        long mostBits = rs.getLong("MOST_BITS");
        long leastBits = rs.getLong("LEAST_BITS");
        String exchange = rs.getString("EXCHANGE");
        String routingKey = rs.getString("ROUTING_KEY");
        byte[] data = Utils.getBytes(rs, "PAYLOAD");

        Payload payload = Utils.fromBytes(data, Thread.currentThread().getContextClassLoader());
        return new Message(mostBits, leastBits, exchange, routingKey, payload);
    }

    static class Message {

        private long mostBits;

        private long leastBits;

        private String exchange;

        private String routingKey;

        private Payload payload;

        public Message(UUID uuid, String exchange, String routingKey, Payload payload) {
            this.mostBits = uuid.getMostSignificantBits();
            this.leastBits = uuid.getLeastSignificantBits();
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
        }

        public Message(long mostBits, long leastBits, String exchange, String routingKey, Payload payload) {
            this.mostBits = mostBits;
            this.leastBits = leastBits;
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
        }

        public long getMostBits() {
            return mostBits;
        }

        public long getLeastBits() {
            return leastBits;
        }

        public String getExchange() {
            return exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public Payload getPayload() {
            return payload;
        }
    }

}
