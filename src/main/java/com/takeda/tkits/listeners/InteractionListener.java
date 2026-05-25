package com.takeda.tkits.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.ArrayList;   // <-- add this line
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.takeda.tkits.TKits;
import com.takeda.tkits.models.Kit;
import com.takeda.tkits.services.CooldownService;
import com.takeda.tkits.services.UtilityService;
import com.takeda.tkits.util.GuiUtils;
import com.takeda.tkits.util.MessageUtil;
import java.util.List; 
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter; 
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder; 
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue; 
import org.bukkit.persistence.PersistentDataType;

public class InteractionListener implements Listener {

    private final TKits plugin;
    private final MessageUtil msg;
    private final UtilityService utilityService;
    private final CooldownService cooldownService;
    private final NamespacedKey regearBoxKey;
    private final NamespacedKey regearShellKey;
    private final Map<Location, UUID> placedRegearBoxes =
        new ConcurrentHashMap<>();
    /** Reverse index for O(1) player-to-box lookups (avoids linear stream scan). */
    private final Map<UUID, Location> playerToBoxLocation =
        new ConcurrentHashMap<>();

    public InteractionListener(TKits plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageUtil();
        this.utilityService = plugin.getUtilityService();
        this.cooldownService = plugin.getCooldownService();
        this.regearBoxKey = new NamespacedKey(plugin, "regear_box");
        this.regearShellKey = new NamespacedKey(plugin, "regear_shell");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRegearBoxPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
        Block blockPlaced = event.getBlockPlaced();

        if (
            !blockPlaced.getType().name().contains("SHULKER_BOX") ||
            !itemInHand.hasItemMeta()
        ) return;

        ItemMeta meta = itemInHand.getItemMeta();
        if (
            meta != null &&
            meta
                .getPersistentDataContainer()
                .has(regearBoxKey, PersistentDataType.BYTE)
        ) {
            if (
                plugin.getCombatTagManager().isTagged(player) &&
                !player.hasPermission("tkits.admin")
            ) {
                long remaining = plugin
                    .getCombatTagManager()
                    .getRemainingTagTimeMillis(player);
                msg.sendMessage(
                    player,
                    "in_combat",
                    "time",
                    String.format("%.1f", remaining / 1000.0)
                );
                msg.playSound(player, "error");
                event.setCancelled(true);
                return;
            }

            Location loc = blockPlaced.getLocation();
            
            

            placedRegearBoxes.put(loc, player.getUniqueId());
            playerToBoxLocation.put(player.getUniqueId(), loc);
            blockPlaced.setMetadata(
                "tkits_regear_owner",
                new FixedMetadataValue(plugin, player.getUniqueId().toString())
            );

            
            msg.sendActionBar(player, "regear_shulker_given"); 
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegearBoxOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (
            clickedBlock == null ||
            !clickedBlock.getType().name().contains("SHULKER_BOX")
        ) return;

        
        String ownerUUIDStr = null;
        List<MetadataValue> metaValues = clickedBlock.getMetadata(
            "tkits_regear_owner"
        );
        if (!metaValues.isEmpty()) {
            ownerUUIDStr = metaValues.get(0).asString();
        }

        
        if (ownerUUIDStr != null) {
            event.setCancelled(true); 

            Player player = event.getPlayer();
            Location loc = clickedBlock.getLocation();

            try {
                UUID ownerUUID = UUID.fromString(ownerUUIDStr);
                if (!player.getUniqueId().equals(ownerUUID)) {
                    msg.sendMessage(player, "not_your_regear_box");
                    msg.playSound(player, "error");
                    return; 
                }
                
                

            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                msg.logWarning(
                    "Failed to parse owner UUID metadata from placed regear box at " +
                    loc
                );
                removeBox(loc); 
                return;
            }

            if (
                plugin.getCombatTagManager().isTagged(player) &&
                !player.hasPermission("tkits.admin")
            ) {
                long remaining = plugin
                    .getCombatTagManager()
                    .getRemainingTagTimeMillis(player);
                msg.sendMessage(
                    player,
                    "in_combat",
                    "time",
                    String.format("%.1f", remaining / 1000.0)
                );
                msg.playSound(player, "error");
                return;
            }

            Kit lastKit = plugin.getKitManager().getLastLoadedKit(player);
            if (lastKit == null) {
                msg.playSound(player, "error"); 
                removeBox(loc);
                return;
            }

            openRegearShellInventory(player, lastKit);
        }
        
    }

    private void openRegearShellInventory(Player player, Kit lastKit) {
        // ========== 标题：从 gui.yml 读取 ==========
        FileConfiguration guiConfig = plugin.getConfigManager().getGuiConfig();
        String titleRaw = guiConfig.getString("titles.regear_box", "&8Regear - Kit {kit_number}");
        titleRaw = titleRaw.replace("{kit_number}", String.valueOf(lastKit.getKitNumber()));
        Component title = msg.deserialize(titleRaw, player);

        Inventory gui = Bukkit.createInventory(
            new RegearInventoryHolder(lastKit),
            InventoryType.SHULKER_BOX,
            title
        );

        // --- 读取配置中的显示名称 ---
        String displayNameRaw = msg.getRawMessage("regear_now",
                "&aRᴇɢᴇᴀʀ Nᴏᴡ");
        Component displayName = msg.deserialize(displayNameRaw, player);

        // --- 读取配置中的 Lore ---
        String loreRaw = msg.getRawMessage("shell_lore",
                "&7Click to restock your inventory using Kit {kit_number}.");
        loreRaw = loreRaw.replace("{kit_number}", String.valueOf(lastKit.getKitNumber()));
        List<Component> lore = new ArrayList<>();
        if (!loreRaw.trim().isEmpty()) {
            lore.add(msg.deserialize(loreRaw, player));
        }

        ItemStack shell = GuiUtils.createGuiItem(
            Material.SHULKER_SHELL,
            displayName,
            lore,
            0
        );
        // --- 其余代码不变：设置持久数据、填充 GUI 等 ---
        ItemMeta shellMeta = shell.getItemMeta();
        if (shellMeta != null) {
            shellMeta.getPersistentDataContainer()
                .set(regearShellKey, PersistentDataType.BYTE, (byte) 1);
            shell.setItemMeta(shellMeta);
        }

        for (int slot = 9; slot <= 17; slot++) {
            gui.setItem(slot, shell.clone());
        }

        player.openInventory(gui);
    }

    /**
     * Handles clicks inside the regear shell GUI. When a player clicks a regear 
     * shell item, executes the regear process and cleans up the placed box.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRegearGuiClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RegearInventoryHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true); // Always cancel — no item manipulation in this GUI

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta clickedMeta = clicked.getItemMeta();
        if (clickedMeta == null) return;
        if (!clickedMeta.getPersistentDataContainer().has(regearShellKey, PersistentDataType.BYTE)) return;

        // Execute regear with cooldown checks
        if (!player.hasPermission("tkits.cooldown.bypass")) {
            long remaining = cooldownService.getRemainingCooldown(player.getUniqueId(), CooldownService.CooldownType.REGEAR);
            if (remaining > 0) {
                msg.sendMessage(player, "on_cooldown", "time", String.format("%.1f", remaining / 1000.0));
                msg.playSound(player, "cooldown");
                return;
            }
        }

        Kit kitToRegear = holder.getLastKit();
        boolean success = utilityService.executeRegear(player, kitToRegear);

        if (success) {
            if (!player.hasPermission("tkits.cooldown.bypass")) {
                cooldownService.applyCooldown(player.getUniqueId(), CooldownService.CooldownType.REGEAR);
            }
            msg.sendActionBar(player, "regear_success");
            msg.playSound(player, "regear_success");
        } else {
            msg.playSound(player, "error");
        }

        player.closeInventory(); // This will trigger onRegearGuiClose for box cleanup
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegearGuiClose(InventoryCloseEvent event) {
        if (
            event.getInventory().getHolder() instanceof RegearInventoryHolder &&
            event.getPlayer() instanceof Player
        ) {
            Player player = (Player) event.getPlayer();
            
            Bukkit.getScheduler()
                .runTaskLater(
                    plugin,
                    () -> {
                        Location boxLoc = findPlacedRegearBox(
                            player.getUniqueId()
                        );
                        if (boxLoc != null) {
                            removeBox(boxLoc);
                        }
                    },
                    1L
                ); 
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        
        boolean isOurBox =
            placedRegearBoxes.containsKey(loc) ||
            block.hasMetadata("tkits_regear_owner");

        if (isOurBox) {
            
            if (!event.getPlayer().hasPermission("tkits.admin")) {
                event.setCancelled(true);
                
                msg.sendRawMessage(
                    event.getPlayer(),
                    "&cCannot break active Regear Boxes.",
                    null
                );
                msg.playSound(event.getPlayer(), "error");
            } else { 
                event.setDropItems(false); 
                removeBox(loc); 
                msg.sendRawMessage(
                    event.getPlayer(),
                    "&aAdmin: Removed placed Regear Box.",
                    null
                );
            }
        }
    }

    private void removeBox(Location loc) {
        if (loc == null) return;
        UUID ownerUUID = placedRegearBoxes.remove(loc); 
        if (ownerUUID != null) {
            playerToBoxLocation.remove(ownerUUID);
        }
        Block block = loc.getBlock();

        if (block.getType().name().contains("SHULKER_BOX")) { 
            block.removeMetadata("tkits_regear_owner", plugin);
            block.removeMetadata("tkits_no_drop", plugin);
            block.setType(Material.AIR, true); 
        }
        if (ownerUUID != null) plugin
            .getLogger()
            .fine(
                "Removed regear box placed by " +
                ownerUUID +
                " at " +
                loc.toVector()
            );
    }

    /** O(1) reverse-index lookup instead of O(n) stream scan. */
    private Location findPlacedRegearBox(UUID playerUUID) {
        return playerToBoxLocation.get(playerUUID);
    }

    
    private static class RegearInventoryHolder implements InventoryHolder {

        @Getter
        private final Kit lastKit;

        public RegearInventoryHolder(Kit lastKit) {
            this.lastKit = lastKit;
        }

        @Override
        public Inventory getInventory() {
            return null;
        } 
    }
}
