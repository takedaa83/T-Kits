package com.takeda.database;

import com.takeda.Main;
import com.takeda.files.KitsFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YAMLStorage implements DataStorage {
    private final Main plugin; // Keep this if needed for future functionality

    public YAMLStorage(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveKit(UUID uuid, int kitNumber, ItemStack[] contents) {
        String path = "kits." + uuid.toString() + ".kit" + kitNumber;
        KitsFile.get().set(path, contents);
        saveAll();
    }

    @Override
    public ItemStack[] loadKit(UUID uuid, int kitNumber) {
        String path = "kits." + uuid.toString() + ".kit" + kitNumber;
        return KitsFile.get().getList(path).toArray(new ItemStack[0]);
    }

    @Override
    public void saveEnderChest(UUID uuid, int echestNumber, ItemStack[] contents) {
        String path = "echest." + uuid.toString() + ".echest" + echestNumber;
        KitsFile.get().set(path, contents);
        saveAll();
    }

    @Override
    public ItemStack[] loadEnderChest(UUID uuid, int echestNumber) {
        String path = "echest." + uuid.toString() + ".echest" + echestNumber;
        return KitsFile.get().getList(path).toArray(new ItemStack[0]);
    }

    @Override
    public void deleteKit(UUID uuid, int kitNumber) {
        String path = "kits." + uuid.toString() + ".kit" + kitNumber;
        KitsFile.get().set(path, null);
        saveAll();
    }

    @Override
    public void deleteEnderChest(UUID uuid, int echestNumber) {
        String path = "echest." + uuid.toString() + ".echest" + echestNumber;
        KitsFile.get().set(path, null);
        saveAll();
    }

    @Override
    public Map<Integer, ItemStack[]> getAllKits(UUID uuid) {
        Map<Integer, ItemStack[]> kits = new HashMap<>();
        ConfigurationSection section = KitsFile.get().getConfigurationSection("kits." + uuid.toString());
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (key.startsWith("kit")) {
                    try {
                        int kitNumber = Integer.parseInt(key.replace("kit", ""));
                        ItemStack[] contents = section.getList(key).toArray(new ItemStack[0]);
                        kits.put(kitNumber, contents);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return kits;
    }

    @Override
    public Map<Integer, ItemStack[]> getAllEnderChests(UUID uuid) {
        Map<Integer, ItemStack[]> enderChests = new HashMap<>();
        ConfigurationSection section = KitsFile.get().getConfigurationSection("echest." + uuid.toString());
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (key.startsWith("echest")) {
                    try {
                        int echestNumber = Integer.parseInt(key.replace("echest", ""));
                        ItemStack[] contents = section.getList(key).toArray(new ItemStack[0]);
                        enderChests.put(echestNumber, contents);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return enderChests;
    }

    @Override
    public boolean kitExists(UUID uuid, int kitNumber) {
        return KitsFile.get().contains("kits." + uuid.toString() + ".kit" + kitNumber);
    }

    @Override
    public boolean enderChestExists(UUID uuid, int echestNumber) {
        return KitsFile.get().contains("echest." + uuid.toString() + ".echest" + echestNumber);
    }

    @Override
    public void saveAll() {
        KitsFile.save();
    }
}