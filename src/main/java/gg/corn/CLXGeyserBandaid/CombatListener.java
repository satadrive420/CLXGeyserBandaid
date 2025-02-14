package gg.corn.CLXGeyserBandaid;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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

    @EventHandler
    public void onPlayerTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        // Iterate over the player's entire inventory.
        for (ItemStack item : player.getInventory().getContents()) {
            if (DamageUtil.isElytra(item)) {
                Damageable dmgMeta = DamageUtil.getDamageableMeta(item);
                if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                    // This will store the original damage if not already stored.
                    DamageUtil.storeOriginalDamage(item, dmgMeta.getDamage());
                }
            }
        }
        // Also, for good measure, store the chestplate if it's an Elytra.
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                DamageUtil.storeOriginalDamage(chestplate, dmgMeta.getDamage());
            }
        }
    }

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

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        // Delay restoration slightly.
        Bukkit.getScheduler().runTaskLater(plugin, () -> elytraManager.restoreElytras(player, mode), 2L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // No global per-player data is used now.
    }

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
