package com.jlivechats.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to manage message threads and replies
 */
public class ThreadService {
    
    private static Map<String, ThreadInfo> threads = new java.util.concurrent.ConcurrentHashMap<>();
    private static Map<String, List<String>> messageThreads = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static class ThreadInfo {
        public String threadId;
        public String parentMessageId;
        public String parentSender;
        public String parentContent;
        public List<Reply> replies;
        public String createdAt;
        
        public ThreadInfo(String threadId, String parentMessageId, String parentSender, String parentContent) {
            this.threadId = threadId;
            this.parentMessageId = parentMessageId;
            this.parentSender = parentSender;
            this.parentContent = parentContent;
            this.replies = Collections.synchronizedList(new ArrayList<>());
            this.createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    public static class Reply {
        public String replyId;
        public String sender;
        public String content;
        public String timestamp;
        
        public Reply(String replyId, String sender, String content, String timestamp) {
            this.replyId = replyId;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    /**
     * Create a thread from a message
     */
    public static ThreadInfo createThread(String parentMessageId, String parentSender, String parentContent) {
        String threadId = "thread-" + System.nanoTime();
        ThreadInfo thread = new ThreadInfo(threadId, parentMessageId, parentSender, parentContent);
        threads.put(threadId, thread);
        messageThreads.computeIfAbsent(parentMessageId, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(threadId);
        return thread;
    }

    /**
     * Add reply to thread
     */
    public static void addReplyToThread(String threadId, String replyId, String sender, String content, String timestamp) {
        ThreadInfo thread = threads.get(threadId);
        if (thread != null) {
            thread.replies.add(new Reply(replyId, sender, content, timestamp));
        }
    }

    /**
     * Get thread by ID
     */
    public static ThreadInfo getThread(String threadId) {
        return threads.get(threadId);
    }

    /**
     * Get threads for a message
     */
    public static List<ThreadInfo> getThreadsForMessage(String messageId) {
        List<ThreadInfo> result = new ArrayList<>();
        List<String> threadIds = messageThreads.getOrDefault(messageId, new ArrayList<>());
        for (String threadId : threadIds) {
            ThreadInfo thread = threads.get(threadId);
            if (thread != null) {
                result.add(thread);
            }
        }
        return result;
    }

    /**
     * Get reply count for message
     */
    public static int getReplyCountForMessage(String messageId) {
        List<ThreadInfo> threadList = getThreadsForMessage(messageId);
        int totalReplies = 0;
        for (ThreadInfo thread : threadList) {
            totalReplies += thread.replies.size();
        }
        return totalReplies;
    }

    /**
     * Check if message has thread
     */
    public static boolean hasThread(String messageId) {
        return messageThreads.containsKey(messageId) && !messageThreads.get(messageId).isEmpty();
    }

    /**
     * Delete thread
     */
    public static void deleteThread(String threadId) {
        ThreadInfo thread = threads.remove(threadId);
        if (thread != null) {
            List<String> threadList = messageThreads.get(thread.parentMessageId);
            if (threadList != null) {
                threadList.remove(threadId);
            }
        }
    }

    /**
     * Get all threads
     */
    public static Collection<ThreadInfo> getAllThreads() {
        return threads.values();
    }
}
