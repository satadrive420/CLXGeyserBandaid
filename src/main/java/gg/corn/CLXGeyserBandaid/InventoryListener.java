package gg.corn.CLXGeyserBandaid;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {
    private final JavaPlugin plugin;
    private final ICombatLogX combatLogX;
    private final ElytraDataManager elytraDataManager;

    public InventoryListener(JavaPlugin plugin, ICombatLogX combatLogX, ElytraDataManager elytraDataManager) {
        this.plugin = plugin;
        this.combatLogX = combatLogX;
        this.elytraDataManager = elytraDataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleChestplateSlot(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> handleChestplateSlot(player), 1L);
    }

    private void handleChestplateSlot(Player player) {
        UUID uuid = player.getUniqueId();
        ICombatManager combatManager = combatLogX.getCombatManager();
        ItemStack chestplate = player.getInventory().getChestplate();
        Map<UUID, Short> originalElytraMap = elytraDataManager.getOriginalElytraMap();

        if (DamageUtil.isElytra(chestplate)) {
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta == null) return;
            if (combatManager.isInCombat(player)) {
                // Save original damage if not already saved
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    elytraDataManager.saveElytraData();
                }
                if (dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                    if (DamageUtil.disableElytra(chestplate)) {
                        player.sendMessage("§cYour Elytra is disabled while you are in combat.");
                    }
                }
            } else {
                if (originalElytraMap.containsKey(uuid)) {
                    short savedDamage = originalElytraMap.get(uuid);
                    if (DamageUtil.restoreElytra(chestplate, savedDamage)) {
                        player.sendMessage("§aYour Elytra has been restored.");
                    }
                    originalElytraMap.remove(uuid);
                    elytraDataManager.saveElytraData();
                }
            }
        } else {
            // If the player has unequipped their Elytra, attempt to restore any disabled ones in inventory.
            if (originalElytraMap.containsKey(uuid)) {
                short savedDamage = originalElytraMap.get(uuid);
                boolean restored = false;
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (DamageUtil.isElytra(item)) {
                        Damageable dmgMeta = DamageUtil.getDamageableMeta(item);
                        if (dmgMeta != null && dmgMeta.getDamage() == DamageUtil.DISABLED_DAMAGE) {
                            if (DamageUtil.restoreElytra(item, savedDamage)) {
                                restored = true;
                                break;
                            }
                        }
                    }
                }
                if (restored) {
                    player.sendMessage("§aYour Elytra has been restored after being unequipped.");
                }
                originalElytraMap.remove(uuid);
                elytraDataManager.saveElytraData();
            }
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