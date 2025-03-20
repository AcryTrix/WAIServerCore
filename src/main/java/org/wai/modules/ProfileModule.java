package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class ProfileModule implements Listener {

    private final JavaPlugin plugin;

    public ProfileModule(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new ProfileMenuListener(), plugin); // Регистрация слушателя
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
                "Дата присоединения: " + clickedPlayer.getFirstPlayed()
        ));
        infoItem.setItemMeta(infoMeta);
        menu.setItem(14, infoItem);

        player.openInventory(menu);
    }

    private String getPlayTime(Player player) {
        long playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playTimeHours = playTimeTicks / 72000; // 1 час = 72000 тиков
        return playTimeHours + " часов";
    }

    private static class ProfileMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory != null && clickedInventory.getType() == InventoryType.CHEST &&
                clickedInventory.getHolder() == null &&
                event.getView().getTitle().startsWith("Профиль")) {
                event.setCancelled(true); // Запрет забора предметов
            }
        }
    }
}