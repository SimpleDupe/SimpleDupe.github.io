package com.skitmc.simpledupe;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> getBlacklist() {
        List<String> list = plugin.getConfig().getStringList("blacklist");
        if (list.isEmpty()) {
            list = new ArrayList<>();
            list.add("BEDROCK");
            list.add("BARRIER");
            list.add("COMMAND_BLOCK");
        }
        return list;
    }

    public int getMaxDupe() {
        return plugin.getConfig().getInt("max-dupe-amount", 5);
    }

    public String getMessage(String path) {
        String msg = messagesConfig.getString(path, "");
        String prefix = messagesConfig.getString("prefix", "&7[&bSimpleDupe&7] ");
        
        if (msg.isEmpty()) {
            return ChatColor.RED + "Missing configuration string entry: " + path;
        }
        
        msg = msg.replace("%prefix%", prefix);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}