package com.gcpd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=GCPD_DB;"
                    + "user=sa;"
                    + "password=YourStrongPassword123;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                connection = DriverManager.getConnection(URL);

                System.out.println("✅ Connected using Windows Authentication");

            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver not found: " + e.getMessage());
            }
        }

        return connection;
    }
}