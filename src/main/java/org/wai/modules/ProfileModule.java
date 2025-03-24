package org.wai.modules;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.WAIServerCore;
import org.wai.modules.titles.TitleManager;
import org.wai.modules.titles.TitlesModule;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.bukkit.inventory.meta.SkullMeta;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ProfileModule implements Listener {

    private final JavaPlugin plugin;
    private final TitlesModule titlesModule;

    public ProfileModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesModule = ((WAIServerCore) plugin).getTitlesModule();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new ProfileMenuListener(), plugin);
        plugin.getLogger().info("ProfileModule успешно загружен!");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player clickedPlayer = (Player) event.getRightClicked();
            Player player = event.getPlayer();
            if (player.isSneaking()) {
                openProfileMenu(player, clickedPlayer);
            }
        }
    }

    private void openProfileMenu(Player player, Player clickedPlayer) {
        Inventory menu = Bukkit.createInventory(null, 27, "Профиль " + clickedPlayer.getName());

        // Кнопка "Репутация"
        ItemStack reputationItem = new ItemStack(Material.EMERALD);
        ItemMeta reputationMeta = reputationItem.getItemMeta();
        reputationMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bРепутация"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&bНажмите, чтобы увидеть репутацию"));
        reputationMeta.setLore(lore);
        reputationItem.setItemMeta(reputationMeta);
        menu.setItem(3, reputationItem);

        // В методе openProfileMenu() класса ProfileModule добавить:

        // Кнопка "Добавить в друзья"
        ItemStack friendItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta friendMeta = (SkullMeta) friendItem.getItemMeta();
        friendMeta.setOwningPlayer(clickedPlayer);
        friendMeta.setDisplayName(ChatColor.GREEN + "Добавить в друзья");
        List<String> friendLore = new ArrayList<>();
        friendLore.add(ChatColor.GRAY + "Нажмите, чтобы отправить запрос");
        friendLore.add(ChatColor.GRAY + "Максимум друзей: 4");
        friendMeta.setLore(friendLore);
        friendItem.setItemMeta(friendMeta);
        menu.setItem(7, friendItem);

        // Кнопка "Информация о профиле"
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lИнформация о вашем профиле"));
        List<String> lore3 = new ArrayList<>();
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fНикнейм: &b" + clickedPlayer.getName()));
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fВремя в игре: &b" + getPlayTime(clickedPlayer)));
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fДата присоединения: &b" + getJoinDate(clickedPlayer)));
        infoMeta.setLore(lore3);
        infoItem.setItemMeta(infoMeta);
        menu.setItem(13, infoItem);

        // Кнопка "Обменяться титулами"
        ItemStack titleExchangeItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleExchangeMeta = titleExchangeItem.getItemMeta();
        titleExchangeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&LОбменяться титулами"));
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.translateAlternateColorCodes('&', "&7Нажмите, чтобы предложить &bобмен титулами"));
        lore2.add(ChatColor.translateAlternateColorCodes('&', "&bВнимание: &7если у вас нет титула или у другого игрока, обмен &cневозможен&7."));
        titleExchangeMeta.setLore(lore2);
        titleExchangeItem.setItemMeta(titleExchangeMeta);
        menu.setItem(5, titleExchangeItem);

        // Заполнитель
        ItemStack fillerItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
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

    // --- Методы репутации ---
    private void openReputationMenu(Player voter, Player target) {
        Inventory menu = Bukkit.createInventory(null, 27, "Репутация " + target.getName());

        // Кнопка повышения репутации
        ItemStack increaseItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta increaseMeta = increaseItem.getItemMeta();
        increaseMeta.setDisplayName(ChatColor.GREEN + "Повысить репутацию");
        increaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Нажмите, чтобы повысить репутацию",
                ChatColor.GRAY + "Текущая репутация: " + getReputation(target)
        ));
        increaseItem.setItemMeta(increaseMeta);
        menu.setItem(11, increaseItem);

        // Кнопка понижения репутации
        ItemStack decreaseItem = new ItemStack(Material.RED_WOOL);
        ItemMeta decreaseMeta = decreaseItem.getItemMeta();
        decreaseMeta.setDisplayName(ChatColor.RED + "Понизить репутацию");
        decreaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Нажмите, чтобы понизить репутацию",
                ChatColor.GRAY + "Текущая репутация: " + getReputation(target)
        ));
        decreaseItem.setItemMeta(decreaseMeta);
        menu.setItem(15, decreaseItem);

        // Заполнитель
        ItemStack filler = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, filler);
            }
        }
        voter.openInventory(menu);
    }

    private int getReputation(Player player) {
        return plugin.getConfig().getInt("reputation." + player.getUniqueId(), 0);
    }

    private void setReputation(Player player, int amount) {
        plugin.getConfig().set("reputation." + player.getUniqueId(), amount);
        plugin.saveConfig();
    }

    private void sendFriendRequest(Player sender, Player target) {
        String senderUUID = sender.getUniqueId().toString();
        String targetUUID = target.getUniqueId().toString();

        // Проверяем, есть ли уже заявка
        String requestTimestamp = plugin.getConfig().getString("friend-requests." + targetUUID + "." + senderUUID);
        if (requestTimestamp != null) {
            long requestTime = Long.parseLong(requestTimestamp);
            if (System.currentTimeMillis() - requestTime < 60 * 1000) { // 1 минута (60 сек * 1000 мс)
                sender.sendMessage(ChatColor.RED + "Вы уже отправили заявку! Подождите 1 минуту.");
                return;
            }
        }

        // Сохраняем новую заявку с временной меткой
        plugin.getConfig().set("friend-requests." + targetUUID + "." + senderUUID, String.valueOf(System.currentTimeMillis()));
        plugin.saveConfig();

        // Отправка сообщения с кнопками
        TextComponent message = new TextComponent(ChatColor.GREEN + "Игрок " + sender.getName() + " хочет добавить вас в друзья. ");
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Принять]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + sender.getName()));
        TextComponent decline = new TextComponent(ChatColor.RED + "[Отклонить]");
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + sender.getName()));

        message.addExtra(accept);
        message.addExtra(new TextComponent(ChatColor.WHITE + " | "));
        message.addExtra(decline);

        target.spigot().sendMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Заявка отправлена игроку " + target.getName() + "!");
    }


    public static class FriendCommand implements CommandExecutor {
        private final JavaPlugin plugin;

        public FriendCommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) return true;
            Player player = (Player) sender;

            if (args.length == 0) return false;

            switch (args[0].toLowerCase()) {
                case "accept":
                    if (args.length < 2) return false;
                    Player requester = Bukkit.getPlayer(args[1]);
                    if (requester == null) {
                        player.sendMessage(ChatColor.RED + "Игрок не в сети!");
                        return true;
                    }
                    acceptFriendRequest(player, requester);
                    break;
                case "deny":
                    if (args.length < 2) return false;
                    Player denier = Bukkit.getPlayer(args[1]);
                    if (denier == null) return true;
                    denyFriendRequest(player, denier);
                    break;
            }
            return true;
        }

        private void acceptFriendRequest(Player target, Player requester) {
            String targetUUID = target.getUniqueId().toString();
            String requesterUUID = requester.getUniqueId().toString();

            // Проверяем, есть ли заявка
            String requestTimestamp = plugin.getConfig().getString("friend-requests." + targetUUID + "." + requesterUUID);
            if (requestTimestamp == null) {
                target.sendMessage(ChatColor.RED + "Нет активного запроса от этого игрока!");
                return;
            }

            // Проверяем, не истекло ли время (1 минута)
            long requestTime = Long.parseLong(requestTimestamp);
            if (System.currentTimeMillis() - requestTime > 60 * 1000) { // 60 секунд * 1000 мс
                target.sendMessage(ChatColor.RED + "Запрос в друзья от " + requester.getName() + " истёк!");
                plugin.getConfig().set("friend-requests." + targetUUID + "." + requesterUUID, null);
                plugin.saveConfig();
                return;
            }

            // Добавляем в список друзей
            List<String> targetFriends = plugin.getConfig().getStringList("friends." + targetUUID);
            targetFriends.add(requesterUUID);
            plugin.getConfig().set("friends." + targetUUID, targetFriends);

            List<String> requesterFriends = plugin.getConfig().getStringList("friends." + requesterUUID);
            requesterFriends.add(targetUUID);
            plugin.getConfig().set("friends." + requesterUUID, requesterFriends);

            // Удаляем заявку после принятия
            plugin.getConfig().set("friend-requests." + targetUUID + "." + requesterUUID, null);
            plugin.saveConfig();

            target.sendMessage(ChatColor.GREEN + "Вы приняли запрос от " + requester.getName());
            requester.sendMessage(ChatColor.GREEN + target.getName() + " принял ваш запрос в друзья!");
        }

        private void denyFriendRequest(Player target, Player requester) {
            String targetUUID = target.getUniqueId().toString();
            String requesterUUID = requester.getUniqueId().toString();

            // Проверяем, есть ли заявка
            String requestTimestamp = plugin.getConfig().getString("friend-requests." + targetUUID + "." + requesterUUID);
            if (requestTimestamp == null) {
                target.sendMessage(ChatColor.RED + "Нет активного запроса от этого игрока!");
                return;
            }

            // Удаляем заявку из конфига
            plugin.getConfig().set("friend-requests." + targetUUID + "." + requesterUUID, null);
            plugin.saveConfig();

            target.sendMessage(ChatColor.RED + "Вы отклонили запрос от " + requester.getName());
            requester.sendMessage(ChatColor.RED + target.getName() + " отклонил ваш запрос в друзья.");
        }
    }

    private void changeReputation(Player voter, Player target, int amount) {
        if (voter.getUniqueId().equals(target.getUniqueId())) {
            voter.sendMessage(ChatColor.RED + "Нельзя менять себе репутацию!");
            return;
        }
        // Получаем запись голосования в формате "voteType;voteTimestamp"
        String voteRecord = plugin.getConfig().getString("votes." + voter.getUniqueId() + "." + target.getUniqueId());
        if (voteRecord != null) {
            String[] parts = voteRecord.split(";");
            if (parts.length == 2) {
                int previousVote = Integer.parseInt(parts[0]);
                Instant voteTime = Instant.parse(parts[1]);
                // Если голос уже дан за последние 24 часа
                if (voteTime.isAfter(Instant.now().minusSeconds(5))) {
                    // Если новый голос отличается от уже поставленного, запрещаем смену типа
                    if (previousVote != amount) {
                        voter.sendMessage(ChatColor.RED + "Нельзя изменить голос с " +
                                (previousVote > 0 ? "+1" : "-1") + " на " +
                                (amount > 0 ? "+1" : "-1") + " в течение суток!");
                        return;
                    }
                    // Если тот же голос повторно пытаются поставить, также отказываем
                    voter.sendMessage(ChatColor.RED + "Вы уже голосовали этому игроку за последние 24 часа!");
                    return;
                }
            }
        }
        int newReputation = getReputation(target) + amount;
        setReputation(target, newReputation);
        // Сохраняем голос с типом и текущей датой
        String newRecord = amount + ";" + Instant.now().toString();
        plugin.getConfig().set("votes." + voter.getUniqueId() + "." + target.getUniqueId(), newRecord);
        plugin.saveConfig();
        voter.sendMessage(ChatColor.GREEN + "Вы изменили репутацию игрока " + target.getName() + " на " + (amount > 0 ? "+1" : "-1"));
        target.sendMessage(ChatColor.GREEN + "Ваша репутация была изменена игроком " + voter.getName() + " на " + (amount > 0 ? "+1" : "-1"));
    }

    // --- Конец методов репутации ---

    // Обработчик кликов в инвентарях "Профиль" и "Репутация"
    private class ProfileMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            // Проверяем, что это инвентарь "Профиль" или "Репутация"
            String title = event.getView().getTitle();
            if (!title.startsWith("Профиль ") && !title.startsWith("Репутация ")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 7) {
                // Получаем имя цели из заголовка инвентаря
                String targetName = title.replace("Профиль ", "");
                Player target = Bukkit.getPlayer(targetName);

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
                    return;
                }

                List<String> friends = plugin.getConfig().getStringList("friends." + player.getUniqueId());

