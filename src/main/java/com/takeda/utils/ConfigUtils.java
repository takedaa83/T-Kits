package com.takeda.utils;

import com.takeda.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtils {
    private static final FileConfiguration config = Main.instance.getConfig();

    public static String getColoredString(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, ""));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', "&8[&3T&bKits&8] &7");
    }

    public static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&f" + message); // Prefix removed here
    }

    public static String formatError(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&cError: &f" + message); // Prefix removed here
    }

    public static String formatSuccess(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&aSuccess: &f" + message); // Prefix removed here
    }
}