package org.wai.modules.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wai.WAIServerCore;

public class TitlesModule {
    private final WAIServerCore plugin;
    private final TitleManager titleManager;

    public TitlesModule(WAIServerCore plugin) {
        this.plugin = plugin;
        this.titleManager = new TitleManager(plugin);
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("titles").setExecutor(new TitleCommand(titleManager));
        plugin.getCommand("tradetitles").setExecutor(new TradeTitlesCommand(titleManager));
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(titleManager), plugin);
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}

class TitleCommand implements org.bukkit.command.CommandExecutor {
    private final TitleManager titleManager;

    public TitleCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> listTitles(player);
            case "set" -> setTitle(player, args);
            case "remove" -> removeTitle(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§eКоманды титулов:");
        player.sendMessage("§7/titles list §f- Показать доступные титулы");
        player.sendMessage("§7/titles set <название> §f- Установить титул");
        player.sendMessage("§7/titles remove §f- Убрать текущий титул");
    }

    private void listTitles(Player player) {
        player.sendMessage("§eДоступные титулы:");
        titleManager.getAvailableTitles().forEach(title ->
                player.sendMessage("§7- §f" + title));
    }

    private void setTitle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользуйте: /titles set <название>");
            return;
        }
        if (titleManager.setPlayerTitle(player, args[1])) {
            player.sendMessage("§aТитул успешно установлен!");
        } else {
            player.sendMessage("§cТитул не найден или у вас нет прав!");
        }
    }

    private void removeTitle(Player player) {
        titleManager.removeTitle(player);
        player.sendMessage("§aТитул удален!");
    }
}

class TradeTitlesCommand implements org.bukkit.command.CommandExecutor {
    private final TitleManager titleManager;

    public TradeTitlesCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "accept":
                titleManager.acceptTradeRequest(player);
                return true;
            case "decline":
                titleManager.declineTradeRequest(player);
                return true;
            default:
                processRequest(player, args[0]);
                return true;
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(org.bukkit.ChatColor.GOLD + "Использование:");
        player.sendMessage("/tradetitles <ник> - отправить запрос");
        player.sendMessage("/tradetitles accept - принять");
        player.sendMessage("/tradetitles decline - отклонить");
    }

    private void processRequest(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Игрок не в сети!");
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Нельзя обменяться с собой!");
            return;
        }
        titleManager.sendTradeRequest(sender, target);
    }
}

class PlayerListener implements Listener {
    private final TitleManager titleManager;

    public PlayerListener(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        titleManager.applyTitle(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        titleManager.savePlayerData(event.getPlayer());
    }
}

class TradeRequest {
    private final Player sender;
    private final Player target;

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
}