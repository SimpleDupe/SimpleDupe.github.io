<p align="center">
  <img src="assets/simpledupe-logo.png" alt="SimpleDupe" width="480">
</p>

<p align="center">
  <a href="https://github.com/skitmc/SimpleDupe/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest%20Release-00bfc7?style=for-the-badge&logo=github&logoColor=white" alt="Download the latest release">
  </a>
  <a href="https://github.com/skitmc/SimpleDupe/releases/latest">
    <img src="https://img.shields.io/github/downloads/skitmc/SimpleDupe/latest/total?style=for-the-badge&label=Latest%20release%20downloads" alt="Downloads of the latest release">
  </a>
</p>

Paper plugin for duplicating the item in your main hand, with configurable limits and blacklists.

## Install

Download the [latest release](https://github.com/skitmc/SimpleDupe/releases/latest) and place the JAR in your server's `plugins` folder.

## Commands

| Command | Permission |
| --- | --- |
| `/dupe [amount]` | `simpledupe.use` |
| `/simpledupereload` | `simpledupe.admin` |

`/dupereload` is an alias for `/simpledupereload`.

## Blacklist

Edit `plugins/SimpleDupe/config.yml`:

```yaml
# SimpleDupe configuration
#
# Maximum number of items a player can duplicate at once.
max-dupe-amount: 5

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

Add material names, custom names, enchantments, or lore entries to their respective lists. `KEY:LEVEL` blocks an enchantment at the specified level and above; `&` color codes are supported in names and lore.

Requires Paper 1.21.1 and Java 21.