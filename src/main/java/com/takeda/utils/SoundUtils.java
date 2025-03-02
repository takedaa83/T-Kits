package com.takeda.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.takeda.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SoundUtils {

    public static void playKitLoadSound(Player player) {
        player.playSound(player.getLocation(), 
            Sound.valueOf(Main.instance.getConfig().getString("sounds.kit-load", "ENTITY_EXPERIENCE_ORB_PICKUP")), 
            1.0f, 1.0f);
    }

    public static void playKitSaveSound(Player player) {
        player.playSound(player.getLocation(), 
            Sound.valueOf(Main.instance.getConfig().getString("sounds.kit-save", "BLOCK_NOTE_BLOCK_PLING")), 
            1.0f, 1.0f);
    }

    public static void playMenuClickSound(Player player) {
        player.playSound(player.getLocation(), 
            Sound.valueOf(Main.instance.getConfig().getString("sounds.menu-click", "UI_BUTTON_CLICK")), 
            1.0f, 1.0f);
    }

    public static void playErrorSound(Player player) {
        player.playSound(player.getLocation(), 
            Sound.valueOf(Main.instance.getConfig().getString("sounds.error", "ENTITY_VILLAGER_NO")), 
            1.0f, 1.0f);
    }

    public static void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), 
            Sound.valueOf(Main.instance.getConfig().getString("sounds.success", "ENTITY_PLAYER_LEVELUP")), 
            1.0f, 1.0f);
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}