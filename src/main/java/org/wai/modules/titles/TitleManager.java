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

        // Добавлено: Инициализация настроек
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
        return settingsConfig.getBoolean("players." + player.getUniqueId() + ".allow-trade", true);
    }

    public void toggleTradeSetting(Player player) {
        boolean newState = !canTrade(player);
        settingsConfig.set("players." + player.getUniqueId() + ".allow-trade", newState);
        saveSettings();

        // Удаляем все активные запросы с участием этого игрока
        cleanupRequestsForPlayer(player);
    }

    private void cleanupRequestsForPlayer(Player player) {
        Iterator<Map.Entry<Player, TradeRequest>> iterator = tradeRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            TradeRequest request = iterator.next().getValue();
            if (request.getSender().equals(player) || request.getTarget().equals(player)) {
                iterator.remove();
                if (request.getSender().isOnline()) {
                    request.getSender().sendMessage(ChatColor.RED + "Обмен отменен: настройки изменены");
                }
            }
        }
    }

    private void saveSettings() {
        try {
            settingsConfig.save(settingsFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    // Добавлено: Задача для очистки устаревших запросов
    private void startRequestCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredRequests();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Проверка каждую минуту
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
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "lp user " + player.getName() + " meta removesuffix"));
        plugin.getConfig().set("players." + player.getUniqueId(), null);
        plugin.saveConfig();
    }

    public void sendTradeRequest(Player sender, Player target) {
        if (!canTrade(sender)) {
            sender.sendMessage(ChatColor.RED + "Вы отключили обмен титулами!");
            return;
        }
        if (!canTrade(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + " отключил обмен титулами!");
            return;
        }

        tradeRequests.put(target, new TradeRequest(sender, target));
        sendRequestMessage(sender, target);
    }

    private void sendRequestMessage(Player sender, Player target) {
        // Создаем основное сообщение
        TextComponent message = new TextComponent(
                ChatColor.GOLD + "➜ " +
                        ChatColor.YELLOW + sender.getName() +
                        ChatColor.GOLD + " предлагает обмен титулами\n" // \n для новой строки
        );

        // Создаем контейнер для кнопок
        TextComponent buttons = new TextComponent();
        buttons.addExtra(createAcceptButton());
        buttons.addExtra(" "); // Пробел между кнопками
        buttons.addExtra(createDeclineButton());

        // Добавляем кнопки к сообщению
        message.addExtra(buttons);

        // Отправляем сообщение
        target.spigot().sendMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Запрос отправлен игроку " + target.getName());
    }

    private TextComponent createAcceptButton() {
        TextComponent button = new TextComponent(ChatColor.GREEN + "[✅ Принять]");
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles accept"));
        button.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text("Принять обмен")
        ));
        return button;
    }

    private TextComponent createDeclineButton() {
        TextComponent button = new TextComponent(ChatColor.RED + "[❌ Отклонить]");
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles decline"));
        button.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text("Отклонить запрос")
        ));
        return button;
    }

    public boolean acceptTradeRequest(Player target) {
        TradeRequest request = tradeRequests.get(target);
        if (request == null) {
            target.sendMessage(ChatColor.RED + "Нет активных запросов");
            return false;
        }

        Player sender = request.getSender();
        if (!sender.isOnline() || !target.isOnline()) {
            target.sendMessage(ChatColor.RED + "Один из игроков покинул сервер");
            tradeRequests.remove(target);
            return false;
        }

        // Получаем текущие титулы
        String senderTitleId = getPlayerTitle(sender);
        String targetTitleId = getPlayerTitle(target);

        // Проверяем, что титулы существуют
        if (senderTitleId == null || targetTitleId == null) {
            target.sendMessage(ChatColor.RED + "У одного из игроков отсутствует титул");
            return false;
        }

        // Проверяем права на новые титулы
        if (!sender.hasPermission("titles.title." + targetTitleId) || !target.hasPermission("titles.title." + senderTitleId)) {
            target.sendMessage(ChatColor.RED + "Нет прав на обмен титулами");
            return false;
        }

        // Обмен титулами
        boolean senderResult = setPlayerTitle(sender, targetTitleId);
        boolean targetResult = setPlayerTitle(target, senderTitleId);

        if (senderResult && targetResult) {
            // Уведомления
            sender.sendMessage(ChatColor.GREEN + "✔ Вы получили титул: " + getTitleDisplayName(targetTitleId));
            target.sendMessage(ChatColor.GREEN + "✔ Вы получили титул: " + getTitleDisplayName(senderTitleId));
            tradeRequests.remove(target);
            return true;
        } else {
            target.sendMessage(ChatColor.RED + "Ошибка при обмене");
            return false;
        }
    }

    // Метод для получения отображаемого имени титула
    private String getTitleDisplayName(String titleId) {
        return titlesConfig.getString("titles." + titleId + ".suffix", titleId);
    }

    private boolean validatePlayersOnline(TradeRequest request) {
        return request.getSender().isOnline() && request.getTarget().isOnline();
    }

    private boolean processTrade(TradeRequest request) {
        Player sender = request.getSender();
        Player target = request.getTarget();

        String senderTitle = getPlayerTitle(sender);
        String targetTitle = getPlayerTitle(target);

        if (senderTitle == null || targetTitle == null) return false;

        swapTitles(sender, target, senderTitle, targetTitle);
        notifyPlayers(sender, target);
        tradeRequests.remove(target);
        return true;
    }

    private void swapTitles(Player sender, Player target, String senderTitle, String targetTitle) {
        setPlayerTitle(sender, targetTitle);
        setPlayerTitle(target, senderTitle);
    }

    private void notifyPlayers(Player sender, Player target) {
        sender.sendMessage(ChatColor.GREEN + "✔ Вы обменялись титулами с " + target.getName());
        target.sendMessage(ChatColor.GREEN + "✔ Вы обменялись титулами с " + sender.getName());
    }

    public void declineTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request != null) {
            request.getSender().sendMessage(ChatColor.GOLD + "✖ " + target.getName() + " отклонил обмен");
        }
    }

    // Добавлено: Очистка устаревших запросов
    public void cleanupExpiredRequests() {
        Iterator<Map.Entry<Player, TradeRequest>> iterator = tradeRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, TradeRequest> entry = iterator.next();
            if (!entry.getKey().isOnline() || !entry.getValue().getSender().isOnline()) {
                iterator.remove();
            }
        }
    }

    private void setLuckPermsSuffix(Player player, String suffix, int priority) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        String.format("lp user %s meta setsuffix %d \"%s\"",
                                player.getName(), priority, suffix.replace("\"", "\\\""))));
    }

    public void savePlayerData(Player player) {
        plugin.getConfig().set("players." + player.getUniqueId(), getPlayerTitle(player));
        plugin.saveConfig();
    }

    private static class TradeRequest {
        private final Player sender;
        private final Player target;

        public TradeRequest(Player sender, Player target) {
            this.sender = sender;
            this.target = target;
        }

        public Player getSender() { return sender; }
        public Player getTarget() { return target; }
    }
}