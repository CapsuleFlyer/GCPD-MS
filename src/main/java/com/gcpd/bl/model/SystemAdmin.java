package com.gcpd.bl.model;

/**
 * SystemAdmin — registers and manages user accounts.
 * GRASP: Controller for user registration workflow.
 */
public class SystemAdmin extends User {

    public SystemAdmin(String userID, String name, String credential) {
        super(userID, name, credential);
    }

    @Override
    public String getRole() { return "SystemAdmin"; }
}
