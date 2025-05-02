package gg.corn.CLXGeyserBandaid.listeners;

import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WaterListener implements Listener {

    private final JavaPlugin plugin;
    private final ElytraManager elytraManager;

    public WaterListener(JavaPlugin plugin, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.elytraManager = elytraManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only care about Bedrock‐bridge players
        if (!PlayerManager.isBedrockPlayer(player)) return;

        Block from = event.getFrom().getBlock();
        Block to   = event.getTo()  .getBlock();

        boolean wasInWater = from.getType().equals(Material.WATER)  || from.getType().equals(Material.WATER_CAULDRON);
        boolean isInWater  = to  .getType().equals(Material.WATER)  || to  .getType().equals(Material.WATER_CAULDRON);

        // Entering water
        if (!wasInWater && isInWater) {
            elytraManager.disableElytras(player);
            player.sendMessage("§cElytras are disabled for bedrock players while in water!");
        }
        // Exiting water
        else if (wasInWater && !isInWater) {
            // delay a couple ticks to let the client settle
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                elytraManager.restoreElytras(player);
                player.sendMessage("§aElytras have been restored—welcome back to air!");
            }, 2L);
        }
    }
}
