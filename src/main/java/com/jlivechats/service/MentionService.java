package com.jlivechats.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to manage user mentions and notifications
 */
public class MentionService {
    
    private static Map<String, List<MentionInfo>> userMentions = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static class MentionInfo {
        public String messageId;
        public String mentionedUser;
        public String mentioningUser;
        public String messageContent;
        public String mentionedAt;
        public boolean isRead;
        
        public MentionInfo(String messageId, String mentionedUser, String mentioningUser, String messageContent) {
            this.messageId = messageId;
            this.mentionedUser = mentionedUser;
            this.mentioningUser = mentioningUser;
            this.messageContent = messageContent;
            this.mentionedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.isRead = false;
        }
    }

    /**
     * Record a mention
     */
    public static void addMention(String messageId, String mentionedUser, String mentioningUser, String messageContent) {
        userMentions.computeIfAbsent(mentionedUser, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(new MentionInfo(messageId, mentionedUser, mentioningUser, messageContent));
    }

    /**
     * Get all mentions for a user
     */
    public static List<MentionInfo> getUserMentions(String username) {
        return new ArrayList<>(userMentions.getOrDefault(username, new ArrayList<>()));
    }

    /**
     * Get unread mention count for user
     */
    public static int getUnreadMentionCount(String username) {
        List<MentionInfo> mentions = userMentions.getOrDefault(username, new ArrayList<>());
        return (int) mentions.stream().filter(m -> !m.isRead).count();
    }

    /**
     * Mark mention as read
     */
    public static void markMentionAsRead(String messageId, String username) {
        List<MentionInfo> mentions = userMentions.get(username);
        if (mentions != null) {
            mentions.stream()
                .filter(m -> m.messageId.equals(messageId))
                .forEach(m -> m.isRead = true);
        }
    }

    /**
     * Mark all mentions as read for user
     */
    public static void markAllMentionsAsRead(String username) {
        List<MentionInfo> mentions = userMentions.get(username);
        if (mentions != null) {
            mentions.forEach(m -> m.isRead = true);
        }
    }

    /**
     * Extract mentioned users from message content
     */
    public static List<String> extractMentions(String content) {
        List<String> mentions = new ArrayList<>();
        // Regex to find @username patterns (word characters only)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String mentionedUser = matcher.group(1);
            if (!mentions.contains(mentionedUser)) {
                mentions.add(mentionedUser);
            }
        }
        return mentions;
    }

    /**
     * Clear mentions for a user
     */
    public static void clearMentions(String username) {
        userMentions.remove(username);
    }

    /**
     * Get all users (for autocomplete)
     */
    public static List<String> getAllUsers() {
        // This should be populated from UserService or database
        return new ArrayList<>(userMentions.keySet());
    }

    /**
     * Get mention count
     */
    public static int getMentionCount(String username) {
        return userMentions.getOrDefault(username, new ArrayList<>()).size();
    }
}
