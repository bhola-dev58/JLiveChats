package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for chat analytics and statistics
 */
public class AnalyticsService {
    
    private static Map<String, Integer> userMessageCount = new ConcurrentHashMap<>();
    private static Map<String, Integer> channelMessageCount = new ConcurrentHashMap<>();
    private static long totalMessages = 0;
    private static long appStartTime = System.currentTimeMillis();

    /**
     * Track message sent
     */
    public static void trackMessageSent(String username, String channel) {
        userMessageCount.merge(username, 1, Integer::sum);
        channelMessageCount.merge(channel, 1, Integer::sum);
        totalMessages++;
    }

    /**
     * Get user message count
     */
    public static int getUserMessageCount(String username) {
        return userMessageCount.getOrDefault(username, 0);
    }

    /**
     * Get channel message count
     */
    public static int getChannelMessageCount(String channel) {
        return channelMessageCount.getOrDefault(channel, 0);
    }

    /**
     * Get total messages
     */
    public static long getTotalMessages() {
        return totalMessages;
    }

    /**
     * Get top users by message count
     */
    public static List<Map.Entry<String, Integer>> getTopUsers(int limit) {
        return userMessageCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .toList();
    }

    /**
     * Get top channels
     */
    public static List<Map.Entry<String, Integer>> getTopChannels(int limit) {
        return channelMessageCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .toList();
    }

    /**
     * Get uptime in milliseconds
     */
    public static long getUptime() {
        return System.currentTimeMillis() - appStartTime;
    }
}
