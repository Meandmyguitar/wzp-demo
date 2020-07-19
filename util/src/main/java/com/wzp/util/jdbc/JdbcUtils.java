package com.wzp.util.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {

	public static void closeQuietly(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
			}
		}
	}

	public static void closeQuietly(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	public static void closeQuietly(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
		}
	}
}
