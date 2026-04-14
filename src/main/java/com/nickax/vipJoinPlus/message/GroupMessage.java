package com.nickax.vipJoinPlus.message;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a group-based message configuration for player to join and quit events.
 * Each group has custom join and quit messages, a priority for determining precedence,
 * and a permission node that controls which players have access to this group's messages.
 * Groups with higher priority values take precedence when a player has permissions for multiple groups.
 *
 * @param joinMessage the list of message lines to display when a player with this group's permission joins
 * @param quitMessage the list of message lines to display when a player with this group's permission quits
 * @param priority    the priority level of this group; higher values take precedence over lower values
 * @param permission  the permission node required for a player to be associated with this group
 */
public record GroupMessage(@Nullable List<String> joinMessage, @Nullable List<String> quitMessage, int priority, String permission) {
}