package com.nickax.vipJoinPlus.listener;

import com.nickax.nexus.api.lang.Lang;
import com.nickax.nexus.api.lang.Placeholder;
import com.nickax.nexus.api.text.TextFormat;
import com.nickax.nexus.bukkit.BukkitNexus;
import com.nickax.nexus.bukkit.schedule.BukkitScheduler;
import com.nickax.nexus.bukkit.text.Messages;
import com.nickax.vipJoinPlus.VIPJoinPlus;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.hook.PlaceholderAPIHook;
import com.nickax.vipJoinPlus.message.GroupMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Broadcasts per-group join and quit messages, localized to each recipient's own client
 * language. When a player connects, their highest-priority group is resolved by permission
 * and that group's {@code groups.<id>.join|quit} message is rendered for every online
 * player in that player's locale, honouring the configured delay, async setting and text
 * format. Configuration and language are read live, so a reload takes effect without
 * re-registering the listener.
 */
public class PlayerConnectionListener implements Listener {

    private static final long MILLIS_PER_TICK = 50L;

    private final VIPJoinPlus plugin;
    private final BukkitScheduler scheduler;
    private final Messages messages;
    private final Consumer<String> debug;

    /**
     * Constructs the listener.
     *
     * @param plugin the VIPJoinPlus plugin instance (source of live config, lang and hooks)
     */
    public PlayerConnectionListener(VIPJoinPlus plugin) {
        this.plugin = plugin;
        BukkitNexus nexus = plugin.getNexus();
        this.scheduler = nexus.scheduler();
        this.messages = nexus.messages();
        this.debug = plugin::debug;
    }

    /**
     * Suppresses the vanilla join message if configured, then broadcasts the player's
     * group join message.
     *
     * @param event the join event
     */
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getMainConfiguration().isVanillaJoinMessageDisabled()) {
            event.setJoinMessage(null);
        }

        handleConnectionMessage(event.getPlayer(), "join", plugin.getMainConfiguration().getJoinMessageDelay());
    }

    /**
     * Suppresses the vanilla quit message if configured, then broadcasts the player's
     * group quit message.
     *
     * @param event the quit event
     */
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getMainConfiguration().isVanillaQuitMessageDisabled()) {
            event.setQuitMessage(null);
        }

        handleConnectionMessage(event.getPlayer(), "quit", plugin.getMainConfiguration().getQuitMessageDelay());
    }

    /**
     * Resolves the subject's group and schedules the localized broadcast, honouring the
     * delay and async settings. Returns early when the player matches no group or the
     * group has no message configured for this event.
     *
     * @param subject the player who joined or quit
     * @param event   the event name and lang suffix ({@code "join"} or {@code "quit"})
     * @param delay   the delay in ticks before broadcasting
     */
    private void handleConnectionMessage(Player subject, String event, long delay) {
        GroupMessage group = plugin.getGroupMessageManager().getHighestPriorityGroupMessage(subject);
        if (group == null) {
            debug.accept(event + " " + subject.getName() + " no matching group");
            return;
        }

        String key = "groups." + group.id() + "." + event;
        MainConfiguration configuration = plugin.getMainConfiguration();

        // Presence check against the default locale: skip when no text is configured here.
        String probe = plugin.getLang().resolve(configuration.getDefaultLanguage(), key);
        if (probe.equals(key) || probe.isBlank()) {
            debug.accept(event + " " + subject.getName() + " group=" + group.id() + " (no message)");
            return;
        }

        boolean async = configuration.isAsyncEnabled();
        TextFormat format = configuration.getFormat();
        debug.accept(event + " " + subject.getName() + " group=" + group.id() + " delay=" + delay + " async=" + async);

        Runnable task = () -> broadcastLocalized(subject, key, format);

        if (delay <= 0) {
            if (async) {
                scheduler.async(task);
            } else {
                task.run();
            }
            return;
        }

        Duration duration = Duration.ofMillis(delay * MILLIS_PER_TICK);
        if (async) {
            scheduler.asyncLater(duration, task);
        } else {
            scheduler.entityLater(subject, duration, task);
        }
    }

    /**
     * Renders the message for each online recipient in that recipient's own locale,
     * applies PlaceholderAPI against the subject, and sends it with the configured format.
     * Recipients whose resolved message is missing or empty are skipped.
     *
     * @param subject the player the message is about (used for placeholder resolution)
     * @param key     the lang key ({@code groups.<id>.join|quit})
     * @param format  the markup dialect to format with
     */
    private void broadcastLocalized(Player subject, String key, TextFormat format) {
        Lang lang = plugin.getLang();
        PlaceholderAPIHook placeholderAPIHook = plugin.getPlaceholderAPIHook();
        Placeholder playerName = Placeholder.of("player_name", subject.getName());

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            String resolved = lang.resolve(localeOf(recipient), key, playerName);
            if (resolved.equals(key) || resolved.isBlank()) {
                continue;
            }

            if (placeholderAPIHook != null) {
                resolved = placeholderAPIHook.setPlaceholders(subject, resolved);
            }

            List<String> lines = Arrays.asList(resolved.split("\n", -1));
            messages.send(recipient, format, lines);
        }
    }

    /**
     * Returns the bare locale code for a player (e.g. {@code "es"} from {@code "es_es"}),
     * matching how Nexus itself resolves a recipient's locale, so the lang lookup uses the
     * player's own client language and falls back to the default locale when absent.
     *
     * @param player the recipient
     * @return the bare locale code
     */
    private static String localeOf(Player player) {
        String locale = player.getLocale();
        int separator = locale.indexOf('_');
        return separator > 0 ? locale.substring(0, separator) : locale;
    }
}
