package gg.corn.CLXGeyserBandaid.listeners;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryListener implements Listener {

    private final JavaPlugin plugin;
    private final ICombatLogX combatLogX;
    private final ElytraManager elytraManager;

    public InventoryListener(JavaPlugin plugin, ICombatLogX combatLogX, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.combatLogX = combatLogX;
        this.elytraManager = elytraManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!PlayerManager.isBedrockPlayer(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!PlayerManager.isBedrockPlayer(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PlayerManager.isBedrockPlayer(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleElytraInventory(player), 1L);
    }

    private void handleElytraInventory(Player player) {
        if (combatLogX.getCombatManager().isInCombat(player)) {
            elytraManager.disableElytras(player);
        } else {
            elytraManager.restoreElytras(player);
        }
    }

}
