package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for moderation and content management
 */
public class ModerationService {
    
    private static Set<String> bannedUsers = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> mutedUsers = Collections.synchronizedSet(new HashSet<>());
    private static Map<String, String> userBanReasons = new ConcurrentHashMap<>();
    
    public static class ModerationAction {
        public String id;
        public String actionType;
        public String targetUser;
        public String moderator;
        public String reason;
        public String timestamp;
        
        public ModerationAction(String actionType, String targetUser, String moderator, String reason) {
            this.id = "action-" + System.nanoTime();
            this.actionType = actionType;
            this.targetUser = targetUser;
            this.moderator = moderator;
            this.reason = reason;
            this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * Ban user
     */
    public static void banUser(String username, String reason) {
        bannedUsers.add(username);
        userBanReasons.put(username, reason);
    }

    /**
     * Unban user
     */
    public static void unbanUser(String username) {
        bannedUsers.remove(username);
        userBanReasons.remove(username);
    }

    /**
     * Check if user is banned
     */
    public static boolean isBanned(String username) {
        return bannedUsers.contains(username);
    }

    /**
     * Get ban reason
     */
    public static String getBanReason(String username) {
        return userBanReasons.get(username);
    }

    /**
     * Mute user
     */
    public static void muteUser(String username) {
        mutedUsers.add(username);
    }

    /**
     * Unmute user
     */
    public static void unmuteUser(String username) {
        mutedUsers.remove(username);
    }

    /**
     * Check if user is muted
     */
    public static boolean isMuted(String username) {
        return mutedUsers.contains(username);
    }

    /**
     * Get banned users
     */
    public static Set<String> getBannedUsers() {
        return new HashSet<>(bannedUsers);
    }

    /**
     * Get muted users
     */
    public static Set<String> getMutedUsers() {
        return new HashSet<>(mutedUsers);
    }
}
