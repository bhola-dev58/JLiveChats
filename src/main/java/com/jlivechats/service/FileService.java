package com.jlivechats.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage file and image uploads
 */
public class FileService {
    
    private static Map<String, FileInfo> files = new ConcurrentHashMap<>();
    
    public static class FileInfo {
        public String id;
        public String filename;
        public String uploadedBy;
        public String uploadedAt;
        public String fileType;
        public long fileSize;
        
        public FileInfo(String filename, String uploadedBy, String fileType, long fileSize) {
            this.id = "file-" + System.nanoTime();
            this.filename = filename;
            this.uploadedBy = uploadedBy;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.uploadedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * Upload file
     */
    public static FileInfo uploadFile(String filename, String uploadedBy, String fileType, long fileSize) {
        FileInfo file = new FileInfo(filename, uploadedBy, fileType, fileSize);
        files.put(file.id, file);
        return file;
    }

    /**
     * Get file info
     */
    public static FileInfo getFileInfo(String fileId) {
        return files.get(fileId);
    }

    /**
     * Get all files
     */
    public static Collection<FileInfo> getAllFiles() {
        return files.values();
    }

    /**
     * Delete file
     */
    public static void deleteFile(String fileId) {
        files.remove(fileId);
    }
}
