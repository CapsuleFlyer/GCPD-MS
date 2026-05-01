package com.gcpd.bl.strategy;

/**
 * NotificationStrategy — GoF Strategy pattern.
 * Swaps communication methods (Email vs Intranet-Push) at runtime.
 */
public interface NotificationStrategy {
    void sendNotification(String recipient, String message);
}
