package com.jlivechats.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service to manage user theme preferences
 */
public class ThemeService {
    
    private static Map<String, String> userThemes = new ConcurrentHashMap<>();
    
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    /**
     * Set user theme preference
     */
    public static void setUserTheme(String username, String theme) {
        if (THEME_DARK.equals(theme) || THEME_LIGHT.equals(theme)) {
            userThemes.put(username, theme);
        }
    }

    /**
     * Get user theme preference
     */
    public static String getUserTheme(String username) {
        return userThemes.getOrDefault(username, THEME_DARK);
    }

    /**
     * Toggle theme for user
     */
    public static String toggleTheme(String username) {
        String currentTheme = getUserTheme(username);
        String newTheme = THEME_DARK.equals(currentTheme) ? THEME_LIGHT : THEME_DARK;
        setUserTheme(username, newTheme);
        return newTheme;
    }

    /**
     * Reset theme to default
     */
    public static void resetTheme(String username) {
        userThemes.remove(username);
    }

    /**
     * Check if light theme is enabled
     */
    public static boolean isLightTheme(String username) {
        return THEME_LIGHT.equals(getUserTheme(username));
    }
}
