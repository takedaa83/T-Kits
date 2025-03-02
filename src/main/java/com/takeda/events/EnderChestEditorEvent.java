package com.takeda.events;

import java.util.UUID;
import com.takeda.Main;
import com.takeda.gui.KitMenu;
import com.takeda.utils.EnderChestUtils;
import com.takeda.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class EnderChestEditorEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID unique = player.getUniqueId();
        if (!Main.echestEditorChecker.containsKey(unique)) return;

        if (event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot >= 27) {
            handleMenuActions(player, slot);
        }
    }

    private void handleMenuActions(Player player, int slot) {
        switch (slot) {
            case 27: // Back to menu
                new KitMenu(player);
                SoundUtils.playMenuClickSound(player);
                break;
            case 30: // Clear inventory
                clearInventory(player);
                break;
            case 31: // Repair all items
                repairItems(player);
                break;
        }
    }

    private void clearInventory(Player player) {
        var inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i <= 26; i++) {
            inventory.setItem(i, null);
        }
        SoundUtils.playMenuClickSound(player);
    }

    private void repairItems(Player player) {
        var inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i <= 26; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getItemMeta() instanceof Damageable meta && meta.hasDamage()) {
                meta.setDamage(0);
                item.setItemMeta((ItemMeta) meta);
                inventory.setItem(i, item);
            }
        }
        SoundUtils.playSuccessSound(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        UUID unique = player.getUniqueId();
        if (Main.echestEditorChecker.containsKey(unique)) {
            int echest = Main.echestEditorChecker.get(unique);
            EnderChestUtils.save(player, echest);
            Main.echestEditorChecker.remove(unique);
        }
    }
}