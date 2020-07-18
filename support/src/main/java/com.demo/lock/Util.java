package com.lanmaoly.cloud.support.lock;

import com.lanmaoly.util.lang.JdbcUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Util {

    private static SQLExceptionTranslator TRANSLATOR = new SQLErrorCodeSQLExceptionTranslator();

    static void close(AutoCloseable statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (Exception ignored) {
        }
    }

    static void randomSleep(int min, int max) throws InterruptedException {
        int i = new Random().nextInt(max - min);
        Thread.sleep(min + i);
    }

    static int execute(DataSource dataSource, String sql, Object... args) throws DataAccessException {
        try {
            return JdbcUtils.execute(dataSource, sql, args);
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    static int execute(Connection connection, String sql, Object... args) throws DataAccessException {
        try {
            return JdbcUtils.execute(connection, sql, args);
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    static List<Map<String, Object>> query(DataSource dataSource, String sql, Object... args) throws DataAccessException {
        try {
            return JdbcUtils.query(dataSource, sql, args);
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    private static DataAccessException translate(String sql, SQLException e) {
        DataAccessException ex = TRANSLATOR.translate("", sql, e);
        if (ex != null) {
            return ex;
        } else {
            return new UncategorizedSQLException(e.getMessage(), sql, e);
        }
    }
}
