package com.takeda.utils;

import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitMapping {
    private static final Map<UUID, Integer> lastLoadedKits = new HashMap<>();
    private static final Map<UUID, Map<Integer, ItemStack[]>> kitContents = new HashMap<>();

    public static void setLastLoadedKit(UUID playerUUID, int kitNumber) {
        lastLoadedKits.put(playerUUID, kitNumber);
    }

    public static int getLastLoadedKit(UUID playerUUID) {
        return lastLoadedKits.getOrDefault(playerUUID, -1);
    }

    public static void storeKitContents(UUID playerUUID, int kitNumber, ItemStack[] contents) {
        kitContents.computeIfAbsent(playerUUID, k -> new HashMap<>())
                   .put(kitNumber, contents.clone());
    }

    public static ItemStack[] getKitContents(UUID playerUUID, int kitNumber) {
        return kitContents.getOrDefault(playerUUID, new HashMap<>())
                         .get(kitNumber);
    }

    public static void clearPlayerData(UUID playerUUID) {
        lastLoadedKits.remove(playerUUID);
        kitContents.remove(playerUUID);
    }

    public static void clearAllData() {
        lastLoadedKits.clear();
        kitContents.clear();
    }
}