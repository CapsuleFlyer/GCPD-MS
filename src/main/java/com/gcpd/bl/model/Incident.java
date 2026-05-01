package com.gcpd.bl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ===========================================================
// INCIDENT — raw crime report created by a Detective
// GRASP: Creator (Detective creates this)
// ===========================================================
public class Incident {
    private String incidentID;
    private LocalDateTime date;
    private String location;
    private String crimeType;
    private String description;
    private String status;          // Reported, Escalated
    private Detective reportedBy;

    public Incident(String incidentID, String location, String crimeType,
                    String description, Detective reportedBy) {
        this.incidentID  = incidentID;
        this.date        = LocalDateTime.now();
        this.location    = location;
        this.crimeType   = crimeType;
        this.description = description;
        this.status      = "Reported";
        this.reportedBy  = reportedBy;
    }

    /** GRASP Information Expert: Incident owns its own data, sets its own status. */
    public void submit()               { this.status = "Reported"; }
    public void updateStatus(String s) { this.status = s; }
    public String validate() {
        if (location == null || location.isBlank()) return "Location required";
        if (crimeType == null || crimeType.isBlank()) return "Crime type required";
        return "OK";
    }
    public String getDetails() {
        return "Incident[" + incidentID + "] " + crimeType + " @ " + location;
    }

    public String getIncidentID()  { return incidentID; }
    public String getStatus()      { return status; }
    public String getLocation()    { return location; }
    public String getCrimeType()   { return crimeType; }
    public String getDescription() { return description; }
    public LocalDateTime getDate() { return date; }
    public Detective getReportedBy(){ return reportedBy; }
    public void setStatus(String s){ this.status = s; }
}
