package com.takeda.database;

import com.takeda.Main;
import com.takeda.files.KitsFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class DatabaseMigration {
    private final Main plugin;
    private final MySQLStorage mysqlStorage;
    private int totalMigrated = 0;
    private int totalErrors = 0;

    public DatabaseMigration(Main plugin) {
        this.plugin = plugin;
        this.mysqlStorage = new MySQLStorage(plugin, plugin.getDatabaseManager());
    }

    public void migrateData() {
        plugin.getLogger().info("§b[T-Kits] §fStarting YAML to MySQL data migration...");

        FileConfiguration config = KitsFile.get();
        if (config == null) {
            plugin.getLogger().info("§b[T-Kits] §fMigration skipped: KitsFile config null.");
            return;
        }

        if (config.getKeys(false).isEmpty()) {
            plugin.getLogger().info("§b[T-Kits] §fMigration skipped: KitsFile config empty.");
            return;
        }

        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String uuidStr : kitsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSection = kitsSection.getConfigurationSection(uuidStr);
                    if (playerSection != null) {
                        migratePlayerKits(uuid, playerSection);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("§c[T-Kits] §fInvalid UUID in kits.yml: " + uuidStr);
                    totalErrors++;
                }
            }
        }

        ConfigurationSection echestSection = config.getConfigurationSection("echest");
        if (echestSection != null) {
            for (String uuidStr : echestSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSection = echestSection.getConfigurationSection(uuidStr);
                    if (playerSection != null) {
                        migratePlayerEnderChests(uuid, playerSection);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("§c[T-Kits] §fInvalid UUID in enderchest section: " + uuidStr);
                    totalErrors++;
                }
            }
        }

        plugin.getLogger().info("§b[T-Kits] §fMigration complete:");
        plugin.getLogger().info("§b[T-Kits] §f- Migrated items: " + totalMigrated);
        if (totalErrors > 0) {
            plugin.getLogger().warning("§c[T-Kits] §f- Migration errors: " + totalErrors);
        }
    }

    private void migratePlayerKits(UUID uuid, ConfigurationSection playerSection) {
        for (String kitKey : playerSection.getKeys(false)) {
            if (kitKey.startsWith("kit")) {
                try {
                    int kitNumber = Integer.parseInt(kitKey.replace("kit", ""));
                    List<?> list = playerSection.getList(kitKey);
                    ItemStack[] contents = list != null ? list.toArray(new ItemStack[0]) : new ItemStack[0];
                    mysqlStorage.saveKit(uuid, kitNumber, contents);
                    totalMigrated++;
                    plugin.getLogger().info("§b[T-Kits] §fMigrated kit " + kitNumber + " for player " + uuid);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("§c[T-Kits] §fInvalid kit number format: " + kitKey);
                    totalErrors++;
                } catch (Exception e) {
                    plugin.getLogger().warning("§c[T-Kits] §fError migrating kit " + kitKey + " for player " + uuid);
                    totalErrors++;
                }
            }
        }
    }

    private void migratePlayerEnderChests(UUID uuid, ConfigurationSection playerSection) {
        for (String echestKey : playerSection.getKeys(false)) {
            if (echestKey.startsWith("echest")) {
                try {
                    int echestNumber = Integer.parseInt(echestKey.replace("echest", ""));
                    List<?> list = playerSection.getList(echestKey);
                    ItemStack[] contents = list != null ? list.toArray(new ItemStack[0]) : new ItemStack[0];
                    mysqlStorage.saveEnderChest(uuid, echestNumber, contents);
                    totalMigrated++;
                    plugin.getLogger().info("§b[T-Kits] §fMigrated enderchest " + echestNumber + " for player " + uuid);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("§c[T-Kits] §fInvalid echest number format: " + echestKey);
                    totalErrors++;
                } catch (Exception e) {
                    plugin.getLogger().warning("§c[T-Kits] §fError migrating enderchest " + echestKey + " for player " + uuid);
                    totalErrors++;
                }
            }
        }
    }
}