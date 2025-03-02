package com.takeda.files;

import com.takeda.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;

public class KitsFile {
    private static File file;
    private static FileConfiguration config;

    public static void setup() {
        File dataFolder = Bukkit.getPluginManager().getPlugin("T-Kits").getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        file = new File(dataFolder, "kits.yml");

		Bukkit.getLogger().info("KitsFile: Attempting setup for kits.yml. File exists: " + file.exists());
        if (!file.exists()) {
            copyResourceToFile();
        }

        config = YamlConfiguration.loadConfiguration(file);
        if (config == null || config.getKeys(false).isEmpty()) {
            config = new YamlConfiguration();
            config.set("kits", new HashMap<>());
            config.set("echest", new HashMap<>());
            Bukkit.getLogger().warning("KitsFile config loaded was either null or empty, setting up default empty maps.");
        }

        Bukkit.getLogger().info("KitsFile setup completed. Config keys: " + config.getKeys(false));
    }


    private static void copyResourceToFile() {
        try (InputStream in = Main.getInstance().getResource("kits.yml");
             OutputStream out = new FileOutputStream(file)) {
            if (in != null){
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                Bukkit.getLogger().info("KitsFile config copied from resource.");
            } else {
                Bukkit.getLogger().severe("kits.yml not found in resources!");
                throw new FileNotFoundException("kits.yml resource not found inside the jar file.");
            }
        } catch (IOException e) {
            config = new YamlConfiguration();
            config.set("kits", new HashMap<>());
            config.set("echest", new HashMap<>());
            Bukkit.getLogger().warning("§c[T-Kits] §fCouldn't create/save kits.yml, setting up defaults: " + e.toString());
        }
    }


    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("§c[T-Kits] §fCouldn't save kits.yml");
            Bukkit.getLogger().warning(e.toString());
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}