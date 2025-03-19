package com.takeda.gui;

import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class EnderChestEditor {
    public EnderChestEditor(Player player, int echest) {
        UUID unique = player.getUniqueId();
        String uuid = unique.toString();
        String echestKey = "echest" + echest;

        Inventory echestEditor = Bukkit.createInventory(player, 36, ChatColor.DARK_GRAY + "Ender Kit " + echest);

        try {
            echestEditor.setContents(((HashMap<String, ItemStack[]>) Main.echestMap.get(uuid)).get(echestKey));
        } catch (Exception ignored) {}

        ItemStack backButton = ItemUtils.getItem("&cBack to Menu", Material.RED_TERRACOTTA, Arrays.asList(
                "&7Return to the main menu."
        ));
        ItemStack clearInv = ItemUtils.getItem("&eClear Kit", Material.TNT, Arrays.asList(
                "&7Clear all items in this Ender Kit."
        ));
        ItemStack repairItems = ItemUtils.getItem("&aRepair Items", Material.ANVIL, Arrays.asList(
                "&7Repair all damageable items."
        ));
        ItemStack filler = ItemUtils.getItem(" ", Material.BLACK_STAINED_GLASS_PANE, null);

        echestEditor.setItem(27, backButton);
        echestEditor.setItem(30, clearInv);
        echestEditor.setItem(31, repairItems);

        for (int i = 27; i <= 35; i++) {
            if (echestEditor.getItem(i) == null) {
                echestEditor.setItem(i, filler);
            }
        }

        player.openInventory(echestEditor);
        Main.echestEditorChecker.put(unique, echest);
    }
}