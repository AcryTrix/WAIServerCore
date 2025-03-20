package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ProfileModule implements Listener {

    private final JavaPlugin plugin;

    public ProfileModule(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new ProfileMenuListener(), plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player clickedPlayer = (Player) event.getRightClicked();
            Player player = event.getPlayer();

            if (player.isSneaking()) { // Проверка, что Shift зажат
                openProfileMenu(player, clickedPlayer);
            }
        }
    }

    private void openProfileMenu(Player player, Player clickedPlayer) {
        Inventory menu = Bukkit.createInventory(null, 27, "§x§A§D§F§3§F§DП§x§A§D§F§3§F§Dр§x§A§D§F§3§F§Dо§x§A§D§F§3§F§Dф§x§A§D§F§3§F§Dи§x§A§D§F§3§F§Dл§x§A§D§F§3§F§Dь " + clickedPlayer.getName());

        // Кнопка "Репутация"
        ItemStack reputationItem = new ItemStack(Material.EMERALD);
        ItemMeta reputationMeta = reputationItem.getItemMeta();
        reputationMeta.setDisplayName("&7Репутация");
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
        infoMeta.setDisplayName("&7Информация");
        infoMeta.setLore(Arrays.asList(
                "&fНикнейм: &7" + clickedPlayer.getName(),
                "&fВремя в игре: &7" + getPlayTime(clickedPlayer),
                "&fДата присоединения: &7" + getJoinDate(clickedPlayer) // Исправленная дата
        ));
        infoItem.setItemMeta(infoMeta);
        menu.setItem(14, infoItem);

        // Кнопка "Обменяться титулами"
        ItemStack titleExchangeItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleExchangeMeta = titleExchangeItem.getItemMeta();
        titleExchangeMeta.setDisplayName("&7Обменяться титулами");
        titleExchangeMeta.setLore(Arrays.asList("Нажмите, чтобы предложить обмен титулами"));
        titleExchangeItem.setItemMeta(titleExchangeMeta);
        menu.setItem(16, titleExchangeItem);

        // Заполнение пустых слотов фиолетовой панелью
        ItemStack fillerItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 10); // Фиолетовая панель
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        fillerMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerMeta);

        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, fillerItem);
            }
        }

        player.openInventory(menu);
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

                // Разрешаем только просмотр предметов (клик без действий)
                if (event.getAction() == InventoryAction.PICKUP_ALL || // ЛКМ
                    event.getAction() == InventoryAction.PICKUP_HALF || // ПКМ
                    event.getAction() == InventoryAction.PICKUP_ONE ||
                    event.getAction() == InventoryAction.PICKUP_SOME ||
                    event.getAction() == InventoryAction.CLONE_STACK) { // Средний клик
                    event.setCancelled(true); // Запрещаем забор предметов
                }

                // Запрет на добавление предметов (даже с одинаковым именем)
                if (event.getAction() == InventoryAction.PLACE_ALL || // Добавление предметов
                    event.getAction() == InventoryAction.PLACE_ONE ||
                    event.getAction() == InventoryAction.PLACE_SOME ||
                    event.getAction() == InventoryAction.SWAP_WITH_CURSOR) { // Обмен с курсором
                    event.setCancelled(true);
                }

                // Запрет на перетаскивание (Shift+клик)
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                }
            }
        }
    }
}