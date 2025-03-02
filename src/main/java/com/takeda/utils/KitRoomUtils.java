package com.takeda.utils;

import java.io.File;
import java.util.Arrays;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.takeda.Main;
import com.takeda.files.KitRoomFile;

import org.bukkit.scheduler.BukkitRunnable;

public class KitRoomUtils {

    public static void save(Player player, int category) {
        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(Main.instance.getDataFolder(), "kitroom.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                // Get only the item slots (0-44), excluding the bottom row
                ItemStack[] items = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 45);

                // Create deep copy of items
                ItemStack[] itemsCopy = new ItemStack[items.length];
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        itemsCopy[i] = items[i].clone();
                    }
                }

                // Update both file and memory
                config.set("items." + category, Arrays.asList(itemsCopy));
                KitRoomFile.setItems(category, itemsCopy);

                try {
                    config.save(file);
                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Kit room changes saved"));
                    SoundUtils.playKitSaveSound(player);
                } catch (Exception e) {
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError("Failed to save kit room changes"));
                    SoundUtils.playErrorSound(player);
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }

    public static void refillItems(Player player, int category) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get the original items from KitRoomFile
                ItemStack[] originalItems = KitRoomFile.getItems(category);

                if (originalItems != null) {
                    // Clear current items first (0-44 slots)
                    for (int i = 0; i < 45; i++) {
                        player.getOpenInventory().getTopInventory().setItem(i, null);
                    }

                    // Set the original items with deep copy
                    for (int i = 0; i < Math.min(originalItems.length, 45); i++) {
                        if (originalItems[i] != null) {
                            player.getOpenInventory().getTopInventory().setItem(i, originalItems[i].clone());
                        }
                    }

                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Items refilled"));
                    SoundUtils.playSuccessSound(player);
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }
}