package gg.corn.CLXGeyserBandaid;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

public class InventoryListener implements Listener {

    private final JavaPlugin plugin;
    private final ICombatLogX combatLogX;
    private final ElytraManager elytraManager;
    // mode: 1 = chestplate only, 2 = process all Elytras in inventory.
    private final int mode;

    public InventoryListener(JavaPlugin plugin, ICombatLogX combatLogX, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.combatLogX = combatLogX;
        this.elytraManager = elytraManager;
        this.mode = plugin.getConfig().getInt("mode", 1);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
        if (!(event.getEntity() instanceof Player player)) return;
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    private void handleElytraInventory(Player player) {
        if (combatLogX.getCombatManager().isInCombat(player)) {
            elytraManager.disableElytras(player, mode);
        } else {
            elytraManager.restoreElytras(player, mode);
        }
    }

    private boolean shouldTarget(Player player) {
        boolean targetJava = plugin.getConfig().getBoolean("target-java", false);
        return targetJava || isBedrockPlayer(player);
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }
}
