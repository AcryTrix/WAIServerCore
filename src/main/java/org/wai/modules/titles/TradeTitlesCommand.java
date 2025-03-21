package org.wai.modules.titles;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.wai.modules.titles.listeners.PlayerListener;
public class TradeTitlesCommand implements CommandExecutor {
    private final TitleManager titleManager;

    public TradeTitlesCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        // Проверяем, указан ли игрок для обмена
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /tradetitles <ник игрока>");
            return true;
        }

        // Получаем целевого игрока
        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок не найден или не в сети!");
            return true;
        }

        // Проверяем, что игрок не пытается обменяться с самим собой
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Вы не можете обменяться титулом с самим собой!");
            return true;
        }

        // Выполняем обмен титулами
        if (titleManager.tradeTitles(player, target)) {
            player.sendMessage(ChatColor.GREEN + "Вы успешно обменялись титулами с " + target.getName() + "!");
            target.sendMessage(ChatColor.GREEN + "Вы успешно обменялись титулами с " + player.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Обмен титулами невозможен! У одного из игроков нет титула.");
        }

        return true;
    }
}