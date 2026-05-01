package com.gcpd.bl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ForensicReport — GRASP Creator (ForensicAnalyst creates this).
 */
public class ForensicReport {
    private String reportID;
    private String findings;
    private String verdict;
    private LocalDateTime date;
    private ForensicAnalyst analyst;
    private Evidence linkedEvidence;
    private String linkedCaseID;

    public ForensicReport(String reportID, String findings, String verdict,
                          ForensicAnalyst analyst, Evidence linkedEvidence) {
        this.reportID      = reportID;
        this.findings      = findings;
        this.verdict       = verdict;
        this.date          = LocalDateTime.now();
        this.analyst       = analyst;
        this.linkedEvidence = linkedEvidence;
    }

    public void attachToCase(Case c)       { this.linkedCaseID = c.getCaseID(); }
    public String getSummary()             { return "Report[" + reportID + "]: " + verdict; }
    public boolean validateFindings()      { return findings != null && !findings.isBlank(); }

    public String getReportID()            { return reportID; }
    public String getFindings()            { return findings; }
    public String getVerdict()             { return verdict; }
    public LocalDateTime getDate()         { return date; }
    public ForensicAnalyst getAnalyst()    { return analyst; }
    public Evidence getLinkedEvidence()    { return linkedEvidence; }
    public String getLinkedCaseID()        { return linkedCaseID; }
    public String getAnalystID()           { return analyst != null ? analyst.getUserID() : null; }
}
