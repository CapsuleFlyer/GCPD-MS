package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Suspect — GRASP Information Expert (owns criminal history and risk level).
 */
public class Suspect {
    private String suspectID;
    private String name;
    private String criminalHistory;
    private int riskLevel;          // 1-5
    private List<Case> linkedCases;
    private boolean repeatOffender;

    public Suspect(String suspectID, String name, String criminalHistory, int riskLevel) {
        this.suspectID       = suspectID;
        this.name            = name;
        this.criminalHistory = criminalHistory;
        this.riskLevel       = riskLevel;
        this.linkedCases     = new ArrayList<>();
        this.repeatOffender  = false;
    }

    public void createProfile()        { /* handled by DB layer */ }
    public void linkToCase(Case c)     { linkedCases.add(c); }
    public List<Case> getHistory()     { return linkedCases; }

    /**
     * Checks if this suspect matches given traits (name, crime type, risk).
     * Used by UC-05 Identify Repeat Offender.
     */
    public boolean matchSuspect(String nameQuery, String crimeQuery, int riskQuery) {
        boolean nameMatch  = name.toLowerCase().contains(nameQuery.toLowerCase());
        boolean crimeMatch = crimeQuery == null || crimeQuery.isBlank()
                             || criminalHistory.toLowerCase().contains(crimeQuery.toLowerCase());
        boolean riskMatch  = riskQuery <= 0 || riskLevel >= riskQuery;
        return nameMatch && crimeMatch && riskMatch;
    }

    public boolean isRepeatOffender()       { return repeatOffender; }
    public void setRepeatOffender(boolean b){ this.repeatOffender = b; }

    public String getSuspectID()     { return suspectID; }
    public String getName()          { return name; }
    public String getCriminalHistory(){ return criminalHistory; }
    public int getRiskLevel()        { return riskLevel; }
    public void setRiskLevel(int r)  { this.riskLevel = r; }
    public void setCriminalHistory(String h) { this.criminalHistory = h; }

    @Override
    public String toString() { return "Suspect[" + suspectID + "] " + name + " Risk=" + riskLevel; }
}
