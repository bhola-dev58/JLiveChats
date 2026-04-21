package com.jlivechats.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.jlivechats.service.MessageService;
import com.jlivechats.service.MessageSearchService;
import com.jlivechats.service.PinnedMessageService;
import com.jlivechats.service.MessageEditService;
import com.jlivechats.service.MentionService;
import com.jlivechats.service.ThreadService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    /**
     * Search messages by keyword
     */
    @GetMapping("/messages/search")
    public ResponseEntity<List<MessageSearchService.SearchResult>> searchMessages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sender) {
        List<MessageSearchService.SearchResult> results;
        
        if (keyword != null && sender != null) {
            results = MessageSearchService.searchMessages(keyword, sender);
        } else if (sender != null) {
            results = MessageSearchService.searchBySender(sender);
        } else if (keyword != null) {
            results = MessageSearchService.searchMessages(keyword);
        } else {
            results = new java.util.ArrayList<>();
        }
        
        return ResponseEntity.ok(results);
    }

    /**
     * Get search statistics
     */
    @GetMapping("/messages/search/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sender) {
        List<MessageSearchService.SearchResult> results;
        
        if (keyword != null && sender != null) {
            results = MessageSearchService.searchMessages(keyword, sender);
        } else if (sender != null) {
            results = MessageSearchService.searchBySender(sender);
        } else if (keyword != null) {
            results = MessageSearchService.searchMessages(keyword);
        } else {
            results = new java.util.ArrayList<>();
        }
        
        return ResponseEntity.ok(MessageSearchService.getSearchStats(results));
    }

    /**
     * Pin a message
     */
    @PostMapping("/messages/{messageId}/pin")
    public ResponseEntity<Map<String, Object>> pinMessage(
            @PathVariable String messageId,
            @RequestParam String sender,
            @RequestParam String content,
            @RequestParam String timestamp,
            @RequestParam String channel,
            HttpSession session) {
        String pinnedBy = (String) session.getAttribute("username");
        PinnedMessageService.pinMessage(messageId, sender, content, timestamp, pinnedBy, channel);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message pinned successfully");
        response.put("messageId", messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Unpin a message
     */
    @DeleteMapping("/messages/{messageId}/pin")
    public ResponseEntity<Map<String, Object>> unpinMessage(
            @PathVariable String messageId,
            @RequestParam String channel) {
        PinnedMessageService.unpinMessage(messageId, channel);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message unpinned successfully");
        response.put("messageId", messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pinned messages for a channel
     */
    @GetMapping("/messages/pinned")
    public ResponseEntity<List<PinnedMessageService.PinnedMessageInfo>> getPinnedMessages(
            @RequestParam String channel) {
        return ResponseEntity.ok(PinnedMessageService.getPinnedMessagesForChannel(channel));
    }

    /**
     * Check if message is pinned
     */
    @GetMapping("/messages/{messageId}/is-pinned")
    public ResponseEntity<Map<String, Boolean>> isMessagePinned(@PathVariable String messageId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isPinned", PinnedMessageService.isMessagePinned(messageId));
        return ResponseEntity.ok(response);
    }

    /**
     * Get pin count for channel
     */
    @GetMapping("/messages/pinned/count")
    public ResponseEntity<Map<String, Integer>> getPinCount(@RequestParam String channel) {
        Map<String, Integer> response = new HashMap<>();
        response.put("pinCount", PinnedMessageService.getPinCountForChannel(channel));
        return ResponseEntity.ok(response);
    }

    /**
     * Edit a message
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> editMessage(
            @PathVariable String messageId,
            @RequestParam String newContent,
            HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MessageService.Message message = MessageService.getMessageById(messageId);
        
        if (message == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Message not found");
            return ResponseEntity.notFound().build();
        }
        
        if (!message.sender.equals(currentUser)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "You can only edit your own messages");
            return ResponseEntity.badRequest().body(error);
        }
        
        MessageEditService.editMessage(messageId, newContent, currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message edited successfully");
        response.put("messageId", messageId);
        response.put("newContent", newContent + " (edited)");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable String messageId,
            HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MessageService.Message message = MessageService.getMessageById(messageId);
        
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!message.sender.equals(currentUser)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "You can only delete your own messages");
            return ResponseEntity.badRequest().body(error);
        }
        
        MessageEditService.deleteMessage(messageId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message deleted successfully");
        response.put("messageId", messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user can edit message
     */
    @GetMapping("/messages/{messageId}/can-edit")
    public ResponseEntity<Map<String, Object>> canEditMessage(
            @PathVariable String messageId,
            HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MessageService.Message message = MessageService.getMessageById(messageId);
        
        Map<String, Object> response = new HashMap<>();
        if (message != null) {
            response.put("canEdit", message.sender.equals(currentUser));
            response.put("canDelete", message.sender.equals(currentUser));
            response.put("isEdited", MessageEditService.isMessageEdited(messageId));
        } else {
            response.put("canEdit", false);
            response.put("canDelete", false);
            response.put("isEdited", false);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get edit info for message
     */
    @GetMapping("/messages/{messageId}/edit-info")
    public ResponseEntity<Map<String, Object>> getEditInfo(@PathVariable String messageId) {
        MessageEditService.EditedMessageInfo editInfo = MessageEditService.getEditInfo(messageId);
        
        Map<String, Object> response = new HashMap<>();
        if (editInfo != null) {
            response.put("messageId", editInfo.messageId);
            response.put("originalContent", editInfo.originalContent);
            response.put("editedContent", editInfo.editedContent);
            response.put("editedAt", editInfo.editedAt);
            response.put("editedBy", editInfo.editedBy);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get mentions for current user
     */
    @GetMapping("/mentions")
    public ResponseEntity<List<MentionService.MentionInfo>> getUserMentions(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        return ResponseEntity.ok(MentionService.getUserMentions(currentUser));
    }

    /**
     * Get unread mention count
     */
    @GetMapping("/mentions/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadMentionCount(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        Map<String, Integer> response = new HashMap<>();
        response.put("unreadCount", MentionService.getUnreadMentionCount(currentUser));
        return ResponseEntity.ok(response);
    }

    /**
     * Mark mention as read
     */
    @PostMapping("/mentions/{messageId}/read")
    public ResponseEntity<Map<String, Object>> markMentionAsRead(
            @PathVariable String messageId,
            HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MentionService.markMentionAsRead(messageId, currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mention marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * Extract mentions from message
     */
    @PostMapping("/messages/extract-mentions")
    public ResponseEntity<List<String>> extractMentions(@RequestParam String content) {
        return ResponseEntity.ok(MentionService.extractMentions(content));
    }

    /**
     * Record mention when message is sent
     */
    @PostMapping("/mentions/record")
    public ResponseEntity<Map<String, Object>> recordMention(
            @RequestParam String messageId,
            @RequestParam String mentionedUser,
            HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        MessageService.Message message = MessageService.getMessageById(messageId);
        
        if (message != null) {
            MentionService.addMention(messageId, mentionedUser, currentUser, message.content);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mention recorded");
        return ResponseEntity.ok(response);
    }

    /**
     * Create thread for a message
     */
    @PostMapping("/messages/{messageId}/thread")
    public ResponseEntity<ThreadService.ThreadInfo> createThread(@PathVariable String messageId) {
        MessageService.Message message = MessageService.getMessageById(messageId);
        
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        
        ThreadService.ThreadInfo thread = ThreadService.createThread(messageId, message.sender, message.content);
        return ResponseEntity.ok(thread);
    }

    /**
     * Get threads for a message
     */
    @GetMapping("/messages/{messageId}/threads")
    public ResponseEntity<List<ThreadService.ThreadInfo>> getThreads(@PathVariable String messageId) {
        return ResponseEntity.ok(ThreadService.getThreadsForMessage(messageId));
    }

    /**
     * Get thread by ID
     */
    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ThreadService.ThreadInfo> getThread(@PathVariable String threadId) {
        ThreadService.ThreadInfo thread = ThreadService.getThread(threadId);
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(thread);
    }

    /**
     * Add reply to thread
     */
    @PostMapping("/threads/{threadId}/reply")
    public ResponseEntity<Map<String, Object>> addReply(
            @PathVariable String threadId,
            @RequestParam String replyContent,
            HttpSession session) {
        String sender = (String) session.getAttribute("username");
        ThreadService.ThreadInfo thread = ThreadService.getThread(threadId);
        
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }
        
        String replyId = "reply-" + System.nanoTime();
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        ThreadService.addReplyToThread(threadId, replyId, sender, replyContent, timestamp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reply added successfully");
        response.put("replyId", replyId);
        response.put("threadId", threadId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get reply count for message
     */
    @GetMapping("/messages/{messageId}/reply-count")
    public ResponseEntity<Map<String, Integer>> getReplyCount(@PathVariable String messageId) {
        Map<String, Integer> response = new HashMap<>();
        response.put("replyCount", ThreadService.getReplyCountForMessage(messageId));
        return ResponseEntity.ok(response);
    }

    /**
     * Check if message has thread
     */
    @GetMapping("/messages/{messageId}/has-thread")
    public ResponseEntity<Map<String, Boolean>> hasThread(@PathVariable String messageId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasThread", ThreadService.hasThread(messageId));
        return ResponseEntity.ok(response);
    }
}
