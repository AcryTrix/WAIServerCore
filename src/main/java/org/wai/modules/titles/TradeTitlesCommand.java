package org.wai.modules.titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeTitlesCommand implements CommandExecutor {
    private final TitleManager titleManager;

    public TradeTitlesCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команда только для игроков!");
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
        player.sendMessage(ChatColor.GOLD + "Команды обмена титулами:");
        player.sendMessage("§e/tradetitles <ник> §7- Отправить запрос на обмен");
        player.sendMessage("§e/tradetitles accept §7- Принять запрос");
        player.sendMessage("§e/tradetitles decline §7- Отклонить запрос");
    }

    private void processRequest(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + targetName + " не в сети!");
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "Нельзя обменяться титулами с собой!");
            return;
        }
        titleManager.sendTradeRequest(sender, target);
    }
}