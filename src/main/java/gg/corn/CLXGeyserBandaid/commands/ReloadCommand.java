package gg.corn.CLXGeyserBandaid.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("clxgeyserbandaid.reload")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("CLXGeyserBandaid config reloaded successfully!", NamedTextColor.GREEN));

            String targetPlayers = plugin.getConfig().getString("target-players", "bedrock-only");
            sender.sendMessage(Component.text("Target players: ", NamedTextColor.GRAY)
                    .append(Component.text(targetPlayers, NamedTextColor.YELLOW)));

            boolean blockMending = plugin.getConfig().getBoolean("block-elytra-mending-in-combat", true);
            sender.sendMessage(Component.text("Block elytra mending in combat: ", NamedTextColor.GRAY)
                    .append(Component.text(blockMending ? "enabled" : "disabled",
                            blockMending ? NamedTextColor.GREEN : NamedTextColor.RED)));

            return true;
        } catch (Exception e) {
            sender.sendMessage(Component.text("Error reloading config: " + e.getMessage(), NamedTextColor.RED));
            return true;
        }
    }
}

