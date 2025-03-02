package com.takeda.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import com.takeda.Main;
import com.takeda.utils.SoundUtils;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.KitMapping;

import java.util.HashMap;
import java.util.Map;

public class ArrangeCommand implements CommandExecutor {
    private final Main plugin;

    public ArrangeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigUtils.formatError("This command can only be used by players!"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("arrange-enabled", true)) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Arrange system is disabled"));
            SoundUtils.playErrorSound(player);
            return true;
        }

        arrangeInventory(player);
        return true;
    }

    private void arrangeInventory(Player player) {
        int kitNumber = KitMapping.getLastLoadedKit(player.getUniqueId());
        if (kitNumber == -1) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("No kit was loaded recently"));
            SoundUtils.playErrorSound(player);
            return;
        }

        ItemStack[] kitLayout = KitMapping.getKitContents(player.getUniqueId(), kitNumber);
        if (kitLayout == null) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Could not find kit layout"));
            SoundUtils.playErrorSound(player);
            return;
        }

        Map<Integer, ItemStack> currentItems = new HashMap<>();
        var playerInv = player.getInventory();

        // Store current inventory items
        for (int i = 0; i < playerInv.getSize(); i++) {
            ItemStack item = playerInv.getItem(i);
            if (item != null) {
                currentItems.put(i, item.clone());
                playerInv.clear(i);
            }
        }

        // Arrange items according to kit layout
        for (int i = 0; i < kitLayout.length; i++) {
            if (kitLayout[i] != null) {
                final int index = i;
                currentItems.entrySet().stream()
                    .filter(entry -> itemsMatch(kitLayout[index], entry.getValue()))
                    .findFirst()
                    .ifPresent(entry -> {
                        playerInv.setItem(index, entry.getValue());
                        currentItems.remove(entry.getKey());
                    });
            }
        }

        // Put remaining items in empty slots
        currentItems.values().forEach(item -> {
            int firstEmpty = playerInv.firstEmpty();
            if (firstEmpty != -1) {
                playerInv.setItem(firstEmpty, item);
            }
        });

        SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory arranged"));
        SoundUtils.playSuccessSound(player);
    }

    private boolean itemsMatch(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (item1.getType() != item2.getType()) return false;

        if (item1.getType() == Material.TIPPED_ARROW) {
            if (!(item1.getItemMeta() instanceof PotionMeta meta1) || 
                !(item2.getItemMeta() instanceof PotionMeta meta2)) {
                return false;
            }
            return meta1.getCustomEffects().equals(meta2.getCustomEffects());
        }
        return item1.isSimilar(item2);
    }
}