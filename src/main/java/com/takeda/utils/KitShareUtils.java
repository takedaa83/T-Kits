package com.takeda.utils;

import com.takeda.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class KitShareUtils {
    private static final Random random = new Random();

    public static String shareKit(Player player, ItemStack[] items) {
        final String code = generateUniqueCode();

        ItemStack[] clonedItems = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                clonedItems[i] = items[i].clone();
            }
        }

        Main.codeMap.put(code, clonedItems);

        int expiryMinutes = Main.instance.getConfig().getInt("code-expiry-minutes", 5);
        new BukkitRunnable() {
            @Override
            public void run() {
                Main.codeMap.remove(code);
            }
        }.runTaskLater(Main.instance, expiryMinutes * 1200L);

        return code;
    }

    public static boolean useCode(String code) {
        if (!Main.codeMap.containsKey(code)) {
            return false;
        }

        ItemStack[] items = Main.codeMap.get(code);
        if (items == null) {
            return false;
        }

        if (Main.instance.getConfig().getBoolean("one-time-codes", true)) {
            ItemStack[] clonedItems = items.clone();
            Main.codeMap.remove(code);
            Main.codeMap.put(code, clonedItems);
        }

        return true;
    }

    private static String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (Main.codeMap.containsKey(code));
        return code;
    }

    private static String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}