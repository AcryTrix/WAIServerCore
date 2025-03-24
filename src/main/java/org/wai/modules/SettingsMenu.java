package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.modules.titles.TitleManager;
import java.util.UUID;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.Statistic;

public class SettingsMenu implements Listener {
    private final JavaPlugin plugin;
    private final TitleManager titleManager;

    public SettingsMenu(JavaPlugin plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.titleManager = titleManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private String getPlayTime(Player player) {
        long playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playTimeHours = playTimeTicks / 72000;
        return playTimeHours + " часов";
    }

    private String getJoinDate(Player player) {
        long firstPlayed = player.getFirstPlayed();
        if (firstPlayed <= 0) return "Неизвестно";

        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(firstPlayed));
    }

    public void openSettingsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§bНастройки");

        // Кнопка "Обмен титулами"
        ItemStack tradeToggle = new ItemStack(
                titleManager.canSendTradeRequest(player) ?
                        Material.LIME_DYE : Material.GRAY_DYE
        );
        ItemMeta tradeMeta = tradeToggle.getItemMeta();
        if (tradeMeta != null) {
            tradeMeta.setDisplayName("§bОбмен титулами");
            tradeMeta.setLore(Arrays.asList(
                    "§7Статус: " + (titleManager.canTrade(player) ? "§aВКЛ" : "§cВЫКЛ"),
                    "",
                    "§7Если выключено:",
                    "§8- Вы не можете отправлять запросы",
                    "§8- Вам не могут отправлять запросы"
            ));
            tradeToggle.setItemMeta(tradeMeta);
        }
        menu.setItem(11, tradeToggle);

        // Кнопка "Discord"
        ItemStack discordInfo = new ItemStack(Material.BEEHIVE);
        ItemMeta discordMeta = discordInfo.getItemMeta();
        if (discordMeta != null) {
            discordMeta.setDisplayName("§bИнформация о дискорд аккаунте");
            discordMeta.setLore(Arrays.asList(
                    "§7Данная функция в разработке."
            ));
            discordInfo.setItemMeta(discordMeta);
        }
        menu.setItem(13, discordInfo);

        // Кнопка "Информация о профиле"
        ItemStack infoItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§bИнформация о профиле");
            List<String> lore3 = new ArrayList<>();
            lore3.add("§b➜ §fНикнейм: §b" + player.getName());
            lore3.add("§b➜ §fВремя в игре: §b" + getPlayTime(player));
            lore3.add("§b➜ §fДата присоединения: §b" + getJoinDate(player));
            int rep = plugin.getConfig().getInt("reputation." + player.getUniqueId(), 0);
            String repStr = (rep >= 0 ? "+" : "") + rep;
            lore3.add("§b➜ §fРепутация: §b" + repStr);

