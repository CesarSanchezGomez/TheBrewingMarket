# BrewMarket

A Minecraft plugin that lets players sell their [TheBrewingProject](https://hangar.papermc.io/BreeweryTeam/TheBrewingProject) brews for money through a customizable GUI.

## Requirements

- **Paper** 1.21.11+
- **Vault** (with an economy provider like EssentialsX)
- **TheBrewingProject**

## Installation

1. Drop `BrewMarket.jar` into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/BrewMarket/config.yml` to your liking.
4. Use `/brewmarket reload` to apply changes without restarting.

## Commands & Permissions

| Command | Alias | Permission | Description |
|---|---|---|---|
| `/brewmarket` | `/bm` | `brewmarket.use` | Opens the sell GUI |
| `/brewmarket reload` | `/bm reload` | `brewmarket.admin` | Reloads configuration |

| Permission | Default | Description |
|---|---|---|
| `brewmarket.*` | op | Full access |
| `brewmarket.use` | true | Use the sell GUI |
| `brewmarket.admin` | op | Reload config |

## Documentation

For full configuration details, see the **[Wiki](../../wiki)**.

- [Home](../../wiki/Home)
- [Prices](../../wiki/Prices)
- [Layout](../../wiki/Layout)
- [Icons](../../wiki/Icons)
- [Sell Buttons](../../wiki/Sell-Buttons)
- [Language](../../wiki/Language)
