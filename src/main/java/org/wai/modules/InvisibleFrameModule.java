package org.wai.modules; // Замените org.wai.modules на фактический пакет вашего модуля

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin; // JavaPlugin больше не нужен здесь

import java.util.HashSet;
import java.util.Set;

public class InvisibleFrameModule implements Listener {

    private final Plugin plugin;
    private Set<Player> cooldownPlayers = new HashSet<>();

    public InvisibleFrameModule(Plugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("InvisibleFrameModule включен!");
    }

    public void onDisable() {
        plugin.getLogger().info("InvisibleFrameModule выключен!");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.getType() == Material.SHEARS) {
            // Проверка на рамку
            if (clickedEntity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) clickedEntity;

                if (!cooldownPlayers.contains(player)) {

                    frame.setVisible(!frame.isVisible());

                    cooldownPlayers.add(player);

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> { //  Используем plugin для шедулера
                        cooldownPlayers.remove(player);
                    }, 20L);
                }
            }
        }
    }
}