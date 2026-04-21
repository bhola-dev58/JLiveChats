package com.jlivechats.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.jlivechats.service.MessageService;
import com.jlivechats.service.UserPresenceService;
import com.jlivechats.service.TypingIndicatorService;
import com.jlivechats.service.MessageReactionService;

/**
 * WebSocket Controller for real-time chat messaging
 * Handles STOMP messages and broadcasts them to all subscribers
 */
@Controller
public class WebSocketController {

    public static class ChatMessage {
        public String id;
        public String sender;
        public String content;
        public String timestamp;
        public String channel;
        public String messageType; // "chat", "system", "reaction", "typing"

        public ChatMessage() {}

        public ChatMessage(String sender, String content, String channel) {
            this.id = java.util.UUID.randomUUID().toString();
            this.sender = sender;
            this.content = content;
            this.channel = channel;
            this.messageType = "chat";
            this.timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    public static class PresenceEvent {
        public String username;
        public String status; // "online", "offline", "away"
        public String channel;
        public int onlineCount;

        public PresenceEvent(String username, String status, String channel, int count) {
            this.username = username;
            this.status = status;
            this.channel = channel;
            this.onlineCount = count;
        }
    }

    public static class TypingEvent {
        public String username;
        public String channel;
        public boolean isTyping;

        public TypingEvent(String username, String channel, boolean isTyping) {
            this.username = username;
            this.channel = channel;
            this.isTyping = isTyping;
        }
    }

    public static class ReactionEvent {
        public String messageId;
        public String emoji;
        public String username;
        public String action; // "add" or "remove"

        public ReactionEvent() {}

        public ReactionEvent(String messageId, String emoji, String username, String action) {
            this.messageId = messageId;
            this.emoji = emoji;
            this.username = username;
            this.action = action;
        }
    }

    /**
     * Handle chat messages and broadcast to all subscribers
     */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        if (message.id == null) {
            message.id = java.util.UUID.randomUUID().toString();
        }
        MessageService.addMessage(message.sender, message.content, true);
        TypingIndicatorService.userStoppedTyping(message.sender, message.channel);
        return message;
    }

    /**
     * Handle channel-specific messages
     */
    @MessageMapping("/sendChannelMessage")
    @SendTo("/topic/channel/{channel}")
    public ChatMessage sendChannelMessage(ChatMessage message) {
        if (message.id == null) {
            message.id = java.util.UUID.randomUUID().toString();
        }
        MessageService.addMessage(message.sender, message.content, true);
        TypingIndicatorService.userStoppedTyping(message.sender, message.channel);
        return message;
    }

    /**
     * Handle user coming online
     */
    @MessageMapping("/user/online")
    @SendTo("/topic/presence")
    public PresenceEvent userOnline(PresenceEvent event) {
        UserPresenceService.userOnline(event.username, event.channel);
        event.onlineCount = UserPresenceService.getOnlineUserCount();
        return event;
    }

    /**
     * Handle user going offline
     */
    @MessageMapping("/user/offline")
    @SendTo("/topic/presence")
    public PresenceEvent userOffline(PresenceEvent event) {
        UserPresenceService.userOffline(event.username);
        event.status = "offline";
        event.onlineCount = UserPresenceService.getOnlineUserCount();
        return event;
    }

    /**
     * Handle typing indicator
     */
    @MessageMapping("/user/typing")
    @SendTo("/topic/typing")
    public TypingEvent userTyping(TypingEvent event) {
        if (event.isTyping) {
            TypingIndicatorService.userTyping(event.username, event.channel);
        } else {
            TypingIndicatorService.userStoppedTyping(event.username, event.channel);
        }
        return event;
    }

    /**
     * Handle message reactions
     */
    @MessageMapping("/message/react")
    @SendTo("/topic/reactions")
    public ReactionEvent reactToMessage(ReactionEvent event) {
        if ("add".equals(event.action)) {
            MessageReactionService.addReaction(event.messageId, event.emoji, event.username);
        } else if ("remove".equals(event.action)) {
            MessageReactionService.removeReaction(event.messageId, event.emoji, event.username);
        }
        return event;
    }
}
