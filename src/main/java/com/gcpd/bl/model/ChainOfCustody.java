package com.gcpd.bl.model;

import java.time.LocalDateTime;

/**
 * ChainOfCustody — GRASP Pure Fabrication.
 * Separates custody-log persistence from Evidence to maintain high cohesion.
 */
public class ChainOfCustody {
    private String logID;
    private String evidenceID;
    private String oldStatus;
    private String newStatus;
    private String changedBy;       // userID
    private LocalDateTime timestamp;
    private String notes;

    public ChainOfCustody(String logID, String evidenceID,
                          String oldStatus, String newStatus, String changedBy) {
        this.logID      = logID;
        this.evidenceID = evidenceID;
        this.oldStatus  = oldStatus;
        this.newStatus  = newStatus;
        this.changedBy  = changedBy;
        this.timestamp  = LocalDateTime.now();
    }

    public void addEntry()                   { /* persisted via DB layer */ }
    public String export()                   { return toString(); }

    public String getLogID()                 { return logID; }
    public String getEvidenceID()            { return evidenceID; }
    public String getOldStatus()             { return oldStatus; }
    public String getNewStatus()             { return newStatus; }
    public String getChangedBy()             { return changedBy; }
    public LocalDateTime getTimestamp()      { return timestamp; }
    public String getNotes()                 { return notes; }
    public void setNotes(String notes)       { this.notes = notes; }

    @Override
    public String toString() {
        return "CustodyLog[" + logID + "] " + oldStatus + " -> " + newStatus
                + " by=" + changedBy + " at=" + timestamp;
    }
}
