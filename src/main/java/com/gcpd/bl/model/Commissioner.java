package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Commissioner — high-level strategic lead.
 * GRASP: Facade Controller for high-risk approvals.
 */
public class Commissioner extends User {
    private String departmentID;
    private List<Operation> operationQueue;

    public Commissioner(String userID, String name, String credential, String departmentID) {
        super(userID, name, credential);
        this.departmentID   = departmentID;
        this.operationQueue = new ArrayList<>();
    }

    @Override
    public String getRole() { return "Commissioner"; }

    /**
     * Approves a high-risk operation.
     * GRASP Facade Controller: single approval authority.
     */
    public boolean approveOperation(String opID, List<Operation> operations) {
        for (Operation op : operations) {
            if (op.getOperationID().equals(opID)) {
                op.approve(this.userID);
                return true;
            }
        }
        return false;
    }

    /** Rejects an operation with a reason. */
    public boolean rejectOperation(String opID, String reason, List<Operation> operations) {
        for (Operation op : operations) {
            if (op.getOperationID().equals(opID)) {
                op.reject(reason);
                return true;
            }
        }
        return false;
    }

    /** Views crime analytics report. */
    public CrimeAnalytics viewAnalytics() {
        return new CrimeAnalytics("RPT-" + System.currentTimeMillis(), this.userID);
    }

    public String getDepartmentID()         { return departmentID; }
    public List<Operation> getOperationQueue() { return operationQueue; }
}
