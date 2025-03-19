package com.takeda;

import com.takeda.commands.*;
import com.takeda.database.DataStorage;
import com.takeda.database.DatabaseManager;
import com.takeda.database.StorageFactory;
import com.takeda.events.*;
import com.takeda.files.KitRoomFile;
import com.takeda.files.KitsFile;
import com.takeda.utils.FileUtils;
import com.takeda.utils.KitMapping;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static final List<UUID> kitMenuChecker = new ArrayList<>();
    public static final HashMap<UUID, Integer> kitEditorChecker = new HashMap<>();
    public static final HashMap<UUID, Integer> echestEditorChecker = new HashMap<>();
    public static final List<UUID> premadeKitChecker = new ArrayList<>();

    public static HashMap<String, HashMap<String, ItemStack[]>> echestMap = new HashMap<>();
    public static final HashMap<String, HashMap<String, ItemStack[]>> kitMap = new HashMap<>();
    public static final HashMap<String, ItemStack[]> codeMap = new HashMap<>();

    private DatabaseManager databaseManager;
    private StorageFactory storageFactory;
    private DataStorage storage;

    private Version versionInfo;

    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        versionInfo = new Version(this);

        kitMap.putIfAbsent("system", new HashMap<>());

        saveDefaultConfig();
        saveResource("kitroom.yml", false);

        initializeDatabase();

        registerCommands();
        registerEvents();

        setupFiles();

        KitRoomFile.loadItems();

        setupAutoSave();

        KitMapping.clearAllData();

        getLogger().info("§b[T-Kits] §fPlugin enabled successfully!");
        getLogger().info("§b[T-Kits] §f" + versionInfo.getFullVersionInfo());
        getLogger().info("§b[T-Kits] §fStorage: " +
                (databaseManager != null && databaseManager.isEnabled() ? "MySQL" : "YAML"));
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.saveAll();
        } else if (databaseManager == null || !databaseManager.isEnabled()) {
            KitsFile.get().set("kits", kitMap);
            KitsFile.save();
            KitsFile.get().set("echest", echestMap);
            KitsFile.save();
        }

        if (databaseManager != null) {
            databaseManager.closeConnection();
            getLogger().info("§b[T-Kits] §fDatabase connection closed");
        }

        KitMapping.clearAllData();

        getLogger().info("§b[T-Kits] §fPlugin disabled successfully!");
    }

    private void initializeDatabase() {
        if (getConfig().getBoolean("database.enabled", false)) {
            databaseManager = new DatabaseManager(this);
            if (databaseManager.isEnabled()) {
                storageFactory = new StorageFactory(this, databaseManager);
                storage = storageFactory.getStorage();
                getLogger().info("§b[T-Kits] §fMySQL connection established successfully");
            } else {
                storageFactory = new StorageFactory(this, null);
                storage = storageFactory.createYAMLStorage();
                getLogger().warning("§c[T-Kits] §fFailed to connect to MySQL, falling back to YAML storage");
            }
        } else {
            storageFactory = new StorageFactory(this, null);
            storage = storageFactory.createYAMLStorage();
            getLogger().info("§b[T-Kits] §fUsing YAML storage");
        }
    }

    private void setupFiles() {
        KitsFile.setup();
        if (KitsFile.get() == null) {
            getLogger().warning("§c[T-Kits] §fFailed to load kits.yml. Creating a new file...");
            KitsFile.get().options().copyDefaults(true);
            KitsFile.save();
        } else {
            getLogger().info("§b[T-Kits] §fKits file is available at " + KitsFile.get().toString());
        }

        saveDefaultConfig();

        if (databaseManager == null || !databaseManager.isEnabled()) {
            FileUtils.restore();
        }
    }

    private void setupAutoSave() {
        if (getConfig().getBoolean("auto-save")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveData();
                    getLogger().info("§b[T-Kits] §fAuto-saving data...");
                }
            }.runTaskTimerAsynchronously(this, 0L, 1200L * getConfig().getInt("auto-save-time"));
        }
    }

    private void saveData() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (storage != null) {
                    storage.saveAll();
                } else if (databaseManager == null || !databaseManager.isEnabled()) {
                    KitsFile.get().set("kits", kitMap);
                    KitsFile.save();
                    KitsFile.get().set("echest", echestMap);
                    KitsFile.save();
                }
            }
        }.runTaskAsynchronously(this);
    }

    public void reload() {
        reloadConfig();

        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        initializeDatabase();

        KitRoomFile.reload();
        KitsFile.reload();

        if (databaseManager == null || !databaseManager.isEnabled()) {
            FileUtils.restore();
        }

        versionInfo = new Version(this);

        getLogger().info("§b[T-Kits] §fPlugin reloaded successfully!");
        getLogger().info("§b[T-Kits] §f" + versionInfo.getFullVersionInfo());
    }

    private void registerCommands() {
        getCommand("kit").setExecutor(new KitCommand());
        for (int i = 1; i <= 7; i++) {
            getCommand("kit" + i).setExecutor(new KitClaimer());
        }

        getCommand("regear").setExecutor(new RegearCommand(this));
        getCommand("arrange").setExecutor(new ArrangeCommand(this));
        getCommand("rearrange").setExecutor(new ArrangeCommand(this));
        getCommand("tkits").setExecutor(new ReloadCommand());
    }

    private void registerEvents() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinEvent(), this);
        pm.registerEvents(new KitEditorEvent(), this);
        pm.registerEvents(new KitMenuEvent(), this);
        pm.registerEvents(new EnderChestEditorEvent(), this);
        pm.registerEvents(new KitRoomEvent(), this);
        pm.registerEvents(new PremadeKitEvent(), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DataStorage getStorage() {
        return storage;
    }

    public static Main getInstance() {
        return instance;
    }
}