package com.gcpd.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** OperationDAO — DB operations for Operations table (UC-04). */
public class OperationDAO {

    public boolean insertOperation(String opID, String requestedBy,
                                   String riskLevel, String linkedCaseID, String description) {
        String sql = "INSERT INTO Operations (operationID, requestedBy, status, riskLevel, linkedCaseID, description) " +
                     "VALUES (?, ?, 'Pending', ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, opID);
            ps.setString(2, requestedBy);
            ps.setString(3, riskLevel);
            ps.setString(4, linkedCaseID);
            ps.setString(5, description);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertOperation error: " + e.getMessage());
            return false;
        }
    }

    public boolean approveOperation(String opID, String commissionerID) {
        String sql = "UPDATE Operations SET status = 'Approved', approvedBy = ? WHERE operationID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commissionerID);
            ps.setString(2, opID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("approveOperation error: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectOperation(String opID, String reason) {
        String sql = "UPDATE Operations SET status = 'Rejected', rejectReason = ? WHERE operationID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setString(2, opID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("rejectOperation error: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getPendingOperations() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Operations WHERE status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("operationID"), rs.getString("requestedBy"),
                    rs.getString("status"), rs.getString("riskLevel"),
                    rs.getString("linkedCaseID"), rs.getString("description"),
                    rs.getString("timestamp")
                });
            }
        } catch (SQLException e) {
            System.err.println("getPendingOperations error: " + e.getMessage());
        }
        return list;
    }

    public List<String[]> getAllOperations() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Operations ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("operationID"), rs.getString("requestedBy"),
                    rs.getString("status"), rs.getString("riskLevel"),
                    rs.getString("description"), rs.getString("approvedBy"),
                    rs.getString("rejectReason"), rs.getString("timestamp")
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllOperations error: " + e.getMessage());
        }
        return list;
    }
}
