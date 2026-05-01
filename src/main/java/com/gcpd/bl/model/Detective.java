package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Detective — primary investigative officer.
 * GRASP: Creator (creates Incident), Controller (investigation lifecycle).
 */
public class Detective extends User {
    private String badgeNumber;
    private List<Case> activeCases;
    private int workloadScore;

    public Detective(String userID, String name, String credential, String badgeNumber) {
        super(userID, name, credential);
        this.badgeNumber   = badgeNumber;
        this.activeCases   = new ArrayList<>();
        this.workloadScore = 0;
    }

    @Override
    public String getRole() { return "Detective"; }

    /**
     * Creates and returns a new Incident.
     * GRASP Creator: Detective aggregates/records incident data.
     */
    public Incident reportIncident(String location, String crimeType, String description) {
        String incidentID = "INC-" + System.currentTimeMillis();
        Incident incident = new Incident(incidentID, location, crimeType, description, this);
        return incident;
    }

    /**
     * Escalates an incident to a full Case.
     */
    public Case escalateToCase(Incident incident) {
        if (!incident.getStatus().equals("Reported")) {
            throw new IllegalStateException("Only 'Reported' incidents can be escalated.");
        }
        String caseID = "CASE-" + System.currentTimeMillis();
        Case newCase = new Case(caseID, incident);
        incident.setStatus("Escalated");
        return newCase;
    }

    /** Updates the status of an assigned case. */
    public void updateCaseStatus(Case c, String newStatus) {
        c.updateStatus(newStatus);
    }

    /** Checks if a suspect is a repeat offender by matching their history. */
    public boolean identifyRepeatOffender(Suspect suspect) {
        return suspect.isRepeatOffender();
    }

    /** Links a suspect to a case. */
    public void linkSuspect(Case c, Suspect suspect) {
        c.addSuspect(suspect);
    }

    // Workload management
    public int getWorkloadScore()              { return workloadScore; }
    public void incrementWorkload()            { this.workloadScore++; }
    public void decrementWorkload()            { if (workloadScore > 0) workloadScore--; }
    public List<Case> getActiveCases()         { return activeCases; }
    public void addActiveCase(Case c)          { activeCases.add(c); workloadScore++; }
    public String getBadgeNumber()             { return badgeNumber; }
}
