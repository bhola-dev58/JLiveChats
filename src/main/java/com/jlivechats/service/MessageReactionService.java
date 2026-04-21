package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage message reactions (emojis)
 */
public class MessageReactionService {
    private static Map<String, List<Reaction>> messageReactions = new ConcurrentHashMap<>();

    public static class Reaction {
        public String emoji;
        public String username;
        public long timestamp;

        public Reaction(String emoji, String username) {
            this.emoji = emoji;
            this.username = username;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class ReactionSummary {
        public String emoji;
        public int count;
        public List<String> usernames;

        public ReactionSummary(String emoji) {
            this.emoji = emoji;
            this.count = 0;
            this.usernames = new ArrayList<>();
        }
    }

    /**
     * Add reaction to a message
     */
    public static void addReaction(String messageId, String emoji, String username) {
        messageReactions.computeIfAbsent(messageId, k -> new ArrayList<>())
            .add(new Reaction(emoji, username));
    }

    /**
     * Remove reaction from a message
     */
    public static void removeReaction(String messageId, String emoji, String username) {
        List<Reaction> reactions = messageReactions.get(messageId);
        if (reactions != null) {
            reactions.removeIf(r -> r.emoji.equals(emoji) && r.username.equals(username));
        }
    }

    /**
     * Get reaction summary for a message
     */
    public static Map<String, ReactionSummary> getMessageReactions(String messageId) {
        Map<String, ReactionSummary> summary = new LinkedHashMap<>();
        List<Reaction> reactions = messageReactions.get(messageId);
        
        if (reactions != null) {
            reactions.forEach(reaction -> {
                ReactionSummary rs = summary.computeIfAbsent(reaction.emoji, ReactionSummary::new);
                rs.count++;
                rs.usernames.add(reaction.username);
            });
        }
        
        return summary;
    }

    /**
     * Get all reactions for a message
     */
    public static List<Reaction> getAllReactions(String messageId) {
        return messageReactions.getOrDefault(messageId, new ArrayList<>());
    }

    /**
     * Clear reactions for a message
     */
    public static void clearReactions(String messageId) {
        messageReactions.remove(messageId);
    }
}
