package com.nickax.vipJoinPlus.config;

import com.nickax.nexus.api.config.Config;
import com.nickax.nexus.api.config.ConfigSection;
import com.nickax.nexus.api.lang.Lang;
import com.nickax.nexus.api.lang.LangBuilder;
import com.nickax.nexus.api.text.TextFormat;
import com.nickax.nexus.bukkit.BukkitNexus;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Typed accessors over the plugin's {@code config.yml}, loaded through Nexus, plus the
 * loader that turns the {@code lang/} files into a Nexus {@link Lang}. The file lives in
 * the plugin's own data folder; Nexus creates it from the bundled defaults on first run
 * and merges any missing keys while preserving comments and order.
 */
public class MainConfiguration {

    private final JavaPlugin plugin;
    private final BukkitNexus nexus;
    private final Consumer<String> debug;

    private Config config;

    /**
     * Constructs the configuration handler.
     *
     * @param plugin the owning plugin, used for the data folder and bundled defaults
     * @param nexus  the Nexus hub providing the config and lang services
     * @param debug  the centralized debug sink (no-op when debug mode is off)
     */
    public MainConfiguration(JavaPlugin plugin, BukkitNexus nexus, Consumer<String> debug) {
        this.plugin = plugin;
        this.nexus = nexus;
        this.debug = debug;
    }

    /**
     * Loads {@code config.yml}, creating it from the bundled defaults if absent.
     */
    public void load() {
        Path file = plugin.getDataFolder().toPath().resolve("config.yml");
        config = nexus.configs().load(file, plugin.getResource("config.yml"));
    }

    /**
     * Reloads {@code config.yml} from disk, discarding unsaved changes.
     */
    public void reload() {
        config.reload();
    }

    /**
     * Builds a {@link Lang} from every {@code lang/<id>.yml} file, saving the bundled
     * {@code en} and {@code es} defaults on first run. Each file becomes a locale bundle
     * keyed by its file name (without {@code .yml}); nested sections are flattened to
     * dotted keys (e.g. {@code groups.vip.join}) and list values are joined with newlines.
     * The default locale is {@code language.default}.
     *
     * @return the built lang
     */
    public Lang loadLang() {
        saveDefaultLang("en");
        saveDefaultLang("es");

        Path langDir = plugin.getDataFolder().toPath().resolve("lang");
        LangBuilder builder = nexus.lang().builder().defaultLocale(getDefaultLanguage());

        try (Stream<Path> files = Files.list(langDir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".yml")).forEach(path -> {
                String locale = path.getFileName().toString().replaceFirst("\\.yml$", "");
                Config bundle = nexus.configs().load(path, null);

                Map<String, String> messages = new HashMap<>();
                flatten(bundle, "", messages);

                builder.bundle(locale, messages);
                debug.accept("Loaded lang bundle '" + locale + "' (" + messages.size() + " keys)");
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to list language files", e);
        }

        return builder.build();
    }

    /**
     * Recursively flattens a config section into dotted keys, joining list values into a
     * single newline-separated string. Used so nested lang YAML (per-group join/quit)
     * maps onto the flat key-to-template model that {@link Lang} expects.
     *
     * @param section the section to flatten
     * @param prefix  the dotted prefix accumulated so far (empty at the root)
     * @param out     the destination key-to-template map
     */
    private void flatten(ConfigSection section, String prefix, Map<String, String> out) {
        for (String key : section.keys()) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            ConfigSection child = section.getSection(key);

            if (child != null) {
                flatten(child, path, out);
                continue;
            }

            Object raw = section.get(key);
            if (raw instanceof List<?> list) {
                out.put(path, String.join("\n", list.stream().map(String::valueOf).toList()));
            } else {
                out.put(path, section.getString(key, ""));
            }
        }
    }

    /**
     * Saves a bundled {@code lang/<locale>.yml} resource into the data folder if it is
     * not already present.
     *
     * @param locale the locale id whose file should be saved
     */
    private void saveDefaultLang(String locale) {
        Path target = plugin.getDataFolder().toPath().resolve("lang").resolve(locale + ".yml");
        if (Files.notExists(target) && plugin.getResource("lang/" + locale + ".yml") != null) {
            plugin.saveResource("lang/" + locale + ".yml", false);
        }
    }

    /**
     * Returns the formatting dialect for join/quit and command messages, defaulting to
     * {@link TextFormat#MINI_MESSAGE} when the configured value is missing or unrecognised.
     *
     * @return the configured text format
     */
    public TextFormat getFormat() {
        String mode = config.getString("behavior.message-formatter-mode", "MINI_MESSAGE");

        try {
            return TextFormat.valueOf(mode.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid message-formatter-mode '" + mode
                    + "'. Defaulting to MINI_MESSAGE. Valid modes are: MINI_MESSAGE, LEGACY, MIXED.");
            return TextFormat.MINI_MESSAGE;
        }
    }

    /**
     * Returns the default language id used when a player's client language has no bundle.
     *
     * @return the default language id
     */
    public String getDefaultLanguage() {
        return config.getString("language.default", "en");
    }

    /**
     * Returns whether vanilla join messages should be suppressed.
     *
     * @return {@code true} if vanilla join messages are disabled
     */
    public boolean isVanillaJoinMessageDisabled() {
        return config.getBoolean("behavior.disable-vanilla-join-message");
    }

    /**
     * Returns whether vanilla quit messages should be suppressed.
     *
     * @return {@code true} if vanilla quit messages are disabled
     */
    public boolean isVanillaQuitMessageDisabled() {
        return config.getBoolean("behavior.disable-vanilla-quit-message");
    }

    /**
     * Returns the join-message delay in ticks.
     *
     * @return the delay in ticks, or 0 if not configured
     */
    public long getJoinMessageDelay() {
        return config.getLong("behavior.join-message-delay", 0);
    }

    /**
     * Returns the quit-message delay in ticks.
     *
     * @return the delay in ticks, or 0 if not configured
     */
    public long getQuitMessageDelay() {
        return config.getLong("behavior.quit-message-delay", 0);
    }

    /**
     * Returns whether join/quit messages are processed asynchronously.
     *
     * @return {@code true} if async processing is enabled
     */
    public boolean isAsyncEnabled() {
        return config.getBoolean("behavior.async-enabled");
    }

    /**
     * Returns whether verbose debug logging is enabled.
     *
     * @return {@code true} if debug logging is enabled
     */
    public boolean isDebug() {
        return config.getBoolean("debug");
    }

    /**
     * Returns the {@code groups} section holding the group definitions.
     *
     * @return the groups section, or {@code null} if absent
     */
    public ConfigSection getGroupsSection() {
        return config.getSection("groups");
    }
}
