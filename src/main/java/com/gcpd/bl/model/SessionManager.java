package com.gcpd.bl.model;

/**
 * SessionManager — GoF Singleton pattern.
 * Single global instance that centralizes role-based session management.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    // Private constructor prevents external instantiation
    private SessionManager() {}

    /** Returns the single SessionManager instance. */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(User user) {
        this.currentUser = user;
        System.out.println("Session started for: " + user.getName() + " [" + user.getRole() + "]");
    }

    public void endSession() {
        System.out.println("Session ended for: " + (currentUser != null ? currentUser.getName() : "null"));
        this.currentUser = null;
    }

    public User getCurrentUser()    { return currentUser; }
    public boolean isLoggedIn()     { return currentUser != null; }
    public String getCurrentRole()  { return currentUser != null ? currentUser.getRole() : null; }
    public String getCurrentUserID(){ return currentUser != null ? currentUser.getUserID() : null; }
}
