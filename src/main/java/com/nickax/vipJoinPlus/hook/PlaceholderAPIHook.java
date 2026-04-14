package com.nickax.vipJoinPlus.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Hook class for integrating with PlaceholderAPI.
 * <p>
 * This class provides a wrapper around PlaceholderAPI functionality,
 * allowing the plugin to parse and replace placeholders in strings
 * with player-specific data.
 * </p>
 */
public class PlaceholderAPIHook {

    /**
     * Replaces all PlaceholderAPI placeholders in the input string with their corresponding values.
     * <p>
     * Placeholders are text patterns (usually in the format %placeholder%) that get replaced
     * with dynamic values specific to the player, such as their name, balance, rank, etc.
     * </p>
     *
     * @param player the player for whom the placeholders should be parsed
     * @param input  the string containing placeholders to be replaced
     * @return the input string with all placeholders replaced with their actual values
     */
    public String setPlaceholders(Player player, String input) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}