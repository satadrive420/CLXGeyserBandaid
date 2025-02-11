package gg.corn.CLXGeyserBandaid;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;

public final class CLXGeyserBandaid extends JavaPlugin implements Listener {

    private ICombatLogX combatLogX;

    @Override
    public void onEnable() {
        // Check if CombatLogX is enabled.
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (plugin == null || !plugin.isEnabled()) {
            getLogger().severe("CombatLogX is not enabled! Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Retrieve the CombatLogX API instance.
        try {
            combatLogX = (ICombatLogX) plugin;
        } catch (ClassCastException e) {
            getLogger().severe("Failed to load CombatLogX API. Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register event listeners.
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    /**
     * This event handler listens for any projectile launch.
     * If the projectile is a firework launched by a gliding player who is in combat,
     * the launch is cancelled.
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // Proceed only if the projectile is a firework.
        if (!(event.getEntity() instanceof Firework)) {
            return;
        }

        // Verify that the shooter is a player.
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity().getShooter();

        // Only block the firework if the player is gliding (using Elytra for flight).
        if (!player.isGliding()) {
            return;
        }

        // Check if the player is in combat using CombatLogX.
        ICombatManager combatManager = combatLogX.getCombatManager();
        if (combatManager.isInCombat(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot boost your flight with a firework rocket while in combat!");
        }
    }
}

