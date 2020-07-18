package com.lanmaoly.cloud.support;

import com.lanmaoly.util.lang.ExceptionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReactorUtils {

    public static Triple<Connection, PreparedStatement, ResultSet> query(DataSource dataSource, String sql, Object... args) throws SQLException {
        boolean ok = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            new ArgumentPreparedStatementSetter(args).setValues(ps);
            rs = ps.executeQuery();
            Triple<Connection, PreparedStatement, ResultSet> result = Triple.of(connection, ps, rs);
            ok = true;
            return result;
        } finally {
            if (!ok) {
                close(rs);
                close(ps);
                close(connection);
            }
        }
    }

    public static <T> Flux<T> query(DataSource dataSource, String sql, RowMapper<T> handler, Object... args) {
        return Flux.generate(
                () -> {
                    try {
                        return query(dataSource, sql, args);
                    } catch (SQLException e) {
                        throw ExceptionUtils.throwUnchecked(e);
                    }
                },
                (state, sink) -> {
                    try {
                        if (state.getRight().next()) {
                            T row = handler.mapRow(state.getRight(), 0);
                            if (row == null) {
                                throw new NullPointerException("mapRow不能返回null");
                            }
                            sink.next(row);
                        } else {
                            sink.complete();
                        }
                        return state;
                    } catch (SQLException e) {
                        throw ExceptionUtils.throwUnchecked(e);
                    }
                }, s -> {
                    close(s.getRight());
                    close(s.getMiddle());
                    close(s.getLeft());
                });
    }

    private static void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {
        }
    }
}
