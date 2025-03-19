package com.takeda.gui;

import com.takeda.files.KitRoomFile;
import com.takeda.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KitRoom {
    public KitRoom(Player player, int category) {
        Inventory kitRoom = Bukkit.createInventory(player, 54, ChatColor.DARK_GRAY + "Kit Room");

        ItemStack[] items = KitRoomFile.getItems(category);
        if (items != null) {
            for (int i = 0; i < Math.min(items.length, 45); i++) {
                kitRoom.setItem(i, items[i]);
            }
        }

        ItemStack filler = ItemUtils.getItem(" ", Material.GRAY_STAINED_GLASS_PANE, null);
        for (int i = 45; i < 54; i++) {
            kitRoom.setItem(i, filler);
        }

        ItemStack backButton = ItemUtils.getItem("&cBack to Menu", Material.RED_TERRACOTTA, Arrays.asList(
                "&7Return to the Kit Menu."
        ));
        kitRoom.setItem(45, backButton);

        ItemStack[] categoryButtons = {
                createCategoryButton("Combat", Material.NETHERITE_SWORD, ChatColor.RED),
                createCategoryButton("Potions", Material.POTION, ChatColor.BLUE),
                createCategoryButton("Pearls", Material.ENDER_PEARL, ChatColor.GREEN),
                createCategoryButton("Arrows", Material.ARROW, ChatColor.YELLOW),
                createCategoryButton("Other", Material.AMETHYST_SHARD, ChatColor.AQUA)
        };

        for (int i = 0; i < categoryButtons.length; i++) {
            kitRoom.setItem(47 + i, categoryButtons[i]);
        }

        if (player.hasPermission("tkits.edit")) {
            ItemStack saveButton = ItemUtils.getItem("&aSave Changes", Material.LIME_TERRACOTTA, Arrays.asList(
                    "&7Save changes to this category."
            ));
            kitRoom.setItem(52, saveButton);
        }

        ItemStack refillButton = ItemUtils.getItem("&eRefill Items", Material.HOPPER, Arrays.asList(
                "&7Refill items to default."
        ));
        kitRoom.setItem(53, refillButton);

        if (kitRoom.getItem(47 + category) != null) {
            ItemStack currentCategory = kitRoom.getItem(47 + category);
            ItemMeta meta = currentCategory.getItemMeta();
            meta.addEnchant(Enchantment.PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            currentCategory.setItemMeta(meta);
        }

        player.openInventory(kitRoom);
    }

    private ItemStack createCategoryButton(String name, Material material, ChatColor color) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to view category.",
                ChatColor.DARK_GRAY + "Browse items in " + color + name + ChatColor.DARK_GRAY + "."
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        button.setItemMeta(meta);
        return button;
    }
}