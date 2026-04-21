package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage pinned messages
 */
public class PinnedMessageService {
    
    private static Map<String, Set<String>> pinnedMessages = new ConcurrentHashMap<>(); // channelId -> messageIds
    private static Map<String, PinnedMessageInfo> pinnedMessageInfo = new ConcurrentHashMap<>(); // messageId -> info
    
    public static class PinnedMessageInfo {
        public String messageId;
        public String sender;
        public String content;
        public String timestamp;
        public String pinnedBy;
        public String pinnedAt;
        public String channel;
        
        public PinnedMessageInfo(String messageId, String sender, String content, String timestamp, 
                                String pinnedBy, String channel) {
            this.messageId = messageId;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.pinnedBy = pinnedBy;
            this.pinnedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.channel = channel;
        }
    }

    /**
     * Pin a message
     */
    public static void pinMessage(String messageId, String sender, String content, String timestamp, 
                                  String pinnedBy, String channel) {
        pinnedMessages.computeIfAbsent(channel, k -> new HashSet<>()).add(messageId);
        pinnedMessageInfo.put(messageId, new PinnedMessageInfo(messageId, sender, content, timestamp, pinnedBy, channel));
    }

    /**
     * Unpin a message
     */
    public static void unpinMessage(String messageId, String channel) {
        Set<String> channelPinned = pinnedMessages.get(channel);
        if (channelPinned != null) {
            channelPinned.remove(messageId);
        }
        pinnedMessageInfo.remove(messageId);
    }

    /**
     * Get all pinned messages for a channel
     */
    public static List<PinnedMessageInfo> getPinnedMessagesForChannel(String channel) {
        List<PinnedMessageInfo> result = new ArrayList<>();
        Set<String> pinnedIds = pinnedMessages.getOrDefault(channel, new HashSet<>());
        
        for (String messageId : pinnedIds) {
            PinnedMessageInfo info = pinnedMessageInfo.get(messageId);
            if (info != null) {
                result.add(info);
            }
        }
        
        // Sort by pinned time (most recent first)
        result.sort((a, b) -> b.pinnedAt.compareTo(a.pinnedAt));
        return result;
    }

    /**
     * Check if a message is pinned
     */
    public static boolean isMessagePinned(String messageId) {
        return pinnedMessageInfo.containsKey(messageId);
    }

    /**
     * Get all pinned messages
     */
    public static List<PinnedMessageInfo> getAllPinnedMessages() {
        return new ArrayList<>(pinnedMessageInfo.values());
    }

    /**
     * Get pin count for channel
     */
    public static int getPinCountForChannel(String channel) {
        return pinnedMessages.getOrDefault(channel, new HashSet<>()).size();
    }
}
