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
            sender.sendMessage(ChatColor.RED + "Только для игроков!");
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
                if (titleManager.acceptTradeRequest(player)) {
                    player.sendMessage(ChatColor.GREEN + "Обмен успешно завершен!");
                }
                return true;
            case "decline":
                titleManager.declineTradeRequest(player);
                player.sendMessage(ChatColor.RED + "Вы отклонили запрос");
                return true;
            default:
                processRequest(player, args[0]);
                return true;
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "Использование:");
        player.sendMessage("/tradetitles <ник> - отправить запрос");
        player.sendMessage("/tradetitles accept - принять");
        player.sendMessage("/tradetitles decline - отклонить");
    }

    private void processRequest(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не в сети!");
            return;
        }

        // Используем titleManager из поля класса
        if (!titleManager.canTrade(sender) || !titleManager.canTrade(target)) {
            sender.sendMessage(ChatColor.RED + "Обмен невозможен (проверьте настройки)!");
            return;
        }

        // Отправляем запрос
        titleManager.sendTradeRequest(sender, target);
    }
}