package com.takeda.gui;

import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PremadeKit {
    public PremadeKit(Player player, boolean copyKit, String code, ItemStack[] items) {
        Inventory premadeKit = Bukkit.createInventory(player, 54, ChatColor.DARK_GRAY + "Premade Kit");
        if (copyKit && items != null) {
            premadeKit.setContents(items);
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Equipment Slots");
        lore.add(ChatColor.DARK_GRAY + "Boots, Legs, Chest, Helm, Offhand");
        ItemStack equipLabel = ItemUtils.getItem("&9Equipment Slots", Material.CYAN_STAINED_GLASS_PANE, lore);
        for (int i = 41; i <= 44; i++) {
            premadeKit.setItem(i, equipLabel);
        }

        if (player.hasPermission("tkits.edit")) {
            ItemStack saveButton = ItemUtils.getItem("&aSave Premade Kit", Material.LIME_TERRACOTTA,
                    Arrays.asList(
                            "&7Save changes to Premade Kit.",
                            "&7Affects all players."
                    ));
            premadeKit.setItem(45, saveButton);

            ItemStack loadInv = ItemUtils.getItem("&aLoad Inventory", Material.BUNDLE,
                    Arrays.asList(
                            "&7Load items from your inventory.",
                            "&7Overwrite Premade Kit."
                    ));
            premadeKit.setItem(47, loadInv);

            ItemStack copyButton = ItemUtils.getItem("&3Import Kit", Material.KNOWLEDGE_BOOK,
                    Arrays.asList(
                            "&7Import items using kit code.",
                            "&7Overwrite Premade Kit."
                    ));
            premadeKit.setItem(48, copyButton);

            ItemStack clearInv = ItemUtils.getItem("&eClear Kit", Material.TNT,
                    Arrays.asList(
                            "&7Clear all Premade Kit items.",
                            "&cCannot be undone!"
                    ));
            premadeKit.setItem(49, clearInv);

            ItemStack repairItems = ItemUtils.getItem("&aRepair Items", Material.ANVIL,
                    Arrays.asList(
                            "&7Repair all damageable items.",
                            "&7Restore item durability."
                    ));
            premadeKit.setItem(50, repairItems);
        } else {
            ItemStack backButton = ItemUtils.getItem("&cBack to Menu", Material.RED_TERRACOTTA,
                    Arrays.asList(
                            "&7Return to Kit Menu."
                    ));
            premadeKit.setItem(45, backButton);
        }

        ItemStack filler = ItemUtils.getItem(" ", Material.BLACK_STAINED_GLASS_PANE, null);
        for (int i = 45; i <= 53; i++) {
            if (premadeKit.getItem(i) == null) {
                premadeKit.setItem(i, filler);
            }
        }
        player.openInventory(premadeKit);
        Main.premadeKitChecker.add(player.getUniqueId());
    }
    public PremadeKit(Player player, boolean copyKit, String code) {
        this(player, copyKit, code, null);
    }
}