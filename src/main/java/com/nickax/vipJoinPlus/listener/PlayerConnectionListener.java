package com.nickax.vipJoinPlus.listener;

import com.nickax.nexus.api.text.TextFormat;
import com.nickax.nexus.bukkit.BukkitNexus;
import com.nickax.nexus.bukkit.schedule.BukkitScheduler;
import com.nickax.vipJoinPlus.VIPJoinPlus;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.hook.PlaceholderAPIHook;
import com.nickax.vipJoinPlus.message.GroupMessage;
import com.nickax.vipJoinPlus.message.GroupMessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Listener that handles player connection events (join and quit) and broadcasts
 * custom messages based on player group permissions, formatting and delivery handled
 * by Nexus.
 */
public class PlayerConnectionListener implements Listener {

    private static final long MILLIS_PER_TICK = 50L;

    private final BukkitNexus nexus;
    private final BukkitScheduler scheduler;
    private final TextFormat format;
    private final boolean isVanillaJoinMessageDisabled;
    private final boolean isVanillaQuitMessageDisabled;
    private final long joinMessageDelay;
    private final long quitMessageDelay;
    private final boolean isAsyncEnabled;
    private final GroupMessageManager groupMessageManager;
    private final PlaceholderAPIHook placeholderAPIHook;

    /**
     * Constructs a new PlayerConnectionListener.
     *
     * @param plugin the VIPJoinPlus plugin instance
     */
    public PlayerConnectionListener(VIPJoinPlus plugin) {
        this.nexus = plugin.getNexus();
        this.scheduler = nexus.scheduler();

        MainConfiguration mainConfiguration = plugin.getMainConfiguration();
        this.format = mainConfiguration.getFormat();
        this.isVanillaJoinMessageDisabled = mainConfiguration.isVanillaJoinMessageDisabled();
        this.isVanillaQuitMessageDisabled = mainConfiguration.isVanillaQuitMessageDisabled();
        this.joinMessageDelay = mainConfiguration.getJoinMessageDelay();
        this.quitMessageDelay = mainConfiguration.getQuitMessageDelay();
        this.isAsyncEnabled = mainConfiguration.isAsyncEnabled();

        this.groupMessageManager = plugin.getGroupMessageManager();
        this.placeholderAPIHook = plugin.getPlaceholderAPIHook();
    }

    /**
     * Handles player join events. Optionally disables the vanilla join message
     * and broadcasts a custom join message based on the player's group.
     *
     * @param event the player join event
     */
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (isVanillaJoinMessageDisabled) {
            event.setJoinMessage(null);
        }

        handleConnectionMessage(event.getPlayer(), joinMessageDelay, GroupMessage::joinMessage);
    }

    /**
     * Handles player quit events. Optionally disables the vanilla quit message
     * and broadcasts a custom quit message based on the player's group.
     *
     * @param event the player quit event
     */
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if (isVanillaQuitMessageDisabled) {
            event.setQuitMessage(null);
        }

        handleConnectionMessage(event.getPlayer(), quitMessageDelay, GroupMessage::quitMessage);
    }

    /**
     * Resolves the highest priority group message for the player and broadcasts it,
     * honouring the configured delay and async settings.
     *
     * @param player           the player who connected or disconnected
     * @param delay            the delay in ticks before broadcasting the message
     * @param messageExtractor function to extract the message list from a GroupMessage
     */
    private void handleConnectionMessage(Player player, long delay, Function<GroupMessage, List<String>> messageExtractor) {
        GroupMessage highestPriorityGroupMessage = groupMessageManager.getHighestPriorityGroupMessage(player);
        if (highestPriorityGroupMessage == null) {
            return;
        }

        List<String> rawMessage = messageExtractor.apply(highestPriorityGroupMessage);
        if (rawMessage == null || rawMessage.isEmpty()) {
            return;
        }

        Runnable task = () -> nexus.messages().broadcast(format, applyPlaceholders(rawMessage, player));

        if (delay <= 0) {
            if (isAsyncEnabled) {
                scheduler.async(task);
            } else {
                task.run();
            }
            return;
        }

        Duration duration = Duration.ofMillis(delay * MILLIS_PER_TICK);
        if (isAsyncEnabled) {
            scheduler.asyncLater(duration, task);
        } else {
            scheduler.entityLater(player, duration, task);
        }
    }

    /**
     * Replaces the player-name token and any PlaceholderAPI placeholders in each line.
     *
     * @param message the raw message lines
     * @param player  the player used for placeholder resolution
     * @return the lines with placeholders replaced
     */
    private List<String> applyPlaceholders(List<String> message, Player player) {
        return message.stream()
                .map(line -> {
                    line = line.replace("%player_name%", player.getName());

                    if (placeholderAPIHook != null) {
                        line = placeholderAPIHook.setPlaceholders(player, line);
                    }

                    return line;
                })
                .toList();
    }
}
