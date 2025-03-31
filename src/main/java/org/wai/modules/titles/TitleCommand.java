package org.wai.modules.titles;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitleCommand implements CommandExecutor {
    private final TitleManager titleManager;

    public TitleCommand(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listTitles(player);
                break;
            case "set":
                setTitle(player, args);
                break;
            case "remove":
                removeTitle(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "Titles commands:");
        player.sendMessage("/titles list - Show available titles");
        player.sendMessage("/titles set <name> - Set your title");
        player.sendMessage("/titles remove - Remove current title");
    }

    private void listTitles(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Available titles:");
        titleManager.getAvailableTitles().forEach(title ->
                player.sendMessage(ChatColor.GREEN + " - " + title));
    }

    private void setTitle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /titles set <title>");
            return;
        }
        if (titleManager.setPlayerTitle(player, args[1])) {
            player.sendMessage(ChatColor.GREEN + "Title set!");
        } else {
            player.sendMessage(ChatColor.RED + "Title not found or no permission!");
        }
    }

    private void removeTitle(Player player) {
        titleManager.removeTitle(player);
        player.sendMessage(ChatColor.GREEN + "Title removed!");
    }
}