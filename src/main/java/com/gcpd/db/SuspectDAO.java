package com.gcpd.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** SuspectDAO — DB operations for Suspects table. */
public class SuspectDAO {

    public boolean insertSuspect(String suspectID, String name, String criminalHistory, int riskLevel) {
        String sql = "INSERT INTO Suspects (suspectID, name, criminalHistory, riskLevel, isRepeatOffender) " +
                "VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, suspectID);
            ps.setString(2, name);
            ps.setString(3, criminalHistory);
            ps.setInt(4, riskLevel);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertSuspect error: " + e.getMessage());
            return false;
        }
    }

    /** Search suspects by name for UC-05. Pass empty string to get all. */
    public List<String[]> searchSuspects(String nameQuery, String crimeQuery, int minRisk) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT suspectID, name, CAST(criminalHistory AS VARCHAR(500)) AS criminalHistory, " +
                "riskLevel, isRepeatOffender " +
                "FROM Suspects WHERE name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (nameQuery == null ? "" : nameQuery) + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("suspectID"),
                        rs.getString("name"),
                        rs.getString("criminalHistory"),
                        String.valueOf(rs.getInt("riskLevel")),
                        rs.getBoolean("isRepeatOffender") ? "Yes" : "No",
                        "—"
                });
            }
        } catch (SQLException e) {
            System.err.println("searchSuspects error: " + e.getMessage());
        }
        return list;
    }

    /** Flags a suspect as a repeat offender. */
    public boolean flagRepeatOffender(String suspectID) {
        String sql = "UPDATE Suspects SET isRepeatOffender = 1 WHERE suspectID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, suspectID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("flagRepeatOffender error: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getAllSuspects() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT suspectID, name, CAST(criminalHistory AS VARCHAR(500)) AS criminalHistory, riskLevel, isRepeatOffender FROM Suspects";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("suspectID"), rs.getString("name"),
                        rs.getString("criminalHistory"), String.valueOf(rs.getInt("riskLevel")),
                        String.valueOf(rs.getBoolean("isRepeatOffender"))
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllSuspects error: " + e.getMessage());
        }
        return list;
    }
}