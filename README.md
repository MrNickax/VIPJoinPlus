# VIPJoinPlus

Modern, lightweight and fully customizable join & quit message plugin — with per-player
languages, MiniMessage formatting and permission-based VIP ranks.

Give your VIPs and ranked players the spotlight they deserve, and show every player the
join/quit message in their own client language.

## Features

- **Per-player languages** — each online player sees the message in their own language
  (`lang/<locale>.yml`; English & Spanish included).
- **Permission-based groups** with a smart priority system.
- **Modern formatting** with MiniMessage (gradients, HEX, hover & click events).
- **Formatting modes:** MiniMessage, Legacy (`&` codes) and Mixed.
- **PlaceholderAPI** support.
- Optionally hide the default vanilla join & quit messages.
- Configurable join & quit message delays.
- Async & performance-friendly.
- Reload-safe (PlugMan-friendly) — no stale or duplicated messages.
- Built-in debug mode.

## Requirements

- Java 21+
- Spigot, Paper or Folia 1.21+
- **[Nexus](https://github.com/MrNickax/Nexus/releases/latest)** core plugin (install it
  alongside VIPJoinPlus)

## Installation

1. Download the latest [Nexus](https://github.com/MrNickax/Nexus/releases/latest) and drop
   it into your server's `plugins/` folder.
2. Drop `VIPJoinPlus.jar` into `plugins/` as well.
3. Start the server, then edit `plugins/VIPJoinPlus/config.yml` and the
   `plugins/VIPJoinPlus/lang/` files to taste.
4. Run `/vipjoinplus reload`.

## Configuration

- `config.yml` — group definitions (priority + permission), formatting mode, vanilla-message
  toggles, delays, async and debug.
- `lang/<locale>.yml` — the join/quit text per group (`groups.<id>.join` / `groups.<id>.quit`)
  and command messages, one file per language. Add a new language by copying `en.yml` to
  `lang/<id>.yml` and translating it.

## Commands

| Command | Description |
| --- | --- |
| `/vipjoinplus reload` | Reload the configuration and language files |

## Permissions

| Permission | Description |
| --- | --- |
| `vipjoinplus.reload` | Allows reloading the plugin |
| `group.<name>` | Assigns a player to a message group (configurable per group) |

## Building

Requires JDK 21 and Maven. VIPJoinPlus depends on Nexus, so install Nexus first.

```bash
mvn clean package
```

The plugin jar is written to `target/VIPJoinPlus-<version>.jar`.

## License

See the repository for license details.
