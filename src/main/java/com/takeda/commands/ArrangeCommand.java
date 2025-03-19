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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrangeCommand implements CommandExecutor {
    private final Main plugin;
    // Special slot constants for Bukkit inventory
    private static final int OFFHAND_SLOT = 40;
    private static final int INVENTORY_SIZE = 36; // Main inventory slots (0-35)

    public ArrangeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigUtils.formatError("Only players can use this command."));
            return true;
        }

        if (!plugin.getConfig().getBoolean("arrange-enabled", true)) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Arrange system is disabled."));
            SoundUtils.playErrorSound(player);
            return true;
        }

        arrangeInventory(player);
        return true;
    }

    private void arrangeInventory(Player player) {
        int kitNumber = KitMapping.getLastLoadedKit(player.getUniqueId());
        if (kitNumber == -1) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("No kit loaded recently."));
            SoundUtils.playErrorSound(player);
            return;
        }

        ItemStack[] kitLayout = KitMapping.getKitContents(player.getUniqueId(), kitNumber);
        if (kitLayout == null || kitLayout.length == 0) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Kit layout not found."));
            SoundUtils.playErrorSound(player);
            return;
        }

        PlayerInventory playerInv = player.getInventory();

        // Create a copy of the current inventory contents
        List<ItemStack> currentItems = new ArrayList<>();

        // Only collect items from the main inventory (0-35), not armor
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack item = playerInv.getItem(i);
            if (item != null && !item.getType().equals(Material.AIR)) {
                currentItems.add(item.clone());
                playerInv.setItem(i, null);
            }
        }

        // Handle offhand separately
        ItemStack offhandItem = playerInv.getItemInOffHand();
        if (offhandItem != null && !offhandItem.getType().equals(Material.AIR)) {
            currentItems.add(offhandItem.clone());
            playerInv.setItemInOffHand(null);
        }

        if (currentItems.isEmpty()) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("No items to arrange."));
            SoundUtils.playErrorSound(player);
            return;
        }

        // Track if any arrangement was performed
        boolean arranged = false;

        // First pass: place items according to kit layout
        for (int kitSlot = 0; kitSlot < kitLayout.length; kitSlot++) {
            // Skip armor slots (36-39)
            if (kitSlot >= INVENTORY_SIZE && kitSlot < OFFHAND_SLOT) continue;

            ItemStack kitItem = kitLayout[kitSlot];
            if (kitItem == null || kitItem.getType() == Material.AIR) continue;

            if (kitSlot == OFFHAND_SLOT) {
                // Handle offhand slot
                ItemStack matchedItem = findMatchingItem(currentItems, kitItem);
                if (matchedItem != null) {
                    playerInv.setItemInOffHand(matchedItem);
                    arranged = true;
                }
            } else if (kitSlot < INVENTORY_SIZE) {
                // Handle main inventory slots
                ItemStack matchedItem = findMatchingItem(currentItems, kitItem);
                if (matchedItem != null) {
                    playerInv.setItem(kitSlot, matchedItem);
                    arranged = true;
                }
            }
        }

        // Analyze kit stacking preferences
        Map<Material, Boolean> stackingPreference = analyzeKitStackingPreferences(kitLayout);

        // Second pass: stack remaining items according to preferences
        if (!currentItems.isEmpty()) {
            stackRemainingItems(currentItems, stackingPreference);

            // Place remaining items in empty slots
            for (ItemStack item : currentItems) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    int firstEmpty = playerInv.firstEmpty();
                    if (firstEmpty != -1 && firstEmpty < INVENTORY_SIZE) {
                        playerInv.setItem(firstEmpty, item);
                        arranged = true;
                    } else {
                        // Drop items that don't fit
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
            }
        }

        if (arranged) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory arranged."));
            SoundUtils.playSuccessSound(player);
        } else {
            SoundUtils.sendActionBar(player, ConfigUtils.formatSuccess("Inventory already arranged."));
            SoundUtils.playSuccessSound(player);
        }
    }

    private ItemStack findMatchingItem(List<ItemStack> items, ItemStack target) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (itemsMatch(item, target)) {
                items.remove(i);
                return item;
            }
        }
        return null;
    }

    private Map<Material, Boolean> analyzeKitStackingPreferences(ItemStack[] kitLayout) {
        Map<Material, Boolean> stackingPreference = new HashMap<>();
        Map<Material, List<Integer>> materialSlots = new HashMap<>();

        // Identify where each material appears in the kit
        for (int i = 0; i < kitLayout.length; i++) {
            ItemStack item = kitLayout[i];
            if (item != null && isStackable(item)) {
                materialSlots.computeIfAbsent(item.getType(), k -> new ArrayList<>()).add(i);
            }
        }

        // Determine stacking preference for each material
        for (Map.Entry<Material, List<Integer>> entry : materialSlots.entrySet()) {
            Material material = entry.getKey();
            List<Integer> slots = entry.getValue();

            // Default to stacking if material appears only once
            if (slots.size() <= 1) {
                stackingPreference.put(material, true);
                continue;
            }

            // Check if any instances are stacked in the kit
            boolean anyStacked = false;
            for (Integer slot : slots) {
                ItemStack item = kitLayout[slot];
                if (item.getAmount() > 1) {
                    anyStacked = true;
                    break;
                }
            }

            // If multiple items exist but none are stacked, prefer not to stack
            stackingPreference.put(material, anyStacked);
        }

        return stackingPreference;
    }

    private boolean isStackable(ItemStack item) {
        return item != null && item.getMaxStackSize() > 1;
    }

    private void stackRemainingItems(List<ItemStack> items, Map<Material, Boolean> stackingPreference) {
        // Group items by material and similarity
        Map<Material, Map<String, List<ItemStack>>> materialGroups = new HashMap<>();

        // Sort items into groups
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item == null) continue;

            Material material = item.getType();
            if (!isStackable(item)) continue;

            // Get stacking preference (default to true if not specified)
            Boolean shouldStack = stackingPreference.getOrDefault(material, true);
            if (!shouldStack) continue;

            // Use a key that identifies similar items
            String similarityKey = getItemSimilarityKey(item);

            materialGroups
                    .computeIfAbsent(material, k -> new HashMap<>())
                    .computeIfAbsent(similarityKey, k -> new ArrayList<>())
                    .add(item);

            // Remove from original list since we're processing it
            items.set(i, null);
        }

        // Stack similar items and add back to the list
        for (Map<String, List<ItemStack>> similarityGroups : materialGroups.values()) {
            for (List<ItemStack> similarItems : similarityGroups.values()) {
                if (similarItems.size() <= 1) {
                    // Add single items back to the list
                    items.add(similarItems.get(0));
                    continue;
                }

                // Stack similar items to maximum stack size
                List<ItemStack> stackedItems = stackSimilarItems(similarItems);
                items.addAll(stackedItems);
            }
        }

        // Remove null entries
        items.removeIf(item -> item == null);
    }

    private List<ItemStack> stackSimilarItems(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();
        int totalAmount = 0;

        for (ItemStack item : items) {
            totalAmount += item.getAmount();
        }

        ItemStack prototype = items.get(0).clone();
        int maxStackSize = prototype.getMaxStackSize();

        while (totalAmount > 0) {
            ItemStack stack = prototype.clone();
            int stackAmount = Math.min(totalAmount, maxStackSize);
            stack.setAmount(stackAmount);
            result.add(stack);
            totalAmount -= stackAmount;
        }

        return result;
    }

    private String getItemSimilarityKey(ItemStack item) {
        if (item == null) return "null";

        StringBuilder key = new StringBuilder(item.getType().toString());

        // For tipped arrows, include potion data
        if (item.getType() == Material.TIPPED_ARROW && item.getItemMeta() instanceof PotionMeta meta) {
            if (meta.hasColor()) {
                key.append("_").append(meta.getColor().asRGB());
            }
            if (!meta.getCustomEffects().isEmpty()) {
                key.append("_").append(meta.getCustomEffects().hashCode());
            }
        }
        // For other items with meta, include relevant meta information
        else if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            // Skip damage values for damageable items
            if (meta instanceof Damageable) {
                ItemMeta metaCopy = meta.clone();
                ((Damageable) metaCopy).setDamage(0);
                key.append("_").append(metaCopy.hashCode());
            } else {
                key.append("_").append(meta.hashCode());
            }
        }

        return key.toString();
    }

    private boolean itemsMatch(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (item1.getType() != item2.getType()) return false;

        // Special handling for tipped arrows
        if (item1.getType() == Material.TIPPED_ARROW) {
            if (item1.getItemMeta() instanceof PotionMeta meta1 &&
                    item2.getItemMeta() instanceof PotionMeta meta2) {

                // Compare colors first
                if (meta1.hasColor() && meta2.hasColor()) {
                    return meta1.getColor().equals(meta2.getColor());
                }

                // Compare custom effects if no color
                return meta1.getCustomEffects().equals(meta2.getCustomEffects());
            }
            return false;
        }

        // Compare item meta (excluding damage for Damageable items)
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 instanceof Damageable && meta2 instanceof Damageable) {
            Damageable damageable1 = (Damageable) meta1.clone();
            Damageable damageable2 = (Damageable) meta2.clone();

            // Reset damage to compare other properties
            damageable1.setDamage(0);
            damageable2.setDamage(0);

            ItemStack copy1 = item1.clone();
            ItemStack copy2 = item2.clone();

            copy1.setItemMeta((ItemMeta) damageable1);
            copy2.setItemMeta((ItemMeta) damageable2);

            return copy1.isSimilar(copy2);
        }

        return item1.isSimilar(item2);
    }
}