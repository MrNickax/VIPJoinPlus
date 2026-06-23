package com.nickax.vipJoinPlus.message;

/**
 * Definition of a VIPJoinPlus group: its matching rule (a permission) and its precedence
 * (a priority). The actual join/quit text is no longer held here; it lives in the
 * {@code lang/<locale>.yml} files under {@code groups.<id>.join} / {@code groups.<id>.quit}
 * so it can be localized per player. Groups with a higher priority take precedence when a
 * player matches several of them.
 *
 * @param id         the group id, also used as the lang key prefix ({@code groups.<id>.*})
 * @param priority   the priority; higher values win over lower ones
 * @param permission the permission node a player must have to belong to this group
 */
public record GroupMessage(String id, int priority, String permission) {
}
