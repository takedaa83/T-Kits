package com.takeda.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.takeda.Main;
import com.takeda.utils.ItemUtils;
import com.takeda.utils.KitUtils;

public class KitEditor {
    public KitEditor(Player player, int kit, boolean copyKit, String code, ItemStack[] items) {
        String kitName = "Kit " + kit;
        Inventory kitEditor = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "✦ " + kitName + " Editor");

      if (copyKit && items != null) {
          // Process the items before setting them
            ItemStack[] processedItems = KitUtils.processKitContents(items);
            // Only set the first 41 slots (inventory + equipment)
            for (int i = 0; i < 41 && i < processedItems.length; i++) {
                 kitEditor.setItem(i, processedItems[i]);
             }
           }  else {
  
               ItemStack[] kitContents = KitUtils.getKitContents(player, kit);
              if (kitContents != null) {
                   // Process the items before setting them
                  ItemStack[] processedItems = KitUtils.processKitContents(kitContents);
                   for (int i = 0; i < 41 && i < processedItems.length; i++) {
                         kitEditor.setItem(i, processedItems[i]);
                  }
                 }
           }
        // Equipment slots label
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Equipment Slots");
        lore.add("");
        lore.add(ChatColor.GRAY + "• Boots");
        lore.add(ChatColor.GRAY + "• Leggings");
       lore.add(ChatColor.GRAY + "• Chestplate");
        lore.add(ChatColor.GRAY + "• Helmet");
        lore.add(ChatColor.GRAY + "• Offhand");
       ItemStack equipLabel = ItemUtils.getItem("", Material.LIGHT_BLUE_STAINED_GLASS_PANE, lore);

          for (int i = 41; i <= 44; i++) {
             kitEditor.setItem(i, equipLabel);
          }
          // Menu buttons
         ItemStack backButton = ItemUtils.getItem(ChatColor.AQUA + "» Back to Menu", Material.SCULK_SENSOR, List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to return",
            "",
           ChatColor.GRAY + "Return to the main menu",
          ChatColor.GRAY + "Your changes will be saved"
          ));
        ItemStack loadInv = ItemUtils.getItem(ChatColor.YELLOW + "» Load Inventory", Material.BUNDLE, List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to import items",
            "",
             ChatColor.GRAY+ "Import all items from your",
           ChatColor.GRAY + "current inventory"
        ));
         ItemStack shareKit = ItemUtils.getItem(ChatColor.GOLD + "» Share Kit", Material.RECOVERY_COMPASS, List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to share kit",
            "",
             ChatColor.GRAY + "Generate a unique code to",
           ChatColor.GRAY + "share this kit with others",
              "",
            ChatColor.YELLOW + "• " + ChatColor.GRAY + "Expires in " + Main.instance.getConfig().getInt("code-expiry-minutes", 5) + " minutes",
            ChatColor.RED + (Main.instance.getConfig().getBoolean("one-time-codes", true) ? 
               "• One-time use only" : "• Multiple uses allowed")
     ));
       ItemStack copyButton = ItemUtils.getItem(ChatColor.BLUE + "» Copy Kit", Material.KNOWLEDGE_BOOK, List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to import kit",
            "",
           ChatColor.GRAY+ "Import items using a kit code",
          ChatColor.GRAY +  "shared by another player"
        ));
        ItemStack clearInv = ItemUtils.getItem(ChatColor.RED + "» Clear Items", Material.BARRIER, List.of(
           ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to clear all",
           "",
           ChatColor.GRAY + "Remove all items from",
           ChatColor.GRAY + "this kit",
            "",
            ChatColor.RED + "✕ " + ChatColor.GRAY + "This cannot be undone!"
    ));
        ItemStack repairItems = ItemUtils.getItem(ChatColor.LIGHT_PURPLE + "» Repair All", Material.AMETHYST_SHARD, List.of(
            ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to repair items",
             "",
             ChatColor.GRAY + "Restore all items to full",
           ChatColor.GRAY+ "durability"
        ));
       ItemStack filler = ItemUtils.getItem("", Material.LIGHT_BLUE_STAINED_GLASS_PANE, null);

        kitEditor.setItem(45, backButton);
        kitEditor.setItem(47, loadInv);
        kitEditor.setItem(48, shareKit);
         kitEditor.setItem(49, copyButton);
        kitEditor.setItem(50, clearInv);
        kitEditor.setItem(51, repairItems);

        // Fill empty slots
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