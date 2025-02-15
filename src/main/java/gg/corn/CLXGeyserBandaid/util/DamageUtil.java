package gg.corn.CLXGeyserBandaid.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class DamageUtil {

    public static final short DISABLED_DAMAGE = 432;
    private static NamespacedKey damageValueKey;

    public static void init(JavaPlugin plugin) {
        damageValueKey = new NamespacedKey(plugin, "original_damage");
    }

    public static Damageable getDamageableMeta(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        if (meta instanceof Damageable) {
            return (Damageable) meta;
        }
        return null;
    }

    public static boolean isElytra(ItemStack item) {
        return item != null && item.getType() == Material.ELYTRA;
    }

    public static boolean disableElytra(ItemStack item) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        // Only store original damage if not already stored and the item isn't already disabled.
        if (dmgMeta.getDamage() != DISABLED_DAMAGE) {
            storeDamageValue(item, dmgMeta.getDamage());
            dmgMeta.setDamage(DISABLED_DAMAGE);
            item.setItemMeta((ItemMeta) dmgMeta);
            return true;
        }
        return false;
    }

    /**
     * Automatically retrieves the stored original damage from the item's PersistentDataContainer
     * and restores the Elytra to that value.
     */
    public static boolean restoreElytra(ItemStack item) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        // Retrieve the stored original damage value from the PersistentDataContainer.
        int storedDamage = getOriginalDamage(item);
        if (storedDamage < 0) return false; // No stored damage found.
        Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] Restoring Elytra with stored original damage "
                + storedDamage + " for Elytra: " + item);
        dmgMeta.setDamage((short) storedDamage);
        item.setItemMeta((ItemMeta) dmgMeta);
        return true;
    }

    public static void storeDamageValue(ItemStack item, int damage) {
        if (!isElytra(item) || damageValueKey == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(damageValueKey, PersistentDataType.INTEGER)) {
            container.set(damageValueKey, PersistentDataType.INTEGER, damage);
            item.setItemMeta(meta);
            Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] Stored original damage " + damage
                    + " for Elytra: " + item);
        } else {
            Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] Original damage already stored for Elytra: "
                    + item);
        }
    }

    public static int getOriginalDamage(ItemStack item) {
        if (!isElytra(item) || damageValueKey == null) return -1;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(damageValueKey, PersistentDataType.INTEGER)) {
            int stored = container.get(damageValueKey, PersistentDataType.INTEGER);
            Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] Retrieved stored original damage " + stored
                    + " for Elytra: " + item);
            return stored;
        }
        Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] No stored original damage found for Elytra: "
                + item);
        return -1;
    }

    public static void removeOriginalDamage(ItemStack item) {
        if (!isElytra(item) || damageValueKey == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(damageValueKey);
        item.setItemMeta(meta);
        Bukkit.getLogger().info("[CLXGeyserBandaid DEBUG] Removed stored original damage for Elytra: " + item);
    }
}