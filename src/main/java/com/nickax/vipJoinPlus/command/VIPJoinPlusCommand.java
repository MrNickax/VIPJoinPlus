package com.nickax.vipJoinPlus.command;

import com.nickax.vipJoinPlus.VIPJoinPlus;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Command executor for the VIPJoinPlus plugin commands.
 * Handles the /vipjoinplus command and its subcommands.
 */
public class VIPJoinPlusCommand implements CommandExecutor, TabCompleter {

    private final VIPJoinPlus plugin;

    /**
     * Constructs a new VIPJoinPlusCommand instance.
     *
     * @param plugin the VIPJoinPlus plugin instance
     */
    public VIPJoinPlusCommand(VIPJoinPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command when called by a command sender.
     *
     * @param sender  the command sender who executed the command
     * @param command the command that was executed
     * @param label   the alias of the command that was used
     * @param args    the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        Audience audience = plugin.getAudience(sender);

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("vipjoinplus.reload")) {
                    plugin.reload();
                    audience.sendMessage(plugin.getMessageFormatter().deserialize(plugin.getMainConfiguration().getReloadMessage()));
                } else {
                    audience.sendMessage(plugin.getMessageFormatter().deserialize(plugin.getMainConfiguration().getNoPermissionMessage()));
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Provides tab completion suggestions for the command.
     *
     * @param sender  the command sender requesting tab completion
     * @param command the command being tab completed
     * @param label   the alias of the command that was used
     * @param args    the arguments passed to the command so far
     * @return a list of tab completion suggestions, or an empty list if none
     */
    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}