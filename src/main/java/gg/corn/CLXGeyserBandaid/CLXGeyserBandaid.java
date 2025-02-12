package gg.corn.CLXGeyserBandaid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class CLXGeyserBandaid extends JavaPlugin implements Listener {

    private ICombatLogX combatLogX;

    // Map to track the last time a message was sent to each player.
    private final Map<UUID, Long> lastMessageMap = new HashMap<>();
    // Cooldown period in milliseconds (e.g., 5000 ms = 5 seconds)
    private static final long MESSAGE_COOLDOWN = 5000L;

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (plugin == null || !plugin.isEnabled()) {
            getLogger().severe("CombatLogX is not enabled! Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            combatLogX = (ICombatLogX) plugin;
        } catch (ClassCastException e) {
            getLogger().severe("Failed to load CombatLogX API. Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // Proceed only if the projectile is a Firework.
        if (!(event.getEntity() instanceof Firework)) {
            return;
        }

        // Verify that the shooter is a Player.
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity().getShooter();

        // Check if the player is gliding using the vanilla method.
        boolean isGliding = player.isGliding();

        // For Bedrock players, the vanilla gliding check might not work reliably.
        // In that case, check if they are a Floodgate player and have an Elytra equipped.
        if (!isGliding && isBedrockPlayer(player)) {
            if (player.getInventory().getChestplate() != null &&
                    player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                isGliding = true;
            }
        }

        if (!isGliding) {
            return;
        }

        // Use CombatLogX to check if the player is in combat.
        ICombatManager combatManager = combatLogX.getCombatManager();
        if (combatManager.isInCombat(player)) {
            event.setCancelled(true);
            UUID playerUUID = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            long lastMessageTime = lastMessageMap.getOrDefault(playerUUID, 0L);
            if (currentTime - lastMessageTime >= MESSAGE_COOLDOWN) {
                player.sendMessage("§cYou cannot boost your flight with a firework rocket while in combat!");
                lastMessageMap.put(playerUUID, currentTime);
            }
        }
    }

    /**
     * Determines if the given player is a Floodgate (Bedrock) player.
     *
     * @param player the player to check
     * @return true if the player is a Floodgate player, false otherwise
     */
    private boolean isBedrockPlayer(Player player) {
        try {
            // Ensure Floodgate is available and check if the player is a Floodgate player.
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (NoClassDefFoundError | Exception e) {
            // Floodgate isn't available or something went wrong; assume not a Bedrock player.
            return false;
        }
    }
}

