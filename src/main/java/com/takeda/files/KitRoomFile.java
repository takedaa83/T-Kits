package com.takeda.files;

import java.io.InputStreamReader;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import com.takeda.Main;

public class KitRoomFile {
    private static final HashMap<Integer, ItemStack[]> items = new HashMap<>();
    private static YamlConfiguration config;

    public static void loadItems() {
        config = YamlConfiguration.loadConfiguration(
            new InputStreamReader(Main.getInstance().getResource("kitroom.yml")));

        items.clear(); // Clear existing items before loading

        if (config.contains("items")) {
            config.getConfigurationSection("items").getKeys(false).forEach(key -> {
                int category = Integer.parseInt(key);
                items.put(category, ((java.util.List<?>) config.getList("items." + key))
                    .toArray(new ItemStack[0]));
            });
        }
    }

    public static ItemStack[] getItems(int category) {
        return items.get(category);
    }

    public static void setItems(int category, ItemStack[] newItems) {
        items.put(category, newItems);
    }

    public static void reload() {
        loadItems();
    }
}