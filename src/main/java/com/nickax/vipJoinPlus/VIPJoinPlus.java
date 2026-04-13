package com.nickax.vipJoinPlus;

import com.nickax.vipJoinPlus.command.VIPJoinPlusCommand;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.message.LegacyMessageFormatter;
import com.nickax.vipJoinPlus.message.MessageFormatter;
import com.nickax.vipJoinPlus.message.MiniMessageMessageFormatter;
import com.nickax.vipJoinPlus.message.MixedMessageFormatter;
import com.nickax.vipJoinPlus.message.GroupMessageManager;
import com.nickax.vipJoinPlus.listener.PlayerConnectionListener;
import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Main plugin class for VIPJoinPlus.
 * Manages custom join and quit messages for VIP players with group-based configurations.
 */
public final class VIPJoinPlus extends JavaPlugin {

    private final FoliaLib foliaLib = new FoliaLib(this);

    private BukkitAudiences adventure;
    private MainConfiguration mainConfiguration;
    private MessageFormatter messageFormatter;
    private GroupMessageManager groupMessageManager;
    private final List<Listener> registeredListeners = new ArrayList<>();

    /**
     * Called when the plugin is enabled.
     * Initializes all plugin components including configuration, message formatting, and event listeners.
     */
    @Override
    public void onEnable() {
        loadAdventure();
        loadMainConfiguration();
        loadMessageFormatter();
        loadGroupMessageManager();
        registerListeners();
        registerCommands();
    }

    /**
     * Called when the plugin is disabled.
     * Unregisters all listeners, clears resources, and closes the adventure platform.
     */
    @Override
    public void onDisable() {
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
        adventure.close();
        groupMessageManager.clear();
    }

    /**
     * Reloads the plugin configuration and reinitialize components.
     * This includes reloading the main configuration, message formatter, group messages, and re-registering listeners.
     */
    public void reload() {
        mainConfiguration.reload();

        if (mainConfiguration.isAsyncEnabled()) {
            getLogger().info("Async processing is enabled. Join and quit messages will be processed asynchronously for better performance.");
        }

        loadMessageFormatter();
        groupMessageManager.load(mainConfiguration.getGroupsSection());

        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
        registerListeners();
    }

    /**
     * Gets the FoliaLib instance for Folia/Paper compatibility.
     *
     * @return the FoliaLib instance
     */
    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    /**
     * Gets the Adventure audience for a specific player.
     *
     * @param player the player to get the audience for
     * @return the audience instance for the player
     */
    public Audience getAudience(Player player) {
        return adventure.player(player);
    }

    /**
     * Gets the Adventure audience for a command sender.
     *
     * @param sender the command sender to get the audience for
     * @return the audience instance for the command sender
     */
    public Audience getAudience(CommandSender sender) {
        return adventure.sender(sender);
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
     * Gets the current message formatter instance.
     *
     * @return the message formatter
     */
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
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
     * Initializes the Adventure platform for message handling.
     */
    private void loadAdventure() {
        adventure = BukkitAudiences.create(this);
    }

    /**
     * Loads the main configuration file and initializes the configuration instance.
     * Logs a message if async processing is enabled.
     */
    private void loadMainConfiguration() {
        mainConfiguration = new MainConfiguration(this);
        mainConfiguration.load();

        if (mainConfiguration.isAsyncEnabled()) {
            getLogger().info("Async processing is enabled. Join and quit messages will be processed asynchronously for better performance.");
        }
    }

    /**
     * Loads and initializes the message formatter based on the configured mode.
     * Supported modes are: MINI_MESSAGE, LEGACY, and MIXED.
     * Defaults to MINI_MESSAGE if an invalid mode is specified.
     */
    private void loadMessageFormatter() {
        String mode = mainConfiguration.getMessageFormatterMode();

        switch (mode.toUpperCase()) {
            case "MINI_MESSAGE":
                messageFormatter = new MiniMessageMessageFormatter();
                getLogger().info("Message formatter mode set to MINI_MESSAGE.");
                break;
            case "LEGACY":
                messageFormatter = new LegacyMessageFormatter();
                getLogger().info("Message formatter mode set to LEGACY.");
                break;
            case "MIXED":
                messageFormatter = new MixedMessageFormatter();
                getLogger().info("Message formatter mode set to MIXED.");
                break;
            default:
                messageFormatter = new MiniMessageMessageFormatter();
                getLogger().warning("Invalid message formatter mode '" + mode + "'. Defaulting to MINI_MESSAGE. Valid modes are: MINI_MESSAGE, LEGACY, MIXED.");
                break;
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
     * Registers all event listeners for the plugin.
     * Currently, registers the PlayerConnectionListener for handling join and quit events.
     */
    private void registerListeners() {
        PlayerConnectionListener playerConnectionListener = new PlayerConnectionListener(this);

        getServer().getPluginManager().registerEvents(playerConnectionListener, this);

        registeredListeners.add(playerConnectionListener);
    }

    /**
     * Registers the plugin commands.
     * Sets up the /vipjoinplus command executor.
     */
    private void registerCommands() {
        PluginCommand command = getCommand("vipjoinplus");

        if (command == null) {
            getLogger().severe("VIPJoinPlus command could not be registered. Please notify the developer about this issue.");
            return;
        }

        command.setExecutor(new VIPJoinPlusCommand(this));
    }
}