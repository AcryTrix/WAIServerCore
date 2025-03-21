package org.wai.modules.titles;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TitleManager {
    private final JavaPlugin plugin;
    private final YamlConfiguration titlesConfig;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>(); // Список активных запросов

    // Конструктор
    public TitleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesConfig = loadTitlesConfig(); // Загружаем конфигурацию титулов
    }

    // Загрузка конфигурации титулов
    private YamlConfiguration loadTitlesConfig() {
        File file = new File(plugin.getDataFolder(), "titles.yml");
        if (!file.exists()) {
            plugin.saveResource("titles.yml", false); // Создаем файл, если его нет
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    // Получение списка доступных титулов
    public Set<String> getAvailableTitles() {
        return titlesConfig.getConfigurationSection("titles").getKeys(false);
    }

    // Получение титула игрока
    public String getPlayerTitle(Player player) {
        return plugin.getConfig().getString("players." + player.getUniqueId());
    }

    // Установка титула игроку (возвращает true, если титул успешно установлен)
    public boolean setPlayerTitle(Player player, String titleId) {
        // Проверяем, существует ли титул в конфигурации
        if (!titlesConfig.contains("titles." + titleId)) {
            return false; // Титул не найден
        }

        // Проверяем, есть ли у игрока право на этот титул
        if (!player.hasPermission("titles.title." + titleId)) {
            return false; // Нет прав
        }

        // Устанавливаем титул
        plugin.getConfig().set("players." + player.getUniqueId(), titleId);
        plugin.saveConfig();
        applyTitle(player); // Применяем новый титул

        return true; // Титул успешно установлен
    }

    // Применение титула
    public void applyTitle(Player player) {
        String titleId = plugin.getConfig().getString("players." + player.getUniqueId());
        if (titleId == null) {
            removeTitle(player); // Если у игрока нет титула, удаляем суффикс
            return;
        }

        // Получаем данные титула из конфига
        String suffix = titlesConfig.getString("titles." + titleId + ".suffix");
        String symbol = titlesConfig.getString("titles." + titleId + ".symbol", " ");
        int priority = titlesConfig.getInt("titles." + titleId + ".priority", 100);

        // Устанавливаем суффикс через LuckPerms
        setLuckPermsSuffix(player, symbol + suffix, priority);
    }

    // Удаление титула (теперь публичный метод)
    public void removeTitle(Player player) {
        String command = String.format("lp user %s meta removesuffix", player.getName());
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });

        // Удаляем титул из конфига
        plugin.getConfig().set("players." + player.getUniqueId(), null);
        plugin.saveConfig();
    }

    public void sendTradeRequest(Player sender, Player target) {
        TradeRequest request = new TradeRequest(sender, target);
        tradeRequests.put(target, request); // Сохраняем запрос для целевого игрока

        // Создаем сообщение с кнопками
        TextComponent message = new TextComponent(ChatColor.GOLD + "Игрок " + sender.getName() + " хочет обменяться с вами титулами. ");

        // Кнопка "Да"
        TextComponent acceptButton = new TextComponent(ChatColor.GREEN + "[ДА]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles accept"));

        // Кнопка "Нет"
        TextComponent declineButton = new TextComponent(ChatColor.RED + " [НЕТ]");
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradetitles decline"));

        // Добавляем кнопки к сообщению
        message.addExtra(acceptButton);
        message.addExtra(declineButton);

        // Отправляем сообщение целевому игроку
        target.spigot().sendMessage(message);
    }

    // Подтверждение запроса
    public boolean acceptTradeRequest(Player target) {
        TradeRequest request = tradeRequests.get(target);
        if (request == null) {
            target.sendMessage(ChatColor.RED + "У вас нет активных запросов на обмен.");
            return false;
        }

        Player sender = request.getSender();

        // Проверяем, что отправитель запроса всё ещё онлайн
        if (sender == null || !sender.isOnline()) {
            target.sendMessage(ChatColor.RED + "Игрок " + request.getSender().getName() + " больше не в сети.");
            tradeRequests.remove(target); // Удаляем запрос
            return false;
        }

        // Получаем текущие титулы игроков
        String senderTitle = getPlayerTitle(sender);
        String targetTitle = getPlayerTitle(target);

        // Если у одного из игроков нет титула, обмен невозможен
        if (senderTitle == null || targetTitle == null) {
            target.sendMessage(ChatColor.RED + "Обмен невозможен: у одного из игроков нет титула.");
            return false;
        }

        // Меняем титулы местами
        setPlayerTitle(sender, targetTitle);
        setPlayerTitle(target, senderTitle);

        // Удаляем запрос
        tradeRequests.remove(target);

        // Сообщаем игрокам об успешном обмене
        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " принял ваш запрос на обмен титулами!");
        target.sendMessage(ChatColor.GREEN + "Вы успешно обменялись титулами с " + sender.getName() + "!");

        return true;
    }

    // Отклонение запроса
    public void declineTradeRequest(Player target) {
        TradeRequest request = tradeRequests.remove(target);
        if (request != null) {
            Player sender = request.getSender();
            sender.sendMessage(ChatColor.RED + "Игрок " + target.getName() + " отклонил ваш запрос на обмен титулами.");
            target.sendMessage(ChatColor.RED + "Вы отклонили запрос на обмен титулами от " + sender.getName() + ".");
        }
    }

    // Установка суффикса через LuckPerms
    private void setLuckPermsSuffix(Player player, String suffix, int priority) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            String command = String.format(
                    "lp user %s meta setsuffix %d \"%s\"",
                    player.getName(),
                    priority,
                    suffix.replace("\"", "\\\"")
            );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });
    }

    // Обмен титулами между двумя игроками
    public boolean tradeTitles(Player player1, Player player2) {
        // Получаем текущие титулы игроков
        String player1Title = getPlayerTitle(player1);
        String player2Title = getPlayerTitle(player2);

        // Если у одного из игроков нет титула, обмен невозможен
        if (player1Title == null || player2Title == null) {
            return false;
        }

        // Меняем титулы местами
        setPlayerTitle(player1, player2Title);
        setPlayerTitle(player2, player1Title);

        // Сообщаем игрокам об успешном обмене
        player1.sendMessage(ChatColor.GREEN + "Вы успешно обменялись титулами с " + player2.getName() + "!");
        player2.sendMessage(ChatColor.GREEN + "Вы успешно обменялись титулами с " + player1.getName() + "!");

        return true;
    }

    // Сохранение данных игрока при выходе
    public void savePlayerData(Player player) {
        String playerTitle = getPlayerTitle(player);
        if (playerTitle != null) {
            plugin.getLogger().info("Данные игрока " + player.getName() + " сохранены. Титул: " + playerTitle);
        } else {
            plugin.getLogger().info("У игрока " + player.getName() + " нет титула.");
        }
    }

    // Внутренний класс для хранения запросов на обмен
    private static class TradeRequest {
        private final Player sender; // Игрок, который отправил запрос
        private final Player target; // Игрок, которому отправлен запрос

        public TradeRequest(Player sender, Player target) {
            this.sender = sender;
            this.target = target;
        }

        public Player getSender() {
            return sender;
        }

        public Player getTarget() {
            return target;
        }
    }
}