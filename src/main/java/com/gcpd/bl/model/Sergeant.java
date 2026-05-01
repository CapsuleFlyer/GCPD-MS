package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sergeant — mid-level operations manager.
 * GRASP: Controller (assignment workflow), Low Coupling (delegates to Case/Detective).
 */
public class Sergeant extends User {
    private String squadID;
    private List<Detective> assignedDetectives;

    public Sergeant(String userID, String name, String credential, String squadID) {
        super(userID, name, credential);
        this.squadID            = squadID;
        this.assignedDetectives = new ArrayList<>();
    }

    @Override
    public String getRole() { return "Sergeant"; }

    /**
     * Assigns a detective to a case after workload check.
     * GRASP Low Coupling: Sergeant delegates to Case.assignDetective(),
     * not directly touching Detective internals.
     */
    public boolean assignDetective(Detective detective, Case c) {
        if (detective.getWorkloadScore() >= 5) {
            return false; // Detective at capacity
        }
        c.assignDetective(detective);
        detective.addActiveCase(c);
        return true;
    }

    /** Reviews all currently unassigned cases. */
    public List<Case> reviewIncidents(List<Case> allCases) {
        List<Case> unassigned = new ArrayList<>();
        for (Case cas : allCases) {
            if (cas.getAssignedDetective() == null) {
                unassigned.add(cas);
            }
        }
        return unassigned;
    }

    /** Monitors progress of a specific case. */
    public String monitorCase(Case c) {
        return "Case " + c.getCaseID() + " | Status: " + c.getStatus()
                + " | Detective: " + (c.getAssignedDetective() != null
                    ? c.getAssignedDetective().getName() : "Unassigned");
    }

    /** Escalates a case to higher priority. */
    public void escalateCaseStatus(Case c) {
        c.updateStatus("Escalated");
    }

    public String getSquadID()                         { return squadID; }
    public List<Detective> getAssignedDetectives()     { return assignedDetectives; }
    public void addDetective(Detective d)               { assignedDetectives.add(d); }
}
