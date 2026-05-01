package com.gcpd.bl.factory;

import com.gcpd.bl.model.*;

/**
 * UserFactory — GoF Factory Method pattern.
 * Decouples SystemAdmin from concrete User subclasses.
 * The factory determines whether to instantiate Detective, Sergeant, etc.
 */
public class UserFactory {

    /**
     * Creates and returns the correct User subtype based on role string.
     * @param userID     unique user ID
     * @param name       full name
     * @param credential password
     * @param role       role string: Detective, Sergeant, Commissioner, etc.
     * @param extra      role-specific extra field (badgeNumber, squadID, departmentID, etc.)
     */
    public static User createUser(String userID, String name, String credential,
                                  String role, String extra) {
        return switch (role) {
            case "Detective"         -> new Detective(userID, name, credential, extra != null ? extra : "");
            case "Sergeant"          -> new Sergeant(userID, name, credential, extra != null ? extra : "");
            case "Commissioner"      -> new Commissioner(userID, name, credential, extra != null ? extra : "");
            case "ForensicAnalyst"   -> new ForensicAnalyst(userID, name, credential, extra != null ? extra : "");
            case "EvidenceCustodian" -> new EvidenceCustodian(userID, name, credential, extra != null ? extra : "");
            case "SystemAdmin"       -> new SystemAdmin(userID, name, credential);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
