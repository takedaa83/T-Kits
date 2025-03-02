package com.takeda.events;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.takeda.Main;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.SoundUtils;

public class JoinEvent implements Listener {
    private static final FileConfiguration config = Main.instance.getConfig();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Initialize maps for new players
        Main.kitMap.putIfAbsent(uuid, new HashMap<>());
        Main.echestMap.putIfAbsent(uuid, new HashMap<>());

        // Show MOTD if enabled
        if (config.getBoolean("motd-enabled")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    config.getStringList("motd").forEach(line -> 
                        SoundUtils.sendActionBar(player, ConfigUtils.formatMessage(line))
                    );
                }
            }.runTaskLater((Plugin) Main.instance, config.getInt("motd-delay") * 20L);
        }
    }
}