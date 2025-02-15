package gg.corn.CLXGeyserBandaid;

import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

public class ElytraManager {

    public ElytraManager(JavaPlugin plugin) {
        // No external data manager needed since we use persistent data on each item.
    }

    public static void storeOriginalDamage(Player player){
        // Iterate over the player's entire inventory.
        for (ItemStack item : player.getInventory().getContents()) {
            if (DamageUtil.isElytra(item)) {
                Damageable dmgMeta = DamageUtil.getDamageableMeta(item);
                if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                    // This will store the original damage if not already stored.
                    DamageUtil.storeDamageValue(item, dmgMeta.getDamage());
                }
            }
        }
        // Also, for good measure, store the chestplate if it's an Elytra.
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            Damageable dmgMeta = DamageUtil.getDamageableMeta(chestplate);
            if (dmgMeta != null && dmgMeta.getDamage() != DamageUtil.DISABLED_DAMAGE) {
                DamageUtil.storeDamageValue(chestplate, dmgMeta.getDamage());
            }
        }
    }

    public void disableElytras(Player player, int mode) {
        storeOriginalDamage(player);
        if (mode == 2) {
            disableAllInventoryElytras(player);
        } else {
            disableChestplateSlotElytra(player);
        }
    }

    public void restoreElytras(Player player, int mode) {
        if (mode == 2) {
            restoreAllInventoryElytras(player);
        } else {
            restoreChestplateSlotElytra(player);
        }
    }

    private void disableChestplateSlotElytra(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            if (DamageUtil.disableElytra(chestplate)) {
                player.sendMessage("§cYour Elytra has been disabled while you are in combat.");
            }
        }
    }

    private void disableAllInventoryElytras(Player player) {
        boolean anyDisabled = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (DamageUtil.isElytra(item)) {
                if (DamageUtil.disableElytra(item)) {
                    anyDisabled = true;
                }
            }
        }
        if (anyDisabled) {
            player.sendMessage("§cAll your Elytras have been disabled while you are in combat.");
        }
    }

    private void restoreChestplateSlotElytra(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            if (DamageUtil.restoreElytra(chestplate)) {
                player.sendMessage("§aYour Elytra has been restored.");
            }
            DamageUtil.removeOriginalDamage(chestplate);
        } else {
            // If no Elytra in the chestplate, scan the inventory.
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (DamageUtil.isElytra(item)) {
                    if (DamageUtil.restoreElytra(item)) {
                        player.sendMessage("§aYour Elytra has been restored after being unequipped.");
                        DamageUtil.removeOriginalDamage(item);
                        break;
                    }
                }
            }
        }
    }

    private void restoreAllInventoryElytras(Player player) {
        boolean restoredAny = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (DamageUtil.isElytra(item)) {
                if (DamageUtil.restoreElytra(item)) {
                    restoredAny = true;
                }
                DamageUtil.removeOriginalDamage(item);
            }
        }
        if (restoredAny) {
            player.sendMessage("§aAll your Elytras have been restored.");
        }
    }

}
