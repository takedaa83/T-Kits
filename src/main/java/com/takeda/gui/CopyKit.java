package com.takeda.gui;

import com.takeda.Main;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.ItemUtils;
import com.takeda.utils.KitShareUtils;
import com.takeda.utils.SoundUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

public class CopyKit {
    public CopyKit(Player player, int kit, boolean premade) {
        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    String code = stateSnapshot.getText().toUpperCase();
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    if (Main.codeMap.containsKey(code) && KitShareUtils.useCode(code)) {
                        ItemStack[] items = Main.codeMap.get(code);
                        if (items != null) {
                            player.sendMessage(ConfigUtils.formatSuccess("Kit code valid. Importing items..."));

                            ItemStack[] clonedItems = new ItemStack[items.length];
                            for (int i = 0; i < items.length; i++) {
                                if (items[i] != null) {
                                    clonedItems[i] = items[i].clone();
                                }
                            }

                            if (premade) {
                                new PremadeKit(player, true, code, clonedItems);
                            } else {
                                new KitEditor(player, kit, true, code, clonedItems);
                            }
                            SoundUtils.playSuccessSound(player);
                        } else {
                            player.sendMessage(ConfigUtils.formatError("Kit items were null."));
                            SoundUtils.playErrorSound(player);
                        }
                    } else {
                        SoundUtils.sendActionBar(player, ConfigUtils.formatError("Invalid or expired code."));
                        SoundUtils.playErrorSound(player);
                    }
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .text(ChatColor.WHITE + "Enter Code")
                .itemLeft(ItemUtils.getItem("&bEnter Kit Code", Material.NAME_TAG, null))
                .title(ChatColor.DARK_GRAY + "Import Kit Code")
                .plugin((Plugin) Main.instance)
                .open(player);
    }
}