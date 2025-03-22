package org.wai.modules.titles;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

public class TitleManager {
    private final JavaPlugin plugin;
    private final YamlConfiguration titlesConfig;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>();

    public TitleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesConfig = loadTitlesConfig();
    }

    private YamlConfiguration loadTitlesConfig() {
        File file = new File(plugin.getDataFolder(), "titles.yml");
        if (!file.exists()) plugin.saveResource("titles.yml", false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public Set<String> getAvailableTitles() {
        return titlesConfig.getConfigurationSection("titles").getKeys(false);
    }

    public String getPlayerTitle(Player player) {
        return plugin.getConfig().getString("players." + player.getUniqueId());
    }

    public boolean setPlayerTitle(Player player, String titleId) {
        if (!titlesConfig.contains("titles." + titleId)) return false;
        if (!player.hasPermission("titles.title." + titleId)) return false;
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
        String suffix = titlesConfig.getString("titles." + titleId + ".suffix");
        String symbol = titlesConfig.getString("titles." + titleId + ".symbol", " ");
        int priority = titlesConfig.getInt("titles." + titleId + ".priority", 100);
        setLuckPermsSuffix(player, symbol + suffix, priority);
    }

    public void removeTitle(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removesuffix"));
        plugin.getConfig().set("players." + player.getUniqueId(), null);
        plugin.saveConfig();
    }

    public void sendTradeRequest(Player sender, Player target) {
        tradeRequests.put(target, new TradeRequest(sender, target));
        TextComponent message = new TextComponent(ChatColor.GOLD + "➜ " + ChatColor.YELLOW + sender.getName() + ChatColor.GOLD + " предлагает обмен титулами ");
        TextComponent acceptButton = new TextComponent(ChatColor.GREEN + "[✅ Принять]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles accept"));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aПринять обмен")));
        TextComponent declineButton = new TextComponent(ChatColor.RED + "[❌ Отклонить]");
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles decline"));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cОтклонить запрос")));
        message.addExtra(acceptButton);
        message.addExtra(declineButton);
        target.spigot().sendMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Запрос на обмен отправлен " + target.getName() + "!");
    }

    public boolean acceptTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request == null) {
            target.sendMessage(ChatColor.RED + "Нет активных запросов на обмен!");
            return false;
        }
        Player sender = request.getSender();
        if (!sender.isOnline()) {
            target.sendMessage(ChatColor.RED + "Игрок " + sender.getName() + " вышел из сети!");
            return false;
        }
        if (tradeTitles(sender, target)) {
            sender.sendMessage(ChatColor.GREEN + "✔ " + target.getName() + " принял ваш запрос на обмен!");
            target.sendMessage(ChatColor.GREEN + "✔ Вы успешно обменялись титулами с " + sender.getName() + "!");
            return true;
        }
        return false;
    }

    public void declineTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request != null) {
            request.getSender().sendMessage(ChatColor.GOLD + "✖ " + target.getName() + " отклонил ваш запрос на обмен");
            target.sendMessage(ChatColor.YELLOW + "Вы отклонили запрос на обмен от " + request.getSender().getName());
        }
    }

    private void setLuckPermsSuffix(Player player, String suffix, int priority) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("lp user %s meta setsuffix %d \"%s\"", player.getName(), priority, suffix.replace("\"", "\\\""))));
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