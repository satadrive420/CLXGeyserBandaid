package gg.corn.CLXGeyserBandaid;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraDataManager {
    private final JavaPlugin plugin;
    private File elytraDataFile;
    private YamlConfiguration elytraDataConfig;
    private final Map<UUID, Short> originalElytraMap = new HashMap<>();

    public ElytraDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadElytraData();
    }

    public void loadElytraData() {
        elytraDataFile = new File(plugin.getDataFolder(), "chestElytraData.yml");
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

    public void saveElytraData() {
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

    public Map<UUID, Short> getOriginalElytraMap() {
        return originalElytraMap;
    }
}