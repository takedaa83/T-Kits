package com.takeda.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;

import com.takeda.Main;
import com.takeda.utils.SoundUtils;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.KitMapping;

import java.util.ArrayList;
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
            sender.sendMessage(ConfigUtils.formatError("This command can only be used by players!"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("regear-enabled", true)) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Regear system is disabled"));
            SoundUtils.playErrorSound(player);
            return true;
        }

        regearInventory(player);
        return true;
    }

    private void regearInventory(Player player) {
        int kitNumber = KitMapping.getLastLoadedKit(player.getUniqueId());
        if (kitNumber == -1) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("No kit was loaded recently"));
            SoundUtils.playErrorSound(player);
            return;
        }

        ItemStack[] kitContents = KitMapping.getKitContents(player.getUniqueId(), kitNumber);
        if (kitContents == null) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Could not find kit layout"));
            SoundUtils.playErrorSound(player);
            return;
        }

        var inv = player.getInventory();
        var itemsToAdd = new ArrayList<ItemStack>();

        // First pass: handle existing items
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack currentItem = inv.getItem(i);
            ItemStack kitItem = (i < kitContents.length) ? kitContents[i] : null;

            if (currentItem != null && isRegearMaterial(currentItem.getType())) {
                if (currentItem.getType() == Material.SHIELD) {
                    repairShield(currentItem);
                } else {
                    currentItem.setAmount(currentItem.getMaxStackSize());
                }
            } else if (kitItem != null && isRegearMaterial(kitItem.getType())) {
                if (itemMatchesKitItem(currentItem, kitItem)) {
                    inv.setItem(i, kitItem.clone());
                } else {
                    itemsToAdd.add(kitItem.clone());
                }
            }
        }

        // Add remaining items to empty slots
        itemsToAdd.forEach(item -> {
            int firstEmpty = inv.firstEmpty();
            if (firstEmpty != -1) {
                inv.setItem(firstEmpty, item);
            }
        });

        SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory regeared"));
        SoundUtils.playSuccessSound(player);
    }

    private boolean isRegearMaterial(Material material) {
        return REGEAR_MATERIALS.contains(material) || material.name().endsWith("_ARROW");
    }

    private void repairShield(ItemStack shield) {
        if (shield.getItemMeta() instanceof Damageable meta) {
            meta.setDamage(0);
            shield.setItemMeta(meta);
        }
    }

    private boolean itemMatchesKitItem(ItemStack current, ItemStack kit) {
        if (current == null || kit == null) return false;
        if (current.getType() != kit.getType()) return false;

        if (current.getType() == Material.TIPPED_ARROW) {
            if (!(current.getItemMeta() instanceof PotionMeta meta1) || 
                !(kit.getItemMeta() instanceof PotionMeta meta2)) {
                return false;
            }
            return meta1.getCustomEffects().equals(meta2.getCustomEffects());
        }
        return current.isSimilar(kit);
    }
}