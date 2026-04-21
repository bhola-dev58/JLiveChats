package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * Service to manage user presence and online status
 */
public class UserPresenceService {
    private static Map<String, UserPresence> userPresenceMap = new ConcurrentHashMap<>();
    
    public static class UserPresence {
        public String username;
        public String status;  // online, away, offline
        public LocalDateTime lastSeen;
        public String channel;
        
        public UserPresence(String username, String channel) {
            this.username = username;
            this.status = "online";
            this.lastSeen = LocalDateTime.now();
            this.channel = channel;
        }
    }

    /**
     * User comes online
     */
    public static void userOnline(String username, String channel) {
        userPresenceMap.put(username, new UserPresence(username, channel));
    }

    /**
     * User goes offline
     */
    public static void userOffline(String username) {
        userPresenceMap.remove(username);
    }

    /**
     * Get all online users
     */
    public static List<String> getOnlineUsers() {
        return new ArrayList<>(userPresenceMap.keySet());
    }

    /**
     * Get online users count
     */
    public static int getOnlineUserCount() {
        return userPresenceMap.size();
    }

    /**
     * Set user status (online, away, offline)
     */
    public static void setUserStatus(String username, String status) {
        if (userPresenceMap.containsKey(username)) {
            userPresenceMap.get(username).status = status;
            userPresenceMap.get(username).lastSeen = LocalDateTime.now();
        }
    }

    /**
     * Get user presence info
     */
    public static UserPresence getUserPresence(String username) {
        return userPresenceMap.get(username);
    }

    /**
     * Get all user presences
     */
    public static List<UserPresence> getAllUserPresences() {
        return new ArrayList<>(userPresenceMap.values());
    }

    /**
     * Update user's channel
     */
    public static void updateUserChannel(String username, String channel) {
        if (userPresenceMap.containsKey(username)) {
            userPresenceMap.get(username).channel = channel;
        }
    }
}
