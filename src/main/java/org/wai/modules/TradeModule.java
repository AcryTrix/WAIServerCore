package org.wai.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TradeModule implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, TradeRequest> tradeRequests = new HashMap<>();

    public TradeModule(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("approvesuffix").setExecutor(this);
    }

    public void sendTradeRequest(Player sender, Player target) {
        TradeRequest request = new TradeRequest(sender, target);
        tradeRequests.put(target, request);

        target.sendMessage("§aВам предложили обмен титулами от " + sender.getName() + ". Принять?");
        target.sendMessage("§aНапишите §e/approvesuffix §aдля подтверждения.");
    }

    private void openTradeMenu(TradeRequest request) {
        Player sender = request.getSender();
        Player target = request.getTarget();

        Inventory senderMenu = Bukkit.createInventory(null, 27, "Выберите суффикс для обмена");
        setupTradeMenu(senderMenu, sender, request, true);

        Inventory targetMenu = Bukkit.createInventory(null, 27, "Выберите суффикс для обмена");
        setupTradeMenu(targetMenu, target, request, false);

        sender.openInventory(senderMenu);
        target.openInventory(targetMenu);
    }

    private void setupTradeMenu(Inventory menu, Player player, TradeRequest request, boolean isSender) {
        ItemStack suffix1 = createSuffixItem("§x§9§7§9§6§E§A ✎", "suffix.5.§x§9§7§9§6§E§A ✎");
        ItemStack suffix2 = createSuffixItem("§x§A§D§F§3§F§D 🗡", "suffix.5.§x§A§D§F§3§F§D 🗡");

        menu.setItem(10, suffix1);
        menu.setItem(12, suffix2);

        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName("§aПринять обмен");
        acceptItem.setItemMeta(acceptMeta);
        menu.setItem(16, acceptItem);
    }

    private ItemStack createSuffixItem(String displayName, String suffix) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList("§7Нажмите, чтобы выбрать этот суффикс."));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Выберите суффикс для обмена")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                String suffix = clickedItem.getItemMeta().getDisplayName();
                TradeRequest request = tradeRequests.get(player);

                if (request != null) {
                    if (player.equals(request.getSender())) {
                        request.setSenderSuffix(suffix);
                    } else {
                        request.setTargetSuffix(suffix);
                    }

                    player.sendMessage("§aВы выбрали суффикс: " + suffix);
                }
            } else if (clickedItem != null && clickedItem.getType() == Material.GREEN_WOOL) {
                TradeRequest request = tradeRequests.get(player);

                if (request != null && request.getSenderSuffix() != null && request.getTargetSuffix() != null) {
                    swapSuffixes(request);
                    player.sendMessage("§aОбмен завершен!");
                    tradeRequests.remove(player);
                } else {
                    player.sendMessage("§cОба игрока должны выбрать суффикс.");
                }
            }
        }
    }

    private void swapSuffixes(TradeRequest request) {
        Player sender = request.getSender();
        Player target = request.getTarget();

        LuckPerms api = LuckPermsProvider.get();
        User senderUser = api.getUserManager().getUser(sender.getUniqueId());
        User targetUser = api.getUserManager().getUser(target.getUniqueId());

        if (senderUser != null && targetUser != null) {
            senderUser.data().add(Node.builder("suffix.100." + request.getTargetSuffix()).build());
            targetUser.data().add(Node.builder("suffix.100." + request.getSenderSuffix()).build());

            api.getUserManager().saveUser(senderUser);
            api.getUserManager().saveUser(targetUser);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("approvesuffix")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                TradeRequest request = tradeRequests.get(player);

                if (request != null) {
                    openTradeMenu(request);
                    return true;
                } else {
                    player.sendMessage("§cУ вас нет активных запросов на обмен.");
                }
            }
        }
        return false;
    }

    public static class TradeRequest {
        private final Player sender;
        private final Player target;
        private String senderSuffix;
        private String targetSuffix;

        public TradeRequest(Player sender, Player target) {
            this.sender = sender;
            this.target = target;
        }

        public Player getSender() {
            return sender;
        }

        public Player getTarget() {
            return target;
        }

        public String getSenderSuffix() {
            return senderSuffix;
        }

        public void setSenderSuffix(String senderSuffix) {
            this.senderSuffix = senderSuffix;
        }

        public String getTargetSuffix() {
            return targetSuffix;
        }

        public void setTargetSuffix(String targetSuffix) {
            this.targetSuffix = targetSuffix;
        }
    }
}