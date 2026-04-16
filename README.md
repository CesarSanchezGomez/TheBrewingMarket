# TheBrewingMarket

A Minecraft plugin that lets players sell their **TheBrewingProject** or **BreweryX** brews for money through a customizable GUI.

## Requirements

- **Paper** 1.21.9+
- **Vault** (with an economy provider like EssentialsX)
- **TheBrewingProject or BreweryX** (at least one is required)

## Installation

1. Drop `TheBrewingMarket.jar` into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/TheBrewingMarket/config.yml` to your liking.
4. Use `/thebrewingmarket reload` to apply changes without restarting.

## Commands & Permissions

| Command | Alias | Description |
|---|---|---|
| `/thebrewingmarket` | `/tbm` | Opens the sell GUI |
| `/thebrewingmarket reload` | `/tbm reload` | Reloads configuration |
| `/thebrewingmarket history` | `/tbm history` | View sell history |

| Permission | Default | Description |
|---|---|---|
| `thebrewingmarket.*` | op | Full access |
| `thebrewingmarket.admin` | op | Reload config |
| `thebrewingmarket.history` | op | View sell history |
| `thebrewingmarket.use` | true | Use the sell GUI |
| `thebrewingmarket.shulker` | true | Sell brews from shulker boxes |

## Features

- Supports **TheBrewingProject** and **BreweryX** (auto-detected)
- Sell GUI with quick and intuitive interactions
- Sell All support (GUI + inventory)
- Shulker box selling (permission-based)
- Sell history with pagination and time filters
- Quality-based pricing using TheBrewingProject scoring API
- Per-recipe configurable pricing
- Sealed brew support with fallback handling
- Database support with automatic schema migrations
- Fully customizable GUI (layout, icons, styles)
- MiniMessage support for all text formatting
- Multi-language support (en_US, es_ES)

## Documentation

Full setup and configuration guide:
> https://cesarsanchezgomez.github.io/TheBrewingMarket/
