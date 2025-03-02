package com.takeda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.takeda.commands.*;
import com.takeda.database.DatabaseManager;
import com.takeda.database.StorageFactory;
import com.takeda.database.DataStorage;
import com.takeda.events.*;
import com.takeda.files.KitRoomFile;
import com.takeda.files.KitsFile;
import com.takeda.utils.FileUtils;
import com.takeda.utils.KitMapping;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {
    // Static variables for tracking player states
    public static final List<UUID> kitMenuChecker = new ArrayList<>();
    public static final HashMap<UUID, Integer> kitEditorChecker = new HashMap<>();
    public static final HashMap<UUID, Integer> echestEditorChecker = new HashMap<>();
    public static final List<UUID> premadeKitChecker = new ArrayList<>();

    // Static maps for storing kit and ender chest data
    public static HashMap<String, HashMap<String, ItemStack[]>> echestMap = new HashMap<>();
	// Explicit initialization of kitMap
    public static final HashMap<String, HashMap<String, ItemStack[]>> kitMap = new HashMap<>();
    public static final HashMap<String, ItemStack[]> codeMap = new HashMap<>();

    // Database and storage management
    private DatabaseManager databaseManager;
    private StorageFactory storageFactory;
    private DataStorage storage;

    // Singleton instance
    public static Main instance;

    @Override
    public void onEnable() {
    instance = this;

    // Initialize kit map for system-wide premade kits
	 kitMap.putIfAbsent("system", new HashMap<>());

    // Save default config and resources
    saveDefaultConfig();
    saveResource("kitroom.yml", false);

    // Initialize database and storage
    initializeDatabase(); // Call this first!

    // Register commands and events
    registerCommands();
    registerEvents();

    // Setup files and restore data
    setupFiles();

    // Load kitroom items
    KitRoomFile.loadItems();

    // Setup auto-save if enabled
    setupAutoSave();

    // Initialize KitMapping
    KitMapping.clearAllData();

    // Log plugin enable message
    getLogger().info("§b[T-Kits] §fPlugin enabled successfully!");
    getLogger().info("§b[T-Kits] §fVersion: " + getDescription().getVersion());
    getLogger().info("§b[T-Kits] §fStorage: " + 
        (databaseManager != null && databaseManager.isEnabled() ? "MySQL" : "YAML"));
}

    @Override
    public void onDisable() {
    // Save data before disabling the plugin
    if (storage != null) {
        storage.saveAll();
    } else if (databaseManager == null || !databaseManager.isEnabled()) {
        KitsFile.get().set("kits", kitMap);
        KitsFile.save();
        KitsFile.get().set("echest", echestMap);
        KitsFile.save();
    }

    // Close database connection if it exists
    if (databaseManager != null) {
        databaseManager.closeConnection();
        getLogger().info("§b[T-Kits] §fDatabase connection closed");
    }

    // Clear KitMapping data
    KitMapping.clearAllData();

    // Log plugin disable message
    getLogger().info("§b[T-Kits] §fPlugin disabled successfully");
}

    /**
     * Initializes the database and storage system.
     */
    private void initializeDatabase() {
        if (getConfig().getBoolean("database.enabled", false)) {
            databaseManager = new DatabaseManager(this);
            if (databaseManager.isEnabled()) {
                storageFactory = new StorageFactory(this, databaseManager);
                storage = storageFactory.getStorage();
                getLogger().info("§b[T-Kits] §fMySQL connection established successfully");
            } else {
                storageFactory = new StorageFactory(this, null); // Fallback to YAML
                storage = storageFactory.createYAMLStorage();
                getLogger().warning("§c[T-Kits] §fFailed to connect to MySQL, falling back to YAML storage");
            }
        } else {
            storageFactory = new StorageFactory(this, null); // Use YAML by default
            storage = storageFactory.createYAMLStorage();
            getLogger().info("§b[T-Kits] §fUsing YAML storage");
        }
    }

    /**
     * Sets up configuration files and restores data.
     */
    private void setupFiles() {
        // Ensure kits.yml exists
        KitsFile.setup();
        if (KitsFile.get() == null) {
            getLogger().warning("§c[T-Kits] §fFailed to load kits.yml. Creating a new file...");
            KitsFile.get().options().copyDefaults(true);
            KitsFile.save();
        }
		else {
			 getLogger().info("§b[T-Kits] §fKits file is available at "+ KitsFile.get().toString());
		}
        // Save default config and resources
        saveDefaultConfig();

        // Restore data from YAML or migrate to MySQL
        if (databaseManager == null || !databaseManager.isEnabled()) {
            FileUtils.restore();
        }
    }

    /**
     * Sets up the auto-save task if enabled in the config.
     */
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

    /**
     * Saves all data to the appropriate storage system.
     */
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

    /**
     * Reloads the plugin configuration and reinitializes components.
     */
    public void reload() {
        reloadConfig();

        // Close existing database connection if it exists
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        // Reinitialize database and storage
        initializeDatabase();

        // Reload other components
        KitRoomFile.reload();
        KitsFile.reload();

        // Restore data from YAML or migrate to MySQL
        if (databaseManager == null || !databaseManager.isEnabled()) {
            FileUtils.restore();
        }

        getLogger().info("§b[T-Kits] §fPlugin reloaded successfully");
    }

    /**
     * Registers all commands.
     */
    private void registerCommands() {
        // Register kit commands
        getCommand("kit").setExecutor(new KitCommand());
        for (int i = 1; i <= 7; i++) {
            getCommand("kit" + i).setExecutor(new KitClaimer());
        }

        // Register other commands
        getCommand("regear").setExecutor(new RegearCommand(this));
        getCommand("arrange").setExecutor(new ArrangeCommand(this));
        getCommand("rearrange").setExecutor(new ArrangeCommand(this));
        getCommand("tkits").setExecutor(new ReloadCommand());
    }

    /**
     * Registers all event listeners.
     */
    private void registerEvents() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinEvent(), this);
        pm.registerEvents(new KitEditorEvent(), this);
        pm.registerEvents(new KitMenuEvent(), this);
        pm.registerEvents(new EnderChestEditorEvent(), this);
        pm.registerEvents(new KitRoomEvent(), this);
        pm.registerEvents(new PremadeKitEvent(), this);
    }

    /**
     * Returns the database manager instance.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Returns the active storage system.
     */
    public DataStorage getStorage() {
        return storage;
    }

    /**
     * Returns the singleton instance of the plugin.
     */
    public static Main getInstance() {
        return instance;
    }
}