package gg.corn.CLXGeyserBandaid;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CLXGeyserBandaid extends JavaPlugin implements Listener {

    private ICombatLogX combatLogX;

    // Map storing the original damage (from Damageable#getDamage()) for the chestplate Elytra of each player.
    private final Map<UUID, Short> originalElytraMap = new HashMap<>();

    // YAML file & configuration for persistence.
    private File elytraDataFile;
    private YamlConfiguration elytraDataConfig;

    @Override
    public void onEnable() {
        // Save default config (config.yml) if not present.
        saveDefaultConfig();

        // Check that CombatLogX is enabled.
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
        loadElytraData();
        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        saveElytraData();
    }

    /*────────────────────────────────────────────
     * Persistence Methods
     *────────────────────────────────────────────*/

    private void loadElytraData() {
        elytraDataFile = new File(getDataFolder(), "chestElytraData.yml");
        if (!elytraDataFile.exists()) {
            elytraDataFile.getParentFile().mkdirs();
            try {
                elytraDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        elytraDataConfig = YamlConfiguration.loadConfiguration(elytraDataFile);
        for (String key : elytraDataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                short savedDamage = (short) elytraDataConfig.getInt(key);
                originalElytraMap.put(uuid, savedDamage);
            } catch (Exception e) {
                // Skip invalid entries.
            }
        }
    }

    private void saveElytraData() {
        if (elytraDataConfig == null || elytraDataFile == null) return;
        for (Map.Entry<UUID, Short> entry : originalElytraMap.entrySet()) {
            elytraDataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            elytraDataConfig.save(elytraDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*────────────────────────────────────────────
     * Helper: Targeting Check
     *────────────────────────────────────────────*/

    /**
     * Returns true if the player should be processed.
     * If "target-java" is true in config, all players are processed;
     * otherwise, only Floodgate (Bedrock) players are processed.
     */
    private boolean shouldTarget(Player player) {
        boolean targetJava = getConfig().getBoolean("target-java", false);
        return targetJava || isBedrockPlayer(player);
    }

    /*────────────────────────────────────────────
     * Combat Event Handlers (Tag/Untag)
     *────────────────────────────────────────────*/

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Act only when the player starts gliding.
        if (!event.isGliding()) return;
        if (!shouldTarget(player)) return;

        ICombatManager combatManager = combatLogX.getCombatManager();
        if (!combatManager.isInCombat(player)) return;

        // Process the chestplate slot.
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
            UUID uuid = player.getUniqueId();
            Damageable dmgMeta = getDamageableMeta(chestplate);
            if (dmgMeta == null) {
                getLogger().warning("Could not obtain Damageable meta for " + player.getName());
                return;
            }
            // Only save and break the Elytra if it's not already fully broken (damage != 432).
            if (dmgMeta.getDamage() != 432) {
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    saveElytraData();
                }
                // "Break" the Elytra by setting its damage to 432.
                dmgMeta.setDamage(432);
                chestplate.setItemMeta((ItemMeta) dmgMeta);
                player.sendMessage("§cYour Elytra has been disabled while you are in combat.");
            }
        }
    }

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        // Delay restoration by 2 ticks.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            ItemStack chestplate = player.getInventory().getChestplate();
            UUID uuid = player.getUniqueId();
            if (chestplate != null && chestplate.getType() == Material.ELYTRA && originalElytraMap.containsKey(uuid)) {
                Damageable dmgMeta = getDamageableMeta(chestplate);
                if (dmgMeta == null) return;
                short savedDamage = originalElytraMap.get(uuid);
                dmgMeta.setDamage(savedDamage);
                chestplate.setItemMeta((ItemMeta) dmgMeta);
                originalElytraMap.remove(uuid);
                saveElytraData();
                player.sendMessage("§aYour Elytra has been restored.");
            }
        }, 2L);
    }

    /*────────────────────────────────────────────
     * Inventory Event Handlers (Chestplate Slot Changes)
     *────────────────────────────────────────────*/

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(this, () -> handleChestplateSlot(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldTarget(player)) return;
        Bukkit.getScheduler().runTaskLater(this, () -> handleChestplateSlot(player), 1L);
    }

    private void handleChestplateSlot(Player player) {
        UUID uuid = player.getUniqueId();
        ICombatManager combatManager = combatLogX.getCombatManager();
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
            // An Elytra is equipped in the chestplate slot.
            Damageable dmgMeta = getDamageableMeta(chestplate);
            if (dmgMeta == null) return;
            if (combatManager.isInCombat(player)) {
                // In combat: ensure the Elytra is broken.
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    saveElytraData();
                }
                if (dmgMeta.getDamage() != 432) {
                    dmgMeta.setDamage(432);
                    chestplate.setItemMeta((ItemMeta) dmgMeta);
                    player.sendMessage("§cYour Elytra is disabled while you are in combat.");
                }
            } else {
                // Not in combat: if we have saved data, restore the Elytra.
                if (originalElytraMap.containsKey(uuid)) {
                    short savedDamage = originalElytraMap.get(uuid);
                    dmgMeta.setDamage(savedDamage);
                    chestplate.setItemMeta((ItemMeta) dmgMeta);
                    originalElytraMap.remove(uuid);
                    saveElytraData();
                    player.sendMessage("§aYour Elytra has been restored.");
                }
            }
        } else {
            // If no Elytra in the chestplate slot, check if we have saved data.
            if (originalElytraMap.containsKey(uuid)) {
                short savedDamage = originalElytraMap.get(uuid);
                boolean restored = false;
                // Scan the player's inventory for an Elytra with damage 432 and restore it.
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.getType() == Material.ELYTRA) {
                        Damageable dmgMeta = getDamageableMeta(item);
                        if (dmgMeta != null && dmgMeta.getDamage() == 432) {
                            dmgMeta.setDamage(savedDamage);
                            item.setItemMeta(dmgMeta);
                            restored = true;
                            break;
                        }
                    }
                }
                if (restored) {
                    player.sendMessage("§aYour Elytra has been restored after being unequipped.");
                }
                originalElytraMap.remove(uuid);
                saveElytraData();
            }
        }
    }
    /*────────────────────────────────────────────
     * Event Handler: PlayerElytraBoostEvent
     *────────────────────────────────────────────*/
    @EventHandler
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        if (!shouldTarget(player)) return;
        ICombatManager combatManager = combatLogX.getCombatManager();
        if (!combatManager.isInCombat(player)) return;

        // Process the chestplate slot.
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
            UUID uuid = player.getUniqueId();
            Damageable dmgMeta = getDamageableMeta(chestplate);
            if (dmgMeta == null) {
                getLogger().warning("Could not obtain Damageable meta for " + player.getName());
                return;
            }
            // Only break the Elytra if it is not already fully broken.
            if (dmgMeta.getDamage() != 432) {
                if (!originalElytraMap.containsKey(uuid)) {
                    originalElytraMap.put(uuid, (short) dmgMeta.getDamage());
                    saveElytraData();
                }
                dmgMeta.setDamage(432);
                chestplate.setItemMeta((ItemMeta) dmgMeta);
                player.sendMessage("§cYour Elytra boost has been disabled because you are in combat.");
            }
        }
    }
    /*────────────────────────────────────────────
     * Helper Method: Retrieve Damageable Meta
     *────────────────────────────────────────────*/
    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        if (originalElytraMap.containsKey(uuid)) {
            originalElytraMap.remove(uuid);
            saveElytraData();
            getLogger().info("Removed saved Elytra data for " + player.getName() + " due to death.");
        }
    }

    private Damageable getDamageableMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        if (meta instanceof Damageable) {
            return (Damageable) meta;
        }
        return null;
    }

    /*────────────────────────────────────────────
     * Helper Method: Floodgate Check
     *────────────────────────────────────────────*/

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }
}