// Проверяем, есть ли у игрока пермишен на 12 друзей
                int maxFriends = player.hasPermission("friends.limit.12") ? 12 : 4;

                if (friends.size() >= maxFriends) {
                    player.sendMessage(ChatColor.RED + "У вас уже максимальное количество друзей (" + maxFriends + ")!");
                    return;
                }

                if (friends.contains(target.getUniqueId().toString())) {
                    player.sendMessage(ChatColor.RED + "Этот игрок уже у вас в друзьях!");
                    return;
                }
                sendFriendRequest(player, target);
            }

            if (title.startsWith("Профиль ")) {
                // Обработка кликов в меню профиля
                String targetName = title.replace("Профиль ", "");
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
                    return;
                }
                if (event.getSlot() == 3) {
                    player.closeInventory();
                    openReputationMenu(player, target);
                }
                // Существующая обработка для кнопки обмена титулами
                if (event.getSlot() == 5) {
                    TitleManager titleManager = ((WAIServerCore) Bukkit.getPluginManager().getPlugin("WAIServerCore"))
                            .getTitlesModule().getTitleManager();
                    if (titleManager != null) {
                        titleManager.sendTradeRequest(player, target);
                    } else {
                        player.sendMessage(ChatColor.RED + "Ошибка: модуль титулов не загружен!");
                    }
                }
            } else if (title.startsWith("Репутация ")) {
                // Обработка кликов в меню репутации
                String targetName = title.replace("Репутация ", "");
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
                    return;
                }
                if (event.getSlot() == 11) {
                    changeReputation(player, target, 1);
                } else if (event.getSlot() == 15) {
                    changeReputation(player, target, -1);
                }
            }
        }
    }
}