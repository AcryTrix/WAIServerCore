package org.wai.modules.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class TradeTitlesCommand implements org.bukkit.command.CommandExecutor {
    private final TitleManager titleManager;

    public TradeTitlesCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Только для игроков!");
            return true;
        }
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