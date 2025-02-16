package gg.corn.CLXGeyserBandaid.managers;

import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;

public class ElytraManager {

    private static ElytraManager instance;

    public ElytraManager() {
        instance = this;
    }

    public static ElytraManager getInstance(){
        return instance;
    }


    public static void storeOriginalDamage(Player player){
        // Store the current damage value of all elytras in the players inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (DamageUtil.isElytra(item)) {
                Damageable dmgMeta = DamageUtil.getDamageableMeta(item);
                if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                    DamageUtil.storeOriginalDamage(item, dmgMeta.getDamage());
                }
            }
        }
        // Store the current damage value of all elytras in the chestplate slot
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                DamageUtil.storeOriginalDamage(chestplate, dmgMeta.getDamage());
            }
        }
    }

    // Break all elytras in the players inventory
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

    // Restore all elytras to original damage value
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

    // Restore elytras that are dropped due to death
    public void restoreOnDeath(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (DamageUtil.isElytra(drop)) {
                DamageUtil.setOriginalDamage(drop);
                DamageUtil.clearOriginalDamage(drop);
            }
        }
    }


}
