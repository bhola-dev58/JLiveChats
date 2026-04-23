package com.jlivechats.controller;

import com.jlivechats.model.ChatMessage;
import com.jlivechats.repository.ChatMessageRepository;
import com.jlivechats.service.UserPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for chat history and online users.
 * Provides endpoints consumed by both index.html (script.js) and chat.html (chat.js).
 */
@RestController
@RequestMapping("/api")
public class ChatApiController {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatApiController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * GET /api/history?channel=general
     * Fetches last 50 messages from ChatMessageRepository for the given channel.
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(defaultValue = "general") String channel) {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByChannelOrderByTimestampAsc(channel);
        
        List<Map<String, Object>> result = messages.stream().map(msg -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", msg.getId());
            map.put("sender", msg.getSender());
            map.put("content", msg.getContent());
            map.put("channel", msg.getChannel());
            map.put("type", msg.getType() != null ? msg.getType().name() : "CHAT");
            map.put("timestamp", msg.getTimestamp() != null 
                ? msg.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) 
                : "");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/users/online
     * Returns list of currently online users from UserPresenceService.
     */
    @GetMapping("/users/online")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        List<String> onlineUsers = UserPresenceService.getOnlineUsers();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("users", onlineUsers);
        response.put("count", onlineUsers.size());
        return ResponseEntity.ok(response);
    }
}
