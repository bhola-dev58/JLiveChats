package com.jlivechats.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

/**
 * Service to manage user roles and permissions
 */
public class UserRoleService {
    
    private static Map<String, String> userRoles = new ConcurrentHashMap<>();
    
    public static final String ROLE_USER = "user";
    public static final String ROLE_MODERATOR = "moderator";
    public static final String ROLE_ADMIN = "admin";

    static {
        // Initialize with default user role
    }

    /**
     * Set user role
     */
    public static void setUserRole(String username, String role) {
        if (isValidRole(role)) {
            userRoles.put(username, role);
        }
    }

    /**
     * Get user role
     */
    public static String getUserRole(String username) {
        return userRoles.getOrDefault(username, ROLE_USER);
    }

    /**
     * Check if user has role
     */
    public static boolean hasRole(String username, String role) {
        return getUserRole(username).equals(role);
    }

    /**
     * Check if user is admin
     */
    public static boolean isAdmin(String username) {
        return ROLE_ADMIN.equals(getUserRole(username));
    }

    /**
     * Check if user is moderator
     */
    public static boolean isModerator(String username) {
        String role = getUserRole(username);
        return ROLE_MODERATOR.equals(role) || ROLE_ADMIN.equals(role);
    }

    /**
     * Get all moderators
     */
    public static Set<String> getAllModerators() {
        Set<String> moderators = new HashSet<>();
        for (Map.Entry<String, String> entry : userRoles.entrySet()) {
            if (ROLE_MODERATOR.equals(entry.getValue()) || ROLE_ADMIN.equals(entry.getValue())) {
                moderators.add(entry.getKey());
            }
        }
        return moderators;
    }

    /**
     * Validate role
     */
    private static boolean isValidRole(String role) {
        return ROLE_USER.equals(role) || ROLE_MODERATOR.equals(role) || ROLE_ADMIN.equals(role);
    }

    /**
     * Promote user to moderator
     */
    public static void promoteToModerator(String username) {
        setUserRole(username, ROLE_MODERATOR);
    }

    /**
     * Promote user to admin
     */
    public static void promoteToAdmin(String username) {
        setUserRole(username, ROLE_ADMIN);
    }

    /**
     * Demote user to regular user
     */
    public static void demoteToUser(String username) {
        setUserRole(username, ROLE_USER);
    }
}
