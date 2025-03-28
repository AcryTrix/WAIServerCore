package org.wai.modules.titles;

import org.bukkit.entity.Player;

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
            case "list":
                listTitles(player);
                break;
            case "set":
                setTitle(player, args);
                break;
            case "remove":
                removeTitle(player);
                break;
            default:
                sendHelp(player);
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