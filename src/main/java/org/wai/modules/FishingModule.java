package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import org.wai.WAIServerCore;

import java.util.*;

public class FishingModule implements Listener {
    private final WAIServerCore plugin;
    private final Map<Player, FishingGame> activeGames = new HashMap<>();
    private final List<FishingItem> fishingItems;

    public FishingModule(WAIServerCore plugin) {
        this.plugin = plugin;
        this.fishingItems = loadFishingItems();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private List<FishingItem> loadFishingItems() {
        List<FishingItem> items = new ArrayList<>();
        if (plugin.getConfigManager().getTomlConfig() == null) {
            items.add(new FishingItem(Material.COD, 1.0, "COD"));
            return items;
        }

        TomlArray configItems = plugin.getConfigManager().getTomlConfig().getArray("fishing.items");
        if (configItems != null) {
            for (int i = 0; i < configItems.size(); i++) {
                TomlTable item = configItems.getTable(i);
                String materialName = item.getString("material");
                Double chance = item.getDouble("chance");
                if (materialName != null && chance != null) {
                    Material material = materialName.equals("MENDING_BOOK") ?
                            Material.ENCHANTED_BOOK : Material.getMaterial(materialName);
                    if (material != null) {
                        items.add(new FishingItem(material, chance, materialName));
                    } else {
                        plugin.getLogger().warning("Invalid material: " + materialName);
                    }
                }
            }
        }
        if (items.isEmpty()) {
            items.add(new FishingItem(Material.COD, 1.0, "COD"));
        }
        return items;
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
        FishingItem selectedItem = selectRandomItem();
        if (selectedItem == null) {
            player.sendMessage("Error starting fishing game");
            return;
        }
        ItemStack fishItem = selectedItem.toItemStack(true);
        inv.setItem(11, fishItem);
        inv.setItem(21, new ItemStack(Material.FEATHER));
        inv.setItem(23, new ItemStack(Material.STONE));
        player.openInventory(inv);
        FishingGame game = new FishingGame(player, inv, 11, 12, 3, selectedItem);
        activeGames.put(player, game);
        game.start();
    }

    private FishingItem selectRandomItem() {
        if (fishingItems.isEmpty()) {
            return null;
        }
        double totalChance = fishingItems.stream().mapToDouble(item -> item.chance).sum();
        if (totalChance <= 0) {
            return fishingItems.get(0);
        }
        double random = Math.random() * totalChance;
        double current = 0;
        for (FishingItem item : fishingItems) {
            current += item.chance;
            if (random <= current) {
                return item;
            }
        }
        return fishingItems.get(0); // Fallback
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
            game.moveCatchZone(-1);
        } else if (slot == 23) {
            game.moveCatchZone(1);
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
        private final double maxTime = 10;
        private final double noRewardThreshold = 16;
        private BukkitTask updateTask;
        private final FishingItem rewardItem;

        public FishingGame(Player player, Inventory inventory, int initialFishPosition, int initialCatchZoneStart, int catchZoneSize, FishingItem rewardItem) {
            this.player = player;
            this.inventory = inventory;
            this.fishPosition = initialFishPosition;
            this.catchZoneStart = initialCatchZoneStart;
            this.catchZoneSize = catchZoneSize;
            this.rewardItem = rewardItem;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public void start() {
            if (updateTask != null && !updateTask.isCancelled()) {
                updateTask.cancel();
            }
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
                    } else if (timeElapsed >= maxTime && timeElapsed < noRewardThreshold) {
                        endGame(false);
                    } else if (timeElapsed >= noRewardThreshold) {
                        endGameWithNoReward();
                    }
                }
            }.runTaskTimer(plugin, 0, 5L);
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
            inventory.setItem(fishPosition, rewardItem.toItemStack(true));
        }

        public void endGame(boolean success) {
            if (updateTask != null) {
                updateTask.cancel();
            }
            activeGames.remove(player);
            if (player.isOnline()) {
                player.closeInventory();
                if (success) {
                    player.sendMessage("You caught something!");
                    ItemStack reward = rewardItem.toItemStack(false);
                    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(reward);
                    if (!leftovers.isEmpty()) {
                        player.getWorld().dropItem(player.getLocation(), reward);
                    }
                    plugin.getLogger().info(player.getName() + " caught a " + reward.getType().name());
                } else {
                    player.sendMessage("The catch escaped.");
                }
            }
        }

        public void endGameWithNoReward() {
            if (updateTask != null) {
                updateTask.cancel();
            }
            activeGames.remove(player);
            if (player.isOnline()) {
                player.closeInventory();
                player.sendMessage("You took too long and caught nothing.");
            }
        }
    }

    private static class FishingItem {
        private final Material material;
        private final double chance;
        private final String originalName;
        private static final List<Enchantment> ENCHANTMENTS = Arrays.asList(Enchantment.values());

        public FishingItem(Material material, double chance, String originalName) {
            this.material = material;
            this.chance = chance;
            this.originalName = originalName;
        }

        public ItemStack toItemStack(boolean isDisplay) {
            ItemStack item = new ItemStack(material);
            if (!isDisplay && item.getItemMeta() != null) {
                // Only apply Mending enchantment for MENDING_BOOK
                if (originalName.equals("MENDING_BOOK")) {
                    item = new ItemStack(Material.ENCHANTED_BOOK);
                    int level = (int) (Math.random() * 3) + 1;
                    item.addEnchantment(Enchantment.MENDING, level);
                } else if (Math.random() < 0.5 && material != Material.BOOK && material != Material.ENCHANTED_BOOK) {
                    // Apply random enchantment only to non-book items with 50% chance
                    Enchantment enchantment = getRandomApplicableEnchantment(item);
                    int level = (int) (Math.random() * 3) + 1;
                    if (enchantment != null) {
                        try {
                            item.addEnchantment(enchantment, level);
                        } catch (IllegalArgumentException e) {
                            Bukkit.getLogger().warning("Failed to apply " + enchantment.getKey() + " to " + material.name());
                        }
                    }
                }
                if (material.isItem() && material.getMaxDurability() > 0) {
                    Damageable damageable = (Damageable) item.getItemMeta();
                    if (damageable != null) {
                        int maxDurability = material.getMaxDurability();
                        damageable.setDamage(Math.min(maxDurability - 5, maxDurability));
                        item.setItemMeta(damageable);
                    }
                }
            }
            return item;
        }

        private Enchantment getRandomApplicableEnchantment(ItemStack item) {
            List<Enchantment> applicable = new ArrayList<>();
            for (Enchantment ench : ENCHANTMENTS) {
                if (ench.canEnchantItem(item)) {
                    applicable.add(ench);
                }
            }
            if (applicable.isEmpty()) {
                return null;
            }
            return applicable.get((int) (Math.random() * applicable.size()));
        }
    }
}