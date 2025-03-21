package org.wai.modules.titles;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TradeCommand implements CommandExecutor {

    private final TitleManager titleManager;

    public TradeCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Если аргументов нет, отправляем подсказку
            player.sendMessage(ChatColor.RED + "Использование:");
            player.sendMessage(ChatColor.GRAY + "/tradetitles <ник игрока> - отправить запрос на обмен.");
            return false;
        }

        if (args.length == 1) {
            // Обработка accept/decline
            if (args[0].equalsIgnoreCase("accept")) {
                titleManager.acceptTradeRequest(player);
                return true;
            } else if (args[0].equalsIgnoreCase("decline")) {
                titleManager.declineTradeRequest(player);
                return true;
            } else {
                // Обработка отправки запроса
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок " + args[0] + " не найден или не в сети.");
                    return false;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "Вы не можете обменяться титулами с самим собой.");
                    return false;
                }

                titleManager.sendTradeRequest(player, target);
                return true;
            }
        }

        // Если аргументов больше одного, отправляем подсказку
        player.sendMessage(ChatColor.RED + "Использование:");
        player.sendMessage(ChatColor.GRAY + "/tradetitles <ник игрока> - отправить запрос на обмен.");
        return false;
    }
}