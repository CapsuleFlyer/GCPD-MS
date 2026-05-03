package com.gcpd.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EvidenceDAO — DB operations for Evidence and ChainOfCustody tables.
 */
public class EvidenceDAO {

    public boolean insertEvidence(String evidenceID, String type,
                                  String storageLocation, String linkedCaseID) {
        String sql = "INSERT INTO Evidence (evidenceID, type, status, storageLocation, linkedCaseID) " +
                     "VALUES (?, ?, 'Collected', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evidenceID);
            ps.setString(2, type);
            ps.setString(3, storageLocation);
            ps.setString(4, linkedCaseID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertEvidence error: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getEvidenceByCase(String caseID) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Evidence WHERE linkedCaseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("evidenceID"), rs.getString("type"),
                    rs.getString("status"), rs.getString("storageLocation")
                });
            }
        } catch (SQLException e) {
            System.err.println("getEvidenceByCase error: " + e.getMessage());
        }
        return list;
    }

    public List<String[]> getAllEvidence() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Evidence ORDER BY collectedDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("evidenceID"), rs.getString("type"),
                    rs.getString("status"), rs.getString("storageLocation"),
                    rs.getString("linkedCaseID"), rs.getString("collectedDate")
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllEvidence error: " + e.getMessage());
        }
        return list;
    }

    public boolean transferEvidence(String evidenceID, String destination) {
        String sql = "UPDATE Evidence SET status = 'Transferred', storageLocation = ? WHERE evidenceID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, destination);
            ps.setString(2, evidenceID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("transferEvidence error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEvidenceStatus(String evidenceID, String newStatus) {
        String sql = "UPDATE Evidence SET status = ? WHERE evidenceID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, evidenceID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updateEvidenceStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean insertCustodyLog(String logID, String evidenceID,
                                    String oldStatus, String newStatus, String changedBy, String notes) {
        String sql = "INSERT INTO ChainOfCustody (logID, evidenceID, oldStatus, newStatus, changedBy, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, logID);
            ps.setString(2, evidenceID);
            ps.setString(3, oldStatus);
            ps.setString(4, newStatus);
            ps.setString(5, changedBy);
            ps.setString(6, notes);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertCustodyLog error: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getCustodyLog(String evidenceID) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM ChainOfCustody WHERE evidenceID = ? ORDER BY timestamp ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evidenceID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("logID"), rs.getString("oldStatus"),
                    rs.getString("newStatus"), rs.getString("changedBy"),
                    rs.getString("timestamp"), rs.getString("notes")
                });
            }
        } catch (SQLException e) {
            System.err.println("getCustodyLog error: " + e.getMessage());
        }
        return list;
    }
}
