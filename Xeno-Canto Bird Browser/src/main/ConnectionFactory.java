package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	// Database constants
	private static final String DB_NAME = "bird_recording";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "password";
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://localhost/"+DB_NAME+
                   "?user="+DB_USER+"&password="+DB_PASS+"&useSSL=false");
	}
}
