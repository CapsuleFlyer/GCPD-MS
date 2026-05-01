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

    /** Search suspects by name and/or crime type (for UC-05 Identify Repeat Offender). */
    public List<String[]> searchSuspects(String nameQuery, String crimeQuery, int minRisk) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT s.*, COUNT(cs.caseID) AS caseCount " +
                     "FROM Suspects s LEFT JOIN Case_Suspects cs ON s.suspectID = cs.suspectID " +
                     "WHERE s.name LIKE ? AND (? = '' OR s.criminalHistory LIKE ?) " +
                     "AND s.riskLevel >= ? " +
                     "GROUP BY s.suspectID, s.name, s.criminalHistory, s.riskLevel, s.isRepeatOffender";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameQuery + "%");
            ps.setString(2, crimeQuery == null ? "" : crimeQuery);
            ps.setString(3, "%" + (crimeQuery == null ? "" : crimeQuery) + "%");
            ps.setInt(4, minRisk <= 0 ? 1 : minRisk);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("suspectID"), rs.getString("name"),
                    rs.getString("criminalHistory"), String.valueOf(rs.getInt("riskLevel")),
                    String.valueOf(rs.getBoolean("isRepeatOffender")),
                    String.valueOf(rs.getInt("caseCount"))
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
        String sql = "SELECT * FROM Suspects";
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
