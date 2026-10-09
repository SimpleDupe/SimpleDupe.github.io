<p align="center">
  <img src="assets/simpledupe-logo.png" alt="SimpleDupe" width="480">
</p>

<p align="center">
  <a href="https://github.com/SimpleDupe/SimpleDupe.github.io/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest%20Release-00bfc7?style=for-the-badge&logo=github&logoColor=white" alt="Download the latest release">
  </a>
  <a href="https://github.com/SimpleDupe/SimpleDupe.github.io/releases/latest">
    <img src="https://img.shields.io/github/downloads/SimpleDupe/SimpleDupe.github.io/latest/total?style=for-the-badge&label=Latest%20release%20downloads" alt="Downloads of the latest release">
  </a>
</p>

Paper plugin for duplicating the item in your main hand, with configurable limits and blacklists.

## Install

Download the [latest release](https://github.com/SimpleDupe/SimpleDupe.github.io/releases/latest) and place the JAR in your server's `plugins` folder.

## Commands

| Command | Permission |
| --- | --- |
| `/dupe [amount]` | `simpledupe.use` |
| `/simpledupereload` | `simpledupe.admin` |
| `/simpledupeguide` | `simpledupe.guide` (operators) |

`/dupereload` is an alias for `/simpledupereload`.

## Custom Dupe Limits

Grant `simpledupe.dupe.<number>` to raise a player's maximum above the configured limit. For example, `simpledupe.dupe.8` allows `/dupe` amounts up to 8. The highest numeric limit granted to a player is used; everyone else follows `max-dupe-amount`.

## Blacklist

Edit `plugins/SimpleDupe/config.yml`:

```yaml
# SimpleDupe configuration
#
# Maximum number of items a player can duplicate at once.
max-dupe-amount: 5
drop-overflow-items: false

# Items matching any entry below cannot be duplicated.
blacklist:
  materials:
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - JIGSAW
  names: []
  # Use an enchantment key, or KEY:LEVEL to block that level and higher.
  enchantments: []
  lore: []
```

Set `drop-overflow-items` to `true` to drop duplicated items that do not fit; the default `false` keeps them off the ground and notifies the player. Add material names, custom names, enchantments, or lore entries to their respective lists. `KEY:LEVEL` blocks an enchantment at the specified level and above. Prefixes, messages, custom names, and lore support legacy colors (`&c` or `§c`), hex colors (`&#42e8e0`), and MiniMessage tags such as `<aqua>text</aqua>`.

Requires Paper 1.21.1 and Java 21.