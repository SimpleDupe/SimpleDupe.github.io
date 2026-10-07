<p align="center">
  <img src="assets/simpledupe-logo.png" alt="SimpleDupe" width="480">
</p>

<p align="center">
  <a href="https://github.com/skitmc/SimpleDupe/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest%20Release-00bfc7?style=for-the-badge&logo=github&logoColor=white" alt="Download the latest release">
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
max-dupe-amount: 5

blacklist:
  materials: [BEDROCK, BARRIER, COMMAND_BLOCK]
  names: []
  enchantments:
    - "SHARPNESS:6"
  lore:
    - "&ctest"
```

`KEY:LEVEL` blocks that enchantment at the specified level and above. Names and lore match complete values; `&` color codes are supported.

Requires Paper 1.21.1 and Java 21.