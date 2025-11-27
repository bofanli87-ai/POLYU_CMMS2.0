package com.polyu.cmms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// import java.sql.Statement;

public class DatabaseUtil {
    // TiDB Cloud database connection configuration (according to the new configuration provided by the user)
    private static final String URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test1?sslMode=VERIFY_IDENTITY";
    private static final String USERNAME = "3yZKtrYwuR4Coqh.root";
    private static final String PASSWORD = "PGzKU7mGSEDy7CKt"; // Use the password provided by the user
    private static Connection connection = null;

    // Get database connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Establish connection
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                // Set transaction isolation level to READ COMMITTED
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                // // Initialize database tables (if needed)
                // initializeDatabase();
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL driver not found", e);
            }
        }
        return connection;
    }

    // Close database connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}