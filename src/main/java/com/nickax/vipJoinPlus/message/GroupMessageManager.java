package com.nickax.vipJoinPlus.message;

import com.nickax.nexus.api.config.ConfigSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Loads the group definitions (priority + permission) from the {@code groups} config
 * section, keeps them sorted by descending priority, and resolves the highest-priority
 * group a player belongs to. The join/quit text itself is not stored here; it is looked
 * up per recipient from the localized {@code lang} bundles using the group id.
 */
public class GroupMessageManager {

    private final List<GroupMessage> groupsByPriority = new ArrayList<>();
    private final Consumer<String> debug;

    /**
     * Constructs the manager.
     *
     * @param debug the centralized debug sink (no-op when debug mode is off)
     */
    public GroupMessageManager(Consumer<String> debug) {
        this.debug = debug;
    }

    /**
     * Loads the group definitions from the provided section, replacing any previously
     * loaded ones and re-sorting by descending priority.
     *
     * @param section the {@code groups} section, or {@code null} to clear all groups
     */
    public void load(ConfigSection section) {
        clear();

        if (section == null) {
            debug.accept("Loaded 0 group(s) (no groups section)");
            return;
        }

        for (String id : section.keys()) {
            ConfigSection groupSection = section.getSection(id);

            if (groupSection == null) {
                continue;
            }

            int priority = groupSection.getInt("priority", 0);
            String permission = groupSection.getString("permission", "group." + id);

            groupsByPriority.add(new GroupMessage(id, priority, permission));
        }

        groupsByPriority.sort(Comparator.comparingInt(GroupMessage::priority).reversed());
        debug.accept("Loaded " + groupsByPriority.size() + " group(s)");
    }

    /**
     * Returns the highest-priority group the player has permission for.
     *
     * @param player the player to match
     * @return the matched group, or {@code null} if the player matches no group
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
     * Clears all loaded group definitions.
     */
    public void clear() {
        groupsByPriority.clear();
    }
}
