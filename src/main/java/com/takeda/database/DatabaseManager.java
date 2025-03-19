package com.takeda.database;

import com.takeda.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseManager {
    private static HikariDataSource dataSource;
    private final Main plugin;
    private boolean enabled;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.enabled = false;
        setupDatabase();
    }

    private void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("database.enabled", false)) {
            plugin.getLogger().info("§b[T-Kits] §fMySQL support is disabled. Using YAML storage.");
            return;
        }

        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String databaseName = config.getString("database.database_name");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        if (host == null || databaseName == null || username == null || password == null) {
            plugin.getLogger().severe("§c[T-Kits] §fInvalid database config. Falling back to YAML.");
            config.set("database.enabled", false);
            plugin.saveConfig();
            return;
        }

        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(config.getInt("database.pool_size", 10));
            hikariConfig.setIdleTimeout(config.getLong("database.idle_timeout", 300000));
            hikariConfig.setConnectionTimeout(config.getLong("database.connection_timeout", 10000));
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hikariConfig);
            enabled = true;
            createTables();
            plugin.getLogger().info("§b[T-Kits] §fMySQL database connected.");

            new DatabaseMigration(plugin).migrateData();
        } catch (Exception e) {
            enabled = false;
            plugin.getLogger().severe("§c[T-Kits] §fMySQL connect failed. Falling back to YAML.");
            plugin.getLogger().severe("§c[T-Kits] §fError: " + e.getMessage());

            config.set("database.enabled", false);
            plugin.saveConfig();
        }
    }

    private void createTables() {
        try {
            String playerKits = """
                CREATE TABLE IF NOT EXISTS player_kits (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    uuid VARCHAR(36) NOT NULL,
                    kit_number INT NOT NULL,
                    kit_data LONGTEXT NOT NULL,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_player_kit (uuid, kit_number)
                );
            """;

            String enderChests = """
                CREATE TABLE IF NOT EXISTS player_enderchests (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    uuid VARCHAR(36) NOT NULL,
                    echest_number INT NOT NULL,
                    echest_data LONGTEXT NOT NULL,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_player_echest (uuid, echest_number)
                );
            """;

            try (var conn = dataSource.getConnection();
                 var stmt = conn.createStatement()) {
                stmt.execute(playerKits);
                stmt.execute(enderChests);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fDatabase table creation failed!");
            plugin.getLogger().severe("§c[T-Kits] §fError: " + e.getMessage());
            enabled = false;
        }
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}