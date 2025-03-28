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

        ItemMeta tradeMeta = tradeToggle.getItemMeta();
            tradeMeta.setLore(Arrays.asList(
            ));
            tradeToggle.setItemMeta(tradeMeta);
        menu.setItem(11, tradeToggle);

        ItemMeta discordMeta = discordInfo.getItemMeta();
            discordMeta.setLore(Arrays.asList(
            ));
            discordInfo.setItemMeta(discordMeta);
        menu.setItem(13, discordInfo);

        }

        ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

                titleManager.toggleTradeSetting(player);
                player.closeInventory();
            player.closeInventory();
        }
    }
}