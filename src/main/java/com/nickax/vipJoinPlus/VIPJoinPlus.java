package com.nickax.vipJoinPlus;

import com.nickax.nexus.api.lang.Lang;
import com.nickax.nexus.bukkit.BukkitNexus;
import com.nickax.vipJoinPlus.command.VIPJoinPlusCommand;
import com.nickax.vipJoinPlus.config.MainConfiguration;
import com.nickax.vipJoinPlus.hook.PlaceholderAPIHook;
import com.nickax.vipJoinPlus.listener.PlayerConnectionListener;
import com.nickax.vipJoinPlus.message.GroupMessageManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * VIPJoinPlus: configurable, per-group join and quit messages built on Nexus. Group
 * definitions (priority + permission) live in {@code config.yml}; the message text lives in
 * the {@code lang/} folder so every player sees the broadcast in their own client language.
 * Listeners and commands are registered through Nexus with this plugin as the owner, so a
 * PlugMan-style unload/reload leaves no stale or duplicate handlers.
 */
public final class VIPJoinPlus extends JavaPlugin {

    private BukkitNexus nexus;
    private MainConfiguration mainConfiguration;
    private GroupMessageManager groupMessageManager;
    private Lang lang;
    private @Nullable PlaceholderAPIHook placeholderAPIHook;

    /**
     * Enables the plugin: resolves the Nexus hub, loads the configuration and language
     * bundles, builds the group definitions, wires the PlaceholderAPI hook (if present),
     * and registers the connection listener and command under this plugin's ownership.
     */
    @Override
    public void onEnable() {
        nexus = BukkitNexus.get();

        mainConfiguration = new MainConfiguration(this, nexus, this::debug);
        mainConfiguration.load();
        lang = mainConfiguration.loadLang();
        debug("Enabling VIPJoinPlus (default locale=" + mainConfiguration.getDefaultLanguage() + ")");

        groupMessageManager = new GroupMessageManager(this::debug);
        groupMessageManager.load(mainConfiguration.getGroupsSection());

        loadPlaceholderAPIHook();

        nexus.listeners().register(this, new PlayerConnectionListener(this));
        nexus.commands().register(this, new VIPJoinPlusCommand(this).build());
        debug("Registered listeners and commands");
    }

    /**
     * Disables the plugin: unregisters this plugin's own listeners and commands and clears
     * the loaded group definitions. Only this plugin's handlers are removed, so other
     * plugins are untouched on a reload.
     */
    @Override
    public void onDisable() {
        debug("Disabling VIPJoinPlus");

        if (nexus != null) {
            nexus.commands().unregister(this);
            nexus.listeners().unregister(this);
        }

        if (groupMessageManager != null) {
            groupMessageManager.clear();
        }
    }

    /**
     * Reloads the configuration, language bundles and group definitions. Listeners read
     * configuration and language live, so they need not be re-registered.
     */
    public void reload() {
        getLogger().info("Reloading VIPJoinPlus...");
        mainConfiguration.reload();
        lang = mainConfiguration.loadLang();
        groupMessageManager.load(mainConfiguration.getGroupsSection());
        debug("Reload complete");
    }

    /**
     * Returns the Nexus hub.
     *
     * @return the Nexus hub
     */
    public BukkitNexus getNexus() {
        return nexus;
    }

    /**
     * Returns the main configuration handler.
     *
     * @return the main configuration
     */
    public MainConfiguration getMainConfiguration() {
        return mainConfiguration;
    }

    /**
     * Returns the group message manager.
     *
     * @return the group message manager
     */
    public GroupMessageManager getGroupMessageManager() {
        return groupMessageManager;
    }

    /**
     * Returns the current language bundles. The instance is rebuilt on reload, so callers
     * should read it fresh rather than caching it.
     *
     * @return the current lang
     */
    public Lang getLang() {
        return lang;
    }

    /**
     * Returns the PlaceholderAPI hook.
     *
     * @return the hook, or {@code null} if PlaceholderAPI is not installed
     */
    public @Nullable PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    /**
     * Logs a message at INFO level when debug mode is enabled, no-op otherwise. Centralised
     * here so every component reports through {@code this::debug}: the gate is read live
     * from config, so toggling {@code debug} and running {@code /vipjoinplus reload} takes
     * effect immediately. Safe to call before the configuration has loaded (treated as
     * disabled).
     *
     * @param message the debug message
     */
    public void debug(String message) {
        if (mainConfiguration != null && mainConfiguration.isDebug()) {
            getLogger().info("[debug] " + message);
        }
    }

    /**
     * Initializes the PlaceholderAPI hook if the PlaceholderAPI plugin is present.
     */
    private void loadPlaceholderAPIHook() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooked into PlaceholderAPI!");
            placeholderAPIHook = new PlaceholderAPIHook();
            debug("PlaceholderAPI hook enabled");
        }
    }
}
