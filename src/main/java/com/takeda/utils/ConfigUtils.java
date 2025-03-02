package com.takeda.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import com.takeda.Main;

public class ConfigUtils {
    private static final FileConfiguration config = Main.instance.getConfig();

    public static String getColoredString(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, ""));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', "&7[&b&lT&3&lK&7] ");
    }

    public static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&3" + message);
    }

    public static String formatError(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&c" + message);
    }

    public static String formatSuccess(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&a" + message);
    }
}