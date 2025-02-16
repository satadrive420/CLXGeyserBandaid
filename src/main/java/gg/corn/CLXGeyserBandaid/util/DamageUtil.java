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

    public static boolean setDisabledDamage(ItemStack item) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        if (dmgMeta.getDamage() != DISABLED_DAMAGE) {
            storeOriginalDamage(item, dmgMeta.getDamage());
            dmgMeta.setDamage(DISABLED_DAMAGE);
            item.setItemMeta(dmgMeta);
            return true;
        }
        return false;
    }

    public static boolean setOriginalDamage(ItemStack item) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        int storedDamage = getOriginalDamage(item);
        if (storedDamage < 0) return false;
        dmgMeta.setDamage((short) storedDamage);
        item.setItemMeta(dmgMeta);
        return true;
    }

    public static void storeOriginalDamage(ItemStack item, int damage) {
        if (!isElytra(item) || damageValueKey == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(damageValueKey, PersistentDataType.INTEGER)) {
            container.set(damageValueKey, PersistentDataType.INTEGER, damage);
            item.setItemMeta(meta);
        }
    }

    public static int getOriginalDamage(ItemStack item) {
        if (!isElytra(item) || damageValueKey == null) return -1;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(damageValueKey, PersistentDataType.INTEGER)) {
            Integer stored = container.get(damageValueKey, PersistentDataType.INTEGER);
            return stored != null ? stored : -1;
        }
        return -1;
    }

    public static void clearOriginalDamage(ItemStack item) {
        if (!isElytra(item) || damageValueKey == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(damageValueKey);
        item.setItemMeta(meta);
    }


}