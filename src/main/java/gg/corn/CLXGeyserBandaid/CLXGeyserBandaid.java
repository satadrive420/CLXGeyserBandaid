package gg.corn.CLXGeyserBandaid;

import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CLXGeyserBandaid extends JavaPlugin {

    private ElytraManager elytraManager;

    @Override
    public void onEnable() {
        // Initialize DamageUtil (sets up the NamespacedKey for NBT storage)
        DamageUtil.init(this);

        saveDefaultConfig();

        // Ensure CombatLogX is enabled.
        Plugin combatLogXPlugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (combatLogXPlugin == null || !combatLogXPlugin.isEnabled()) {
            getLogger().severe("CombatLogX is not enabled! Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ICombatLogX combatLogX;
        try {
            combatLogX = (ICombatLogX) combatLogXPlugin;
        } catch (ClassCastException e) {
            getLogger().severe("Failed to load CombatLogX API. Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize the ElytraManager (no external data manager is needed).
        elytraManager = new ElytraManager(this);

        // Register listeners.
        Bukkit.getPluginManager().registerEvents(new CombatListener(this, combatLogX, elytraManager), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this, combatLogX, elytraManager), this);

        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // For each online player, scan their inventory and restore any Elytra that has stored original damage.
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (DamageUtil.isElytra(item)) {
                    if (DamageUtil.restoreElytra(item)) {
                        // If restoration is successful, remove the stored NBT data.
                        DamageUtil.removeOriginalDamage(item);
                    }
                }
            }
        }
    }
}
