package org.wai.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TradeCommandHandler implements CommandExecutor {

    private final TradeModule tradeModule;

    public TradeCommandHandler(TradeModule tradeModule) {
        this.tradeModule = tradeModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tradeaccept")) {
            tradeModule.acceptTrade(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("tradedecline")) {
            tradeModule.declineTrade(player);
            return true;
        }

        return false;
    }
}