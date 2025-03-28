package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AltsModule implements Listener {
    private final JavaPlugin plugin;
    private final Connection connection;

    public AltsModule(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("alts").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("waiservercore.alts")) {
                sender.sendMessage("§cУ вас нет прав на использование этой команды!");
                return true;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (args.length == 0) {
                    handleOnlineAlts(sender);
                } else {
                    handlePlayerAlts(sender, args[0]);
                }
            });
            return true;
        });
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "REPLACE INTO players (uuid, name, ip) VALUES (?, ?, ?)")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, player.getName());
                ps.setString(3, player.getAddress().getAddress().getHostAddress());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка сохранения данных игрока: " + e.getMessage());
            }
        });
    }

    private void handleOnlineAlts(org.bukkit.command.CommandSender sender) {
        Set<String> processedIPs = new HashSet<>();
        boolean foundAlts = false;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String ip = onlinePlayer.getAddress().getAddress().getHostAddress();
            if (processedIPs.contains(ip)) continue;
            processedIPs.add(ip);
            try {
                List<String> names = getNamesByIP(ip);
                if (names.size() > 1) {
                    foundAlts = true;
                    sender.sendMessage(formatAltMessage(ip, names));
                }
            } catch (SQLException e) {
                sender.sendMessage("§cОшибка базы данных при проверке альтов!");
                return;
            }
        }
        if (!foundAlts) {
            sender.sendMessage("§eНа сервере сейчас нет игроков с альтами.");
        }
    }

    private void handlePlayerAlts(org.bukkit.command.CommandSender sender, String targetName) {
        try {
            String targetIP = getIPByName(targetName);
            if (targetIP == null) {
                sender.sendMessage("§cИгрок " + targetName + " не найден в базе данных!");
                return;
            }
            List<String> names = getNamesByIP(targetIP);
            if (names.size() <= 1) {
                sender.sendMessage("§eУ игрока " + targetName + " нет альтов.");
                return;
            }
            sender.sendMessage(formatAltMessage(targetIP, names));
        } catch (SQLException e) {
            sender.sendMessage("§cОшибка базы данных при проверке альтов игрока!");
        }
    }

    private List<String> getNamesByIP(String ip) throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM players WHERE ip = ?")) {
            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    private String getIPByName(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT ip FROM players WHERE name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("ip") : null;
        }
    }

    private String formatAltMessage(String ip, List<String> names) {
        StringBuilder sb = new StringBuilder("§eАльты для IP §f" + ip + "§e:\n");
        for (String name : names) {
            Player player = Bukkit.getPlayerExact(name);
            sb.append("  §7- ")
                    .append(player != null && player.isOnline() ? "§a" : "§8")
                    .append(name)
                    .append("\n");
        }
        return sb.toString();
    }
}