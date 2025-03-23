package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.wai.WAIServerCore;
import java.util.HashMap;
import java.util.Map;

public class FishingModule implements Listener {
    private final WAIServerCore plugin;
    private final Map<Player, FishingGame> activeGames = new HashMap<>();

    public FishingModule(WAIServerCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!activeGames.containsKey(player)) {
                startMiniGame(player);
            }
        }
    }

    private void startMiniGame(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Fishing Mini-Game");
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, background);
        }
        ItemStack catchZoneItem = new ItemStack(Material.GREEN_WOOL);
        for (int i = 12; i <= 14; i++) {
            inv.setItem(i, catchZoneItem);
        }
        ItemStack fishItem = new ItemStack(Material.COD);
        inv.setItem(11, fishItem);
        inv.setItem(21, new ItemStack(Material.FEATHER));
        inv.setItem(23, new ItemStack(Material.STONE));
        player.openInventory(inv);
        FishingGame game = new FishingGame(player, inv, 11, 12, 3);
        activeGames.put(player, game);
        game.start();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!activeGames.containsKey(player)) return;
        if (!event.getInventory().equals(activeGames.get(player).getInventory())) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        FishingGame game = activeGames.get(player);
        if (slot == 21) {
            game.moveCatchZone(1);
        } else if (slot == 23) {
            game.moveCatchZone(-1);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (activeGames.containsKey(player)) {
            FishingGame game = activeGames.get(player);
            game.endGame(false);
        }
    }

    private class FishingGame {
        private final Player player;
        private final Inventory inventory;
        private int fishPosition;
        private int catchZoneStart;
        private final int catchZoneSize;
        private double catchProgress = 0;
        private final double catchThreshold = 5;
        private double timeElapsed = 0;
        private final double maxTime = 20;
        private BukkitTask updateTask;

        public FishingGame(Player player, Inventory inventory, int initialFishPosition, int initialCatchZoneStart, int catchZoneSize) {
            this.player = player;
            this.inventory = inventory;
            this.fishPosition = initialFishPosition;
            this.catchZoneStart = initialCatchZoneStart;
            this.catchZoneSize = catchZoneSize;
        }

        public void start() {
            updateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    int move = (int) (Math.random() * 3) - 1;
                    fishPosition = Math.max(9, Math.min(17, fishPosition + move));
                    if (fishPosition >= catchZoneStart && fishPosition < catchZoneStart + catchZoneSize) {
                        catchProgress += 0.25;
                    }
                    timeElapsed += 0.25;
                    updateInventory();
                    if (catchProgress >= catchThreshold) {
                        endGame(true);
                    } else if (timeElapsed >= maxTime) {
                        endGame(false);
                    }
                }
            }.runTaskTimer(plugin, 0, 5);
        }

        public void moveCatchZone(int direction) {
            int newStart = catchZoneStart + direction;
            if (newStart >= 9 && newStart + catchZoneSize - 1 <= 17) {
                catchZoneStart = newStart;
                updateInventory();
            }
        }

        private void updateInventory() {
            for (int i = 9; i <= 17; i++) {
                inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
            for (int i = catchZoneStart; i < catchZoneStart + catchZoneSize; i++) {
                inventory.setItem(i, new ItemStack(Material.GREEN_WOOL));
            }
            inventory.setItem(fishPosition, new ItemStack(Material.COD));
        }

        public void endGame(boolean success) {
            updateTask.cancel();
            activeGames.remove(player);
            player.closeInventory();
            if (success) {
                player.sendMessage("You caught a fish!");
                player.getInventory().addItem(new ItemStack(Material.COD));
            } else {
                player.sendMessage("The fish escaped.");
            }
        }

        public Inventory getInventory() {
            return inventory;
        }
    }
}