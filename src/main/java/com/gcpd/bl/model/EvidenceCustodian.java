package com.gcpd.bl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * EvidenceCustodian — logistics and integrity officer.
 * GRASP: Information Expert for transfers, disposal, and chain-of-custody.
 *        Controller for the evidence lifecycle.
 */
public class EvidenceCustodian extends User {
    private String vaultID;
    private List<Evidence> custodyList;

    public EvidenceCustodian(String userID, String name, String credential, String vaultID) {
        super(userID, name, credential);
        this.vaultID     = vaultID;
        this.custodyList = new ArrayList<>();
    }

    @Override
    public String getRole() { return "EvidenceCustodian"; }

    /** Logs new evidence into the system and starts chain of custody. */
    public ChainOfCustody logEvidence(Evidence evidence) {
        custodyList.add(evidence);
        return evidence.logEntry(this.userID, "Logged", "Initial custody log");
    }

    /** Authorizes transfer of evidence to a destination. */
    public ChainOfCustody authorizeTransfer(Evidence evidence, String destination) {
        String oldStatus = evidence.getStatus();
        evidence.updateStatus("Transferred");
        return evidence.logEntry(this.userID, "Transferred to " + destination, oldStatus);
    }

    /** Disposes of evidence after authorization. */
    public ChainOfCustody disposeEvidence(Evidence evidence) {
        String oldStatus = evidence.getStatus();
        evidence.updateStatus("Disposed");
        evidence.markDisposed();
        return evidence.logEntry(this.userID, "Disposed", oldStatus);
    }

    /** Tracks the full chain of custody for an evidence item. */
    public List<ChainOfCustody> trackChainOfCustody(Evidence evidence) {
        return evidence.getCustodyLog();
    }

    public String getVaultID()             { return vaultID; }
    public List<Evidence> getCustodyList() { return custodyList; }
}
