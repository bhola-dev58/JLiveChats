package com.jlivechats.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage user avatars and colors
 */
public class AvatarService {
    
    // Color palette for avatars
    private static final String[] AVATAR_COLORS = {
        "#FF6B6B", // Red
        "#4ECDC4", // Teal
        "#45B7D1", // Blue
        "#FFA07A", // Salmon
        "#98D8C8", // Mint
        "#F7DC6F", // Yellow
        "#BB8FCE", // Purple
        "#85C1E2", // Sky Blue
        "#F8B88B", // Peach
        "#ABEBC6", // Green
        "#F5B7B1", // Rose
        "#D7BDE2"  // Lavender
    };
    
    private static Map<String, String> userColorMap = new HashMap<>();

    /**
     * Get avatar color for a user
     * Same user always gets same color
     */
    public static String getAvatarColor(String username) {
        return userColorMap.computeIfAbsent(username, key -> {
            int hashCode = username.hashCode();
            int index = Math.abs(hashCode) % AVATAR_COLORS.length;
            return AVATAR_COLORS[index];
        });
    }

    /**
     * Get user initials (first letter of first and last name)
     */
    public static String getInitials(String username) {
        if (username == null || username.isEmpty()) {
            return "?";
        }
        
        String[] parts = username.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            String initials = username.substring(0, Math.min(2, username.length())).toUpperCase();
            return initials;
        }
    }

    /**
     * Get avatar style string
     */
    public static Map<String, String> getAvatarStyle(String username) {
        Map<String, String> style = new HashMap<>();
        style.put("backgroundColor", getAvatarColor(username));
        style.put("initials", getInitials(username));
        return style;
    }

    /**
     * Reset avatar cache (for testing)
     */
    public static void reset() {
        userColorMap.clear();
    }
}
