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

    public static void setCombatManager(ICombatManager cm) {
        combatManager = cm;
    }

    public static boolean isInCombat(Player player) {
        return combatManager != null && combatManager.isInCombat(player);
    }

}
