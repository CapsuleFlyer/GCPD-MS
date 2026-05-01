package com.gcpd.bl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Case — central investigation unit.
 * GRASP: Information Expert + High Cohesion (owns status, suspects, evidence logic).
 * GoF Observer: Case is the Subject; notifies Sergeant/Commissioner on status change.
 */
public class Case {
    private String caseID;
    private String status;          // Reported, UnderInvestigation, Escalated, Closed, ColdCase
    private String priority;        // Low, Medium, High
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Detective assignedDetective;
    private Incident linkedIncident;
    private List<Evidence> evidence;
    private List<Suspect> suspects;

    // Observer list (Sergeant, Commissioner observers)
    private List<CaseObserver> observers;

    public Case(String caseID, Incident incident) {
        this.caseID         = caseID;
        this.status         = "Reported";
        this.priority       = "Medium";
        this.startDate      = LocalDateTime.now();
        this.linkedIncident = incident;
        this.evidence       = new ArrayList<>();
        this.suspects       = new ArrayList<>();
        this.observers      = new ArrayList<>();
    }

    // Constructor for DB-loaded cases (no incident object)
    public Case(String caseID, String status, String priority,
                LocalDateTime startDate, LocalDateTime endDate) {
        this.caseID     = caseID;
        this.status     = status;
        this.priority   = priority;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.evidence   = new ArrayList<>();
        this.suspects   = new ArrayList<>();
        this.observers  = new ArrayList<>();
    }

    /** Updates case status and notifies all observers. */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        if (newStatus.equals("Closed")) {
            this.endDate = LocalDateTime.now();
        }
        notifyObservers();
    }

    public void assignDetective(Detective detective) {
        this.assignedDetective = detective;
        this.status = "UnderInvestigation";
        notifyObservers();
    }

    public void addEvidence(Evidence e) { this.evidence.add(e); }
    public void addSuspect(Suspect s)   { this.suspects.add(s); }

    public void closeCase() {
        this.status  = "Closed";
        this.endDate = LocalDateTime.now();
        notifyObservers();
    }

    public void escalate() { updateStatus("Escalated"); }

    // --- Observer pattern ---
    public void addObserver(CaseObserver o)    { observers.add(o); }
    public void removeObserver(CaseObserver o) { observers.remove(o); }
    private void notifyObservers() {
        for (CaseObserver o : observers) {
            o.onCaseUpdated(this);
        }
    }

    // --- Getters ---
    public String getCaseID()                { return caseID; }
    public String getStatus()                { return status; }
    public String getPriority()              { return priority; }
    public LocalDateTime getStartDate()      { return startDate; }
    public LocalDateTime getEndDate()        { return endDate; }
    public Detective getAssignedDetective()  { return assignedDetective; }
    public Incident getLinkedIncident()      { return linkedIncident; }
    public List<Evidence> getEvidence()      { return evidence; }
    public List<Suspect> getSuspects()       { return suspects; }

    public void setPriority(String priority) { this.priority = priority; }
    public void setAssignedDetective(Detective d) { this.assignedDetective = d; }

    @Override
    public String toString() {
        return "Case[" + caseID + "] Status=" + status + " Priority=" + priority;
    }
}
