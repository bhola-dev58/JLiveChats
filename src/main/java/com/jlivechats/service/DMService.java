package com.jlivechats.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to manage direct messages between users
 */
public class DMService {
    
    private static Map<String, List<DMMessage>> conversations = new java.util.concurrent.ConcurrentHashMap<>();
    private static Map<String, Set<String>> userDMs = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static class DMMessage {
        public String id;
        public String sender;
        public String recipient;
        public String content;
        public String timestamp;
        public boolean isRead;
        
        public DMMessage(String sender, String recipient, String content) {
            this.id = "dm-" + System.nanoTime();
            this.sender = sender;
            this.recipient = recipient;
            this.content = content;
            this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            this.isRead = false;
        }
    }

    /**
     * Send a direct message
     */
    public static DMMessage sendMessage(String sender, String recipient, String content) {
        String conversationKey = getConversationKey(sender, recipient);
        DMMessage message = new DMMessage(sender, recipient, content);
        
        conversations.computeIfAbsent(conversationKey, k -> 
            Collections.synchronizedList(new ArrayList<>())).add(message);
        
        // Add to user's DM list
        userDMs.computeIfAbsent(sender, k -> Collections.synchronizedSet(new HashSet<>())).add(recipient);
        userDMs.computeIfAbsent(recipient, k -> Collections.synchronizedSet(new HashSet<>())).add(sender);
        
        return message;
    }

    /**
     * Get conversation between two users
     */
    public static List<DMMessage> getConversation(String user1, String user2) {
        String conversationKey = getConversationKey(user1, user2);
        return new ArrayList<>(conversations.getOrDefault(conversationKey, new ArrayList<>()));
    }

    /**
     * Get all DM conversations for a user
     */
    public static Set<String> getUserDMList(String username) {
        return new HashSet<>(userDMs.getOrDefault(username, new HashSet<>()));
    }

    /**
     * Mark message as read
     */
    public static void markAsRead(String sender, String recipient) {
        String conversationKey = getConversationKey(sender, recipient);
        List<DMMessage> messages = conversations.get(conversationKey);
        if (messages != null) {
            messages.stream()
                .filter(m -> m.recipient.equals(sender) && !m.isRead)
                .forEach(m -> m.isRead = true);
        }
    }

    /**
     * Get unread message count for user
     */
    public static int getUnreadCount(String username) {
        int count = 0;
        for (String otherUser : getUserDMList(username)) {
            String conversationKey = getConversationKey(username, otherUser);
            List<DMMessage> messages = conversations.get(conversationKey);
            if (messages != null) {
                count += (int) messages.stream()
                    .filter(m -> m.recipient.equals(username) && !m.isRead)
                    .count();
            }
        }
        return count;
    }

    /**
     * Delete a DM conversation
     */
    public static void deleteConversation(String user1, String user2) {
        String conversationKey = getConversationKey(user1, user2);
        conversations.remove(conversationKey);
    }

    /**
     * Get conversation key (always in alphabetical order for consistency)
     */
    private static String getConversationKey(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? 
            user1 + ":" + user2 : user2 + ":" + user1;
    }

    /**
     * Get total message count in conversation
     */
    public static int getConversationSize(String user1, String user2) {
        String conversationKey = getConversationKey(user1, user2);
        List<DMMessage> messages = conversations.get(conversationKey);
        return messages != null ? messages.size() : 0;
    }
}
