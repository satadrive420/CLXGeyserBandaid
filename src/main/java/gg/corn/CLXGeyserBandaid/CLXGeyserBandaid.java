package gg.corn.CLXGeyserBandaid;

import gg.corn.CLXGeyserBandaid.commands.ReloadCommand;
import gg.corn.CLXGeyserBandaid.listeners.*;
import gg.corn.CLXGeyserBandaid.managers.ElytraManager;
import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CLXGeyserBandaid extends JavaPlugin {


    @Override
    public void onEnable() {

        // Load and save default config
        saveDefaultConfig();

        DamageUtil.init(this);
        new PlayerManager(this);
        ElytraManager elytraManager = new ElytraManager();

        Plugin combatLogXPlugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (combatLogXPlugin == null || !combatLogXPlugin.isEnabled()) {
            getLogger().severe("CombatLogX is not enabled! Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ICombatLogX combatLogX;
        try {
            combatLogX = (ICombatLogX) combatLogXPlugin;
            PlayerManager.setCombatManager(combatLogX.getCombatManager());

        } catch (ClassCastException e) {
            getLogger().severe("Failed to load CombatLogX API. Disabling CLXGeyserBandaid.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new CombatListener(this, elytraManager), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this, combatLogX, elytraManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new LevitationListener(this, elytraManager), this);
        Bukkit.getPluginManager().registerEvents(new MendingListener(this), this);

        // Register reload command
        getCommand("clxgeyserbanaid").setExecutor(new ReloadCommand(this));

        getLogger().info("CLXGeyserBandaid has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CLXGeyserBandaid has been disabled successfully!");
    }
}
