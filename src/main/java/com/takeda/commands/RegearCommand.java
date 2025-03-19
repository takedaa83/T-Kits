package com.takeda.commands;

import com.takeda.Main;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.KitMapping;
import com.takeda.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.HashSet;
import java.util.Set;

public class RegearCommand implements CommandExecutor {
    private final Main plugin;
    private static final Set<Material> REGEAR_MATERIALS = Set.of(
            Material.EXPERIENCE_BOTTLE, Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW,
            Material.END_CRYSTAL, Material.OBSIDIAN, Material.ENDER_PEARL, Material.GLOWSTONE,
            Material.GOLDEN_APPLE, Material.SHIELD, Material.RESPAWN_ANCHOR
    );

    public RegearCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigUtils.formatError("Only players can use this command."));
            return true;
        }

        if (!plugin.getConfig().getBoolean("regear-enabled", true)) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Regear system is disabled."));
            SoundUtils.playErrorSound(player);
            return true;
        }

        regearInventory(player);
        return true;
    }

    private void regearInventory(Player player) {
        int kitNumber = KitMapping.getLastLoadedKit(player.getUniqueId());
        if (kitNumber == -1) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("No kit loaded recently."));
            SoundUtils.playErrorSound(player);
            return;
        }

        ItemStack[] kitContents = KitMapping.getKitContents(player.getUniqueId(), kitNumber);
        if (kitContents == null || kitContents.length == 0) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Kit layout not found."));
            SoundUtils.playErrorSound(player);
            return;
        }

        PlayerInventory inv = player.getInventory();
        Set<Integer> processedSlots = new HashSet<>();

        // Track if any changes were made
        boolean changesMade = false;

        // First pass: refill existing items in their current slots
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack currentItem = inv.getItem(i);
            if (currentItem == null) continue;

            if (isRegearMaterial(currentItem.getType())) {
                if (currentItem.getType() == Material.SHIELD) {
                    // Repair shield if damaged
                    if (repairShield(currentItem)) {
                        changesMade = true;
                    }
                } else if (currentItem.getAmount() < currentItem.getMaxStackSize()) {
                    // Refill stackable items
                    currentItem.setAmount(currentItem.getMaxStackSize());
                    changesMade = true;
                }

                processedSlots.add(i);
            }
        }

        // Second pass: add missing items from kit
        for (int i = 0; i < kitContents.length; i++) {
            ItemStack kitItem = kitContents[i];
            if (kitItem == null || !isRegearMaterial(kitItem.getType())) continue;

            // Skip slots that already have regear materials
            if (processedSlots.contains(i)) continue;

            ItemStack currentItem = i < inv.getSize() ? inv.getItem(i) : null;

            // If exact slot is empty or has different item, add kit item there
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                inv.setItem(i, kitItem.clone());
                changesMade = true;
            } else if (!itemMatchesKitItem(currentItem, kitItem)) {
                // Look for another spot if current slot is occupied with different item
                int emptySlot = inv.firstEmpty();
                if (emptySlot != -1) {
                    inv.setItem(emptySlot, kitItem.clone());
                    changesMade = true;
                }
            }
        }

        if (changesMade) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory regeared."));
            SoundUtils.playSuccessSound(player);
        } else {
            SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory already fully equipped."));
            SoundUtils.playSuccessSound(player);
        }
    }

    private boolean isRegearMaterial(Material material) {
        return REGEAR_MATERIALS.contains(material) || material.name().endsWith("_ARROW");
    }

    private boolean repairShield(ItemStack shield) {
        if (shield.getItemMeta() instanceof Damageable meta) {
            if (meta.getDamage() > 0) {
                meta.setDamage(0);
                shield.setItemMeta(meta);
                return true;
            }
        }
        return false;
    }

    private boolean itemMatchesKitItem(ItemStack current, ItemStack kit) {
        if (current == null || kit == null) return false;
        if (current.getType() != kit.getType()) return false;

        // Special handling for tipped arrows
        if (current.getType() == Material.TIPPED_ARROW) {
            if (current.getItemMeta() instanceof PotionMeta currentMeta &&
                    kit.getItemMeta() instanceof PotionMeta kitMeta) {

                // Compare potion effects
                if (currentMeta.hasColor() && kitMeta.hasColor()) {
                    return currentMeta.getColor().equals(kitMeta.getColor());
                }

                // Compare custom effects if no color
                return currentMeta.getCustomEffects().equals(kitMeta.getCustomEffects());
            }
            return false;
        }

        // Compare item meta (excluding damage for Damageable items)
        ItemMeta currentMeta = current.getItemMeta();
        ItemMeta kitMeta = kit.getItemMeta();

        if (currentMeta instanceof Damageable && kitMeta instanceof Damageable) {
            Damageable currentDamageable = (Damageable) currentMeta.clone();
            Damageable kitDamageable = (Damageable) kitMeta.clone();

            // Reset damage to compare other properties
            currentDamageable.setDamage(0);
            kitDamageable.setDamage(0);

            ItemStack currentCopy = current.clone();
            ItemStack kitCopy = kit.clone();

            currentCopy.setItemMeta((ItemMeta) currentDamageable);
            kitCopy.setItemMeta((ItemMeta) kitDamageable);

            return currentCopy.isSimilar(kitCopy);
        }

        return current.isSimilar(kit);
    }
}