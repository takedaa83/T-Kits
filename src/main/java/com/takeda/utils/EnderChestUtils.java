package com.takeda.utils;

import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.takeda.Main;
import org.bukkit.scheduler.BukkitRunnable;

public class EnderChestUtils {

    public static void claim(Player player, int echest) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String uuid = player.getUniqueId().toString();
                String echestKey = "echest" + echest;
                try {
                    ItemStack[] contents;
                    if (Main.getInstance().getDatabaseManager() != null && 
                        Main.getInstance().getDatabaseManager().isEnabled()) {
                        contents = Main.getInstance().getStorage().loadEnderChest(player.getUniqueId(), echest);
                    } else {
                        contents = ((HashMap<String, ItemStack[]>) Main.echestMap.get(uuid)).get(echestKey);
                    }

                    if (contents != null) {
                        player.getEnderChest().setContents(contents);
                    }
                } catch (Exception ignored) {}
            }
        }.runTaskAsynchronously(Main.instance);
    }

    public static void save(Player player, int echest) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String uuid = player.getUniqueId().toString();
                String echestKey = "echest" + echest;
                ItemStack[] echestItems = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 27);

                boolean echestItemsEmpty = true;
                for (int i = 0; i <= 25; i++) {
                    if (echestItems[i] != null) {
                        echestItemsEmpty = false;
                        break;
                    }
                }

                if (echestItemsEmpty) {
                    if (Main.getInstance().getDatabaseManager() != null && 
                        Main.getInstance().getDatabaseManager().isEnabled()) {
                        Main.getInstance().getStorage().deleteEnderChest(player.getUniqueId(), echest);
                    } else {
                        ((HashMap<String, ItemStack[]>) Main.echestMap.get(uuid)).remove(echestKey);
                    }
                    SoundUtils.sendActionBar(player, ConfigUtils.formatMessage("Ender Chest " + echest + " cleared"));
                    return;
                }

                // Save to appropriate storage
                if (Main.getInstance().getDatabaseManager() != null && 
                    Main.getInstance().getDatabaseManager().isEnabled()) {
                    Main.getInstance().getStorage().saveEnderChest(player.getUniqueId(), echest, echestItems);
                } else {
                    ((HashMap<String, ItemStack[]>) Main.echestMap.get(uuid)).put(echestKey, echestItems);
                }

                SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Ender Chest " + echest + " saved"));
                SoundUtils.playKitSaveSound(player);
            }
        }.runTaskAsynchronously(Main.instance);
    }
}