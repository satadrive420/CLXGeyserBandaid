package gg.corn.CLXGeyserBandaid;

import gg.corn.CLXGeyserBandaid.util.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ElytraManager {

    public ElytraManager(JavaPlugin plugin) {
        // No external data manager needed since we use persistent data on each item.
    }

    public void disableElytras(Player player, int mode) {
        if (mode == 2) {
            disableAllElytras(player);
        } else {
            disableChestplate(player);
        }
    }

    public void restoreElytras(Player player, int mode) {
        if (mode == 2) {
            restoreAllElytras(player);
        } else {
            restoreChestplate(player);
        }
    }

    private void disableChestplate(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (DamageUtil.isElytra(chestplate)) {
            if (DamageUtil.disableElytra(chestplate)) {
                player.sendMessage("§cYour Elytra has been disabled while you are in combat.");
            }
        }
    }

    private void disableAllElytras(Player player) {
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

    private void restoreChestplate(Player player) {
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

    private void restoreAllElytras(Player player) {
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
