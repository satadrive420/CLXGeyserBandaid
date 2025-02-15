package gg.corn.CLXGeyserBandaid;

import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ElytraManager {

    public ElytraManager() {
        // No external data manager needed since we use persistent data on each item.
    }

    public static void storeOriginalDamage(Player player){
        // Iterate over the player's entire inventory.
        for (ItemStack item : player.getInventory().getContents()) {
            if (DamageUtil.isElytra(item)) {
                Damageable dmgMeta = DamageUtil.getDamageableMeta(item);
                if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                    // This will store the original damage if not already stored.
                    DamageUtil.storeOriginalDamage(item, dmgMeta.getDamage());
                }
            }
        }
        // Also, for good measure, store the chestplate if it's an Elytra.
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                DamageUtil.storeOriginalDamage(chestplate, dmgMeta.getDamage());
            }
        }
    }



    public void disableElytras(Player player) {
        storeOriginalDamage(player);
        boolean anyDisabled = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (DamageUtil.isElytra(item)) {
                if (DamageUtil.setDisabledDamage(item)) {
                    anyDisabled = true;
                }
            }
        }
        if (anyDisabled) {
            player.sendMessage("§cAll your Elytras have been disabled while you are in combat.");
        }
    }

    public void restoreElytras(Player player) {
        boolean restoredAny = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (DamageUtil.isElytra(item)) {
                if (DamageUtil.setOriginalDamage(item)) {
                    restoredAny = true;
                }
                DamageUtil.clearOriginalDamage(item);
            }
        }
        if (restoredAny) {
            player.sendMessage("§aAll your Elytras have been restored.");
        }
    }

}
