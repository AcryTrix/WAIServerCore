package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;
import java.util.*;

public class AltsModule implements Listener {
    private final JavaPlugin plugin;
    private final Connection connection;

    public AltsModule(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("alts").setExecutor(this::onCommand);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            PreparedStatement ps = connection.prepareStatement("REPLACE INTO players (uuid, name, ip) VALUES (?, ?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.setString(3, player.getAddress().getAddress().getHostAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Player data save error");
        }
    }

    private boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("waiservercore.alts")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length == 0) {
            handleOnlineAlts(sender);
        } else {
            handlePlayerAlts(sender, args[0]);
        }
        return true;
    }

    private void handleOnlineAlts(CommandSender sender) {
        Set<String> processedIPs = new HashSet<>();
        boolean foundAlts = false;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getAddress() == null) continue;

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
                sender.sendMessage(ChatColor.RED + "Database error");
                return;
            }
        }

        if (!foundAlts) {
            sender.sendMessage(ChatColor.YELLOW + "No alts found online.");
        }
    }

    private void handlePlayerAlts(CommandSender sender, String targetName) {
        try {
            String targetIP = getIPByName(targetName);
            if (targetIP == null) {
                sender.sendMessage(ChatColor.RED + "Player not found");
                return;
            }

            List<String> names = getNamesByIP(targetIP);
            if (names.size() <= 1) {
                sender.sendMessage(ChatColor.YELLOW + "No alts found for " + targetName);
                return;
            }

            sender.sendMessage(formatAltMessage(targetIP, names));
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Database error");
        }
    }

    private List<String> getNamesByIP(String ip) throws SQLException {
        List<String> names = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT name FROM players WHERE ip = ?");
        ps.setString(1, ip);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            names.add(rs.getString("name"));
        }
        return names;
    }

    private String getIPByName(String name) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT ip FROM players WHERE name = ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("ip") : null;
    }

    private String formatAltMessage(String ip, List<String> names) {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.YELLOW).append(ip).append(" - ");

        for (String name : names) {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null && player.isOnline()) {
                sb.append(ChatColor.GREEN).append(name);
            } else {
                sb.append(ChatColor.DARK_GRAY).append(name);
            }
            sb.append(ChatColor.YELLOW).append(", ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
}