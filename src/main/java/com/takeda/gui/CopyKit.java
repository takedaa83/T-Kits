package com.takeda.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.takeda.Main;
import com.takeda.utils.ConfigUtils;
import com.takeda.utils.ItemUtils;
import com.takeda.utils.KitShareUtils;
import com.takeda.utils.SoundUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.inventory.ItemStack;

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
                        // Debug log the items
                        player.sendMessage(ConfigUtils.formatSuccess("Found " + Arrays.stream(items)
                            .filter(Objects::nonNull)
                            .count() + " items in kit"));
                        
                        // Clone the items to prevent reference issues
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
                        player.sendMessage(ConfigUtils.formatError("Kit items were null"));
                        SoundUtils.playErrorSound(player);
                    }
                } else {
                    SoundUtils.sendActionBar(player, ConfigUtils.formatError("Invalid or expired code"));
                    SoundUtils.playErrorSound(player);
                }
                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            })
            .text(ChatColor.GOLD + "Enter Here")
            .itemLeft(ItemUtils.getItem(ChatColor.GOLD + "Enter Code", Material.PAPER, null))
            .title(ChatColor.GOLD + "Enter Kit Code")
            .plugin((Plugin) Main.instance)
            .open(player);
    }
}