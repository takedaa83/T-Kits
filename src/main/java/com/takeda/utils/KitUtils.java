package com.takeda.utils;

import com.takeda.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;
import java.util.HashMap;

public class KitUtils {
    private static final FileConfiguration config = Main.instance.getConfig();

    public static void claim(final Player player, final int kit, final boolean command) {
        new BukkitRunnable() {
            public void run() {
                String uuid = player.getUniqueId().toString();
                String kitKey = "kit" + kit;
                String kitName = "Kit " + kit;
                try {
                    ItemStack[] contents;
                    if (Main.getInstance().getDatabaseManager() != null &&
                            Main.getInstance().getDatabaseManager().isEnabled()) {
                         contents = Main.getInstance().getStorage().loadKit(player.getUniqueId(), kit);
                     } else {
                            // Ensure the player has a map entry
                         if (!Main.kitMap.containsKey(uuid)) {
                                Main.kitMap.put(uuid, new HashMap<>());
                         }
                             contents = Main.kitMap.get(uuid).get(kitKey);
                     }


                    if (contents == null || isKitEmpty(contents)) {
                        throw new Exception("Kit not found or empty");
                    }

                    // Clone the contents before setting
                    ItemStack[] clonedContents = new ItemStack[contents.length];
                    for (int i = 0; i < contents.length; i++) {
                        if (contents[i] != null) {
                            clonedContents[i] = contents[i].clone();
                        }
                    }

                    player.getInventory().setContents(clonedContents);
                    KitMapping.setLastLoadedKit(player.getUniqueId(), kit);
                    KitMapping.storeKitContents(player.getUniqueId(), kit, clonedContents.clone());

                    EnderChestUtils.claim(player, kit);
                    SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Loaded " + kitName));
                    SoundUtils.playKitLoadSound(player);

                    if (config.getBoolean("loadkit-enabled")) {
                        String message = ConfigUtils.getColoredString("loadkit-prefix") +
                                       ConfigUtils.getColoredString("loadkit-message") +
                                        player.getName();
                        Main.instance.getServer().broadcastMessage(message);
                    }
                } catch (Exception e) {
                    String message = kitName + " has not been created! ";
                    if (command) {
                        message = message + "Type /kit to get started!";
                    } else {
                        message = message + "Right click the chest to customize!";
                    }
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError(message));
                    SoundUtils.playErrorSound(player);
                }
            }
        }.runTaskAsynchronously((Plugin) Main.instance);
    }

     public static void save(final Player player, final int kit) {
         String uuid = player.getUniqueId().toString();
        String kitKey = "kit" + kit;

        // Get items from the top inventory (kit editor)
         ItemStack[] kitItems = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 41);
        
         // Check if kit is empty before processing
        boolean kitItemsEmpty = true;
        for (int i = 0; i <= 40; i++) {
            if (kitItems[i] != null) {
                kitItemsEmpty = false;
                break;
           }
        }
     
        // If kit is empty, remove it from storage
         if (kitItemsEmpty) {
             if (Main.getInstance().getDatabaseManager() != null &&
                    Main.getInstance().getDatabaseManager().isEnabled()) {
                Main.getInstance().getStorage().deleteKit(player.getUniqueId(), kit);
            } else {
                // Ensure the player has a map entry before trying to remove
                if (Main.kitMap.containsKey(uuid)) {
                     Main.kitMap.get(uuid).remove(kitKey);
                 }
          }
            return;
        }

       // Process equipment slots
        ItemStack[] equipmentItems = new ItemStack[5];
        for (int i = 36; i <= 40; i++) {
          equipmentItems[i - 36] = kitItems[i];
           kitItems[i] = null;
        }

        // Sort equipment items into correct slots
         for (int i = 0; i <= 4; i++) {
            if (equipmentItems[i] != null) {
               String item = equipmentItems[i].getType().toString();
                if (item.contains("BOOTS")) {
                    kitItems[36] = equipmentItems[i].clone();
                } else if (item.contains("LEGGINGS")) {
                  kitItems[37] = equipmentItems[i].clone();
              } else if (item.contains("CHESTPLATE") || item.contains("ELYTRA")) {
                   kitItems[38] = equipmentItems[i].clone();
                } else if (item.contains("HELMET")) {
                   kitItems[39] = equipmentItems[i].clone();
               } else {
                   kitItems[40] = equipmentItems[i].clone();
               }
           }
        }

        // Clone all items before saving
        ItemStack[] clonedItems = new ItemStack[kitItems.length];
         for (int i = 0; i < kitItems.length; i++) {
            if (kitItems[i] != null) {
                clonedItems[i] = kitItems[i].clone();
            }
        }
        
        // Save to appropriate storage
        if (Main.getInstance().getDatabaseManager() != null &&
                Main.getInstance().getDatabaseManager().isEnabled()) {
            Main.getInstance().getStorage().saveKit(player.getUniqueId(), kit, clonedItems);
        } else {
            // Ensure the player has a map entry
            if (!Main.kitMap.containsKey(uuid)) {
                Main.kitMap.put(uuid, new HashMap<>());
            }
            Main.kitMap.get(uuid).put(kitKey, clonedItems);
        }


        // Update kit mapping
       KitMapping.storeKitContents(player.getUniqueId(), kit, clonedItems.clone());
   }

  public static boolean isKitEmpty(Player player) {
        ItemStack[] kitItems = Arrays.copyOfRange(player.getOpenInventory().getTopInventory().getContents(), 0, 41);
        return isKitEmpty(kitItems);
    }

    public static ItemStack[] getKitContents(Player player, int kit) {
        if (Main.getInstance().getDatabaseManager() != null &&
                Main.getInstance().getDatabaseManager().isEnabled()) {
             return Main.getInstance().getStorage().loadKit(player.getUniqueId(), kit);
        }
        String uuid = player.getUniqueId().toString();
        String kitKey = "kit" + kit;
         try {
            // Ensure the player has a map entry
            if (!Main.kitMap.containsKey(uuid)) {
                return null;
            }


            ItemStack[] contents = Main.kitMap.get(uuid).get(kitKey);
	         if (contents == null) {
                return null;
           }

            // Clone the contents before returning
            ItemStack[] clonedContents = new ItemStack[contents.length];
             for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    clonedContents[i] = contents[i].clone();
                }
            }
           return clonedContents;
        } catch (Exception e) {
           return null;
        }
    }

    public static boolean kitExists(Player player, int kit) {
        if (Main.getInstance().getDatabaseManager() != null &&
                Main.getInstance().getDatabaseManager().isEnabled()) {
             return Main.getInstance().getStorage().kitExists(player.getUniqueId(), kit);
        }
        String uuid = player.getUniqueId().toString();
        String kitKey = "kit" + kit;
         try {
            return Main.kitMap.containsKey(uuid) &&
                   Main.kitMap.get(uuid).containsKey(kitKey) &&
                  !isKitEmpty(Main.kitMap.get(uuid).get(kitKey));
        } catch (Exception e) {
            return false;
        }
    }


     public static boolean isKitEmpty(ItemStack[] items) {
        if (items == null) return true;
         for (ItemStack item : items) {
             if (item != null)
                 return false;
         }
        return true;
    }
        
    public static ItemStack[] processKitContents(ItemStack[] kitItems) {
        if (kitItems == null) return new ItemStack[41];

        ItemStack[] processedItems = new ItemStack[41];
         ItemStack[] equipmentItems = new ItemStack[5];

         // Copy inventory items
         for (int i = 0; i <= 35 && i < kitItems.length; i++) {
           if (kitItems[i] != null) {
                 processedItems[i] = kitItems[i].clone();
             }
		}

         // Extract equipment items
       for (int i = 36; i <= 40 && i < kitItems.length; i++) {
          if (kitItems[i] != null) {
               equipmentItems[i - 36] = kitItems[i].clone();
            }
       }
		
       // Process equipment items
       for (int i = 0; i <= 4; i++) {
           if (equipmentItems[i] != null) {
              String item = equipmentItems[i].getType().toString();
             if (item.contains("BOOTS")) {
                  processedItems[36] = equipmentItems[i];
                } else if (item.contains("LEGGINGS")) {
                  processedItems[37] = equipmentItems[i];
           } else if (item.contains("CHESTPLATE") || item.contains("ELYTRA")) {
                    processedItems[38] = equipmentItems[i];
            } else if (item.contains("HELMET")) {
                  processedItems[39] = equipmentItems[i];
                } else {
                   processedItems[40] = equipmentItems[i];
               }
            }
       }
     return processedItems;
   }
}