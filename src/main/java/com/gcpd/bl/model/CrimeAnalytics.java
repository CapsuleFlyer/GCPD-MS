package com.gcpd.bl.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CrimeAnalytics — GRASP Pure Fabrication.
 * Decoupled analytics service for Commissioner's dashboard.
 */
public class CrimeAnalytics {
    private String reportID;
    private Map<String, Integer> statistics;   // crimeType -> count
    private LocalDateTime generatedDate;
    private String commissionerID;

    public CrimeAnalytics(String reportID, String commissionerID) {
        this.reportID       = reportID;
        this.statistics     = new HashMap<>();
        this.generatedDate  = LocalDateTime.now();
        this.commissionerID = commissionerID;
    }

    /** Generates statistics from a list of cases. */
    public void generate(List<Case> cases) {
        statistics.clear();
        for (Case c : cases) {
            String type = (c.getLinkedIncident() != null)
                    ? c.getLinkedIncident().getCrimeType() : "Unknown";
            statistics.merge(type, 1, Integer::sum);
        }
    }

    /** Returns cases filtered by crime type. */
    public List<Case> filterByCrimeType(String type, List<Case> cases) {
        return cases.stream()
                .filter(c -> c.getLinkedIncident() != null
                        && c.getLinkedIncident().getCrimeType().equalsIgnoreCase(type))
                .toList();
    }

    /** Returns the top N priority open cases. */
    public List<Case> rankPriorityCases(List<Case> cases, int topN) {
        return cases.stream()
                .filter(c -> !c.getStatus().equals("Closed"))
                .limit(topN)
                .toList();
    }

    public String getReportID()             { return reportID; }
    public Map<String, Integer> getStatistics() { return statistics; }
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public String getCommissionerID()       { return commissionerID; }
}
