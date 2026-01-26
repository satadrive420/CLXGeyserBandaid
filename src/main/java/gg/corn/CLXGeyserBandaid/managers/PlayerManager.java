package gg.corn.CLXGeyserBandaid.managers;

import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

public class PlayerManager {

    private static JavaPlugin plugin;
    public static ICombatManager combatManager;

    public PlayerManager(JavaPlugin plugin) {
        PlayerManager.plugin = plugin;
    }

    public static boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }

    /**
     * Determines if the plugin's fixes should be applied to the given player.
     * Checks the config setting to decide whether to target only Bedrock players or all players.
     *
     * @param player The player to check
     * @return true if fixes should be applied, false otherwise
     */
    public static boolean shouldApplyFixes(Player player) {
        if (plugin == null) {
            return false;
        }

        String targetPlayers = plugin.getConfig().getString("target-players", "bedrock-only");

        if ("all".equalsIgnoreCase(targetPlayers)) {
            // Apply fixes to all players
            return true;
        } else {
            // Default: bedrock-only - only apply to Bedrock players
            return isBedrockPlayer(player);
        }
    }

    public static void setCombatManager(ICombatManager cm) {
        combatManager = cm;
    }

    public static boolean isInCombat(Player player) {
        return combatManager != null && combatManager.isInCombat(player);
    }

}
