package com.takeda.gui;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.takeda.Main;
import com.takeda.utils.ItemUtils;

public class EnderChestEditor {
    public EnderChestEditor(Player player, int echest) {
        UUID unique = player.getUniqueId();
        String uuid = unique.toString();
        String echestKey = "echest" + echest;

        Inventory echestEditor = Bukkit.createInventory(player, 36, ChatColor.AQUA + "Ender Chest " + echest);

        try {
            echestEditor.setContents(((HashMap<String, ItemStack[]>) Main.echestMap.get(uuid)).get(echestKey));
        } catch (Exception ignored) {}

        // Menu buttons
        ItemStack backButton = ItemUtils.getItem(ChatColor.AQUA + "Back to Menu", Material.OAK_DOOR, null);
        ItemStack clearInv = ItemUtils.getItem(ChatColor.AQUA + "Clear Inventory", Material.STRUCTURE_VOID, null);
        ItemStack repairItems = ItemUtils.getItem(ChatColor.AQUA + "Repair All Items", Material.EXPERIENCE_BOTTLE, null);
        ItemStack filler = ItemUtils.getItem(" ", Material.BLACK_STAINED_GLASS_PANE, null);

        echestEditor.setItem(27, backButton);
        echestEditor.setItem(30, clearInv);
        echestEditor.setItem(31, repairItems);

        // Fill empty slots
        for (int i = 27; i <= 35; i++) {
            if (echestEditor.getItem(i) == null) {
                echestEditor.setItem(i, filler);
            }
        }

        player.openInventory(echestEditor);
        Main.echestEditorChecker.put(unique, echest);
    }
}