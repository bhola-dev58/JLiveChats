package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage typing indicators
 */
public class TypingIndicatorService {
    private static Map<String, TypingIndicator> typingIndicators = new ConcurrentHashMap<>();

    public static class TypingIndicator {
        public String username;
        public String channel;
        public long timestamp;

        public TypingIndicator(String username, String channel) {
            this.username = username;
            this.channel = channel;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            // Expire after 3 seconds of inactivity
            return System.currentTimeMillis() - timestamp > 3000;
        }
    }

    /**
     * Mark user as typing
     */
    public static void userTyping(String username, String channel) {
        typingIndicators.put(username + ":" + channel, new TypingIndicator(username, channel));
    }

    /**
     * Mark user as stopped typing
     */
    public static void userStoppedTyping(String username, String channel) {
        typingIndicators.remove(username + ":" + channel);
    }

    /**
     * Get users typing in a channel
     */
    public static List<String> getUsersTypingInChannel(String channel) {
        List<String> typingUsers = new ArrayList<>();
        typingIndicators.forEach((key, indicator) -> {
            if (indicator.channel.equals(channel) && !indicator.isExpired()) {
                typingUsers.add(indicator.username);
            } else if (indicator.isExpired()) {
                typingIndicators.remove(key);
            }
        });
        return typingUsers;
    }

    /**
     * Clean up expired indicators
     */
    public static void cleanupExpired() {
        typingIndicators.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
