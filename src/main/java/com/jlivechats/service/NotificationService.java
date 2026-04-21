package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for smart notifications
 */
public class NotificationService {
    
    private static Map<String, List<Notification>> userNotifications = new ConcurrentHashMap<>();
    
    public static class Notification {
        public String id;
        public String type;
        public String title;
        public String message;
        public String timestamp;
        public boolean isRead;
        
        public Notification(String type, String title, String message) {
            this.id = "notif-" + System.nanoTime();
            this.type = type;
            this.title = title;
            this.message = message;
            this.isRead = false;
            this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    /**
     * Send notification to user
     */
    public static void sendNotification(String username, String type, String title, String message) {
        Notification notif = new Notification(type, title, message);
        userNotifications.computeIfAbsent(username, k -> 
            Collections.synchronizedList(new ArrayList<>())).add(notif);
    }

    /**
     * Get user notifications
     */
    public static List<Notification> getUserNotifications(String username) {
        return new ArrayList<>(userNotifications.getOrDefault(username, new ArrayList<>()));
    }

    /**
     * Get unread notification count
     */
    public static int getUnreadCount(String username) {
        List<Notification> notifs = userNotifications.get(username);
        if (notifs == null) return 0;
        return (int) notifs.stream().filter(n -> !n.isRead).count();
    }

    /**
     * Mark notification as read
     */
    public static void markAsRead(String username, String notificationId) {
        List<Notification> notifs = userNotifications.get(username);
        if (notifs != null) {
            notifs.stream()
                .filter(n -> n.id.equals(notificationId))
                .forEach(n -> n.isRead = true);
        }
    }

    /**
     * Clear all notifications for user
     */
    public static void clearNotifications(String username) {
        userNotifications.remove(username);
    }

    /**
     * Send mention notification
     */
    public static void sendMentionNotification(String username, String mentioner) {
        sendNotification(username, "mention", "New mention", mentioner + " mentioned you");
    }

    /**
     * Send DM notification
     */
    public static void sendDMNotification(String username, String sender) {
        sendNotification(username, "dm", "New message", "Message from " + sender);
    }

    /**
     * Send reply notification
     */
    public static void sendReplyNotification(String username, String replier) {
        sendNotification(username, "reply", "New reply", replier + " replied to your message");
    }
}
