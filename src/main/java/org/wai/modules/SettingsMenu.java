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
import java.util.Arrays;

public class SettingsMenu implements Listener {
    private final JavaPlugin plugin;
    private final TitleManager titleManager;

    public SettingsMenu(JavaPlugin plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.titleManager = titleManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSettingsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Настройки");

        // Кнопка "Обмен титулами"
        ItemStack tradeToggle = new ItemStack(
                titleManager.canSendTradeRequest(player) ?
                        Material.LIME_DYE : Material.GRAY_DYE
        );
        ItemMeta tradeMeta = tradeToggle.getItemMeta();
        tradeMeta.setDisplayName("§eОбмен титулами");
        tradeMeta.setLore(Arrays.asList(
                "§7Статус: " + (titleManager.canTrade(player) ? "§aВКЛ" : "§cВЫКЛ"),
                "",
                "§7Если выключено:",
                "§8- Вы не можете отправлять запросы",
                "§8- Вам не могут отправлять запросы"
        ));
        tradeToggle.setItemMeta(tradeMeta);
        menu.setItem(11, tradeToggle);

        // Кнопка "Discord"
        ItemStack discordInfo = new ItemStack(Material.BOOK);
        ItemMeta discordMeta = discordInfo.getItemMeta();
        discordMeta.setDisplayName("§9Discord");
        discordMeta.setLore(Arrays.asList(
                "§7Ссылка: §f" + plugin.getConfig().getString("discord-link", "https://discord.gg/example")
        ));
        discordInfo.setItemMeta(discordMeta);
        menu.setItem(13, discordInfo);

        // Заполнение пустых слотов
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            if (menu.getItem(i) == null) menu.setItem(i, filler);
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Настройки")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (event.getSlot()) {
            case 11: // Кнопка "Обмен титулами"
                boolean newState = !titleManager.canSendTradeRequest(player);
                titleManager.toggleTradeSetting(player);
                player.sendMessage("§6Обмен титулами: " + (newState ? "§aВКЛ" : "§cВЫКЛ"));
                player.closeInventory();
                break;
            case 13: // Кнопка "Discord"
                player.sendMessage("§6Discord: §9" + plugin.getConfig().getString("discord-link"));
                break;
        }
    }
}