# SimpleDupe 💎

A lightweight, high-performance, and fully customizable duplication plugin for Bukkit/Spigot/Paper Minecraft servers. Created by **SkitMC**.

---

## 🚀 Features

* **Customizable Limits:** Set a maximum duplication cap via config (e.g., limit users to multiplying 1–5 items at a time).
* **Robust Blacklist:** Restrict specific material types (like Bedrock, Barriers, or Shulker Boxes) from being duplicated.
* **Preserves Data (NBT):** Uses safe item cloning, ensuring custom names, complex lore, enchantments, and container contents (like Shulker items) are retained perfectly.
* **Smart Drop System:** If a player's inventory fills up during duplication, excess items are dropped safely at their feet instead of vanishing.
* **Full Translation & Prefix Support:** Every message and structural prefix is completely customizable with formatting/color codes via `messages.yml`.

---

## 🛠️ Commands & Permissions

| Command | Description | Default Permission |
| :--- | :--- | :--- |
| `/dupe [amount]` | Duplicates the item currently held in your main hand. | `simpledupe.use` |
| `/simpledupereload` | Reloads all configurations and localizations instantly. | `simpledupe.admin` |

* **Alias:** `/dupereload` can be used interchangeably with `/simpledupereload`.
