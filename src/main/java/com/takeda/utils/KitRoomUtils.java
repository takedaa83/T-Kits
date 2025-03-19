package com.takeda.utils;

import com.takeda.Main;
import com.takeda.files.KitRoomFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;

public class KitRoomUtils {

    public static void save(Player player, int category) {
        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(Main.instance.getDataFolder(), "kitroom.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                ItemStack[] items = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 45);

                ItemStack[] itemsCopy = new ItemStack[items.length];
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        itemsCopy[i] = items[i].clone();
                    }
                }

                config.set("items." + category, Arrays.asList(itemsCopy));
                KitRoomFile.setItems(category, itemsCopy);

                try {
                    config.save(file);
                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Kit Room Updated"));
                    SoundUtils.playKitSaveSound(player);
                } catch (Exception e) {
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError("Failed to save Kit Room"));
                    SoundUtils.playErrorSound(player);
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }

    public static void refillItems(Player player, int category) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack[] originalItems = KitRoomFile.getItems(category);

                if (originalItems != null) {
                    for (int i = 0; i < 45; i++) {
                        player.getOpenInventory().getTopInventory().setItem(i, null);
                    }

                    for (int i = 0; i < Math.min(originalItems.length, 45); i++) {
                        if (originalItems[i] != null) {
                            player.getOpenInventory().getTopInventory().setItem(i, originalItems[i].clone());
                        }
                    }

                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Kit Room Refilled"));
                    SoundUtils.playSuccessSound(player);
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }
}