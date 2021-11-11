package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {
	private static final String url = "jdbc:postgresql://enterprise.cj9bj3alk8be.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1";
	private static final String username = "rhaynes";
	private static final String password = "WarpDamm1tWarp!!";
	private static SQLConnector instance;
	private static Connection connection;

	private SQLConnector() {
	}

	public static SQLConnector getInstance() {
		if (instance == null) instance = new SQLConnector();
		return instance;
	}
	//connects to sql server
	public static Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			try {
				Class.forName("org.postgresql.Driver");

				connection = DriverManager.getConnection(url, username, password);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;

	}
}
