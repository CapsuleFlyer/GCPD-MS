package com.gcpd.db;

import com.gcpd.bl.model.Incident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * IncidentDAO — DB operations for Incidents table.
 */
public class IncidentDAO {

    /** Inserts a new incident. */
    public boolean insertIncident(String incidentID, String location, String crimeType,
                                  String description, String reportedBy) {
        String sql = "INSERT INTO Incidents (incidentID, location, crimeType, description, " +
                     "status, reportedBy) VALUES (?, ?, ?, ?, 'Reported', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, incidentID);
            ps.setString(2, location);
            ps.setString(3, crimeType);
            ps.setString(4, description);
            ps.setString(5, reportedBy);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertIncident error: " + e.getMessage());
            return false;
        }
    }

    /** Returns all incidents from the DB. */
    public List<String[]> getAllIncidents() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Incidents ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("incidentID"),
                    rs.getString("location"),
                    rs.getString("crimeType"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("reportedBy"),
                    rs.getString("date")
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllIncidents error: " + e.getMessage());
        }
        return list;
    }

    /** Returns incidents with status = 'Reported' (not yet escalated). */
    public List<String[]> getReportedIncidents() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM Incidents WHERE status = 'Reported'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("incidentID"),
                    rs.getString("location"),
                    rs.getString("crimeType"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("reportedBy")
                });
            }
        } catch (SQLException e) {
            System.err.println("getReportedIncidents error: " + e.getMessage());
        }
        return list;
    }

    /** Updates the status of an incident. */
    public boolean updateIncidentStatus(String incidentID, String newStatus) {
        String sql = "UPDATE Incidents SET status = ? WHERE incidentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, incidentID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updateIncidentStatus error: " + e.getMessage());
            return false;
        }
    }
}
