package com.takeda.gui;

import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitMenu {
    private static final FileConfiguration config = Main.instance.getConfig();

    public KitMenu(Player player) {
        Inventory kitMenu = Bukkit.createInventory(player, 36, ChatColor.DARK_GRAY + "Kit Menu");
        List<String> lore = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            lore.add(ChatColor.GRAY + "Left-Click to Load Kit");
            lore.add(ChatColor.GRAY + "Right-Click to Edit Kit");
            ItemStack itemStack = ItemUtils.getItem("&bKit " + i, Material.CHEST, lore);
            kitMenu.setItem(i + 9, itemStack);
            lore.clear();
        }

        for (int i = 1; i <= 7; i++) {
            if (config.getBoolean("disable-echest")) {
                lore.add(ChatColor.RED + "Disabled");
            } else {
                lore.add(ChatColor.GRAY + "Click to Edit Ender Kit");
            }

            ItemStack itemStack = ItemUtils.getItem("&3Ender Kit " + i, Material.ENDER_CHEST, lore);
            kitMenu.setItem(i + 18, itemStack);
            lore.clear();
        }

        List<String> kitRoomLore = new ArrayList<>();
        kitRoomLore.add(ChatColor.GRAY + "Browse all available items.");
        kitRoomLore.add(ChatColor.GRAY + "Organized by categories.");
        ItemStack kitRoom = ItemUtils.getItem("&aKit Room", Material.NETHER_STAR, kitRoomLore);
        kitMenu.setItem(30, kitRoom);

        if (config.getBoolean("info-enabled")) {
            List<String> infoLore = new ArrayList<>();
            infoLore.add(ChatColor.GRAY + "View helpful kit information.");
            ItemStack info = ItemUtils.getItem("&eInformation", Material.OAK_SIGN, infoLore);
            kitMenu.setItem(31, info);
        }

        lore.add(ChatColor.GRAY + "Left-Click to Load Premade Kit");
        lore.add(ChatColor.GRAY + "Right-Click to View Premade Kit");
        lore.add(ChatColor.GRAY + "Pre-configured kit with essentials.");
        ItemStack premadeKit = ItemUtils.getItem("&aPremade Kit", Material.EMERALD, lore);
        kitMenu.setItem(32, premadeKit);

        ItemStack filler = ItemUtils.getItem(" ", Material.BLACK_STAINED_GLASS_PANE, null);
        for (int i = 27; i < 36; i++) {
            if (i != 30 && i != 31 && i != 32 && kitMenu.getItem(i) == null) {
                kitMenu.setItem(i, filler);
            }
        }

        player.openInventory(kitMenu);
        Main.kitMenuChecker.add(player.getUniqueId());
    }
}