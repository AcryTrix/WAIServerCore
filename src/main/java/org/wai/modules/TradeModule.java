package org.wai.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeModule implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<UUID, UUID> tradeRequests = new HashMap<>(); // Хранение запросов

    public TradeModule(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("tradesuffix").setExecutor(this); // Регистрируем команду
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§cИспользование: /tradesuffix <никнейм>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage("§cИгрок не найден или не в сети.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cВы не можете обменяться титулами с самим собой.");
            return true;
        }

        // Отправляем запрос на обмен
        tradeRequests.put(target.getUniqueId(), player.getUniqueId());
        player.sendMessage("§aЗапрос на обмен титулами отправлен игроку " + target.getName() + ".");
        target.sendMessage("§aИгрок " + player.getName() + " предлагает обменяться титулами.");
        target.sendMessage("§aНапишите §e/tradeaccept §aили §e/tradedecline §aдля ответа.");

        return true;
    }

    public void acceptTrade(Player player) {
        UUID senderUuid = tradeRequests.get(player.getUniqueId());

        if (senderUuid == null) {
            player.sendMessage("§cУ вас нет активных запросов на обмен.");
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);

        if (sender == null) {
            player.sendMessage("§cИгрок, отправивший запрос, больше не в сети.");
            return;
        }

        // Меняем суффиксы
        swapSuffixes(sender, player);
        player.sendMessage("§aВы обменялись титулами с " + sender.getName() + ".");
        sender.sendMessage("§aВы обменялись титулами с " + player.getName() + ".");

        // Удаляем запрос
        tradeRequests.remove(player.getUniqueId());
    }

    public void declineTrade(Player player) {
        UUID senderUuid = tradeRequests.get(player.getUniqueId());

        if (senderUuid == null) {
            player.sendMessage("§cУ вас нет активных запросов на обмен.");
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);

        if (sender != null) {
            sender.sendMessage("§cИгрок " + player.getName() + " отклонил ваш запрос на обмен титулами.");
        }

        player.sendMessage("§aВы отклонили запрос на обмен титулами.");

        // Удаляем запрос
        tradeRequests.remove(player.getUniqueId());
    }

    private void swapSuffixes(Player player1, Player player2) {
        LuckPerms api = LuckPermsProvider.get();
        User user1 = api.getUserManager().getUser(player1.getUniqueId());
        User user2 = api.getUserManager().getUser(player2.getUniqueId());

        if (user1 != null && user2 != null) {
            // Получаем текущие суффиксы
            String suffix1 = user1.getCachedData().getMetaData().getSuffix();
            String suffix2 = user2.getCachedData().getMetaData().getSuffix();

            // Меняем суффиксы
            if (suffix1 != null) {
                user2.data().add(Node.builder("suffix.100." + suffix1).build());
            } else {
                user2.data().remove(Node.builder("suffix.100.").build());
            }

            if (suffix2 != null) {
                user1.data().add(Node.builder("suffix.100." + suffix2).build());
            } else {
                user1.data().remove(Node.builder("suffix.100.").build());
            }

            // Сохраняем изменения
            api.getUserManager().saveUser(user1);
            api.getUserManager().saveUser(user2);
        }
    }
}