package com.takeda.events;

import com.takeda.Main;
import com.takeda.gui.EnderChestEditor;
import com.takeda.gui.KitEditor;
import com.takeda.gui.KitRoom;
import com.takeda.gui.PremadeKit;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.KitUtils;
import com.takeda.utils.SoundUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public class KitMenuEvent implements Listener {
    private static final FileConfiguration config = Main.instance.getConfig();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID unique = player.getUniqueId();
        if (!Main.kitMenuChecker.contains(unique)) return;

        if (event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        handleMenuActions(player, slot, event);
    }

    private void handleMenuActions(Player player, int slot, InventoryClickEvent event) {
        if (10 <= slot && slot <= 16) {
            handleKitSlotClick(player, slot, event);
        } else if (19 <= slot && slot <= 25 && !config.getBoolean("disable-echest")) {
            handleEnderChestSlotClick(player, slot);
        } else {
            switch (slot) {
                case 30:
                    handleKitRoomButton(player);
                    break;
                case 31:
                    handleInfoButton(player);
                    break;
                case 32:
                    handlePremadeKitButton(player, event.isLeftClick(), event.isRightClick());
                    break;
            }
        }
    }

    private void handleKitSlotClick(Player player, int slot, InventoryClickEvent event) {
        int kit = slot - 9;
        if (event.isLeftClick()) {
            KitUtils.claim(player, kit, false);
            player.closeInventory();
        } else if (event.isRightClick()) {
            new KitEditor(player, kit, false, null);
            SoundUtils.playMenuClickSound(player);
        }
    }

    private void handleEnderChestSlotClick(Player player, int slot) {
        int echest = slot - 18;
        new EnderChestEditor(player, echest);
        SoundUtils.playMenuClickSound(player);
    }

    private void handleKitRoomButton(Player player) {
        new KitRoom(player, 0);
        SoundUtils.playMenuClickSound(player);
        if (config.getBoolean("open-kitroom-enabled")) {
            String message = ConfigUtils.getColoredString("open-kitroom-prefix") +
                    player.getName() +
                    ConfigUtils.getColoredString("open-kitroom-suffix");
            Main.instance.getServer().broadcastMessage(message);
        }
    }

    private void handleInfoButton(Player player) {
        if (config.getBoolean("info-enabled")) {
            player.closeInventory();
            config.getStringList("info").forEach(line ->
                    SoundUtils.sendActionBar(player, ConfigUtils.formatMessage(line))
            );
            SoundUtils.playMenuClickSound(player);
        }
    }

    private void handlePremadeKitButton(Player player, boolean isLeftClick, boolean isRightClick) {
        if (isRightClick) {
            new PremadeKit(player, false, null);
            SoundUtils.playMenuClickSound(player);
        } else if (isLeftClick) {
            player.closeInventory();
            KitUtils.claim(player, 0, false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Main.kitMenuChecker.remove(event.getPlayer().getUniqueId());
        }
    }
}