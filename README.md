# BrewMarket

A Minecraft plugin that lets players sell their [TheBrewingProject](https://hangar.papermc.io/BreweryTeam/TheBrewingProject) brews for money through a customizable GUI.

## Requirements

- **Paper** 1.21.11
- **Vault** (with an economy provider like EssentialsX)
- **TheBrewingProject**

## Installation

1. Drop the `BrewMarket.jar` into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/BrewMarket/config.yml` to your liking.
4. Use `/brewmarket reload` to apply changes.

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

## Configuration

### Language

```yaml
lang: 'en_US'
```

Set the language file to load from `plugins/BrewMarket/lang/`. Available: `en_US`, `es_MX`, `es_ES`. You can create your own by copying an existing file and translating it.

### Prices

```yaml
prices:
  default: 10
  beer: 0.1
  wine: 150
  whiskey: 200
```

The final sell price is calculated as:

```
final_price = base_price * brewery:score
```

Where `brewery:score` ranges from `0.01` to `1.0` based on brew quality. If a recipe isn't listed, the `default` price is used. Set a price to `0` to make that brew unsellable.

### Layout

The GUI layout is defined as a grid of symbols, where each symbol maps to a slot type:

```yaml
layout:
  - "JKMLTLMKJ"
  - "IIIIIIIII"
  - "IIIIIIIII"
  - "IIIIIIIII"
  - "IIIIIIIII"
  - "JKMBCSMKJ"
```

Each character represents a slot type configured elsewhere in the config:

| Config Key | Description |
|---|---|
| `item-slot.symbol` | Slots where players place brews to sell |
| `sell-icons.symbol` | Sell button (sells only brews in the GUI) |
| `sell-all-icons.symbol` | Sell All button (sells GUI + player inventory) |
| `close-icon.symbol` | Closes the GUI |
| `title-icon.symbol` | Decorative title icon |
| `decorative-icons.*.symbol` | Decorative filler icons |

### Icons

All icons support Minecraft data components for full customization. Every icon requires a `material` field (e.g. `DIAMOND`, `POTION`, `PLAYER_HEAD`) and can use any combination of the components below.

| Component | Description |
|---|---|
| `custom_name` | Item display name |
| `lore` | Item lore lines |
| `profile` | Player head texture |
| `enchantments` | Enchantments map |
| `custom_model_data` | Custom model data |
| `item_model` | Item model reference |
| `potion_contents` | Potion color |
| `tooltip_display` | Tooltip visibility |
| `tooltip_style` | Custom tooltip style |
| `enchantment_glint_override` | Force or hide enchant glint |

#### `custom_name`

Sets the item's display name. Supports [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

```yaml
custom_name: '<gradient:#C173FF:#950DFF><b>Sell drink</b></gradient>'
```

#### `lore`

List of lines shown below the item name. Each line supports MiniMessage formatting.

```yaml
lore:
  - ''
  - ' <white>Line one</white> '
  - ' <white>Price: <#C173FF>{money}</#C173FF></white> '
  - ''
```

#### `profile`

Sets a player head texture. Automatically converts the material to `PLAYER_HEAD`. Two modes:

**By player name:**
```yaml
profile: 'Steve'
```

**By base64 texture:**
```yaml
profile:
  textures: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L...'
```

You can get texture values from sites like [minecraft-heads.com](https://minecraft-heads.com).

#### `enchantments`

Map of enchantment IDs to levels. Uses Minecraft registry names (lowercase).

```yaml
enchantments:
  sharpness: 5
  unbreaking: 3
```

#### `custom_model_data`

Custom model data with four sub-fields. All are optional lists.

```yaml
custom_model_data:
  floats: [1.0, 2.5]
  flags: [true, false]
  strings: ['variant_a']
  colors: [16711680]
```

#### `item_model`

Sets a custom item model using a namespaced key.

```yaml
item_model: 'myplugin:custom_sword'
```

#### `potion_contents`

Customizes the potion bottle color. Only works with `POTION`, `SPLASH_POTION`, `LINGERING_POTION`, or `TIPPED_ARROW` materials.

```yaml
material: 'POTION'
potion_contents:
  custom_color: 9768447
```

The color value is an RGB integer. You can convert hex to decimal (e.g. `#FF0000` = `16711680`).

#### `tooltip_display`

Controls what parts of the tooltip are visible.

- `hide_tooltip` — Hides the entire tooltip when hovering.
- `hidden_components` — List of specific data components to hide from the tooltip.

```yaml
tooltip_display:
  hide_tooltip: false
  hidden_components:
    - 'potion_contents'
    - 'enchantments'
```

#### `tooltip_style`

Sets a custom tooltip style using a namespaced key (resource pack required).

```yaml
tooltip_style: 'myplugin:dark_tooltip'
```

#### `enchantment_glint_override`

Forces the enchantment glint animation on or off, regardless of actual enchantments.

```yaml
enchantment_glint_override: true
```

#### Full example

```yaml
close-icon:
  symbol: 'C'
  material: 'PLAYER_HEAD'
  profile:
    textures: 'eyJ0ZXh0dXJlcyI6ey...'
  custom_name: '<gradient:#C173FF:#950DFF><b>Close</b></gradient>'
  enchantment_glint_override: true
  tooltip_display:
    hidden_components:
      - 'enchantments'
```

### Sell Button Actions

Sell buttons support `allow-icon` (shown when there are brews to sell) and `deny-icon` (shown when empty). Each can have an action block with sound and message:

```yaml
sell-icons:
  symbol: 'B'
  allow-icon:
    material: 'POTION'
    potion_contents:
      custom_color: 9768447
    tooltip_display:
      hidden_components:
        - 'potion_contents'
    custom_name: '<gradient:#C173FF:#950DFF>● <b>Sell drink</b></gradient>'
    lore:
      - ' <white>Sell all brews for <#C173FF>{money}</#C173FF> coins.</white> '
    action:
      sound:
        type: 'sound'
        value: 'minecraft:block.amethyst_block.place'
      message:
        type: 'message'
        value: '<green>You sold <gold>{sold_amount}</gold> brews for <gold>{money}</gold>!</green>'
  deny-icon:
    material: 'GLASS_BOTTLE'
    custom_name: '<gradient:#FF7373:#FF0D0D><b>Nothing to sell!</b></gradient>'
```

Available placeholders for sell icons:
- `{money}` - Formatted currency amount
- `{sold_amount}` - Number of brews sold

### Language Files

Language files are stored in `plugins/BrewMarket/lang/` and use MiniMessage formatting. To create a custom translation:

1. Copy `en_US.yml` to your desired filename (e.g., `fr_FR.yml`).
2. Translate the values (keep the keys unchanged).
3. Set `lang: 'fr_FR'` in `config.yml`.
4. Reload with `/brewmarket reload`.

Available placeholders in language files:
- `{prefix}` - Plugin prefix defined at the top of the lang file
- `{money}` - Formatted currency amount
- `{sold_amount}` - Number of brews sold
