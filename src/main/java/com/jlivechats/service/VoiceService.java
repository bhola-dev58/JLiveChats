package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage voice messages
 */
public class VoiceService {
    
    private static Map<String, VoiceMessage> voiceMessages = new ConcurrentHashMap<>();
    
    public static class VoiceMessage {
        public String id;
        public String sender;
        public String channel;
        public String duration;
        public String timestamp;
        public String audioUrl;
        
        public VoiceMessage(String sender, String channel, String duration, String audioUrl) {
            this.id = "voice-" + System.nanoTime();
            this.sender = sender;
            this.channel = channel;
            this.duration = duration;
            this.audioUrl = audioUrl;
            this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    /**
     * Upload voice message
     */
    public static VoiceMessage uploadVoiceMessage(String sender, String channel, String duration, String audioUrl) {
        VoiceMessage message = new VoiceMessage(sender, channel, duration, audioUrl);
        voiceMessages.put(message.id, message);
        return message;
    }

    /**
     * Get voice message
     */
    public static VoiceMessage getVoiceMessage(String messageId) {
        return voiceMessages.get(messageId);
    }

    /**
     * Get all voice messages in channel
     */
    public static Collection<VoiceMessage> getChannelVoiceMessages(String channel) {
        return voiceMessages.values().stream()
            .filter(v -> v.channel.equals(channel))
            .toList();
    }

    /**
     * Delete voice message
     */
    public static void deleteVoiceMessage(String messageId) {
        voiceMessages.remove(messageId);
    }
}
