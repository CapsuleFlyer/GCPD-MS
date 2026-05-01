package com.gcpd.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** ForensicReportDAO — DB operations for ForensicReports table (UC-03). */
public class ForensicReportDAO {

    public boolean insertReport(String reportID, String findings, String verdict,
                                String analystID, String evidenceID, String caseID) {
        String sql = "INSERT INTO ForensicReports (reportID, findings, verdict, analystID, " +
                     "linkedEvidenceID, linkedCaseID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportID);
            ps.setString(2, findings);
            ps.setString(3, verdict);
            ps.setString(4, analystID);
            ps.setString(5, evidenceID);
            ps.setString(6, caseID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertReport error: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getReportsByCase(String caseID) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM ForensicReports WHERE linkedCaseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("reportID"), rs.getString("findings"),
                    rs.getString("verdict"), rs.getString("analystID"),
                    rs.getString("linkedEvidenceID"), rs.getString("date")
                });
            }
        } catch (SQLException e) {
            System.err.println("getReportsByCase error: " + e.getMessage());
        }
        return list;
    }

    public List<String[]> getAllReports() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM ForensicReports ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("reportID"), rs.getString("findings"),
                    rs.getString("verdict"), rs.getString("analystID"),
                    rs.getString("linkedEvidenceID"), rs.getString("linkedCaseID"),
                    rs.getString("date")
                });
            }
        } catch (SQLException e) {
            System.err.println("getAllReports error: " + e.getMessage());
        }
        return list;
    }
}
