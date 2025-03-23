package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.WAIServerCore;
import org.wai.modules.titles.TitlesModule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProfileModule implements Listener {
    private final JavaPlugin plugin;
    private final TitlesModule titlesModule;

    public ProfileModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titlesModule = ((WAIServerCore) plugin).getTitlesModule();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player target && event.getPlayer().isSneaking()) {
            openProfileMenu(event.getPlayer(), target);
        }
    }

    private void openProfileMenu(Player player, Player target) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Профиль " + target.getName());

        ItemStack infoItem = createInfoItem(target);
        menu.setItem(13, infoItem);

        ItemStack titleExchangeItem = createTitleExchangeItem();
        menu.setItem(5, titleExchangeItem);

        fillInventory(menu);
        player.openInventory(menu);
    }

    private ItemStack createInfoItem(Player target) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eИнформация о профиле");
        List<String> lore = new ArrayList<>();
        lore.add("§7Ник: §f" + target.getName());
        lore.add("§7Время в игре: §f" + getPlayTime(target));
        lore.add("§7Дата входа: §f" + getJoinDate(target));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTitleExchangeItem() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eОбмен титулами");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите, чтобы предложить обмен");
        lore.add("§cТребуются титулы у обоих игроков!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillInventory(Inventory menu) {
        ItemStack filler = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, filler);
            }
        }
    }

    private String getPlayTime(Player player) {
        long playTimeTicks = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
        return (playTimeTicks / 72000) + " ч.";
    }

    private String getJoinDate(Player player) {
        long firstPlayed = player.getFirstPlayed();
        return firstPlayed <= 0 ? "Неизвестно" :
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(firstPlayed));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§6Профиль")) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        String targetName = event.getView().getTitle().replace("§6Профиль ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cИгрок " + targetName + " не в сети!");
            return;
        }

        if (clickedItem.getType() == Material.NAME_TAG) {
            titlesModule.getTitleManager().sendTradeRequest(player, target);
        }
    }
}