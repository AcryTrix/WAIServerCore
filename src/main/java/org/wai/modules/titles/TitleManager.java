package org.wai.modules.titles;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TitleManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>();

    public TitleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = ((org.wai.WAIServerCore) plugin).getConfigManager();
    }

    public Set<String> getAvailableTitles() {
        return configManager.getTomlConfig().getTable("titles_menu.permissions").toMap().keySet();
    }

    public String getPlayerTitle(Player player) {
        return plugin.getConfig().getString("players." + player.getUniqueId());
    }

    public boolean setPlayerTitle(Player player, String titleId) {
        if (!configManager.getTomlConfig().contains("titles_menu.permissions." + titleId)) return false;
        String permission = configManager.getString("titles_menu.permissions." + titleId);
        if (!player.hasPermission(permission)) return false;
        plugin.getConfig().set("players." + player.getUniqueId(), titleId);
        plugin.saveConfig();
        applyTitle(player);
        return true;
    }

    public void applyTitle(Player player) {
        String titleId = plugin.getConfig().getString("players." + player.getUniqueId());
        if (titleId == null) {
            removeTitle(player);
            return;
        }
        String suffix = configManager.getString("titles_menu.item_name").replace("{title}", titleId);
        int priority = 100;
        setLuckPermsSuffix(player, suffix, priority);
    }

    public void removeTitle(Player player) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "lp user " + player.getName() + " meta removesuffix"));
        plugin.getConfig().set("players." + player.getUniqueId(), null);
        plugin.saveConfig();
    }

    public void sendTradeRequest(Player sender, Player target) {
        tradeRequests.put(target, new TradeRequest(sender, target));
        TextComponent message = new TextComponent(ChatColor.GOLD + "➜ " + ChatColor.YELLOW +
                sender.getName() + ChatColor.GOLD + " предлагает обмен титулами ");

        TextComponent acceptButton = new TextComponent(ChatColor.GREEN + "[✅ Принять]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles accept"));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new net.md_5.bungee.api.chat.ComponentBuilder("Принять обмен").color(
                        net.md_5.bungee.api.ChatColor.GREEN).create())));

        TextComponent declineButton = new TextComponent(ChatColor.RED + "[❌ Отклонить]");
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles decline"));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new net.md_5.bungee.api.chat.ComponentBuilder("Отклонить запрос").color(
                        net.md_5.bungee.api.ChatColor.RED).create())));

        message.addExtra(acceptButton);
        message.addExtra(declineButton);
        target.spigot().sendMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Запрос отправлен игроку " + target.getName());
    }

    public boolean acceptTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request == null) {
            target.sendMessage(ChatColor.RED + "Нет активных запросов");
            return false;
        }
        Player sender = request.getSender();
        if (!sender.isOnline()) {
            target.sendMessage(ChatColor.RED + "Игрок вышел");
            return false;
        }
        if (tradeTitles(sender, target)) {
            sender.sendMessage(ChatColor.GREEN + "✔ " + target.getName() + " принял ваш обмен");
            return true;
        }
        return false;
    }

    public void declineTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request != null) {
            request.getSender().sendMessage(ChatColor.GOLD + "✖ " + target.getName() + " отклонил обмен");
        }
    }

    private void setLuckPermsSuffix(Player player, String suffix, int priority) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        String.format("lp user %s meta setsuffix %d \"%s\"",
                                player.getName(), priority, suffix.replace("\"", "\\\""))));
    }

    public boolean tradeTitles(Player player1, Player player2) {
        String player1Title = getPlayerTitle(player1);
        String player2Title = getPlayerTitle(player2);
        if (player1Title == null || player2Title == null) return false;
        setPlayerTitle(player1, player2Title);
        setPlayerTitle(player2, player1Title);
        return true;
    }

    public void savePlayerData(Player player) {
        plugin.getConfig().set("players." + player.getUniqueId(), getPlayerTitle(player));
        plugin.saveConfig();
    }
}