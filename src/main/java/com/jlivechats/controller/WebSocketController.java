package com.jlivechats.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.jlivechats.service.MessageService;

/**
 * WebSocket Controller for real-time chat messaging
 * Handles STOMP messages and broadcasts them to all subscribers
 */
@Controller
public class WebSocketController {

    public static class ChatMessage {
        public String sender;
        public String content;
        public String timestamp;
        public String channel;

        public ChatMessage() {}

        public ChatMessage(String sender, String content, String channel) {
            this.sender = sender;
            this.content = content;
            this.channel = channel;
            this.timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    /**
     * Handle chat messages and broadcast to all subscribers
     * Maps /app/sendMessage to /topic/messages
     */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        // Save message to service
        MessageService.addMessage(message.sender, message.content, true);
        
        // Broadcast to all subscribers
        return message;
    }

    /**
     * Handle channel-specific messages
     */
    @MessageMapping("/sendChannelMessage")
    @SendTo("/topic/channel/{channel}")
    public ChatMessage sendChannelMessage(ChatMessage message) {
        // Save message to service
        MessageService.addMessage(message.sender, message.content, true);
        
        // Broadcast to channel subscribers
        return message;
    }
}
