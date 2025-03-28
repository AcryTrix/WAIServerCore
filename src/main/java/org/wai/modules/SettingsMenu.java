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
import org.wai.config.ConfigManager;
import org.wai.modules.titles.TitleManager;

import java.util.Arrays;

public class SettingsMenu implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final TitleManager titleManager;

    public SettingsMenu(JavaPlugin plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.configManager = ((org.wai.WAIServerCore) plugin).getConfigManager();
        this.titleManager = titleManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSettingsMenu(Player player) {
        String title = configManager.getString("settings_menu.title");
        Inventory menu = Bukkit.createInventory(null, 27, title != null ? title : "§6Настройки");

        ItemStack tradeToggle = new ItemStack(Material.CHEST);
        ItemMeta tradeMeta = tradeToggle.getItemMeta();
        tradeMeta.setDisplayName("§eНастройки торговли");
        tradeMeta.setLore(Arrays.asList("§7Нажмите для переключения"));
        tradeToggle.setItemMeta(tradeMeta);
        menu.setItem(11, tradeToggle);

        String discordLink = configManager.getString("settings_menu.discord_link");
        ItemStack discordInfo = new ItemStack(Material.PAPER);
        ItemMeta discordMeta = discordInfo.getItemMeta();
        discordMeta.setDisplayName("§bDiscord");
        discordMeta.setLore(Arrays.asList("§7Наш Discord: " + (discordLink != null ? discordLink : "не указан")));
        discordInfo.setItemMeta(discordMeta);
        menu.setItem(13, discordInfo);

        boolean fillEmptySlots = configManager.getBoolean("settings_menu.fill_empty_slots");
        if (fillEmptySlots) {
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

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(configManager.getString("settings_menu.title"))) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.CHEST) {
            player.closeInventory();
        } else if (clicked.getType() == Material.PAPER) {
            player.closeInventory();
        }
    }
}