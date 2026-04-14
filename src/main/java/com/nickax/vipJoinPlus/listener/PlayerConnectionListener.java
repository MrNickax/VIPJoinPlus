package com.nickax.vipJoinPlus.listener;

import com.nickax.vipJoinPlus.VIPJoinPlus;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.message.MessageFormatter;
import com.nickax.vipJoinPlus.message.GroupMessage;
import com.nickax.vipJoinPlus.message.GroupMessageManager;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Listener that handles player connection events (join and quit) and displays
 * custom messages based on player group permissions.
 */
public class PlayerConnectionListener implements Listener {

    private final VIPJoinPlus plugin;
    private final PlatformScheduler scheduler;
    private final boolean isVanillaJoinMessageDisabled;
    private final boolean isVanillaQuitMessageDisabled;
    private final long joinMessageDelay;
    private final long quitMessageDelay;
    private final boolean isAsyncEnabled;
    private final MessageFormatter messageFormatter;
    private final GroupMessageManager groupMessageManager;

    /**
     * Constructs a new PlayerConnectionListener.
     *
     * @param plugin the VIPJoinPlus plugin instance
     */
    public PlayerConnectionListener(VIPJoinPlus plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getFoliaLib().getScheduler();

        MainConfiguration mainConfiguration = plugin.getMainConfiguration();
        isVanillaJoinMessageDisabled = mainConfiguration.isVanillaJoinMessageDisabled();
        isVanillaQuitMessageDisabled = mainConfiguration.isVanillaQuitMessageDisabled();
        joinMessageDelay = mainConfiguration.getJoinMessageDelay();
        quitMessageDelay = mainConfiguration.getQuitMessageDelay();
        isAsyncEnabled = mainConfiguration.isAsyncEnabled();

        this.messageFormatter = plugin.getMessageFormatter();
        this.groupMessageManager = plugin.getGroupMessageManager();
    }

    /**
     * Handles player join events. Optionally disables the vanilla join message
     * and displays a custom join message based on the player's group.
     *
     * @param event the player joins the event
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
     * and displays a custom quit message based on the player's group.
     *
     * @param event the player quit the event
     */
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if (isVanillaQuitMessageDisabled) {
            event.setQuitMessage(null);
        }

        handleConnectionMessage(event.getPlayer(), quitMessageDelay, GroupMessage::quitMessage);
    }

    /**
     * Handles the processing and sending of connection messages (join or quit).
     * Retrieves the highest priority group message for the player and sends it
     * with the configured delay.
     *
     * @param player           the player who connected or disconnected
     * @param delay            the delay in ticks before sending the message
     * @param messageExtractor function to extract the message list from a GroupMessage
     */
    private void handleConnectionMessage(Player player, long delay, Function<GroupMessage, List<String>> messageExtractor) {
        GroupMessage highestPriorityGroupMessage = groupMessageManager.getHighestPriorityGroupMessage(player);
        if (highestPriorityGroupMessage == null) {
            return;
        }

        String playerName = player.getName();
        List<String> rawMessage = messageExtractor.apply(highestPriorityGroupMessage);

        if (isAsyncEnabled) {
            CompletableFuture.supplyAsync(() -> format(rawMessage, playerName)).thenAccept(message -> sendMessage(player, message, delay));
        } else {
            Component message = format(rawMessage, playerName);
            sendMessage(player, message, delay);
        }
    }

    /**
     * Formats a list of message strings by replacing placeholders and converting
     * them into a Component using the configured message formatter.
     *
     * @param message    the list of raw message strings
     * @param playerName the name of the player to replace in placeholders
     * @return the formatted Component
     */
    private Component format(List<String> message, String playerName) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        List<String> formattedMessage = message.stream()
                .map(line -> line.replace("%player_name%", playerName))
                .toList();

        return messageFormatter.deserialize(formattedMessage);
    }

    /**
     * Sends a message to all online players, either immediately or after a delay.
     *
     * @param player  the player associated with the message (used for entity-based scheduling)
     * @param message the formatted message Component to send
     * @param delay   the delay in ticks before sending the message (0 or less for immediate sending)
     */
    private void sendMessage(Player player, Component message, long delay) {
        if (message == null) {
            return;
        }

        Runnable runnable = () -> {
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                Audience audience = plugin.getAudience(onlinePlayer);
                audience.sendMessage(message);
            }
        };

        if (delay <= 0) {
            runnable.run();
        } else {
            scheduleMessage(player, runnable, delay);
        }
    }

    /**
     * Schedules a message to be sent after a delay. Uses async scheduling if enabled,
     * otherwise uses entity-based scheduling.
     *
     * @param player   the player associated with the message (used for entity-based scheduling)
     * @param runnable the task to execute
     * @param delay    the delay in ticks before executing the task
     */
    private void scheduleMessage(Player player, Runnable runnable, long delay) {
        if (isAsyncEnabled) {
            scheduler.runLaterAsync(runnable, delay);
        } else {
            scheduler.runAtEntityLater(player, runnable, delay);
        }
    }
}