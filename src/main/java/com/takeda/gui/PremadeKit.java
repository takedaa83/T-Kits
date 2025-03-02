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

public class PremadeKit {
   public PremadeKit(Player player, boolean copyKit, String code, ItemStack[] items) {
       Inventory premadeKit = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "✦ Premade Kit");
       if (copyKit && items != null) {
                premadeKit.setContents(items);
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
        ItemStack equipLabel = ItemUtils.getItem("", Material.BLUE_STAINED_GLASS_PANE, lore);
      for (int i = 41; i <= 44; i++) {
            premadeKit.setItem(i, equipLabel);
        }
      // Menu buttons
       if (player.hasPermission("tkits.edit")) {
           ItemStack saveButton = ItemUtils.getItem(ChatColor.GREEN + "» Save Changes", Material.GREEN_TERRACOTTA, 
              List.of(
                   ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to save changes",
                    "",
                   ChatColor.GRAY + "Save your modifications to",
                  ChatColor.GRAY + "the premade kit for everyone"
             ));
            premadeKit.setItem(45, saveButton);

           ItemStack loadInv = ItemUtils.getItem(ChatColor.YELLOW + "» Load Inventory", Material.CHEST, 
                List.of(
                   ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to import items",
                   "",
                 ChatColor.GRAY+   "Import all items from your",
                  ChatColor.GRAY + "current inventory"
               ));
          premadeKit.setItem(47, loadInv);

            ItemStack copyButton = ItemUtils.getItem(ChatColor.GOLD + "» Copy Kit", Material.BOOK, 
              List.of(
                    ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to import kit",
                    "",
                    ChatColor.GRAY + "Import items using a kit code",
                    ChatColor.GRAY+"shared by another player"
              ));
          premadeKit.setItem(48, copyButton);

           ItemStack clearInv = ItemUtils.getItem(ChatColor.RED + "» Clear Items", Material.STRUCTURE_VOID, 
             List.of(
                    ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to clear all",
                     "",
                     ChatColor.GRAY + "Remove all items from",
                   ChatColor.GRAY+   "the premade kit",
                      "",
                    ChatColor.RED + "✕ " + ChatColor.GRAY + "This cannot be undone!"
               ));
            premadeKit.setItem(49, clearInv);

           ItemStack repairItems = ItemUtils.getItem(ChatColor.LIGHT_PURPLE + "» Repair All", Material.EXPERIENCE_BOTTLE, 
              List.of(
                  ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to repair items",
                   "",
                   ChatColor.GRAY + "Restore all items to full",
                   ChatColor.GRAY+ "durability"
                ));
          premadeKit.setItem(50, repairItems);
        } else {
          ItemStack backButton = ItemUtils.getItem(ChatColor.AQUA + "» Back to Menu", Material.OAK_DOOR, 
               List.of(
                   ChatColor.GRAY + "➤ " + ChatColor.WHITE + "Click to return",
                    "",
                  ChatColor.GRAY + "Return to the main menu"
                ));
            premadeKit.setItem(45, backButton);
        }


        // Fill empty slots
        ItemStack filler = ItemUtils.getItem("", Material.BLACK_STAINED_GLASS_PANE, null);
         for (int i = 45; i <= 53; i++) {
            if (premadeKit.getItem(i) == null) {
               premadeKit.setItem(i, filler);
             }
      }
        player.openInventory(premadeKit);
       Main.premadeKitChecker.add(player.getUniqueId());
    }
    public PremadeKit(Player player, boolean copyKit, String code) {
		this(player, copyKit, code,null);
	}
}