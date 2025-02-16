package gg.corn.CLXGeyserBandaid.listeners;

import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final ElytraManager elytraManager;

    public CombatListener(JavaPlugin plugin, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.elytraManager = elytraManager;
    }

    @EventHandler
    public void onPlayerTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        if (!PlayerManager.isBedrockPlayer(player)) return;
        elytraManager.disableElytras(player);
    }

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        if (!PlayerManager.isBedrockPlayer(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> elytraManager.restoreElytras(player), 2L);
    }

}
