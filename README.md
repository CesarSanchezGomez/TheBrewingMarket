# TheBrewingMarket

A Minecraft plugin that lets players sell their [TheBrewingProject](https://hangar.papermc.io/BreeweryTeam/TheBrewingProject) brews for money through a customizable GUI.

## Requirements

- **Paper** 1.21.11+
- **Vault** (with an economy provider like EssentialsX)
- **TheBrewingProject**

## Installation

1. Drop `TheBrewingMarket.jar` into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/TheBrewingMarket/config.yml` to your liking.
4. Use `/thebrewingmarket reload` to apply changes without restarting.

## Commands & Permissions

| Command | Alias | Permission | Description |
|---|---|---|---|
| `/thebrewingmarket` | `/tbm` | `thebrewingmarket.use` | Opens the sell GUI |
| `/thebrewingmarket reload` | `/tbm reload` | `thebrewingmarket.admin` | Reloads configuration |
| `/thebrewingmarket history` | `/tbm history` | `thebrewingmarket.history` | View sell history |

| Permission | Default | Description |
|---|---|---|
| `thebrewingmarket.*` | op | Full access |
| `thebrewingmarket.admin` | op | Reload config |
| `thebrewingmarket.history` | op | View sell history |
| `thebrewingmarket.use` | true | Use the sell GUI |
| `thebrewingmarket.shulker` | true | Sell brews from shulker boxes |

## Documentation

For full configuration details, see the **[WIP]()**.