package com.takeda.utils;

import com.takeda.Main;
import com.takeda.database.DataStorage;
import com.takeda.files.KitsFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FileUtils {
    public static void restore() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Main.getInstance().getDatabaseManager() != null &&
                        Main.getInstance().getDatabaseManager().isEnabled()) {
                    migrateToMySQL();
                } else {
                    restoreFromYAML();
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }


    private static void restoreFromYAML() {
        try {
            FileConfiguration config = KitsFile.get();
            if (config == null) {
                Bukkit.getLogger().warning("§c[T-Kits] §fKitsFile configuration is null, cannot restore from YAML.");
                return;
            }
            if (config.contains("kits")) {
                for (String uuid : config.getConfigurationSection("kits").getKeys(false)) {
                    Main.kitMap.putIfAbsent(uuid, new HashMap<>());
                    if (config.getConfigurationSection("kits." + uuid) == null) continue;

                    for (String kitKey : config.getConfigurationSection("kits." + uuid).getKeys(false)) {
                        List<?> list = config.getList("kits." + uuid + "." + kitKey);
                        ItemStack[] items = list != null ? list.stream()
                                .filter(item -> item instanceof ItemStack)
                                .map(item -> (ItemStack) item)
                                .toArray(ItemStack[]::new)
                                : new ItemStack[0];
                        if (Main.kitMap.get(uuid) != null) {
                            Main.kitMap.get(uuid).put(kitKey, items);
                        }
                    }
                }

                if (config != null && Main.kitMap != null) {
                    config.set("kits", Main.kitMap);
                    KitsFile.save();
                }

            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[T-Kits] §fCouldn't restore the KitMap: " + e.toString());
        }

        try {
            FileConfiguration config = KitsFile.get();
            if (config == null) {
                Bukkit.getLogger().warning("§c[T-Kits] §fKitsFile configuration is null, cannot restore echests from YAML.");
                return;
            }
            if (config.contains("echest")) {
                if (Main.echestMap == null) Main.echestMap = new HashMap<>();
                for (String uuid : config.getConfigurationSection("echest").getKeys(false)) {
                    Main.echestMap.putIfAbsent(uuid, new HashMap<>());
                    if (config.getConfigurationSection("echest." + uuid) == null) continue;

                    for (String echestKey : config.getConfigurationSection("echest." + uuid).getKeys(false)) {
                        List<?> list = config.getList("echest." + uuid + "." + echestKey);
                        ItemStack[] items = list != null ? list.stream()
                                .filter(item -> item instanceof ItemStack)
                                .map(item -> (ItemStack) item)
                                .toArray(ItemStack[]::new)
                                : new ItemStack[0];
                        if (Main.echestMap.get(uuid) != null) {
                            Main.echestMap.get(uuid).put(echestKey, items);
                        }
                    }
                }

                if (config != null && Main.echestMap != null) {
                    config.set("echest", Main.echestMap);
                    KitsFile.save();
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[T-Kits] §fCouldn't restore the EnderChestMap: " + e.toString());
        }
    }

    private static void migrateToMySQL() {
        DataStorage storage = Main.getInstance().getStorage();
        int migratedKits = 0;
        int migratedEChests = 0;

        FileConfiguration config = KitsFile.get();
        if (config == null) {
            Main.getInstance().getLogger().severe("§c[T-Kits] §fCannot migrate to MySQL due to kits.yml config being null.");
            return;
        }

        try {
            if (config.contains("kits")) {
                for (String uuidStr : config.getConfigurationSection("kits").getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        if (config.getConfigurationSection("kits." + uuidStr) == null) continue;
                        for (String kitKey : config.getConfigurationSection("kits." + uuidStr).getKeys(false)) {
                            try {
                                int kitNumber = Integer.parseInt(kitKey.replace("kit", ""));
                                List<?> list = config.getList("kits." + uuidStr + "." + kitKey);
                                ItemStack[] contents = list != null ? list.toArray(new ItemStack[0]) : new ItemStack[0];
                                storage.saveKit(uuid, kitNumber, contents);
                                migratedKits++;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("§c[T-Kits] §fError during kit migration: " + e.getMessage());
        }

        try {
            if (config.contains("echest")) {
                for (String uuidStr : config.getConfigurationSection("echest").getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        if (config.getConfigurationSection("echest." + uuidStr) == null) continue;
                        for (String echestKey : config.getConfigurationSection("echest." + uuidStr).getKeys(false)) {
                            try {
                                int echestNumber = Integer.parseInt(echestKey.replace("echest", ""));
                                List<?> list = config.getList("echest." + uuidStr + "." + echestKey);
                                ItemStack[] contents = list != null ? list.toArray(new ItemStack[0]) : new ItemStack[0];
                                storage.saveEnderChest(uuid, echestNumber, contents);
                                migratedEChests++;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("§c[T-Kits] §fError during enderchest migration: " + e.getMessage());
        }

        Main.getInstance().getLogger().info("§b[T-Kits] §fMigration complete:");
        Main.getInstance().getLogger().info("§b[T-Kits] §f- Migrated kits: " + migratedKits);
        Main.getInstance().getLogger().info("§b[T-Kits] §f- Migrated enderchests: " + migratedEChests);
    }
}