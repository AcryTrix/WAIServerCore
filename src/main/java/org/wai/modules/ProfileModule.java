package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.WAIServerCore;
import org.wai.modules.titles.TitleManager;
import org.wai.modules.titles.TitlesModule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ProfileModule implements Listener {

    private final JavaPlugin plugin;
    private final TitlesModule titlesModule;

    public ProfileModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesModule = ((WAIServerCore) plugin).getTitlesModule(); // Получаем TitlesModule
        Bukkit.getPluginManager().registerEvents(this, plugin); // Регистрируем этот класс как слушатель событий
        Bukkit.getPluginManager().registerEvents(new ProfileMenuListener(), plugin); // Регистрируем слушатель меню
        Bukkit.getPluginManager().registerEvents(new TradeResponseListener(), plugin); // Регистрируем слушатель ответов
        plugin.getLogger().info("ProfileModule успешно загружен!"); // Логируем загрузку модуля
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Проверяем, что кликнули по игроку
        if (event.getRightClicked() instanceof Player) {
            Player clickedPlayer = (Player) event.getRightClicked();
            Player player = event.getPlayer();

            // Проверяем, что игрок зажал Shift
            if (player.isSneaking()) {
                plugin.getLogger().info("Игрок " + player.getName() + " зажал Shift и кликнул по " + clickedPlayer.getName());
                openProfileMenu(player, clickedPlayer); // Открываем меню профиля
            }
        }
    }

    private void openProfileMenu(Player player, Player clickedPlayer) {
        // Создаем инвентарь с названием "Профиль <ник игрока>"
        Inventory menu = Bukkit.createInventory(null, 27, "Профиль " + clickedPlayer.getName());

        // Кнопка "Репутация"
        ItemStack reputationItem = new ItemStack(Material.EMERALD);
        ItemMeta reputationMeta = reputationItem.getItemMeta();
        reputationMeta.setDisplayName("Репутация");
        reputationMeta.setLore(Arrays.asList("Нажмите, чтобы увидеть репутацию"));
        reputationItem.setItemMeta(reputationMeta);
        menu.setItem(10, reputationItem);

        // Кнопка "Добавить в друзья"
        ItemStack addFriendItem = new ItemStack(Material.PAPER);
        ItemMeta addFriendMeta = addFriendItem.getItemMeta();
        addFriendMeta.setDisplayName("Добавить в друзья");
        addFriendMeta.setLore(Arrays.asList("Нажмите, чтобы добавить в друзья"));
        addFriendItem.setItemMeta(addFriendMeta);
        menu.setItem(12, addFriendItem);

        // Информация о игроке
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("Информация");
        infoMeta.setLore(Arrays.asList(
                "Никнейм: " + clickedPlayer.getName(),
                "Время в игре: " + getPlayTime(clickedPlayer),
                "Дата присоединения: " + getJoinDate(clickedPlayer) // Исправленная дата
        ));
        infoItem.setItemMeta(infoMeta);
        menu.setItem(14, infoItem);

        // Кнопка "Обменяться титулами"
        ItemStack titleExchangeItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleExchangeMeta = titleExchangeItem.getItemMeta();
        titleExchangeMeta.setDisplayName("Обменяться титулами");

        titleExchangeMeta.setLore(Arrays.asList("Нажмите, чтобы предложить обмен титулами"));
        titleExchangeItem.setItemMeta(titleExchangeMeta);
        menu.setItem(16, titleExchangeItem);

        // Заполнение пустых слотов фиолетовой панелью
        ItemStack fillerItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, (short) 10); // Фиолетовая панель
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        fillerMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerMeta);

        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, fillerItem); // Заполняем пустые слоты
            }
        }

        player.openInventory(menu); // Открываем меню игроку
        plugin.getLogger().info("Меню профиля открыто для " + player.getName());
    }

    private String getPlayTime(Player player) {
        long playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playTimeHours = playTimeTicks / 72000; // 1 час = 72000 тиков
        return playTimeHours + " часов";
    }

    private String getJoinDate(Player player) {
        long firstPlayed = player.getFirstPlayed();

        // Проверка на корректность данных
        if (firstPlayed <= 0) {
            return "Неизвестно"; // Если данные недоступны
        }

        // Преобразование времени в читаемый формат
        Instant instant = Instant.ofEpochMilli(firstPlayed);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private static class ProfileMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory clickedInventory = event.getClickedInventory();

            // Проверяем, что кликнули в меню профиля
            if (clickedInventory != null && clickedInventory.getType() == InventoryType.CHEST &&
                    clickedInventory.getHolder() == null &&
                    event.getView().getTitle().startsWith("Профиль")) {

                // Запрет на все действия с предметами
                event.setCancelled(true);

                // Получаем предмет, по которому кликнули
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return; // Если кликнули по пустому слоту, ничего не делаем
                }

                // Получаем игрока, который открыл меню
                Player player = (Player) event.getWhoClicked();

                // Получаем имя целевого игрока из названия инвентаря
                String targetName = event.getView().getTitle().replace("Профиль ", "");
                Player target = Bukkit.getPlayer(targetName);

                // Проверяем, что целевой игрок онлайн
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
                    return;
                }

                // Обработка клика по кнопке "Обменяться титулами"
                if (clickedItem.getType() == Material.NAME_TAG) {
                    // Проверяем, что игрок не пытается обменяться с самим собой
                    if (target.equals(player)) {
                        player.sendMessage(ChatColor.RED + "Вы не можете обменяться титулами с самим собой!");
                        return;
                    }

                    // Получаем TitleManager из TitlesModule
                    TitlesModule titlesModule = ((WAIServerCore) Bukkit.getPluginManager().getPlugin("WAIServerCore")).getTitlesModule();
                    if (titlesModule != null) {
                        TitleManager titleManager = titlesModule.getTitleManager();

                        // Отправляем запрос на обмен титулами
                        titleManager.sendTradeRequest(player, target);
                        player.sendMessage(ChatColor.GREEN + "Запрос на обмен титулами отправлен игроку " + target.getName() + "!");
                        target.sendMessage(ChatColor.GREEN + "Игрок " + player.getName() + " предложил вам обменяться титулами. Напишите 'да' или 'нет' в чат.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Ошибка: модуль титулов не загружен!");
                    }
                }

                // Запрет на забор, добавление и перетаскивание предметов
                if (event.getAction() == InventoryAction.PICKUP_ALL || // ЛКМ
                        event.getAction() == InventoryAction.PICKUP_HALF || // ПКМ
                        event.getAction() == InventoryAction.PICKUP_ONE ||
                        event.getAction() == InventoryAction.PICKUP_SOME ||
                        event.getAction() == InventoryAction.CLONE_STACK || // Средний клик
                        event.getAction() == InventoryAction.PLACE_ALL || // Добавление предметов
                        event.getAction() == InventoryAction.PLACE_ONE ||
                        event.getAction() == InventoryAction.PLACE_SOME ||
                        event.getAction() == InventoryAction.SWAP_WITH_CURSOR || // Обмен с курсором
                        event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Перетаскивание (Shift+клик)
                    event.setCancelled(true);
                }
            }
        }
    }

    private static class TradeResponseListener implements Listener {
        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            String message = event.getMessage().toLowerCase();

            TitlesModule titlesModule = ((WAIServerCore) Bukkit.getPluginManager().getPlugin("WAIServerCore")).getTitlesModule();
            if (titlesModule == null) {
                return;
            }

            TitleManager titleManager = titlesModule.getTitleManager();

            // Проверяем, является ли сообщение ответом на запрос
            if (message.equals("да") || message.equals("нет")) {
                event.setCancelled(true); // Отменяем отправку сообщения в чат

                if (message.equals("да")) {
                    // Подтверждение запроса
                    if (titleManager.acceptTradeRequest(player)) {
                        player.sendMessage(ChatColor.GREEN + "Вы приняли запрос на обмен титулами!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Ошибка: запрос на обмен не найден или у одного из игроков нет титула.");
                    }
                } else if (message.equals("нет")) {
                    // Отклонение запроса
                    titleManager.declineTradeRequest(player);
                    player.sendMessage(ChatColor.RED + "Вы отклонили запрос на обмен титулами.");
                }
            }
        }
    }
}