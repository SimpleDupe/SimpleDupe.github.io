package com.skitmc.simpledupe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

public final class SimpleDupe extends JavaPlugin implements CommandExecutor, Listener {

    private ConfigManager configManager;

    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = "";
    private volatile String downloadUrl = "";

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.setupFiles();

        if (this.getCommand("dupe") != null) this.getCommand("dupe").setExecutor(this);
        if (this.getCommand("simpledupereload") != null) this.getCommand("simpledupereload").setExecutor(this);

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        broadcastCredits();
        sendConsoleBanner();
    }

    private void checkForUpdates() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/SimpleDupe/SimpleDupe.github.io/releases/latest"))
                    .header("User-Agent", "SimpleDupe-UpdateChecker")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                sendConsoleStatus(Component.text("GitHub update check returned HTTP " + response.statusCode() + ".", NamedTextColor.YELLOW));
                return;
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!json.has("tag_name") || !json.has("html_url")) {
                sendConsoleStatus(Component.text("GitHub returned incomplete release data.", NamedTextColor.YELLOW));
                return;
            }

            latestVersion = json.get("tag_name").getAsString().replaceFirst("(?i)^v", "");
            String currentVersion = getPluginMeta().getVersion().replaceFirst("(?i)^v", "");
            updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);
            downloadUrl = json.get("html_url").getAsString();

            if (updateAvailable) {
                sendConsoleStatus(Component.text("Update v" + latestVersion + " is available.", NamedTextColor.AQUA));
                getServer().getScheduler().runTask(this, () -> getServer().getOnlinePlayers().stream()
                        .filter(Player::isOp)
                        .forEach(this::sendUpdateNotification));
            } else {
                sendConsoleStatus(Component.text("Running the latest release (v" + currentVersion + ").", NamedTextColor.GREEN));
            }
        } catch (Exception e) {
            sendConsoleStatus(Component.text("Unable to check for updates: " + e.getMessage(), NamedTextColor.YELLOW));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() && updateAvailable) {
            sendUpdateNotification(player);
        }
    }

    private void sendUpdateNotification(Player player) {
        Component line1 = deserialize(configManager.getRawMessage("update-line-1"));
        Component line2 = deserialize(configManager.getRawMessage("update-line-2").replace("%version%", latestVersion));
        Component line3 = deserialize(configManager.getRawMessage("update-line-3"))
                .clickEvent(ClickEvent.openUrl(downloadUrl));
        Component line4 = deserialize(configManager.getRawMessage("update-line-4"));

        Component updateMessage = Component.empty()
                .append(Component.newline())
                .append(line1).append(Component.newline())
                .append(line2).append(Component.newline())
                .append(line3).append(Component.newline())
                .append(line4).append(Component.newline());

        player.sendMessage(updateMessage);
    }

    private void broadcastCredits() {
        Component line1 = deserialize(configManager.getRawMessage("credit-line-1"));
        Component line2 = deserialize(configManager.getRawMessage("credit-line-2"));
        Component line3 = deserialize(configManager.getRawMessage("credit-line-3"))
                .clickEvent(ClickEvent.openUrl("https://github.com/SimpleDupe/SimpleDupe.github.io"));
        Component line4 = deserialize(configManager.getRawMessage("credit-line-4"));

        Component creditBanner = Component.empty()
                .append(Component.newline())
                .append(line1).append(Component.newline())
                .append(line2).append(Component.newline())
                .append(line3).append(Component.newline())
                .append(line4).append(Component.newline());

        getServer().broadcast(creditBanner);
    }

    private Component deserialize(String text) {
        return configManager.deserialize(text);
    }

    private void sendConsoleBanner() {
        CommandSender console = getServer().getConsoleSender();
        Component divider = Component.text("----------------------------------------", NamedTextColor.DARK_AQUA);

        console.sendMessage(divider);
        console.sendMessage(Component.text("  SIMPLEDUPE ", NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
                .append(Component.text("v" + getPluginMeta().getVersion(), NamedTextColor.GRAY)));
        console.sendMessage(Component.text("  Paper plugin | Configurable item duplication", NamedTextColor.GRAY));
        console.sendMessage(Component.text("  github.com/SimpleDupe/SimpleDupe.github.io", NamedTextColor.DARK_AQUA));
        console.sendMessage(divider);
    }

    private void sendConsoleStatus(Component message) {
        Component prefix = Component.text("[SimpleDupe] ", NamedTextColor.AQUA).decorate(TextDecoration.BOLD);
        getServer().getScheduler().runTask(this, () ->
                getServer().getConsoleSender().sendMessage(prefix.append(message)));
    }

    private int getMaxDupe(CommandSender sender) {
        int maxDupe = configManager.getMaxDupe();
        String permissionPrefix = "simpledupe.dupe.";

        for (PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
            if (!permission.getValue()) {
                continue;
            }

            String node = permission.getPermission().toLowerCase(Locale.ROOT);
            if (!node.startsWith(permissionPrefix)) {
                continue;
            }

            try {
                maxDupe = Math.max(maxDupe, Integer.parseInt(node.substring(permissionPrefix.length())));
            } catch (NumberFormatException ignored) {
                // Ignore wildcard or otherwise non-numeric permission nodes.
            }
        }

        return maxDupe;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simpledupereload")) {
            if (!sender.hasPermission("simpledupe.admin")) {
                sender.sendMessage(configManager.getMessage("no-permission"));
                return true;
            }

            configManager.setupFiles();
            sender.sendMessage(configManager.getMessage("reload-success"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("dupe")) {
            if (!sender.hasPermission("simpledupe.use")) {
                sender.sendMessage(configManager.getMessage("no-permission"));
                return true;
            }

            if (args.length > 1) {
                sender.sendMessage(configManager.getMessage("invalid-usage"));
                return true;
            }

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

            if (configManager.isBlacklisted(itemInHand)) {
                player.sendMessage(configManager.getMessage("blacklisted"));
                return true;
            }

            int amount = 1;
            int maxLimit = getMaxDupe(player);

            if (args.length > 0) {
                try {
                    amount = Integer.parseInt(args[0]);
                    if (amount <= 0) {
                        String rawMsg = configManager.getRawMessage("invalid-number").replace("%max%", String.valueOf(maxLimit));
                        player.sendMessage(deserialize(rawMsg));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    String rawMsg = configManager.getRawMessage("invalid-number").replace("%max%", String.valueOf(maxLimit));
                    player.sendMessage(deserialize(rawMsg));
                    return true;
                }

            }

            if (amount > maxLimit) {
                String rawMsg = configManager.getRawMessage("exceeds-max").replace("%max%", String.valueOf(maxLimit));
                player.sendMessage(deserialize(rawMsg));
                return true;
            }

            for (int i = 0; i < amount; i++) {
                ItemStack duplicatedItem = itemInHand.clone();
                player.getInventory().addItem(duplicatedItem).values().forEach(remainingItem -> 
                    player.getWorld().dropItemNaturally(player.getLocation(), remainingItem)
                );
            }

            String rawMsg = configManager.getRawMessage("success").replace("%amount%", String.valueOf(amount));
            player.sendMessage(deserialize(rawMsg));
            return true;
        }
        return false;
    }
}