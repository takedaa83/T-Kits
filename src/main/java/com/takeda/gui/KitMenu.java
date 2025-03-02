package com.takeda.gui;

import java.util.ArrayList;
import java.util.List;
import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitMenu {
    private static final FileConfiguration config = Main.instance.getConfig();

    public KitMenu(Player player) {
        Inventory kitMenu = Bukkit.createInventory(player, 36, ChatColor.DARK_PURPLE + "✦ " + player.getName() + "'s Kits");
        List<String> lore = new ArrayList<>();

        // Kit slots
        for (int i = 1; i <= 7; i++) {
            lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Left-Click to load kit");
            lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Right-Click to edit");
            lore.add("");
            lore.add(ChatColor.GRAY + "Commands:");
            lore.add(ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "/k" + i);
            lore.add(ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "/kit" + i);

            ItemStack itemStack = ItemUtils.getItem(ChatColor.GOLD + "» " + ChatColor.YELLOW + "Kit " + i, Material.CHEST, lore);
            kitMenu.setItem(i + 9, itemStack);
            lore.clear();
        }

        // Ender chest slots
        for (int i = 1; i <= 7; i++) {
            if (config.getBoolean("disable-echest")) {
                lore.add(ChatColor.RED + "✕ " + ChatColor.GRAY + "Disabled");
            } else {
                lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to edit");
                lore.add("");
                lore.add(ChatColor.GRAY + "Commands:");
                lore.add(ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "/k" + i);
                lore.add(ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "/kit" + i);
            }

            ItemStack itemStack = ItemUtils.getItem(ChatColor.DARK_PURPLE + "» " + ChatColor.LIGHT_PURPLE + "Ender Kit " + i, Material.ENDER_CHEST, lore);
            kitMenu.setItem(i + 18, itemStack);
            lore.clear();
        }

        // Kit room button
        List<String> kitRoomLore = new ArrayList<>();
        kitRoomLore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to browse items");
        kitRoomLore.add("");
        kitRoomLore.add(ChatColor.GRAY + "Access all available items");
        kitRoomLore.add(ChatColor.GRAY + "organized by categories");
        ItemStack kitRoom = ItemUtils.getItem(ChatColor.GOLD + "» " + ChatColor.YELLOW + "Kit Room", Material.NETHER_STAR, kitRoomLore);
        kitMenu.setItem(30, kitRoom);

        // Info button
        if (config.getBoolean("info-enabled")) {
            List<String> infoLore = new ArrayList<>();
            infoLore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click for information");
            infoLore.add("");
            infoLore.add(ChatColor.GRAY + "View helpful information");
            infoLore.add(ChatColor.GRAY + "about using kits");
            ItemStack info = ItemUtils.getItem(ChatColor.AQUA + "» " + ChatColor.WHITE + "Information", Material.OAK_SIGN, infoLore);
            kitMenu.setItem(31, info);
        }

        // Premade kit button
        lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Left-Click to load kit");
        lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Right-Click to view");
        lore.add("");
        lore.add(ChatColor.GRAY + "Pre-configured kit with");
        lore.add(ChatColor.GRAY + "essential items");
        ItemStack premadeKit = ItemUtils.getItem(ChatColor.GREEN + "» " + ChatColor.WHITE + "Premade Kit", Material.EMERALD, lore);
        kitMenu.setItem(32, premadeKit);

        // Fill bottom row with black glass (except for buttons)
        ItemStack filler = ItemUtils.getItem(" ", Material.BLACK_STAINED_GLASS_PANE, null);
        for (int i = 27; i < 36; i++) {
            // Skip the button slots (30, 31, 32)
            if (i != 30 && i != 31 && i != 32 && kitMenu.getItem(i) == null) {
                kitMenu.setItem(i, filler);
            }
        }

        player.openInventory(kitMenu);
        Main.kitMenuChecker.add(player.getUniqueId());
    }
}