package org.wai.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand implements CommandExecutor {
    private final SettingsMenu settingsMenu;

    public SettingsCommand(SettingsMenu settingsMenu) {
        this.settingsMenu = settingsMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cКоманда доступна только игрокам!");
            return true;
        }
        Player player = (Player) sender;
        settingsMenu.openSettingsMenu(player); // Вызов метода открытия меню
        return true;
    }
}