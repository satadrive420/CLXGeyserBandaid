package gg.corn.CLXGeyserBandaid;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.sirblobman.combatlogx.api.ICombatLogX;

public class CLXGeyserBandaid extends JavaPlugin {
    private ICombatLogX combatLogX;
    private ElytraDataManager elytraDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Ensure CombatLogX is enabled
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

        // Initialize persistence manager
        elytraDataManager = new ElytraDataManager(this);

        // Register separate listeners
        Bukkit.getPluginManager().registerEvents(new CombatListener(this, combatLogX, elytraDataManager), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this, combatLogX, elytraDataManager), this);

        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        elytraDataManager.saveElytraData();
    }
}