package com.gcpd.bl.model;

/**
 * Observer interface for Case state changes.
 * GoF Observer pattern.
 */
public interface CaseObserver {
    void onCaseUpdated(Case updatedCase);
}
