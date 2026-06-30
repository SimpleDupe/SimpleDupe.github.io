package com.skitmc.simpledupe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleDupe extends JavaPlugin implements CommandExecutor {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.setupFiles();

        if (this.getCommand("dupe") != null) this.getCommand("dupe").setExecutor(this);
        if (this.getCommand("simpledupereload") != null) this.getCommand("simpledupereload").setExecutor(this);

        broadcastCredits();
    }

    private void broadcastCredits() {
        Component lines = Component.text("\n")
            .append(Component.text("Using SimpleDupe Created by ", NamedTextColor.GRAY))
            .append(Component.text("SkitMC ", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("Known as ", NamedTextColor.GRAY))
            .append(Component.text("Skitxoe", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .append(Component.text("!\n", NamedTextColor.GRAY))
            .append(Component.text("Click here to view GitHub project profile", NamedTextColor.YELLOW, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://github.com/skitmc/")))
            .append(Component.text("\n"));

        getServer().broadcast(lines);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simpledupereload")) {
            configManager.setupFiles();
            sender.sendMessage(configManager.getMessage("reload-success"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("dupe")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getMessage("only-players"));
                return true;
            }

            Player player = (Player) sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                player.sendMessage(configManager.getMessage("no-item"));
                return true;
            }

            if (configManager.getBlacklist().contains(itemInHand.getType().name())) {
                player.sendMessage(configManager.getMessage("blacklisted"));
                return true;
            }

            int amount = 1;
            int maxLimit = configManager.getMaxDupe();

            if (args.length > 0) {
                try {
                    amount = Integer.parseInt(args[0]);
                    if (amount <= 0) {
                        player.sendMessage(configManager.getMessage("invalid-number").replace("%max%", String.valueOf(maxLimit)));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(configManager.getMessage("invalid-number").replace("%max%", String.valueOf(maxLimit)));
                    return true;
                }

                if (amount > maxLimit) {
                    player.sendMessage(configManager.getMessage("exceeds-max").replace("%max%", String.valueOf(maxLimit)));
                    return true;
                }
            }

            for (int i = 0; i < amount; i++) {
                ItemStack duplicatedItem = itemInHand.clone();
                player.getInventory().addItem(duplicatedItem).values().forEach(remainingItem -> 
                    player.getWorld().dropItemNaturally(player.getLocation(), remainingItem)
                );
            }

            player.sendMessage(configManager.getMessage("success").replace("%amount%", String.valueOf(amount)));
            return true;
        }
        return false;
    }
}