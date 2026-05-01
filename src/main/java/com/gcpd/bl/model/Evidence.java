package com.gcpd.bl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evidence — GRASP Information Expert (owns its own status and case link).
 */
public class Evidence {
    private String evidenceID;
    private String type;
    private String status;          // Collected, InAnalysis, Transferred, Disposed, FlaggedForDisposal
    private String storageLocation;
    private LocalDateTime collectedDate;
    private String linkedCaseID;
    private boolean disposed;

    // Chain of custody log — Pure Fabrication (ChainOfCustody)
    private List<ChainOfCustody> custodyLog;

    public Evidence(String evidenceID, String type, String storageLocation, String linkedCaseID) {
        this.evidenceID      = evidenceID;
        this.type            = type;
        this.status          = "Collected";
        this.storageLocation = storageLocation;
        this.collectedDate   = LocalDateTime.now();
        this.linkedCaseID    = linkedCaseID;
        this.disposed        = false;
        this.custodyLog      = new ArrayList<>();
    }

    /** GRASP Information Expert: Evidence owns its own status. */
    public void updateStatus(String newStatus) { this.status = newStatus; }
    public void markDisposed()                 { this.disposed = true; this.status = "Disposed"; }
    public boolean validate()                  { return type != null && !type.isBlank(); }

    /**
     * Logs a chain-of-custody entry.
     * GRASP Pure Fabrication: ChainOfCustody is a fabricated class.
     */
    public ChainOfCustody logEntry(String changedBy, String newStatus, String oldStatus) {
        String logID = "LOG-" + System.currentTimeMillis();
        ChainOfCustody entry = new ChainOfCustody(logID, evidenceID, oldStatus, newStatus, changedBy);
        custodyLog.add(entry);
        return entry;
    }

    public List<ChainOfCustody> getCustodyLog() { return custodyLog; }

    public String getEvidenceID()       { return evidenceID; }
    public String getType()             { return type; }
    public String getStatus()           { return status; }
    public String getStorageLocation()  { return storageLocation; }
    public LocalDateTime getCollectedDate() { return collectedDate; }
    public String getLinkedCaseID()     { return linkedCaseID; }
    public boolean isDisposed()         { return disposed; }

    @Override
    public String toString() { return "Evidence[" + evidenceID + "] " + type + " Status=" + status; }
}
