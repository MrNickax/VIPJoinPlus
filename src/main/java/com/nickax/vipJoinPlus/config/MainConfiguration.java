package com.nickax.vipJoinPlus.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main configuration handler for the VIPJoinPlus plugin.
 * Manages loading, reloading, and accessing configuration values.
 */
public class MainConfiguration {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    /**
     * Constructs a new MainConfiguration instance.
     *
     * @param plugin the JavaPlugin instance
     */
    public MainConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the configuration file.
     * Saves the default config if it doesn't exist and loads it into memory.
     */
    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    /**
     * Reloads the configuration file from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Gets the message formatter mode from the configuration.
     *
     * @return the message formatter mode (e.g., "MINI_MESSAGE", "LEGACY", "MIXED")
     */
    public String getMessageFormatterMode() {
        return config.getString("behavior.message-formatter-mode");
    }

    /**
     * Checks if vanilla join messages should be disabled.
     *
     * @return true if vanilla join messages should be disabled, false otherwise
     */
    public boolean isVanillaJoinMessageDisabled() {
        return config.getBoolean("behavior.disable-vanilla-join-message");
    }

    /**
     * Checks if vanilla quit messages should be disabled.
     *
     * @return true if vanilla quit messages should be disabled, false otherwise
     */
    public boolean isVanillaQuitMessageDisabled() {
        return config.getBoolean("behavior.disable-vanilla-quit-message");
    }

    /**
     * Gets the delay for join messages in ticks.
     *
     * @return the delay in ticks, or 0 if not configured
     */
    public long getJoinMessageDelay() {
        String delay = config.getString("behavior.join-message-delay");

        if (delay == null) {
            return 0;
        }

        return Long.parseLong(delay);
    }

    /**
     * Gets the delay for quit messages in ticks.
     *
     * @return the delay in ticks, or 0 if not configured
     */
    public long getQuitMessageDelay() {
        String delay = config.getString("behavior.quit-message-delay");

        if (delay == null) {
            return 0;
        }

        return Long.parseLong(delay);
    }

    /**
     * Checks if async processing is enabled for connection messages.
     *
     * @return true if async processing is enabled, false otherwise
     */
    public boolean isAsyncEnabled() {
        return config.getBoolean("behavior.async-enabled");
    }

    /**
     * Gets the groups configuration section.
     *
     * @return the ConfigurationSection containing group definitions, or null if not present
     */
    public ConfigurationSection getGroupsSection() {
        return config.getConfigurationSection("groups");
    }

    /**
     * Gets the reload message from the configuration.
     *
     * @return the reload message string
     */
    public String getReloadMessage() {
        return config.getString("messages.reload");
    }

    /**
     * Gets the no permission message from the configuration.
     *
     * @return the no permission message string
     */
    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission");
    }
}