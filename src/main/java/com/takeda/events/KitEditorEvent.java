package com.takeda.events;

import com.takeda.Main;
import com.takeda.gui.CopyKit;
import com.takeda.gui.KitMenu;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.ItemUtils;
import com.takeda.utils.KitMapping;
import com.takeda.utils.KitShareUtils;
import com.takeda.utils.KitUtils;
import com.takeda.utils.SoundUtils;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class KitEditorEvent implements Listener {
    private static final String SHARE_KIT_FLAG = "shareKitInProgress";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player;
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            player = (Player) humanEntity;
        } else {
            return;
        }
        UUID unique = player.getUniqueId();
        if (!Main.kitEditorChecker.containsKey(unique))
            return;
        if (event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory)
            return;
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot >= 45)
            handleMenuActions(player, slot, event);
    }

    private void handleMenuActions(Player player, int slot, InventoryClickEvent event) {
        int kit = ((Integer) Main.kitEditorChecker.get(player.getUniqueId())).intValue();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        switch (slot) {
            case 45:
                new KitMenu(player);
                SoundUtils.playMenuClickSound(player);
                break;
            case 47:
                loadInventory(player, inventory, kit);
                break;
            case 48:
               shareKit(player, inventory, event);
               break;
            case 49:
                new CopyKit(player, kit, false);
                SoundUtils.playMenuClickSound(player);
                break;
           case 50:
                clearInventory(inventory, player);
               break;
            case 51:
                repairItems(inventory, player);
               break;
        }
    }


     private void loadInventory(Player player, Inventory inventory, int kit) {
        // Get player's current inventory contents
        ItemStack[] items = new ItemStack[41];
        for (int i = 0; i <= 35; i++) {
            if (player.getInventory().getItem(i) != null) {
                items[i] = player.getInventory().getItem(i).clone();
            }
        }
        // Handle equipment slots
        ItemStack[] equipment = player.getInventory().getArmorContents();
        for (int i = 0; i < equipment.length; i++) {
           if (equipment[i] != null) {
               items[36 + i] = equipment[i].clone();
           }
        }

         // Handle off-hand
         if (player.getInventory().getItemInOffHand() != null) {
             items[40] = player.getInventory().getItemInOffHand().clone();
        }


        // Set items in kit editor inventory
        for (int i = 0; i<= 40; i++) {
            inventory.setItem(i, items[i]);
        }
        
      // Save the kit immediately
        KitUtils.save(player, kit);

        // Store in kit mapping
         KitMapping.storeKitContents(player.getUniqueId(), kit, items.clone());

       // Confirmation message and sound
      SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Kit " + kit + " saved"));
    SoundUtils.playMenuClickSound(player);
    }


    private void shareKit(Player player, Inventory inventory, InventoryClickEvent event) {
        if (!KitUtils.isKitEmpty(player)) {
            ItemStack[] items = Arrays.copyOfRange(inventory.getContents(), 0, 41);
            String code = KitShareUtils.shareKit(player, items);
             player.sendMessage(ConfigUtils.formatSuccess("Kit shared! Code: " + code));
			event.getInventory().setItem(event.getSlot(), ItemUtils.enchant(event.getCurrentItem()));
            SoundUtils.playSuccessSound(player);
             player.closeInventory();
            player.setMetadata(SHARE_KIT_FLAG, new org.bukkit.metadata.FixedMetadataValue(Main.instance, true));
        } else {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Cannot share an empty kit"));
            SoundUtils.playErrorSound(player);
         }
     }

  private void clearInventory(Inventory inventory, Player player) {
      for (int i = 0; i <= 40; i++)
           inventory.setItem(i, null);
        SoundUtils.playMenuClickSound(player);
    }

    private void repairItems(Inventory inventory, Player player) {
        for (int i = 0; i <= 40; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
               ItemMeta itemMeta = item.getItemMeta();
               if (itemMeta instanceof Damageable) {
                    Damageable meta = (Damageable) itemMeta;
                   if (meta.hasDamage()) {
                        meta.setDamage(0);
                        item.setItemMeta((ItemMeta) meta);
                        inventory.setItem(i, item);
                    }
                }
           }
        }
       SoundUtils.playSuccessSound(player);
     }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player;
         HumanEntity humanEntity = event.getPlayer();
         if (humanEntity instanceof Player) {
           player = (Player) humanEntity;
          } else {
            return;
         }
       if(player.hasMetadata(SHARE_KIT_FLAG)){
          player.removeMetadata(SHARE_KIT_FLAG, Main.instance);
            return;
         }
        UUID unique = player.getUniqueId();
         if (Main.kitEditorChecker.containsKey(unique)) {
            int kit = ((Integer) Main.kitEditorChecker.get(unique)).intValue();
            ItemStack[] items = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 41);
             KitUtils.save(player, kit);
            KitMapping.storeKitContents(player.getUniqueId(), kit, items);
             SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Kit " + kit + " saved"));
            SoundUtils.playKitSaveSound(player);
             Main.kitEditorChecker.remove(unique);
        }
    }
}