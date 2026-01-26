package gg.corn.CLXGeyserBandaid.listeners;

import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class LevitationListener implements Listener {

    private final JavaPlugin plugin;
    private final ElytraManager elytraManager;

    public LevitationListener(JavaPlugin plugin, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.elytraManager = elytraManager;
    }

    @EventHandler
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        // Only proceed if the affected entity is a player.
        if (!(event.getEntity() instanceof Player player)) return;

        // This should only target bedrock players.
        if (!PlayerManager.isBedrockPlayer(player)) return;

        // Check if the potion effect change is for levitation.
        if (!PotionEffectType.LEVITATION.equals(event.getModifiedType())) return;

        if (event.getNewEffect() != null) {
            // Levitation effect applied or updated: disable elytras.
            elytraManager.disableElytras(player);
        } else {
            // Levitation effect removed: schedule elytra restoration after 2 ticks.
            Bukkit.getScheduler().runTaskLater(plugin, () -> elytraManager.restoreElytras(player), 2L);
        }
    }
}