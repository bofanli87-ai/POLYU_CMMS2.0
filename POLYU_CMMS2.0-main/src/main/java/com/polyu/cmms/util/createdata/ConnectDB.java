package com.polyu.cmms.util.createdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectDB {
    public static void main(String[] args) {
        // 1. Database connection parameters (obtained from TiDB Cloud, replace password
        String host = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
        int port = 4000;
        String database = "test1";
        String username = "3yZKtrYwuR4Coqh.root";
        String password = "PGzKU7mGSEDy7CKt"; // Replace with your reset password
        // CA certificate path (relative path in resources directory, no need to write full path)
        String caPath = ConnectDB.class.getClassLoader().getResource("cert/isrgrootx1.pem").getPath();

        // 2. Construct JDBC URL (Windows path automatically compatible, no need to escape)
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?sslMode=VERIFY_IDENTITY&sslCa=%s",
                host, port, database, caPath
        );


        // 3. Connect to database and create table
        try (
                // Automatically close connection (try-with-resources syntax, no need to manually close)
                Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                Statement stmt = conn.createStatement()
        ) {
            System.out.println("✅ Database connection successful!");

            // Create table SQL (create users table, containing id, name, age fields)
            String createTableSql = "CREATE TABLE IF NOT EXISTS `users` (" +
                    "`id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'User ID'," +
                    "`name` VARCHAR(50) NOT NULL COMMENT 'Username'," +
                    "`age` INT COMMENT 'User age'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            String addingData = "INSERT INTO `users` (`name`, `age`) VALUES ('Zhang San', 25);";
            stmt.executeUpdate(createTableSql);
            stmt.executeUpdate(addingData);
            System.out.println("✅ Table `users` created successfully!");
            System.out.println("✅ Data inserted successfully!");

        } catch (Exception e) {
            System.err.println("❌ Operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}