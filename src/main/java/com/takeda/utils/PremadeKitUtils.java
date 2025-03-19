package com.takeda.utils;

import com.takeda.Main;
import com.takeda.database.DataStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.UUID;

public class PremadeKitUtils {
    private static final FileConfiguration config = Main.instance.getConfig();
    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int PREMADE_KIT_NUMBER = 0;

    public static void claim(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    DataStorage storage = Main.getInstance().getStorage();
                    ItemStack[] contents = storage.loadKit(SYSTEM_UUID, PREMADE_KIT_NUMBER);

                    if (contents != null) {
                        player.getInventory().setContents(contents);
                        SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Premade Kit Loaded"));
                        SoundUtils.playKitLoadSound(player);

                        if (config.getBoolean("premadekit-enabled")) {
                            String message = ConfigUtils.getColoredString("premadekit-prefix") +
                                    player.getName() +
                                    ConfigUtils.getColoredString("premadekit-suffix");
                            Main.instance.getServer().broadcastMessage(message);
                        }
                    } else {
                        throw new Exception("Premade kit not found");
                    }
                } catch (Exception e) {
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError("Premade Kit not Created"));
                    SoundUtils.playErrorSound(player);
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }

    public static void save(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    DataStorage storage = Main.getInstance().getStorage();
                    ItemStack[] kitItems = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 41);
                    ItemStack[] processedItems = KitUtils.processKitContents(kitItems);

                    if (KitUtils.isKitEmpty(processedItems)) {
                        storage.deleteKit(SYSTEM_UUID, PREMADE_KIT_NUMBER);
                        SoundUtils.sendActionBar(player, ConfigUtils.formatMessage("Premade Kit Cleared"));
                        return;
                    }

                    storage.saveKit(SYSTEM_UUID, PREMADE_KIT_NUMBER, processedItems);
                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Premade Kit Saved"));
                    SoundUtils.playKitSaveSound(player);
                } catch (Exception e) {
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError("Failed to Save Premade Kit"));
                    SoundUtils.playErrorSound(player);
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.instance);
    }
}