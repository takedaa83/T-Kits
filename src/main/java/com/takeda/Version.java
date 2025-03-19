package com.takeda;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Properties;

public class Version {
    private final String version;
    private final String buildTimestamp;

    public Version(JavaPlugin plugin) {
        Properties props = new Properties();
        String tempVersion;
        String tempTimestamp;

        try {
            var inputStream = plugin.getClass().getClassLoader().getResourceAsStream("plugin.properties");
            if (inputStream != null) {
                props.load(inputStream);
                tempVersion = props.getProperty("version", "unknown");
                tempTimestamp = props.getProperty("build.timestamp", "unknown");
            } else {
                tempVersion = "unknown";
                tempTimestamp = "unknown";
                plugin.getLogger().warning("Could not find plugin.properties");
            }
        } catch (IOException e) {
            tempVersion = "unknown";
            tempTimestamp = "unknown";
            plugin.getLogger().warning("Failed to load version from properties: " + e.getMessage());
        }

        this.version = tempVersion;
        this.buildTimestamp = tempTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public String getFullVersionInfo() {
        return String.format("Version: %s (Built: %s)", version, buildTimestamp);
    }
}