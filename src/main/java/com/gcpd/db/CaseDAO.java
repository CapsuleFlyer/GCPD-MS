package com.gcpd.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CaseDAO — DB operations for Cases table.
 */
public class CaseDAO {

    /** Inserts a new case (from escalated incident). */
    public boolean insertCase(String caseID, String incidentID, String priority) {
        String sql = "INSERT INTO Cases (caseID, status, priority, startDate, incidentID) " +
                     "VALUES (?, 'Reported', ?, GETDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseID);
            ps.setString(2, priority);
            ps.setString(3, incidentID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertCase error: " + e.getMessage());
            return false;
        }
    }

    /** Returns all cases with their incident info. */
    public List<String[]> getAllCases() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT c.caseID, c.status, c.priority, c.startDate, " +
                     "c.assignedDetective, i.location, i.crimeType " +
                     "FROM Cases c LEFT JOIN Incidents i ON c.incidentID = i.incidentID " +
                     "ORDER BY c.startDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("caseID"),
                    rs.getString("status"),
                    rs.getString("priority"),
                    rs.getString("startDate"),
                    rs.getString("assignedDetective"),
                    rs.getString("location"),
                    rs.getString("crimeType")
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllCases error: " + e.getMessage());
        }
        return list;
    }

    /** Returns cases not yet assigned to a detective. */
    public List<String[]> getUnassignedCases() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT c.caseID, c.status, c.priority, i.location, i.crimeType " +
                     "FROM Cases c LEFT JOIN Incidents i ON c.incidentID = i.incidentID " +
                     "WHERE c.assignedDetective IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("caseID"),
                    rs.getString("status"),
                    rs.getString("priority"),
                    rs.getString("location"),
                    rs.getString("crimeType")
                });
            }
        } catch (SQLException e) {
            System.err.println("getUnassignedCases error: " + e.getMessage());
        }
        return list;
    }

    /** Assigns a detective to a case and updates status. */
    public boolean assignDetective(String caseID, String detectiveID) {
        String sql = "UPDATE Cases SET assignedDetective = ?, status = 'UnderInvestigation' " +
                     "WHERE caseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, detectiveID);
            ps.setString(2, caseID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("assignDetective error: " + e.getMessage());
            return false;
        }
    }

    /** Updates the status of a case. */
    public boolean updateCaseStatus(String caseID, String newStatus) {
        String sql = "UPDATE Cases SET status = ? WHERE caseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, caseID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updateCaseStatus error: " + e.getMessage());
            return false;
        }
    }

    /** Returns case counts grouped by crime type (for analytics). */
    public List<String[]> getCrimeTypeCounts() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT i.crimeType, COUNT(*) AS cnt " +
                     "FROM Cases c JOIN Incidents i ON c.incidentID = i.incidentID " +
                     "GROUP BY i.crimeType";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{ rs.getString("crimeType"), rs.getString("cnt") });
            }
        } catch (SQLException e) {
            System.err.println("getCrimeTypeCounts error: " + e.getMessage());
        }
        return list;
    }
}
