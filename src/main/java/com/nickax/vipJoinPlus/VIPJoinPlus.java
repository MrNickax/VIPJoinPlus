package com.nickax.vipJoinPlus;

import com.nickax.nexus.bukkit.BukkitNexus;
import com.nickax.vipJoinPlus.command.VIPJoinPlusCommand;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.hook.PlaceholderAPIHook;
import com.nickax.vipJoinPlus.listener.PlayerConnectionListener;
import com.nickax.vipJoinPlus.message.GroupMessageManager;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Main plugin class for VIPJoinPlus.
 * Manages custom join and quit messages for VIP players with group-based
 * configurations, built on top of the Nexus core for scheduling, configuration,
 * commands and message formatting.
 */
public final class VIPJoinPlus extends JavaPlugin {

    private final List<Listener> registeredListeners = new ArrayList<>();

    private BukkitNexus nexus;
    private MainConfiguration mainConfiguration;
    private GroupMessageManager groupMessageManager;
    private PlaceholderAPIHook placeholderAPIHook;

    /**
     * Called when the plugin is enabled.
     * Resolves the Nexus hub and initializes configuration, group messages,
     * the PlaceholderAPI hook, event listeners, and commands.
     */
    @Override
    public void onEnable() {
        nexus = BukkitNexus.get();
        loadMainConfiguration();
        loadGroupMessageManager();
        loadPlaceholderAPIHook();
        registerListeners();
        registerCommands();
    }

    /**
     * Called when the plugin is disabled.
     * Unregisters all listeners and clears loaded group messages. Adventure and the
     * command map are owned by Nexus, so nothing else needs closing here.
     */
    @Override
    public void onDisable() {
        if (nexus != null) {
            nexus.commands().unregister(this);
        }

        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();

        if (groupMessageManager != null) {
            groupMessageManager.clear();
        }
    }

    /**
     * Reloads the plugin configuration and reinitializes components.
     * Reloads the main configuration and group messages, then re-registers listeners
     * so they pick up the new settings.
     */
    public void reload() {
        getLogger().info("Reloading VIPJoinPlus...");
        mainConfiguration.reload();

        if (mainConfiguration.isAsyncEnabled()) {
            getLogger().info("Async processing is enabled. Join and quit messages will be processed asynchronously for better performance.");
        }

        groupMessageManager.load(mainConfiguration.getGroupsSection());

        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
        registerListeners();
    }

    /**
     * Gets the Nexus hub.
     *
     * @return the Nexus hub
     */
    public BukkitNexus getNexus() {
        return nexus;
    }

    /**
     * Gets the main configuration instance.
     *
     * @return the main configuration
     */
    public MainConfiguration getMainConfiguration() {
        return mainConfiguration;
    }

    /**
     * Gets the group message manager instance.
     *
     * @return the group message manager
     */
    public GroupMessageManager getGroupMessageManager() {
        return groupMessageManager;
    }

    /**
     * Gets the PlaceholderAPI hook instance.
     *
     * @return the PlaceholderAPI hook instance, or null if PlaceholderAPI is not available
     */
    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    /**
     * Loads the main configuration file and initializes the configuration instance.
     * Logs a message if async processing is enabled.
     */
    private void loadMainConfiguration() {
        mainConfiguration = new MainConfiguration(this, nexus);
        mainConfiguration.load();

        if (mainConfiguration.isAsyncEnabled()) {
            getLogger().info("Async processing is enabled. Join and quit messages will be processed asynchronously for better performance.");
        }
    }

    /**
     * Initializes the group message manager and loads group configurations.
     */
    private void loadGroupMessageManager() {
        groupMessageManager = new GroupMessageManager();
        groupMessageManager.load(mainConfiguration.getGroupsSection());
    }

    /**
     * Loads and initializes the PlaceholderAPI hook if the PlaceholderAPI plugin is detected.
     */
    private void loadPlaceholderAPIHook() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI hook detected. Enabling PlaceholderAPI support.");
            placeholderAPIHook = new PlaceholderAPIHook();
        }
    }

    /**
     * Registers all event listeners for the plugin.
     */
    private void registerListeners() {
        PlayerConnectionListener playerConnectionListener = new PlayerConnectionListener(this);

        getServer().getPluginManager().registerEvents(playerConnectionListener, this);

        registeredListeners.add(playerConnectionListener);
    }

    /**
     * Registers the {@code /vipjoinplus} command through the Nexus command engine.
     */
    private void registerCommands() {
        nexus.commands().register(this, new VIPJoinPlusCommand(this).build());
    }
}
