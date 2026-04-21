package com.jlivechats.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.jlivechats.service.MessageService;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/messages")
    public ResponseEntity<List<MessageService.Message>> getMessages(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        return ResponseEntity.ok(MessageService.getRecentMessages(50, currentUser));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageService.Message> sendMessage(@RequestParam String sender, 
                                                              @RequestParam String content,
                                                              HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MessageService.addMessage(sender, content, true);  // Always true for posted messages
        List<MessageService.Message> messages = MessageService.getRecentMessages(1, currentUser);
        if (!messages.isEmpty()) {
            return ResponseEntity.ok(messages.get(0));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/messages/count")
    public ResponseEntity<Integer> getMessageCount() {
        return ResponseEntity.ok(MessageService.getMessageCount());
    }
}
