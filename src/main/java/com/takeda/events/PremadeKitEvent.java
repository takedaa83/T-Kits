package com.takeda.events;

import com.takeda.Main;
import com.takeda.gui.CopyKit;
import com.takeda.gui.KitMenu;
import com.takeda.utils.PremadeKitUtils;
import com.takeda.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class PremadeKitEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID unique = player.getUniqueId();
        if (!Main.premadeKitChecker.contains(unique)) return;

        if (player.hasPermission("tkits.edit")) {
            handleAdminActions(player, event);
        } else {
            handlePlayerActions(player, event);
        }
    }

    private void handleAdminActions(Player player, InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory) return;

        int slot = event.getSlot();
        if (slot >= 45) {
            event.setCancelled(true);
            handleBottomRowClick(player, slot);
        }
    }

    private void handleBottomRowClick(Player player, int slot) {
        switch (slot) {
            case 45:
                PremadeKitUtils.save(player);
                SoundUtils.playKitSaveSound(player);
                break;
            case 47:
                loadInventory(player);
                break;
            case 48:
                new CopyKit(player, 0, true);
                SoundUtils.playMenuClickSound(player);
                break;
            case 49:
                clearInventory(player);
                break;
            case 50:
                repairItems(player);
                break;
        }
    }

    private void loadInventory(Player player) {
        var inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i <= 40; i++) {
            inventory.setItem(i, player.getInventory().getItem(i));
        }
        SoundUtils.playMenuClickSound(player);
    }

    private void clearInventory(Player player) {
        var inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i <= 40; i++) {
            inventory.setItem(i, null);
        }
        SoundUtils.playMenuClickSound(player);
    }

    private void repairItems(Player player) {
        var inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i <= 40; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getItemMeta() instanceof Damageable meta && meta.hasDamage()) {
                meta.setDamage(0);
                item.setItemMeta((ItemMeta) meta);
                inventory.setItem(i, item);
            }
        }
        SoundUtils.playSuccessSound(player);
    }

    private void handlePlayerActions(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getSlot() == 45) {
            new KitMenu(player);
            SoundUtils.playMenuClickSound(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Main.premadeKitChecker.remove(event.getPlayer().getUniqueId());
        }
    }
}