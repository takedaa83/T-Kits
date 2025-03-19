package com.takeda.gui;

import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import com.takeda.utils.KitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitEditor {
    public KitEditor(Player player, int kit, boolean copyKit, String code, ItemStack[] items) {
        String kitName = "Kit " + kit;
        Inventory kitEditor = Bukkit.createInventory(player, 54, ChatColor.DARK_GRAY + "Kit Editor - " + kitName);

        if (copyKit && items != null) {
            ItemStack[] processedItems = KitUtils.processKitContents(items);
            for (int i = 0; i < 41 && i < processedItems.length; i++) {
                kitEditor.setItem(i, processedItems[i]);
            }
        } else {

            ItemStack[] kitContents = KitUtils.getKitContents(player, kit);
            if (kitContents != null) {
                ItemStack[] processedItems = KitUtils.processKitContents(kitContents);
                for (int i = 0; i < 41 && i < processedItems.length; i++) {
                    kitEditor.setItem(i, processedItems[i]);
                }
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Equipment Slots");
        lore.add(ChatColor.DARK_GRAY + "Boots, Legs, Chest, Helm, Offhand");
        ItemStack equipLabel = ItemUtils.getItem("&9Equipment Slots", Material.CYAN_STAINED_GLASS_PANE, lore);

        for (int i = 41; i <= 44; i++) {
            kitEditor.setItem(i, equipLabel);
        }

        ItemStack backButton = ItemUtils.getItem("&cBack", Material.RED_CONCRETE, Arrays.asList(
                "&7Return to Kit Menu."
        ));
        ItemStack loadInv = ItemUtils.getItem("&aLoad Inventory", Material.BUNDLE, Arrays.asList(
                "&7Load items from your inventory.",
                "&7Overwrites current kit items."
        ));
        ItemStack shareKit = ItemUtils.getItem("&bShare Kit", Material.COMPASS, Arrays.asList(
                "&7Share this kit with a code.",
                "&7Expires after &e" + Main.instance.getConfig().getInt("code-expiry-minutes", 5) + " minutes&7."
        ));
        ItemStack copyButton = ItemUtils.getItem("&3Import Kit", Material.KNOWLEDGE_BOOK, Arrays.asList(
                "&7Import kit using a shared code."
        ));
        ItemStack clearInv = ItemUtils.getItem("&eClear Kit", Material.BARRIER, Arrays.asList(
                "&7Remove all items from this kit.",
                "&cCannot be undone!"
        ));
        ItemStack repairItems = ItemUtils.getItem("&aRepair Items", Material.ANVIL, Arrays.asList(
                "&7Repair all damageable items.",
                "&7Restores item durability."
        ));
        ItemStack filler = ItemUtils.getItem(" ", Material.LIGHT_GRAY_STAINED_GLASS_PANE, null);

        kitEditor.setItem(45, backButton);
        kitEditor.setItem(47, loadInv);
        kitEditor.setItem(48, shareKit);
        kitEditor.setItem(49, copyButton);
        kitEditor.setItem(50, clearInv);
        kitEditor.setItem(51, repairItems);

        for (int i = 45; i <= 53; i++) {
            if (kitEditor.getItem(i) == null) {
                kitEditor.setItem(i, filler);
            }
        }
        player.openInventory(kitEditor);
        Main.kitEditorChecker.put(player.getUniqueId(), kit);
    }
    public KitEditor(Player player, int kit, boolean copyKit, String code) {
        this(player, kit, copyKit, code, null);
    }
}