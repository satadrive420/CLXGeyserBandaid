package gg.corn.CLXGeyserBandaid.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageUtil {

    // Constant representing the "disabled" damage value for an Elytra.
    public static final short DISABLED_DAMAGE = 432;

    /**
     * Retrieves the Damageable meta from an ItemStack.
     *
     * @param item The ItemStack to inspect.
     * @return The Damageable meta, or null if not available.
     */
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

    /**
     * Checks whether the given ItemStack is an Elytra.
     *
     * @param item The item to check.
     * @return true if the item is a non-null Elytra; false otherwise.
     */
    public static boolean isElytra(ItemStack item) {
        return item != null && item.getType() == Material.ELYTRA;
    }

    /**
     * "Disables" an Elytra by setting its damage to the disabled value.
     *
     * @param item The Elytra item.
     * @return true if the Elytra was successfully disabled; false otherwise.
     */
    public static boolean disableElytra(ItemStack item) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        if (dmgMeta.getDamage() != DISABLED_DAMAGE) {
            dmgMeta.setDamage(DISABLED_DAMAGE);
            item.setItemMeta((ItemMeta) dmgMeta);
            return true;
        }
        return false;
    }

    /**
     * Restores an Elytra's damage value to its original value, if it is disabled.
     *
     * @param item The Elytra item.
     * @param originalDamage The original damage value to restore.
     * @return true if the Elytra was restored; false otherwise.
     */
    public static boolean restoreElytra(ItemStack item, short originalDamage) {
        if (!isElytra(item)) return false;
        Damageable dmgMeta = getDamageableMeta(item);
        if (dmgMeta == null) return false;
        if (dmgMeta.getDamage() == DISABLED_DAMAGE) {
            dmgMeta.setDamage(originalDamage);
            item.setItemMeta((ItemMeta) dmgMeta);
            return true;
        }
        return false;
    }
}