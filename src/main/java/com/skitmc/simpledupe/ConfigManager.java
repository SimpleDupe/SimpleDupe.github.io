package com.skitmc.simpledupe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class ConfigManager {

    private final SimpleDupe plugin;
    private File configFile;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ConfigManager(SimpleDupe plugin) {
        this.plugin = plugin;
    }

    public void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        // Extracts files from src/main/resources into the plugin folder if missing
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Wipe runtime memory cache and pull fresh disk configurations
        plugin.reloadConfig();
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public boolean isBlacklisted(ItemStack item) {
        FileConfiguration config = plugin.getConfig();
        List<String> materials = config.getStringList("blacklist.materials");

        // Older versions stored the material list directly under "blacklist".
        if (config.isList("blacklist")) {
            materials = config.getStringList("blacklist");
        }

        if (materials.stream().anyMatch(material -> material.trim().equalsIgnoreCase(item.getType().name()))) {
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (meta.hasDisplayName() && matchesText(config.getStringList("blacklist.names"), meta.getDisplayName())) {
            return true;
        }

        if (meta.hasLore() && meta.getLore().stream()
                .anyMatch(line -> matchesText(config.getStringList("blacklist.lore"), line))) {
            return true;
        }

        return meta.getEnchants().entrySet().stream().anyMatch(entry ->
                config.getStringList("blacklist.enchantments").stream().anyMatch(value ->
                        matchesEnchantment(value, entry.getKey(), entry.getValue())));
    }

    private boolean matchesEnchantment(String rule, Enchantment enchantment, int level) {
        String enchantmentName = rule.trim();
        int minimumLevel = 1;
        int separator = enchantmentName.lastIndexOf(':');

        if (separator >= 0) {
            try {
                minimumLevel = Integer.parseInt(enchantmentName.substring(separator + 1).trim());
                enchantmentName = enchantmentName.substring(0, separator).trim();
            } catch (NumberFormatException ignored) {
                // A namespace separator is not a level unless its suffix is numeric.
            }
        }

        return minimumLevel > 0
                && level >= minimumLevel
                && normalizeEnchantment(enchantmentName).equals(normalizeEnchantment(enchantment.getKey().toString()));
    }

    private boolean matchesText(List<String> blockedValues, String itemValue) {
        String normalizedItemValue = normalizeText(itemValue);
        return blockedValues.stream().anyMatch(value -> normalizeText(value).equals(normalizedItemValue));
    }

    private String normalizeText(String value) {
        return ChatColor.translateAlternateColorCodes('&', value).toLowerCase(Locale.ROOT);
    }

    private String normalizeEnchantment(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("minecraft:")) {
            return normalized.substring("minecraft:".length());
        }
        return normalized;
    }

    public int getMaxDupe() {
        return Math.max(1, plugin.getConfig().getInt("max-dupe-amount", 5));
    }

    /**
     * Gets the raw String from messages.yml with %prefix% replaced.
     */
    public String getRawMessage(String path) {
        String msg = messagesConfig.getString(path, "");
        String prefix = messagesConfig.getString("prefix", "&8[&bSimpleDupe&8] ");

        if (msg.isEmpty()) {
            return "&cMissing configuration string entry: " + path;
        }

        return msg.replace("%prefix%", prefix);
    }

    /**
     * Gets a Legacy formatted String with color codes translated ('&' -> '§').
     */
    public String getMessageString(String path) {
        return ChatColor.translateAlternateColorCodes('&', getRawMessage(path));
    }

    /**
     * Gets an Adventure Component for direct player messaging with color codes applied.
     */
    public Component getMessage(String path) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(getRawMessage(path));
    }
}