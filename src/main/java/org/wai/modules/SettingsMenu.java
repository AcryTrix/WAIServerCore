package org.wai.modules;

import org.bukkit.Bukkit;
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

        ItemStack tradeToggle = new ItemStack(titleManager.canSendTradeRequest(player) ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta tradeMeta = tradeToggle.getItemMeta();
        tradeMeta.setDisplayName("§eОбмен титулами");
        tradeMeta.setLore(Arrays.asList(
                "§7Состояние: " + (titleManager.canTrade(player) ? "§aВключен" : "§cВыключен"),
                "§7Кликните для переключения"
        ));
        tradeToggle.setItemMeta(tradeMeta);
        menu.setItem(11, tradeToggle);

        ItemStack discordInfo = new ItemStack(Material.BOOK);
        ItemMeta discordMeta = discordInfo.getItemMeta();
        discordMeta.setDisplayName("§eDiscord");
        discordMeta.setLore(Arrays.asList(
                "§7Ссылка: §f" + plugin.getConfig().getString("discord-link", "https://discord.gg/example"),
                "§7Кликните для получения"
        ));
        discordInfo.setItemMeta(discordMeta);
        menu.setItem(13, discordInfo);

        fillInventory(menu);
        player.openInventory(menu);
    }

    private void fillInventory(Inventory menu) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Настройки")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 11) {
            boolean newState = !titleManager.canSendTradeRequest(player);
            titleManager.toggleTradeSetting(player);
            player.sendMessage("§eОбмен титулами " + (newState ? "§aвключен" : "§cвыключен") + "!");
            player.closeInventory();
            openSettingsMenu(player);
        } else if (slot == 13) {
            player.sendMessage("§eСсылка на Discord: §f" + plugin.getConfig().getString("discord-link"));
            player.closeInventory();
        }
    }
}