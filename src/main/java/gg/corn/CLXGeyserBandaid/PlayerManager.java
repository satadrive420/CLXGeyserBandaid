package gg.corn.CLXGeyserBandaid;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

public class PlayerManager {

    private static JavaPlugin plugin;

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

}
