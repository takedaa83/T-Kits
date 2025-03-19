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
}