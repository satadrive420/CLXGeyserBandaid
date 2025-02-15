package gg.corn.CLXGeyserBandaid;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final ICombatLogX combatLogX;
    private final ElytraManager elytraManager;
    // mode: 1 = chestplate only, 2 = all Elytras in inventory.
    private final int mode;

    public CombatListener(JavaPlugin plugin, ICombatLogX combatLogX, ElytraManager elytraManager) {
        this.plugin = plugin;
        this.combatLogX = combatLogX;
        this.elytraManager = elytraManager;
        this.mode = plugin.getConfig().getInt("mode", 1);
    }

    // Turns out onPlayerTag works for what we're trying to do after all.
    // Probably don't need the events below.

        /*
    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.isGliding()) return;
        if (!shouldTarget(player)) return;
        if (!combatLogX.getCombatManager().isInCombat(player)) return;
        elytraManager.disableElytras(player, mode);
    }

    @EventHandler
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        if (!combatLogX.getCombatManager().isInCombat(player)) return;
        elytraManager.disableElytras(player, mode);
    }
        */

    @EventHandler
    public void onPlayerTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        //ElytraManager.storeOriginalDamage(player);
        elytraManager.disableElytras(player, mode);

    }

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        // Delay restoration slightly.
        Bukkit.getScheduler().runTaskLater(plugin, () -> elytraManager.restoreElytras(player, mode), 2L);
    }

    //todo: move shouldTarget and isBedrockPlayer into their own class

    private boolean shouldTarget(Player player) {
        boolean targetJava = plugin.getConfig().getBoolean("target-java", false);
        return targetJava || isBedrockPlayer(player);
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }
}
