package com.jlivechats.service;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Service for searching messages
 */
public class MessageSearchService {
    
    public static class SearchResult {
        public String id;
        public String sender;
        public String content;
        public String timestamp;
        public String channel;
        public int matchCount; // Number of keyword matches
        
        public SearchResult(String id, String sender, String content, String timestamp, String channel) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.channel = channel;
            this.matchCount = 1;
        }
    }

    /**
     * Search messages by keyword (case-insensitive)
     */
    public static List<SearchResult> searchMessages(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SearchResult> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        List<MessageService.Message> allMessages = MessageService.getAllMessages();
        
        for (MessageService.Message msg : allMessages) {
            if (msg.content.toLowerCase().contains(lowerKeyword)) {
                SearchResult result = new SearchResult(
                    UUID.randomUUID().toString(),
                    msg.sender,
                    msg.content,
                    msg.timestamp,
                    "general" // Default channel
                );
                // Count occurrences
                int count = 0;
                int index = 0;
                while ((index = msg.content.toLowerCase().indexOf(lowerKeyword, index)) != -1) {
                    count++;
                    index += lowerKeyword.length();
                }
                result.matchCount = count;
                results.add(result);
            }
        }
        
        // Sort by match count (most relevant first)
        results.sort((a, b) -> Integer.compare(b.matchCount, a.matchCount));
        
        return results;
    }

    /**
     * Search messages by sender
     */
    public static List<SearchResult> searchBySender(String sender) {
        List<SearchResult> results = new ArrayList<>();
        
        List<MessageService.Message> allMessages = MessageService.getAllMessages();
        
        for (MessageService.Message msg : allMessages) {
            if (msg.sender.equalsIgnoreCase(sender)) {
                SearchResult result = new SearchResult(
                    UUID.randomUUID().toString(),
                    msg.sender,
                    msg.content,
                    msg.timestamp,
                    "general"
                );
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * Search by keyword and sender combined
     */
    public static List<SearchResult> searchMessages(String keyword, String sender) {
        List<SearchResult> results = new ArrayList<>();
        String lowerKeyword = keyword != null ? keyword.toLowerCase() : "";
        
        List<MessageService.Message> allMessages = MessageService.getAllMessages();
        
        for (MessageService.Message msg : allMessages) {
            boolean matchesSender = sender == null || msg.sender.equalsIgnoreCase(sender);
            boolean matchesKeyword = lowerKeyword.isEmpty() || msg.content.toLowerCase().contains(lowerKeyword);
            
            if (matchesSender && matchesKeyword) {
                SearchResult result = new SearchResult(
                    UUID.randomUUID().toString(),
                    msg.sender,
                    msg.content,
                    msg.timestamp,
                    "general"
                );
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * Get search stats
     */
    public static Map<String, Object> getSearchStats(List<SearchResult> results) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalResults", results.size());
        
        // Count by sender
        Map<String, Integer> senderCount = new HashMap<>();
        for (SearchResult result : results) {
            senderCount.put(result.sender, senderCount.getOrDefault(result.sender, 0) + 1);
        }
        stats.put("byUser", senderCount);
        
        return stats;
    }
}
