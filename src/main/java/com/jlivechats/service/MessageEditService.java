package com.jlivechats.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to manage message editing and deletion
 */
public class MessageEditService {
    
    private static Map<String, EditedMessageInfo> editedMessages = new java.util.concurrent.ConcurrentHashMap<>();
    private static Set<String> deletedMessages = Collections.synchronizedSet(new HashSet<>());
    
    public static class EditedMessageInfo {
        public String messageId;
        public String originalContent;
        public String editedContent;
        public String editedAt;
        public String editedBy;
        
        public EditedMessageInfo(String messageId, String originalContent, String editedContent, String editedBy) {
            this.messageId = messageId;
            this.originalContent = originalContent;
            this.editedContent = editedContent;
            this.editedBy = editedBy;
            this.editedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * Edit a message
     */
    public static void editMessage(String messageId, String newContent, String editedBy) {
        // Get original message from MessageService
        MessageService.Message originalMessage = MessageService.getMessageById(messageId);
        if (originalMessage != null) {
            EditedMessageInfo editInfo = new EditedMessageInfo(
                messageId, 
                originalMessage.content, 
                newContent, 
                editedBy
            );
            editedMessages.put(messageId, editInfo);
            
            // Update the message content in MessageService
            originalMessage.content = newContent + " (edited)";
        }
    }

    /**
     * Delete a message
     */
    public static void deleteMessage(String messageId) {
        deletedMessages.add(messageId);
    }

    /**
     * Restore a deleted message
     */
    public static void restoreMessage(String messageId) {
        deletedMessages.remove(messageId);
    }

    /**
     * Check if message is deleted
     */
    public static boolean isMessageDeleted(String messageId) {
        return deletedMessages.contains(messageId);
    }

    /**
     * Check if message is edited
     */
    public static boolean isMessageEdited(String messageId) {
        return editedMessages.containsKey(messageId);
    }

    /**
     * Get edit info for a message
     */
    public static EditedMessageInfo getEditInfo(String messageId) {
        return editedMessages.get(messageId);
    }

    /**
     * Can user edit message (only if they're the sender)
     */
    public static boolean canEditMessage(String messageId, String currentUser, String messageSender) {
        return !isMessageDeleted(messageId) && currentUser.equals(messageSender);
    }

    /**
     * Can user delete message (only if they're the sender)
     */
    public static boolean canDeleteMessage(String messageId, String currentUser, String messageSender) {
        return !isMessageDeleted(messageId) && currentUser.equals(messageSender);
    }

    /**
     * Get all edited messages
     */
    public static List<EditedMessageInfo> getAllEditedMessages() {
        return new ArrayList<>(editedMessages.values());
    }

    /**
     * Get deleted message count
     */
    public static int getDeletedMessageCount() {
        return deletedMessages.size();
    }
}
