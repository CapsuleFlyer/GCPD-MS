package com.gcpd.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnectionTest {

    public static void main(String[] args) {

        try {
            Connection conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM Users";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("👮 Users in Database:\n");

            while (rs.next()) {
                System.out.println(
                        "UserID: " + rs.getString("userID") +
                                " | Name: " + rs.getString("name") +
                                " | Role: " + rs.getString("role") +
                                " | Badge: " + rs.getString("badgeNumber") +
                                " | Department: " + rs.getString("departmentID")
                );
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.out.println("❌ Read failed");
            e.printStackTrace();
        }
    }
}