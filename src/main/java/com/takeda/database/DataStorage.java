package com.takeda.database;

import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.UUID;

public interface DataStorage {
    void saveKit(UUID uuid, int kitNumber, ItemStack[] contents);
    ItemStack[] loadKit(UUID uuid, int kitNumber);
    void saveEnderChest(UUID uuid, int echestNumber, ItemStack[] contents);
    ItemStack[] loadEnderChest(UUID uuid, int echestNumber);
    void deleteKit(UUID uuid, int kitNumber);
    void deleteEnderChest(UUID uuid, int echestNumber);
    Map<Integer, ItemStack[]> getAllKits(UUID uuid);
    Map<Integer, ItemStack[]> getAllEnderChests(UUID uuid);
    boolean kitExists(UUID uuid, int kitNumber);
    boolean enderChestExists(UUID uuid, int echestNumber);
    void saveAll();
}