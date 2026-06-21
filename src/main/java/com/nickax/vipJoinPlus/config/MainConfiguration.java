package com.nickax.vipJoinPlus.config;

import com.nickax.nexus.api.config.Config;
import com.nickax.nexus.api.config.ConfigSection;
import com.nickax.nexus.api.text.TextFormat;
import com.nickax.nexus.bukkit.BukkitNexus;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

/**
 * Main configuration handler for the VIPJoinPlus plugin.
 * Loads {@code config.yml} through Nexus and exposes typed accessors for the rest of
 * the plugin. The file lives in the plugin's own data folder; Nexus creates it from
 * the bundled defaults on first run and merges any missing keys.
 */
public class MainConfiguration {

    private final JavaPlugin plugin;
    private final BukkitNexus nexus;

    private Config config;

    /**
     * Constructs a new MainConfiguration.
     *
     * @param plugin the owning plugin, used for the data folder and bundled defaults
     * @param nexus  the Nexus hub providing the config service
     */
    public MainConfiguration(JavaPlugin plugin, BukkitNexus nexus) {
        this.plugin = plugin;
        this.nexus = nexus;
    }

    /**
     * Loads the configuration file, creating it from the bundled defaults if absent.
     */
    public void load() {
        Path file = plugin.getDataFolder().toPath().resolve("config.yml");
        config = nexus.configs().load(file, plugin.getResource("config.yml"));
    }

    /**
     * Reloads the configuration file from disk, discarding unsaved changes.
     */
    public void reload() {
        config.reload();
    }

    /**
     * Gets the formatting dialect used for join/quit and command messages.
     * Falls back to {@link TextFormat#MINI_MESSAGE} when the configured value is
     * missing or not a recognised mode.
     *
     * @return the configured text format
     */
    public TextFormat getFormat() {
        String mode = config.getString("behavior.message-formatter-mode", "MINI_MESSAGE");

        try {
            return TextFormat.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid message-formatter-mode '" + mode
                    + "'. Defaulting to MINI_MESSAGE. Valid modes are: MINI_MESSAGE, LEGACY, MIXED.");
            return TextFormat.MINI_MESSAGE;
        }
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
        return config.getLong("behavior.join-message-delay", 0);
    }

    /**
     * Gets the delay for quit messages in ticks.
     *
     * @return the delay in ticks, or 0 if not configured
     */
    public long getQuitMessageDelay() {
        return config.getLong("behavior.quit-message-delay", 0);
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
     * @return the section containing group definitions, or null if not present
     */
    public ConfigSection getGroupsSection() {
        return config.getSection("groups");
    }

    /**
     * Gets the reload message from the configuration.
     *
     * @return the reload message string
     */
    public String getReloadMessage() {
        return config.getString("messages.reload", "");
    }

    /**
     * Gets the no-permission message from the configuration.
     *
     * @return the no-permission message string
     */
    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission", "");
    }
}
