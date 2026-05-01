package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ForensicAnalyst — technical specialist.
 * GRASP: Creator (creates ForensicReport), Information Expert.
 */
public class ForensicAnalyst extends User {
    private String labID;
    private List<ForensicReport> activeReports;

    public ForensicAnalyst(String userID, String name, String credential, String labID) {
        super(userID, name, credential);
        this.labID         = labID;
        this.activeReports = new ArrayList<>();
    }

    @Override
    public String getRole() { return "ForensicAnalyst"; }

    /**
     * Analyzes evidence and creates a forensic report.
     * GRASP Creator: ForensicAnalyst generates and records the data.
     */
    public ForensicReport generateReport(Evidence evidence, String findings, String verdict) {
        String reportID = "RPT-" + System.currentTimeMillis();
        ForensicReport report = new ForensicReport(reportID, findings, verdict, this, evidence);
        activeReports.add(report);
        return report;
    }

    /** Attaches a forensic report to a case. */
    public void attachReport(ForensicReport report, Case c) {
        report.attachToCase(c);
    }

    /** Flags evidence for disposal. */
    public void flagDisposal(Evidence evidence) {
        evidence.updateStatus("FlaggedForDisposal");
    }

    public String getLabID()                      { return labID; }
    public List<ForensicReport> getActiveReports(){ return activeReports; }
}
