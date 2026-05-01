package com.gcpd.bl.model;

/**
 * Abstract base class for all system users.
 * GRASP: Information Expert — owns identity and role data.
 * OOP: Abstract class enabling inheritance for all role subtypes.
 */
public abstract class User {
    protected String userID;
    protected String name;
    protected int role;           // stored as int, mapped via enum in subclasses
    protected String credential;  // password

    public User(String userID, String name, String credential) {
        this.userID     = userID;
        this.name       = name;
        this.credential = credential;
    }

    /** Verify password against stored credential. */
    public boolean authenticate(String inputPassword) {
        return this.credential.equals(inputPassword);
    }

    /** Returns the role string for this user (e.g. "Detective"). */
    public abstract String getRole();

    // ---- Getters & Setters ----
    public String getUserID()     { return userID; }
    public String getName()       { return name; }
    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }

    @Override
    public String toString() {
        return getRole() + ": " + name + " [" + userID + "]";
    }
}
