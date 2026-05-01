package com.gcpd.bl.model;

import java.time.LocalDateTime;

/**
 * Operation — GRASP Indirection + Controller.
 * Buffer between Commissioner and the field team.
 * GoF Observer: Subject that notifies Sergeant on state change.
 */
public class Operation {
    private String operationID;
    private String requestedBy;     // Sergeant userID
    private String status;          // Pending, Approved, Rejected
    private String riskLevel;
    private String linkedCaseID;
    private LocalDateTime timestamp;
    private String approvedBy;      // Commissioner userID
    private String rejectReason;
    private String description;

    public Operation(String operationID, String requestedBy,
                     String riskLevel, String linkedCaseID, String description) {
        this.operationID  = operationID;
        this.requestedBy  = requestedBy;
        this.status       = "Pending";
        this.riskLevel    = riskLevel;
        this.linkedCaseID = linkedCaseID;
        this.description  = description;
        this.timestamp    = LocalDateTime.now();
    }

    /** Commissioner approves this operation. */
    public void approve(String commissionerID) {
        this.status     = "Approved";
        this.approvedBy = commissionerID;
    }

    /** Commissioner rejects this operation. */
    public void reject(String reason) {
        this.status       = "Rejected";
        this.rejectReason = reason;
    }

    /** Log approval with timestamp (owned by Operation — Information Expert). */
    public String logApproval() {
        return "Operation " + operationID + " | Status=" + status
                + " | By=" + approvedBy + " | At=" + timestamp;
    }

    public String getOperationID()   { return operationID; }
    public String getRequestedBy()   { return requestedBy; }
    public String getStatus()        { return status; }
    public String getRiskLevel()     { return riskLevel; }
    public String getLinkedCaseID()  { return linkedCaseID; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getApprovedBy()    { return approvedBy; }
    public String getRejectReason()  { return rejectReason; }
    public String getDescription()   { return description; }
}