            infoMeta.setLore(lore3);
            infoItem.setItemMeta(infoMeta);
        }
        menu.setItem(4, infoItem); // Устанавливаем на 15-й слот

        // Кнопка "Управление титулами"
        ItemStack titleManagerButton = new ItemStack(Material.NAME_TAG);
        ItemMeta titleMeta = titleManagerButton.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName("§bУправление титулами");
            titleMeta.setLore(Arrays.asList(
                    "§bОткрыть меню управления титулами",
                    "§8- Просмотр доступных титулов",
                    "§8- Установка титула"
            ));
            titleManagerButton.setItemMeta(titleMeta);
        }
        menu.setItem(15, titleManagerButton);

        // Кнопка "Друзья"
        ItemStack friendsItem = new ItemStack(Material.PAPER);
        ItemMeta friendsMeta = friendsItem.getItemMeta();
        friendsMeta.setDisplayName(ChatColor.BLUE + "Друзья");
        friendsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Нажмите, чтобы просмотреть список друзей",
                ChatColor.GRAY + "Друзей: " + plugin.getConfig().getStringList("friends." + player.getUniqueId()).size()
        ));
        friendsItem.setItemMeta(friendsMeta);
        menu.setItem(22, friendsItem); // Ставим кнопку на 14-й слот

        // Заполнение пустых слотов
        ItemStack filler = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (menu.getItem(i) == null) menu.setItem(i, filler);
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§bНастройки")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (event.getSlot()) {
            case 11: // Обмен титулами
                titleManager.toggleTradeSetting(player);
                player.sendMessage("§bОбмен титулами: " + (titleManager.canTrade(player) ? "§aВКЛ" : "§cВЫКЛ"));
                openSettingsMenu(player);
                break;
            case 13: // Discord
                player.sendMessage("§bDiscord: §9" + plugin.getConfig().getString("discord-link"));
                break;
            case 22: // Друзья (исправленный case)
                openFriendsMenu(player);
                break;
            case 15: // Управление титулами
                player.closeInventory();
                openTitleManagementMenu(player);
                break;
        }
    }

    private void openFriendsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, "Друзья");
        List<String> friendUUIDs = plugin.getConfig().getStringList("friends." + player.getUniqueId());

        for (String uuidStr : friendUUIDs) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                OfflinePlayer friend = Bukkit.getOfflinePlayer(uuid);

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                if (meta != null) {
                    meta.setOwningPlayer(friend);
                    meta.setDisplayName(ChatColor.GREEN + friend.getName());

                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Никнейм: " + friend.getName());

                    if (friend.isOnline()) {
                        Player onlineFriend = friend.getPlayer();
                        if (onlineFriend != null) { // Проверка на null
                            lore.add(ChatColor.GREEN + "В сети: Да");

                            // Определяем название мира
                            String worldName = onlineFriend.getWorld().getName();
                            String translatedWorld;
                            switch (worldName) {
                                case "world":
                                    translatedWorld = "Верхний мир";
                                    break;
                                case "world_nether":
                                    translatedWorld = "Ад";
                                    break;
                                case "world_the_end":
                                    translatedWorld = "Эндер мир";
                                    break;
                                default:
                                    translatedWorld = "Неизвестный мир";
                                    break;
                            }

                            lore.add(ChatColor.GRAY + "Мир: " + translatedWorld);
                            lore.add(ChatColor.GRAY + "Координаты: " +
                                    onlineFriend.getLocation().getBlockX() + ", " +
                                    onlineFriend.getLocation().getBlockY() + ", " +
                                    onlineFriend.getLocation().getBlockZ());
                        }
                    } else {
                        lore.add(ChatColor.RED + "В сети: Нет");
                    }

                    meta.setLore(lore);
                    head.setItemMeta(meta);
                    menu.addItem(head);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неверный UUID друга: " + uuidStr);
            }
        }

        player.openInventory(menu);

        // Заполнение пустых слотов
        ItemStack filler = new ItemStack(Material.BARRIER);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) menu.setItem(i, filler);
        }

        player.openInventory(menu);
    }

    // Открывает меню управления титулами
    private void openTitleManagementMenu(Player player) {
        Inventory titlesMenu = Bukkit.createInventory(null, 27, "§bВыбор титула");
        Set<String> availableTitles = titleManager.getAvailableTitles();

        int slot = 0;
        for (String title : availableTitles) {
            String permission = "titles.title." + title;
            boolean hasPermission = player.hasPermission(permission);

            // Используем разные предметы для разных титулов
            Material material = hasPermission ? Material.GREEN_TERRACOTTA : Material.BARRIER;
            ItemStack titleItem = new ItemStack(material);
            ItemMeta titleMeta = titleItem.getItemMeta();
            if (titleMeta != null) {
                titleMeta.setDisplayName((hasPermission ? "§a" : "§c") + title);
                titleMeta.setLore(Arrays.asList(
                        "§7Титул: §f" + title,
                        hasPermission ? "§aДоступен" : "§cНедоступен"
                ));
                titleItem.setItemMeta(titleMeta);
            }
            titlesMenu.setItem(slot++, titleItem);
        }

        player.openInventory(titlesMenu);
    }

    @EventHandler
    public void onFriendsMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Друзья")) return;

        event.setCancelled(true); // Отменяем любые действия в меню

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getClick().isKeyboardClick() || event.getClick().isShiftClick()) {
            return; // Игнорируем ненужные клики
        }

        // Проверяем, нажал ли игрок среднее колесико мыши
        if (event.getClick().isCreativeAction() || event.getClick().name().equals("MIDDLE")) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer friend = meta.getOwningPlayer();
            UUID friendUUID = friend.getUniqueId();

            // Удаляем друга из конфига
            List<String> friendList = plugin.getConfig().getStringList("friends." + player.getUniqueId());
            friendList.remove(friendUUID.toString());
            plugin.getConfig().set("friends." + player.getUniqueId(), friendList);
            plugin.saveConfig();

            player.sendMessage("§cВы удалили друга §e" + friend.getName() + "§c!");
            openFriendsMenu(player); // Обновляем меню
        }
    }


    @EventHandler
    public void onTitleMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§bВыбор титула")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        String title = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (player.hasPermission("titles.title." + title)) {
            titleManager.setPlayerTitle(player, title);
            player.sendMessage("§aВы установили титул: " + title);
            player.closeInventory();
        } else {
            player.sendMessage("§cУ вас нет прав на этот титул!");
        }
    }
}