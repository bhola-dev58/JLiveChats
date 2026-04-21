package com.jlivechats.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private static List<Message> messages = new ArrayList<>();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int messageIdCounter = 0;

    static {
        // Initialize with some sample messages
        addMessage("System", "Welcome to JLiveChats! 🎉");
        addMessage("John Doe", "Hey everyone! How's it going?");
        addMessage("Jane Smith", "Hello! Everything is great here 👋");
        addMessage("System", "Alex Johnson joined the chat");
    }

    public static class Message {
        public String id;
        public String sender;
        public String content;
        public String timestamp;
        public boolean isCurrentUser;

        public Message(String sender, String content, boolean isCurrentUser) {
            this.id = "msg-" + (++messageIdCounter);
            this.sender = sender;
            this.content = content;
            this.timestamp = LocalDateTime.now().format(formatter);
            this.isCurrentUser = isCurrentUser;
        }

        @Override
        public String toString() {
            return sender + " [" + timestamp + "]: " + content;
        }
    }

    /**
     * Add a message to the chat
     * @param sender Username of the sender
     * @param content Message content
     */
    public static void addMessage(String sender, String content) {
        messages.add(new Message(sender, content, false));
    }

    /**
     * Add a message with sender info
     * @param sender Username of the sender
     * @param content Message content
     * @param isCurrentUser Whether this is from the current user
     */
    public static void addMessage(String sender, String content, boolean isCurrentUser) {
        messages.add(new Message(sender, content, isCurrentUser));
    }

    /**
     * Get all messages
     */
    public static List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Get recent messages (last 50) with isCurrentUser flag set
     */
    public static List<Message> getRecentMessages(int limit) {
        return getRecentMessages(limit, null);
    }

    /**
     * Get recent messages (last 50) with isCurrentUser flag set based on currentUser
     */
    public static List<Message> getRecentMessages(int limit, String currentUser) {
        int start = Math.max(0, messages.size() - limit);
        List<Message> recentMessages = new ArrayList<>();
        
        for (Message msg : messages.subList(start, messages.size())) {
            Message msgCopy = new Message(msg.sender, msg.content, 
                currentUser != null && msg.sender.equals(currentUser));
            msgCopy.timestamp = msg.timestamp;
            recentMessages.add(msgCopy);
        }
        
        return recentMessages;
    }

    /**
     * Clear all messages
     */
    public static void clearMessages() {
        messages.clear();
    }

    /**
     * Get message count
     */
    public static int getMessageCount() {
        return messages.size();
    }

    /**
     * Get message by ID
     */
    public static Message getMessageById(String messageId) {
        for (Message message : messages) {
            if (message.id.equals(messageId)) {
                return message;
            }
        }
        return null;
    }
}
