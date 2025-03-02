package com.takeda.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.takeda.files.KitRoomFile;
import com.takeda.utils.ItemUtils;
import java.util.List;

public class KitRoom {
    public KitRoom(Player player, int category) {
        Inventory kitRoom = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "✦ Kit Room");

        // Load predefined items for this category
        ItemStack[] items = KitRoomFile.getItems(category);
        if (items != null) {
            for (int i = 0; i < Math.min(items.length, 45); i++) {
                kitRoom.setItem(i, items[i]);
            }
        }

        // Fill bottom row with glass panes first
        ItemStack filler = ItemUtils.getItem(" ", Material.PURPLE_STAINED_GLASS_PANE, null);
        for (int i = 45; i < 54; i++) {
            kitRoom.setItem(i, filler);
        }

        // Add buttons on top of the glass panes
        ItemStack backButton = ItemUtils.getItem(ChatColor.AQUA + "» Back to Menu", Material.SCULK_SENSOR, 
            List.of(
                ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to return",
                "",
                ChatColor.GRAY + "Return to the main menu"
            ));
        kitRoom.setItem(45, backButton);

        // Category buttons
        ItemStack[] categoryButtons = {
            createCategoryButton("Combat", Material.NETHERITE_AXE, ChatColor.RED),
            createCategoryButton("Potions", Material.DRAGON_BREATH, ChatColor.LIGHT_PURPLE),
            createCategoryButton("Pearls", Material.ECHO_SHARD, ChatColor.GREEN),
            createCategoryButton("Arrows", Material.SPECTRAL_ARROW, ChatColor.YELLOW),
            createCategoryButton("Other", Material.AMETHYST_CLUSTER, ChatColor.AQUA)
        };

        // Add category buttons
        for (int i = 0; i < categoryButtons.length; i++) {
            kitRoom.setItem(47 + i, categoryButtons[i]);
        }

        // Save button (only for players with permission)
        if (player.hasPermission("tkits.edit")) {
            ItemStack saveButton = ItemUtils.getItem(ChatColor.GREEN + "» Save Changes", Material.LODESTONE, 
                List.of(
                    ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to save changes",
                    "",
                    ChatColor.GRAY + "Save your modifications to",
                    ChatColor.GRAY + "the current category"
                ));
            kitRoom.setItem(52, saveButton);
        }

        // Refill button
        ItemStack refillButton = ItemUtils.getItem(ChatColor.YELLOW + "» Refill Items", Material.HOPPER, 
            List.of(
                ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to refill items",
                "",
                ChatColor.GRAY + "Restores all items to their",
                ChatColor.GRAY + "original quantities"
            ));
        kitRoom.setItem(53, refillButton);

        // Highlight current category
        if (kitRoom.getItem(47 + category) != null) {
            ItemStack currentCategory = kitRoom.getItem(47 + category);
            ItemMeta meta = currentCategory.getItemMeta();
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            currentCategory.setItemMeta(meta);
        }

        player.openInventory(kitRoom);
    }

    private ItemStack createCategoryButton(String name, Material material, ChatColor color) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(color + "» " + name);
        meta.setLore(List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to view category",
            "",
            ChatColor.GRAY + "Browse all available items",
            ChatColor.GRAY + "in the " + color + name + ChatColor.GRAY + " category"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        button.setItemMeta(meta);
        return button;
    }
}