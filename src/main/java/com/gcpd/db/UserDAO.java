package com.gcpd.db;

import com.gcpd.bl.factory.UserFactory;
import com.gcpd.bl.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Data Access Object for Users table.
 * Handles all DB operations: insert, find, authenticate, list.
 */
public class UserDAO {

    /** Inserts a new user into the DB. */
    public boolean insertUser(String userID, String name, String role, String credential,
                              String badgeNumber, String squadID, String departmentID,
                              String vaultID, String labID) {
        String sql = "INSERT INTO Users (userID, name, role, credential, badgeNumber, squadID, " +
                     "departmentID, vaultID, labID, workloadScore) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.setString(2, name);
            ps.setString(3, role);
            ps.setString(4, credential);
            ps.setString(5, badgeNumber);
            ps.setString(6, squadID);
            ps.setString(7, departmentID);
            ps.setString(8, vaultID);
            ps.setString(9, labID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertUser error: " + e.getMessage());
            return false;
        }
    }

    /** Checks if a username (userID) already exists. */
    public boolean userExists(String userID) {
        String sql = "SELECT COUNT(*) FROM Users WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("userExists error: " + e.getMessage());
        }
        return false;
    }

    /** Finds a user by userID and credential (login). */
    public User authenticate(String userID, String credential) {
        String sql = "SELECT * FROM Users WHERE userID = ? AND credential = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.setString(2, credential);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("authenticate error: " + e.getMessage());
        }
        return null; // null means login failed
    }

    /** Returns all users. */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRowToUser(rs));
        } catch (SQLException e) {
            System.err.println("getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    /** Returns all users with role = 'Detective'. */
    public List<User> getAllDetectives() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = 'Detective'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRowToUser(rs));
        } catch (SQLException e) {
            System.err.println("getAllDetectives error: " + e.getMessage());
        }
        return list;
    }

    /** Updates the workload score for a detective. */
    public boolean updateWorkload(String userID, int score) {
        String sql = "UPDATE Users SET workloadScore = ? WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setString(2, userID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updateWorkload error: " + e.getMessage());
            return false;
        }
    }

    /** Maps a ResultSet row to the correct User subtype via UserFactory. */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String role  = rs.getString("role");
        String extra = switch (role) {
            case "Detective"         -> rs.getString("badgeNumber");
            case "Sergeant"          -> rs.getString("squadID");
            case "Commissioner"      -> rs.getString("departmentID");
            case "EvidenceCustodian" -> rs.getString("vaultID");
            case "ForensicAnalyst"   -> rs.getString("labID");
            default -> null;
        };
        return UserFactory.createUser(
            rs.getString("userID"),
            rs.getString("name"),
            rs.getString("credential"),
            role, extra
        );
    }
}
