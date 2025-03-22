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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.WAIServerCore;
import org.wai.modules.titles.TitleManager;
import org.wai.modules.titles.TitlesModule;
import java.util.Arrays; // Это может быть, но сейчас не используется.
import java.util.List;
import java.util.ArrayList;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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

        ItemStack reputationItem = new ItemStack(Material.EMERALD);
        ItemMeta reputationMeta = reputationItem.getItemMeta();

        reputationMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bРепутация"));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&bНажмите, чтобы увидеть репутацию"));
        reputationMeta.setLore(lore);

        reputationItem.setItemMeta(reputationMeta);
        menu.setItem(3, reputationItem);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lИнформация о вашем профиле")); // Золотой цвет для названия

        List<String> lore3 = new ArrayList<>();
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fНикнейм: &b" + clickedPlayer.getName()));
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fВремя в игре: &b" + getPlayTime(clickedPlayer)));
        lore3.add(ChatColor.translateAlternateColorCodes('&', "&b&l➜ &fДата присоединения: &b" + getJoinDate(clickedPlayer)));

        infoMeta.setLore(lore3);
        infoItem.setItemMeta(infoMeta);
        menu.setItem(13, infoItem);

        ItemStack titleExchangeItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleExchangeMeta = titleExchangeItem.getItemMeta();

        titleExchangeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&LОбменяться титулами"));

        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.translateAlternateColorCodes('&', "&7Нажмите, чтобы предложить &bобмен титулами"));
        lore2.add(ChatColor.translateAlternateColorCodes('&', "&bВнимание: &7если у вас нет титула или у другого игрока, обмен &cневозможен&7."));
        titleExchangeMeta.setLore(lore2);

        titleExchangeItem.setItemMeta(titleExchangeMeta);
        menu.setItem(5, titleExchangeItem);

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

    private static class ProfileMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getClickedInventory() == null ||
                    event.getClickedInventory().getType() != InventoryType.CHEST ||
                    !event.getView().getTitle().startsWith("Профиль")) return;

            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            String targetName = event.getView().getTitle().replace("Профиль ", "");
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
                return;
            }

            if (clickedItem.getType() == Material.NAME_TAG) {
                TitleManager titleManager = ((WAIServerCore) Bukkit.getPluginManager().getPlugin("WAIServerCore"))
                        .getTitlesModule().getTitleManager();

                if (titleManager != null) {
                    titleManager.sendTradeRequest(player, target);
                } else {
                    player.sendMessage(ChatColor.RED + "Ошибка: модуль титулов не загружен!");
                }
            }

            if (event.getAction() == InventoryAction.PICKUP_ALL ||
                    event.getAction() == InventoryAction.PICKUP_HALF ||
                    event.getAction() == InventoryAction.PICKUP_ONE ||
                    event.getAction() == InventoryAction.PICKUP_SOME ||
                    event.getAction() == InventoryAction.CLONE_STACK ||
                    event.getAction() == InventoryAction.PLACE_ALL ||
                    event.getAction() == InventoryAction.PLACE_ONE ||
                    event.getAction() == InventoryAction.PLACE_SOME ||
                    event.getAction() == InventoryAction.SWAP_WITH_CURSOR ||
                    event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
        }
    }
}