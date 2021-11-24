package com.revature.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnector {
	private static SQLConnector instance;

	private SQLConnector() {
	}

	public static SQLConnector getInstance() {
		if (instance == null) instance = new SQLConnector();
		return instance;
	}
	//connects to sql server
	public static Connection getConnection(String Settings) throws SQLException {
		Properties loadProps = new Properties();
		try {
			loadProps.loadFromXML(SQLConnector.class.getClassLoader().getResourceAsStream(Settings));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Class.forName("org.postgresql.Driver");
			return DriverManager.getConnection(loadProps.getProperty("url"), loadProps.getProperty("username"), loadProps.getProperty("password"));
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return null;

	}
}
