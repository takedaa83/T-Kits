package com.takeda.commands;

import com.takeda.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private static final String PERMISSION = "tkits.reload";
    private static final String NO_PERMISSION_MESSAGE = ChatColor.RED + "You don't have permission to use this command!";
    private static final String SUCCESS_MESSAGE = ChatColor.GREEN + "T-Kits configurations have been reloaded!";
    private static final String ERROR_MESSAGE = ChatColor.RED + "An error occurred while reloading configurations!";
    private static final String STORAGE_MESSAGE = ChatColor.GRAY + "Storage type: %s";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        try {
            Main.getInstance().reload();
            sender.sendMessage(SUCCESS_MESSAGE);
            
            // Show storage type
            String storageType = Main.getInstance().getDatabaseManager() != null && 
                               Main.getInstance().getDatabaseManager().isEnabled() ? "MySQL" : "YAML";
            sender.sendMessage(String.format(STORAGE_MESSAGE, storageType));
        } catch (Exception e) {
            sender.sendMessage(ERROR_MESSAGE);
            Main.getInstance().getLogger().severe("Error during reload: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}