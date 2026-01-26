package gg.corn.CLXGeyserBandaid.listeners;

import gg.corn.CLXGeyserBandaid.managers.PlayerManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MendingListener implements Listener {

    private final JavaPlugin plugin;

    public MendingListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        // Check if the config option is enabled
        if (!plugin.getConfig().getBoolean("block-elytra-mending-in-combat", true)) {
            return;
        }

        Player player = event.getPlayer();

        // Check if fixes should be applied to this player
        if (!PlayerManager.shouldApplyFixes(player)) {
            return;
        }

        // Check if player is in combat
        if (!PlayerManager.isInCombat(player)) {
            return;
        }

        // Check if player has any damaged elytras with mending
        if (!hasMendingElytra(player)) {
            return;
        }

        // Temporarily remove mending from elytras, let XP be applied, then restore
        List<ItemStack> elytrasWithMending = new ArrayList<>();

        // Check all inventory slots
        for (ItemStack item : player.getInventory().getContents()) {
            if (isElytraWithMending(item)) {
                elytrasWithMending.add(item);
            }
        }

        // Check equipped chestplate slot
        ItemStack chestplate = player.getInventory().getChestplate();
        if (isElytraWithMending(chestplate)) {
            elytrasWithMending.add(chestplate);
        }

        // If we found any elytras with mending, prevent them from being repaired
        if (!elytrasWithMending.isEmpty()) {
            // Store the damage values before XP is applied
            List<Integer> originalDamages = new ArrayList<>();
            for (ItemStack elytra : elytrasWithMending) {
                Damageable meta = (Damageable) elytra.getItemMeta();
                originalDamages.add(meta != null ? meta.getDamage() : 0);
            }

            // Schedule a task to restore the damage after the event
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < elytrasWithMending.size(); i++) {
                    ItemStack elytra = elytrasWithMending.get(i);
                    if (elytra != null && elytra.getItemMeta() instanceof Damageable) {
                        Damageable meta = (Damageable) elytra.getItemMeta();
                        meta.setDamage(originalDamages.get(i));
                        elytra.setItemMeta(meta);
                    }
                }
            });
        }
    }

    private boolean hasMendingElytra(Player player) {
        // Check all inventory slots
        for (ItemStack item : player.getInventory().getContents()) {
            if (isElytraWithMending(item) && isDamaged(item)) {
                return true;
            }
        }

        // Check equipped chestplate slot
        ItemStack chestplate = player.getInventory().getChestplate();
        if (isElytraWithMending(chestplate) && isDamaged(chestplate)) {
            return true;
        }

        return false;
    }

    private boolean isElytraWithMending(ItemStack item) {
        return item != null &&
               item.getType() == Material.ELYTRA &&
               item.containsEnchantment(Enchantment.MENDING);
    }

    private boolean isDamaged(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof Damageable)) {
            return false;
        }
        Damageable meta = (Damageable) item.getItemMeta();
        return meta.getDamage() > 0;
    }
}

