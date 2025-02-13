package gg.corn.CLXGeyserBandaid;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
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

import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final ICombatLogX combatLogX;
    private final ElytraDataManager elytraDataManager;

    public CombatListener(JavaPlugin plugin, ICombatLogX combatLogX, ElytraDataManager elytraDataManager) {
        this.plugin = plugin;
        this.combatLogX = combatLogX;
        this.elytraDataManager = elytraDataManager;
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!event.isGliding()) return;
        if (!shouldTarget(player)) return;

        ICombatManager combatManager = combatLogX.getCombatManager();
        if (!combatManager.isInCombat(player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            UUID uuid = player.getUniqueId();
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta == null) {
                plugin.getLogger().warning("Could not obtain Damageable meta for " + player.getName());
                return;
            }
            // Record original damage if not already stored
            if (dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                Map<UUID, Short> originalElytraMap = elytraDataManager.getOriginalElytraMap();
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    elytraDataManager.saveElytraData();
                }
                if (DamageUtil.disableElytra(chestplate)) {
                    player.sendMessage("§cYour Elytra has been disabled while you are in combat.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        ICombatManager combatManager = combatLogX.getCombatManager();
        if (!combatManager.isInCombat(player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            UUID uuid = player.getUniqueId();
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta == null) {
                plugin.getLogger().warning("Could not obtain Damageable meta for " + player.getName());
                return;
            }
            if (dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                Map<UUID, Short> originalElytraMap = elytraDataManager.getOriginalElytraMap();
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    elytraDataManager.saveElytraData();
                }
                if (DamageUtil.disableElytra(chestplate)) {
                    player.sendMessage("§cYour Elytra boost has been disabled because you are in combat.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack chestplate = player.getInventory().getChestplate();
            UUID uuid = player.getUniqueId();
            if (DamageUtil.isElytra(chestplate) &&
                    elytraDataManager.getOriginalElytraMap().containsKey(uuid)) {
                short savedDamage = elytraDataManager.getOriginalElytraMap().get(uuid);
                if (DamageUtil.restoreElytra(chestplate, savedDamage)) {
                    player.sendMessage("§aYour Elytra has been restored.");
                }
                elytraDataManager.getOriginalElytraMap().remove(uuid);
                elytraDataManager.saveElytraData();
            }
        }, 2L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        if (elytraDataManager.getOriginalElytraMap().containsKey(uuid)) {
            elytraDataManager.getOriginalElytraMap().remove(uuid);
            elytraDataManager.saveElytraData();
            plugin.getLogger().info("Removed saved Elytra data for " + player.getName() + " due to death.");
        }
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