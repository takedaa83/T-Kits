package com.takeda.database;

import com.takeda.Main;

public class StorageFactory {
    private final Main plugin;
    private final DatabaseManager databaseManager;
    private DataStorage activeStorage;

    public StorageFactory(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        initializeStorage();
    }

    private void initializeStorage() {
        if (databaseManager != null && databaseManager.isEnabled()) {
            activeStorage = new MySQLStorage(plugin, databaseManager);
            plugin.getLogger().info("§b[T-Kits] §fUsing MySQL storage");
        } else {
            activeStorage = createYAMLStorage();
            plugin.getLogger().info("§b[T-Kits] §fUsing YAML storage");
        }
    }

    public DataStorage createYAMLStorage() {
        return new YAMLStorage(plugin);
    }

    public DataStorage getStorage() {
        return activeStorage;
    }

    public void switchToYAML() {
        activeStorage = createYAMLStorage();
        plugin.getLogger().info("§b[T-Kits] §fSwitched to YAML storage");
    }

    public void switchToMySQL() {
        if (databaseManager.isEnabled()) {
            activeStorage = new MySQLStorage(plugin, databaseManager);
            plugin.getLogger().info("§b[T-Kits] §fSwitched to MySQL storage");
        } else {
            plugin.getLogger().warning("§c[T-Kits] §fFailed to switch to MySQL storage - database not enabled");
        }
    }
}