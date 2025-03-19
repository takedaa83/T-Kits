package com.takeda.database;

import com.takeda.Main;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLStorage implements DataStorage {
    private final Main plugin;
    private final DatabaseManager databaseManager;

    public MySQLStorage(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public void saveKit(UUID uuid, int kitNumber, ItemStack[] contents) {
        if (!databaseManager.isEnabled()) return;

        try {
            String serializedContents = itemStackArrayToBase64(contents);
            String query = """
                INSERT INTO player_kits (uuid, kit_number, kit_data, last_updated)
                VALUES (?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE kit_data = ?, last_updated = NOW()
                """;

            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, kitNumber);
                stmt.setString(3, serializedContents);
                stmt.setString(4, serializedContents);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to save kit " + kitNumber + " for player " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack[] loadKit(UUID uuid, int kitNumber) {
        if (!databaseManager.isEnabled()) return null;

        try {
            String query = "SELECT kit_data FROM player_kits WHERE uuid = ? AND kit_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, kitNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return itemStackArrayFromBase64(rs.getString("kit_data"));
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to load kit " + kitNumber + " for player " + uuid);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveEnderChest(UUID uuid, int echestNumber, ItemStack[] contents) {
        if (!databaseManager.isEnabled()) return;

        try {
            String serializedContents = itemStackArrayToBase64(contents);
            String query = """
                INSERT INTO player_enderchests (uuid, echest_number, echest_data, last_updated)
                VALUES (?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE echest_data = ?, last_updated = NOW()
                """;

            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, echestNumber);
                stmt.setString(3, serializedContents);
                stmt.setString(4, serializedContents);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to save enderchest " + echestNumber + " for player " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack[] loadEnderChest(UUID uuid, int echestNumber) {
        if (!databaseManager.isEnabled()) return null;

        try {
            String query = "SELECT echest_data FROM player_enderchests WHERE uuid = ? AND echest_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, echestNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return itemStackArrayFromBase64(rs.getString("echest_data"));
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to load enderchest " + echestNumber + " for player " + uuid);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteKit(UUID uuid, int kitNumber) {
        if (!databaseManager.isEnabled()) return;

        try {
            String query = "DELETE FROM player_kits WHERE uuid = ? AND kit_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, kitNumber);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to delete kit " + kitNumber + " for player " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEnderChest(UUID uuid, int echestNumber) {
        if (!databaseManager.isEnabled()) return;

        try {
            String query = "DELETE FROM player_enderchests WHERE uuid = ? AND echest_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, echestNumber);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to delete enderchest " + echestNumber + " for player " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public Map<Integer, ItemStack[]> getAllKits(UUID uuid) {
        if (!databaseManager.isEnabled()) return new HashMap<>();

        Map<Integer, ItemStack[]> kits = new HashMap<>();
        try {
            String query = "SELECT kit_number, kit_data FROM player_kits WHERE uuid = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int kitNumber = rs.getInt("kit_number");
                        ItemStack[] contents = itemStackArrayFromBase64(rs.getString("kit_data"));
                        kits.put(kitNumber, contents);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to load all kits for player " + uuid);
            e.printStackTrace();
        }
        return kits;
    }

    @Override
    public Map<Integer, ItemStack[]> getAllEnderChests(UUID uuid) {
        if (!databaseManager.isEnabled()) return new HashMap<>();

        Map<Integer, ItemStack[]> enderChests = new HashMap<>();
        try {
            String query = "SELECT echest_number, echest_data FROM player_enderchests WHERE uuid = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int echestNumber = rs.getInt("echest_number");
                        ItemStack[] contents = itemStackArrayFromBase64(rs.getString("echest_data"));
                        enderChests.put(echestNumber, contents);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to load all enderchests for player " + uuid);
            e.printStackTrace();
        }
        return enderChests;
    }

    @Override
    public boolean kitExists(UUID uuid, int kitNumber) {
        if (!databaseManager.isEnabled()) return false;

        try {
            String query = "SELECT COUNT(*) FROM player_kits WHERE uuid = ? AND kit_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, kitNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to check kit existence for player " + uuid);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean enderChestExists(UUID uuid, int echestNumber) {
        if (!databaseManager.isEnabled()) return false;

        try {
            String query = "SELECT COUNT(*) FROM player_enderchests WHERE uuid = ? AND echest_number = ?";
            try (Connection conn = DatabaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, echestNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§c[T-Kits] §fFailed to check enderchest existence for player " + uuid);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveAll() {
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;
        }
    }
}