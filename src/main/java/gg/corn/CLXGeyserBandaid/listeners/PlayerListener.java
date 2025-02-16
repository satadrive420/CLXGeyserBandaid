package gg.corn.CLXGeyserBandaid.listeners;

import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    // Restore elytras that are dropped on death
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (PlayerManager.isBedrockPlayer(player) && PlayerManager.isInCombat(player)) {
            ElytraManager.getInstance().restoreOnDeath(event.getDrops());
        }
    }

    // Handle instances where the server restarts 
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PlayerManager.isBedrockPlayer(player)) {
            ElytraManager.getInstance().restoreElytras(player);
        }
    }

}
