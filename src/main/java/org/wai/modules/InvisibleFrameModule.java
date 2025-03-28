package org.wai.modules;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class InvisibleFrameModule implements Listener {
    private final JavaPlugin plugin;
    private final Set<Player> cooldownPlayers = new HashSet<>();

    public InvisibleFrameModule(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() == Material.SHEARS && clickedEntity instanceof ItemFrame frame) {
            if (!cooldownPlayers.contains(player)) {
                frame.setVisible(!frame.isVisible());
                cooldownPlayers.add(player);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> cooldownPlayers.remove(player), 20L);
            }
        }
    }
}