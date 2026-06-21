package com.nickax.vipJoinPlus.command;

import com.nickax.nexus.api.command.Command;
import com.nickax.nexus.bukkit.BukkitNexus;
import com.nickax.nexus.bukkit.command.BukkitSender;
import com.nickax.vipJoinPlus.VIPJoinPlus;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import org.bukkit.command.CommandSender;

/**
 * Builds the {@code /vipjoinplus} command on top of the Nexus command engine.
 * Provides the {@code reload} subcommand; permission is checked inside the executor so
 * the feedback message honours the plugin's configured formatting mode.
 */
public class VIPJoinPlusCommand {

    private final VIPJoinPlus plugin;
    private final BukkitNexus nexus;

    /**
     * Constructs a new VIPJoinPlusCommand builder.
     *
     * @param plugin the VIPJoinPlus plugin instance
     */
    public VIPJoinPlusCommand(VIPJoinPlus plugin) {
        this.plugin = plugin;
        this.nexus = plugin.getNexus();
    }

    /**
     * Builds the Nexus command tree for {@code /vipjoinplus}.
     *
     * @return the built command, ready to register with {@code nexus.commands()}
     */
    public Command build() {
        Command reload = Command.named("reload")
                .executes(ctx -> handleReload(((BukkitSender) ctx.sender()).bukkit()))
                .build();

        return Command.named("vipjoinplus")
                .subcommand(reload)
                .build();
    }

    /**
     * Reloads the plugin when the sender is permitted, otherwise sends the configured
     * no-permission message. Both messages are formatted with the configured mode.
     *
     * @param sender the sender that ran the command
     */
    private void handleReload(CommandSender sender) {
        MainConfiguration configuration = plugin.getMainConfiguration();

        if (!sender.hasPermission("vipjoinplus.reload")) {
            nexus.messages().send(sender, configuration.getFormat(), configuration.getNoPermissionMessage());
            return;
        }

        plugin.reload();
        nexus.messages().send(sender, configuration.getFormat(), configuration.getReloadMessage());
    }
}
