package com.takeda.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.takeda.gui.KitMenu;
import com.takeda.gui.KitRoom;
import com.takeda.utils.KitRoomUtils;
import com.takeda.utils.SoundUtils;

public class KitRoomEvent implements Listener {

    private InventoryClickEvent currentEvent;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().contains("Kit Room")) return;

        currentEvent = event;
        int slot = event.getSlot();
        if (slot >= 45) {
            event.setCancelled(true);
            handleBottomRowClick(player, slot);
        } else if (!player.hasPermission("tkits.edit")) {
            event.setCancelled(true);
        }
    }

    private void handleBottomRowClick(Player player, int slot) {
        switch (slot) {
            case 45: // Back button
                new KitMenu(player);
                SoundUtils.playMenuClickSound(player);
                break;
            case 47: case 48: case 49: case 50: case 51: // Category buttons
                handleCategoryButtonClick(player, slot);
                break;
            case 52: // Save button
                handleSaveButtonClick(player);
                break;
            case 53: // Refill button
                handleRefillButtonClick(player);
                break;
        }
    }

    private void handleCategoryButtonClick(Player player, int slot) {
        int category = slot - 47; // Convert slot to category number (0-4)
        new KitRoom(player, category);
    }

    private void handleSaveButtonClick(Player player) {
        if (player.hasPermission("tkits.edit")) {
            int category = getCurrentCategory(currentEvent);
            if (category != -1) {
                KitRoomUtils.save(player, category);
            }
        }
    }
            
    private void handleRefillButtonClick(Player player) {
        int category = getCurrentCategory(currentEvent);
        if (category != -1) {
            KitRoomUtils.refillItems(player, category);
        }
    }

    private int getCurrentCategory(InventoryClickEvent event) {
        for (int i = 47; i <= 51; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item != null && item.getItemMeta().hasEnchants()) {
                return i - 47;
            }
        }
        return -1;
    }
}