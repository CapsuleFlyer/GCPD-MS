package com.gcpd.bl.strategy;

/**
 * IntranetNotification — concrete Strategy implementation.
 * Simulates intranet push notification (console log for now).
 */
public class IntranetNotification implements NotificationStrategy {
    @Override
    public void sendNotification(String recipient, String message) {
        System.out.println("[INTRANET] To: " + recipient + " | Msg: " + message);
    }
}
