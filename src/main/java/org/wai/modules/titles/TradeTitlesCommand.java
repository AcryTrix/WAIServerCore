package org.wai.modules.titles;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> titleManager.acceptTradeRequest(player);
            case "decline" -> titleManager.declineTradeRequest(player);
            case "player" -> {
                if (args.length < 2) {
                    player.sendMessage("§cУкажите игрока: /tradetitles player <ник>");
                    return true;
                }
                Player target = player.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cИгрок " + args[1] + " не найден!");
                    return true;
                }
                titleManager.sendTradeRequest(player, target);
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§eКоманды обмена титулами:");
        player.sendMessage("§7/tradetitles player <ник> §f- Предложить обмен");
        player.sendMessage("§7/tradetitles accept §f- Принять запрос");
        player.sendMessage("§7/tradetitles decline §f- Отклонить запрос");
    }
}