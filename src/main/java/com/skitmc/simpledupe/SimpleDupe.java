package com.skitmc.simpledupe;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public final class SimpleDupe extends JavaPlugin implements CommandExecutor {

    private List<String> blacklist;
    private int maxDupeAmount;
    
    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // 1. Create data folder if missing
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // 2. Safe config.yml initialization
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResource("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    saveDefaultConfig(); // Fallback
                }
            } catch (IOException e) {
                getLogger().warning("Could not generate config.yml automatically.");
            }
        }
        reloadConfig();

        // 3. Safe messages.yml initialization
        createMessagesConfig();
        
        // 4. Load memory variables
        loadPluginData();

        // 5. Register commands
        if (this.getCommand("dupe") != null) this.getCommand("dupe").setExecutor(this);
        if (this.getCommand("simpledupereload") != null) this.getCommand("simpledupereload").setExecutor(this);
    }

    private void loadPluginData() {
        this.blacklist = getConfig().getStringList("blacklist");
        this.maxDupeAmount = getConfig().getInt("max-dupe-amount", 5);
        
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void createMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try (InputStream in = getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    try { messagesFile.createNewFile(); } catch (IOException ignored) {}
                }
            } catch (IOException e) {
                getLogger().warning("Could not generate messages.yml automatically.");
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String path) {
        String msg = messagesConfig.getString(path, "");
        String prefix = messagesConfig.getString("prefix", "");
        msg = msg.replace("%prefix%", prefix);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simpledupereload")) {
            reloadConfig();
            loadPluginData();
            sender.sendMessage(getMessage("reload-success"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("dupe")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(getMessage("only-players"));
                return true;
            }

            Player player = (Player) sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                player.sendMessage(getMessage("no-item"));
                return true;
            }

            String itemType = itemInHand.getType().name();
            if (blacklist.contains(itemType)) {
                player.sendMessage(getMessage("blacklisted"));
                return true;
            }

            int amount = 1;

            if (args.length > 0) {
                try {
                    amount = Integer.parseInt(args[0]);
                    if (amount <= 0) {
                        player.sendMessage(getMessage("invalid-number").replace("%max%", String.valueOf(maxDupeAmount)));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(getMessage("invalid-number").replace("%max%", String.valueOf(maxDupeAmount)));
                    return true;
                }

                if (amount > maxDupeAmount) {
                    player.sendMessage(getMessage("exceeds-max").replace("%max%", String.valueOf(maxDupeAmount)));
                    return true;
                }
            }

            for (int i = 0; i < amount; i++) {
                ItemStack duplicatedItem = itemInHand.clone();
                player.getInventory().addItem(duplicatedItem).values().forEach(remainingItem -> 
                    player.getWorld().dropItemNaturally(player.getLocation(), remainingItem)
                );
            }

            player.sendMessage(getMessage("success").replace("%amount%", String.valueOf(amount)));
            return true;
        }
        return false;
    }
}