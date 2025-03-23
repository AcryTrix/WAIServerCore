package org.wai.modules.titles;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class TitleManager {
    private final JavaPlugin plugin;
    private final YamlConfiguration titlesConfig;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>();
    private final YamlConfiguration settingsConfig;
    private final File settingsFile;

    public TitleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesConfig = loadTitlesConfig();
        this.settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        this.settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        if (!settingsFile.exists()) {
            saveSettings();
        }
        startRequestCleanupTask();
    }

    public boolean canTrade(Player player) {
        return settingsConfig.getBoolean("players." + player.getUniqueId() + ".allow-trade", true);
    }

    public boolean canSendTradeRequest(Player player) {
        return canTrade(player);
    }

    public void toggleTradeSetting(Player player) {
        boolean newState = !canTrade(player);
        settingsConfig.set("players." + player.getUniqueId() + ".allow-trade", newState);
        saveSettings();
        cleanupRequestsForPlayer(player);
    }

    private void cleanupRequestsForPlayer(Player player) {
        tradeRequests.entrySet().removeIf(entry -> {
            TradeRequest request = entry.getValue();
            if (request.getSender().equals(player) || request.getTarget().equals(player)) {
                if (request.getSender().isOnline()) {
                    request.getSender().sendMessage("§cОбмен отменен из-за изменения настроек!");
                }
                return true;
            }
            return false;
        });
    }

    private void saveSettings() {
        try {
            settingsConfig.save(settingsFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    private void startRequestCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredRequests();
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
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
        if (!titlesConfig.contains("titles." + titleId) || !player.hasPermission("titles.title." + titleId)) {
            return false;
        }
        plugin.getConfig().set("players." + player.getUniqueId(), titleId);
        plugin.saveConfig();
        applyTitle(player);
        return true;
    }

    public void applyTitle(Player player) {
        String titleId = getPlayerTitle(player);
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removesuffix");
        plugin.getConfig().set("players." + player.getUniqueId(), null);
        plugin.saveConfig();
    }

    public void sendTradeRequest(Player sender, Player target) {
        if (!canTrade(sender)) {
            sender.sendMessage("§cВы отключили обмен титулами!");
            return;
        }
        if (!canTrade(target)) {
            sender.sendMessage("§c" + target.getName() + " отключил обмен титулами!");
            return;
        }

        tradeRequests.put(target, new TradeRequest(sender, target));
        sendRequestMessage(sender, target);
    }

    private void sendRequestMessage(Player sender, Player target) {
        TextComponent message = new TextComponent("§e" + sender.getName() + " предлагает обмен титулами!\n");
        TextComponent accept = new TextComponent("§a[Принять]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Принять обмен")));
        TextComponent decline = new TextComponent(" §c[Отклонить]");
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles decline"));
        decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Отклонить запрос")));
        message.addExtra(accept);
        message.addExtra(decline);
        target.spigot().sendMessage(message);
        sender.sendMessage("§aЗапрос на обмен отправлен " + target.getName() + "!");
    }

    public boolean acceptTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request == null || !request.getSender().isOnline() || !target.isOnline()) {
            target.sendMessage("§cЗапрос на обмен отсутствует или недействителен!");
            return false;
        }

        Player sender = request.getSender();
        String senderTitleId = getPlayerTitle(sender);
        String targetTitleId = getPlayerTitle(target);

        if (senderTitleId == null || targetTitleId == null) {
            target.sendMessage("§cУ одного из игроков нет титула!");
            return false;
        }

        if (!sender.hasPermission("titles.title." + targetTitleId) || !target.hasPermission("titles.title." + senderTitleId)) {
            target.sendMessage("§cНет прав на обмен этими титулами!");
            return false;
        }

        if (setPlayerTitle(sender, targetTitleId) && setPlayerTitle(target, senderTitleId)) {
            sender.sendMessage("§aВы получили титул: " + titlesConfig.getString("titles." + targetTitleId + ".suffix"));
            target.sendMessage("§aВы получили титул: " + titlesConfig.getString("titles." + senderTitleId + ".suffix"));
            return true;
        }
        target.sendMessage("§cОшибка при обмене титулами!");
        return false;
    }

    public void declineTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request != null && request.getSender().isOnline()) {
            request.getSender().sendMessage("§c" + target.getName() + " отклонил ваш запрос на обмен!");
            target.sendMessage("§aЗапрос отклонен!");
        }
    }

    private void cleanupExpiredRequests() {
        tradeRequests.entrySet().removeIf(entry -> !entry.getKey().isOnline() || !entry.getValue().getSender().isOnline());
    }

    private void setLuckPermsSuffix(Player player, String suffix, int priority) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                String.format("lp user %s meta setsuffix %d \"%s\"", player.getName(), priority, suffix.replace("\"", "\\\"")));
    }

    public void savePlayerData(Player player) {
        plugin.getConfig().set("players." + player.getUniqueId(), getPlayerTitle(player));
        plugin.saveConfig();
    }
}