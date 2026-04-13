package com.nickax.vipJoinPlus.message;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages group-based join and quit messages for players.
 * This class loads group configurations from the configuration file,
 * stores them sorted by priority, and provides access to the appropriate
 * group message based on player permissions.
 */
public class GroupMessageManager {

    private final List<GroupMessage> groupsByPriority = new ArrayList<>();

    /**
     * Loads group message configurations from the provided configuration section.
     * Clears existing groups before loading new ones. Groups are sorted by priority
     * in descending order after loading.
     *
     * @param section the configuration section containing group definitions or null to clear all groups
     */
    public void load(ConfigurationSection section) {
        clear();

        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection groupSection = section.getConfigurationSection(id);

            if (groupSection == null) {
                continue;
            }

            int priority = groupSection.getInt("priority", 0);
            String permission = groupSection.getString("permission", "group." + id);
            List<String> joinMessage = groupSection.getStringList("join");
            List<String> quitMessage = groupSection.getStringList("quit");

            groupsByPriority.add(
                    new GroupMessage(joinMessage, quitMessage, priority, permission)
            );
        }

        groupsByPriority.sort(Comparator.comparingInt(GroupMessage::priority).reversed());
    }

    /**
     * Retrieves the highest priority group message that the player has permission for.
     * Groups are checked in descending priority order, and the first group for which
     * the player has the required permission is returned.
     *
     * @param player the player to check permissions for
     * @return the highest priority GroupMessage the player has permission for, or null if none found
     */
    public GroupMessage getHighestPriorityGroupMessage(Player player) {
        for (GroupMessage groupMessage : groupsByPriority) {
            if (player.hasPermission(groupMessage.permission())) {
                return groupMessage;
            }
        }
        return null;
    }

    /**
     * Clears all loaded group messages from the manager.
     */
    public void clear() {
        groupsByPriority.clear();
    }
}