package org.wai.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
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
import java.util.HashMap;
import java.util.Map;

public class ProfileModule implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>();

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
        reputationMeta.setDisplayName("§7Репутация");
        reputationMeta.setLore(Arrays.asList("Нажмите, чтобы увидеть репутацию"));
        reputationItem.setItemMeta(reputationMeta);
        menu.setItem(10, reputationItem);

        // Кнопка "Добавить в друзья"
        ItemStack addFriendItem = new ItemStack(Material.PAPER);
        ItemMeta addFriendMeta = addFriendItem.getItemMeta();
        addFriendMeta.setDisplayName("§7Добавить в друзья");
        addFriendMeta.setLore(Arrays.asList("Нажмите, чтобы добавить в друзья"));
        addFriendItem.setItemMeta(addFriendMeta);
        menu.setItem(12, addFriendItem);

        // Информация о игроке
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§7Информация");
        infoMeta.setLore(Arrays.asList(
                "§fНикнейм: §7" + clickedPlayer.getName(),
                "§fВремя в игре: §7" + getPlayTime(clickedPlayer),
                "§fДата присоединения: §7" + getJoinDate(clickedPlayer) // Исправленная дата
        ));
        infoItem.setItemMeta(infoMeta);
        menu.setItem(14, infoItem);

        // Кнопка "Обменяться титулами"
        ItemStack titleExchangeItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleExchangeMeta = titleExchangeItem.getItemMeta();
        titleExchangeMeta.setDisplayName("§7Обменяться титулами");
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

    private void sendTradeRequest(Player sender, Player target) {
        TradeRequest request = new TradeRequest(sender, target);
        tradeRequests.put(target, request);

        target.sendMessage("§aВам предложили обмен титулами от " + sender.getName() + ". Принять?");
        target.sendMessage("§aНапишите §e/approvesuffix §aдля подтверждения.");
    }

    private void openTradeMenu(TradeRequest request) {
        Player sender = request.getSender();
        Player target = request.getTarget();

        Inventory senderMenu = Bukkit.createInventory(null, 27, "Выберите суффикс для обмена");
        setupTradeMenu(senderMenu, sender, request, true);

        Inventory targetMenu = Bukkit.createInventory(null, 27, "Выберите суффикс для обмена");
        setupTradeMenu(targetMenu, target, request, false);

        sender.openInventory(senderMenu);
        target.openInventory(targetMenu);
    }

    private void setupTradeMenu(Inventory menu, Player player, TradeRequest request, boolean isSender) {
        ItemStack suffix1 = createSuffixItem("§x§9§7§9§6§E§A ✎", "suffix.5.§x§9§7§9§6§E§A ✎");
        ItemStack suffix2 = createSuffixItem("§x§A§D§F§3§F§D 🗡", "suffix.5.§x§A§D§F§3§F§D 🗡");

        menu.setItem(10, suffix1);
        menu.setItem(12, suffix2);

        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName("§aПринять обмен");
        acceptItem.setItemMeta(acceptMeta);
        menu.setItem(16, acceptItem);
    }

    private ItemStack createSuffixItem(String displayName, String suffix) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList("§7Нажмите, чтобы выбрать этот суффикс."));
        item.setItemMeta(meta);
        return item;
    }

    private void swapSuffixes(TradeRequest request) {
        Player sender = request.getSender();
        Player target = request.getTarget();

        LuckPerms api = LuckPermsProvider.get();
        User senderUser = api.getUserManager().getUser(sender.getUniqueId());
        User targetUser = api.getUserManager().getUser(target.getUniqueId());

        if (senderUser != null && targetUser != null) {
            senderUser.data().add(Node.builder("suffix.100." + request.getTargetSuffix()).build());
            targetUser.data().add(Node.builder("suffix.100." + request.getSenderSuffix()).build());

            api.getUserManager().saveUser(senderUser);
            api.getUserManager().saveUser(targetUser);
        }
    }

    private static class ProfileMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory clickedInventory = event.getClickedInventory();

            // Проверяем, что кликнули в меню профиля
            if (clickedInventory != null && clickedInventory.getType() == InventoryType.CHEST &&
                clickedInventory.getHolder() == null &&
                event.getView().getTitle().startsWith("§x§A§D§F§3§F§DП§x§A§D§F§3§F§Dр§x§A§D§F§3§F§Dо§x§A§D§F§3§F§Dф§x§A§D§F§3§F§Dи§x§A§D§F§3§F§Dл§x§A§D§F§3§F§Dь")) {

                // Запрет на все действия с предметами
                event.setCancelled(true);

                Player player = (Player) event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem != null && clickedItem.getType() == Material.NAME_TAG) {
                    // Получаем игрока, на которого нажали Shift + ПКМ
                    Player target = Bukkit.getPlayer(event.getView().getTitle().replace("§x§A§D§F§3§F§DП§x§A§D§F§3§F§Dр§x§A§D§F§3§F§Dо§x§A§D§F§3§F§Dф§x§A§D§F§3§F§Dи§x§A§D§F§3§F§Dл§x§A§D§F§3§F§Dь ", ""));

                    if (target != null) {
                        ((ProfileModule) Bukkit.getPluginManager().getPlugin("WAIServerCore").getModule(ProfileModule.class))
                                .sendTradeRequest(player, target); // Отправляем предложение обмена
                    }
                }
            }

            // Проверяем, что кликнули в меню обмена титулами
            if (clickedInventory != null && clickedInventory.getType() == InventoryType.CHEST &&
                clickedInventory.getHolder() == null &&
                event.getView().getTitle().equals("Выберите суффикс для обмена")) {

                event.setCancelled(true);

                Player player = (Player) event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                    String suffix = clickedItem.getItemMeta().getDisplayName();
                    TradeRequest request = ((ProfileModule) Bukkit.getPluginManager().getPlugin("WAIServerCore").getModule(ProfileModule.class))
                            .tradeRequests.get(player);

                    if (request != null) {
                        if (player.equals(request.getSender())) {
                            request.setSenderSuffix(suffix);
                        } else {
                            request.setTargetSuffix(suffix);
                        }

                        player.sendMessage("§aВы выбрали суффикс: " + suffix);
                    }
                } else if (clickedItem != null && clickedItem.getType() == Material.GREEN_WOOL) {
                    TradeRequest request = ((ProfileModule) Bukkit.getPluginManager().getPlugin("WAIServerCore").getModule(ProfileModule.class))
                            .tradeRequests.get(player);

                    if (request != null && request.getSenderSuffix() != null && request.getTargetSuffix() != null) {
                        ((ProfileModule) Bukkit.getPluginManager().getPlugin("WAIServerCore").getModule(ProfileModule.class))
                                .swapSuffixes(request);
                        player.sendMessage("§aОбмен завершен!");
                        ((ProfileModule) Bukkit.getPluginManager().getPlugin("WAIServerCore").getModule(ProfileModule.class))
                                .tradeRequests.remove(player);
                    } else {
                        player.sendMessage("§cОба игрока должны выбрать суффикс.");
                    }
                }
            }
        }
    }

    public static class TradeRequest {
        private final Player sender;
        private final Player target;
        private String senderSuffix;
        private String targetSuffix;

        public TradeRequest(Player sender, Player target) {
            this.sender = sender;https://pastebin.com/HyvzgaR0
            this.target = target;
        }

        public Player getSender() {
            return sender;
        }

        public Player getTarget() {
            return target;
        }

        public String getSenderSuffix() {
            return senderSuffix;
        }

        public void setSenderSuffix(String senderSuffix) {
            this.senderSuffix = senderSuffix;
        }

        public String getTargetSuffix() {
            return targetSuffix;
        }

        public void setTargetSuffix(String targetSuffix) {
            this.targetSuffix = targetSuffix;
        }
    }
}