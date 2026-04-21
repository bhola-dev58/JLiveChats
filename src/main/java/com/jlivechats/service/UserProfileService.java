package com.jlivechats.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service to manage user profiles and information
 */
public class UserProfileService {
    
    private static Map<String, UserProfile> profiles = new ConcurrentHashMap<>();
    
    public static class UserProfile {
        public String username;
        public String displayName;
        public String bio;
        public String status;
        public String avatarColor;
        public String joinedAt;
        
        public UserProfile(String username) {
            this.username = username;
            this.displayName = username;
            this.bio = "";
            this.status = "online";
            this.avatarColor = generateAvatarColor(username);
            this.joinedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        private static String generateAvatarColor(String username) {
            String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#6C5CE7"};
            return colors[Math.abs(username.hashCode()) % colors.length];
        }
    }

    /**
     * Get or create user profile
     */
    public static UserProfile getOrCreateProfile(String username) {
        return profiles.computeIfAbsent(username, k -> new UserProfile(username));
    }

    /**
     * Get user profile
     */
    public static UserProfile getProfile(String username) {
        return profiles.get(username);
    }

    /**
     * Update user profile
     */
    public static void updateProfile(String username, String displayName, String bio) {
        UserProfile profile = getOrCreateProfile(username);
        if (displayName != null && !displayName.isEmpty()) {
            profile.displayName = displayName;
        }
        if (bio != null) {
            profile.bio = bio;
        }
    }

    /**
     * Set user status
     */
    public static void setStatus(String username, String status) {
        UserProfile profile = getOrCreateProfile(username);
        profile.status = status;
    }

    /**
     * Get user display name
     */
    public static String getDisplayName(String username) {
        UserProfile profile = getProfile(username);
        return profile != null ? profile.displayName : username;
    }
}
