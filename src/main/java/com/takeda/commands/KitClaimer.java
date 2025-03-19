package com.takeda.commands;

import com.takeda.Main;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.KitUtils;
import com.takeda.utils.SoundUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class KitClaimer implements CommandExecutor {
    private static final FileConfiguration config = Main.instance.getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigUtils.formatError("Only players can use this command."));
            return true;
        }

        if (config.getBoolean("disable-worlds") &&
                config.getStringList("worlds").contains(player.getWorld().getName()) &&
            !player.hasPermission("tkits.bypass")) {
            SoundUtils.playErrorSound(player);
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Kits disabled in this world."));
            return true;
        }

        try {
            int kit = Integer.parseInt(command.getName().replace("kit", "").replace("k", ""));
            KitUtils.claim(player, kit, true);
        } catch (Exception e) {
            SoundUtils.sendActionBar(player, ConfigUtils.formatError("Invalid kit number."));
            SoundUtils.playErrorSound(player);
        }

        return true;
    }
}