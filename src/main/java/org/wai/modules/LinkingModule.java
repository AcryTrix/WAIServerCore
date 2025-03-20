package org.wai.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;

public class LinkingModule {
    private final JavaPlugin plugin;
    private final Connection connection;

    public LinkingModule(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
        plugin.getCommand("link").setExecutor(this::onCommand);
        plugin.getCommand("linkadmin").setExecutor(this::onCommand);
    }

    private boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("link")) {
            handleLinkCommand(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("linkadmin")) {
            handleLinkAdminCommand(sender, args);
            return true;
        }
        return false;
    }

    private void handleLinkCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command for players only!");
            return;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("Usage: /link <code>");
            return;
        }

        try {
            connection.setAutoCommit(false);
            PreparedStatement checkCode = connection.prepareStatement("SELECT discord_id FROM codes WHERE code = ?");
            checkCode.setString(1, args[0]);
            ResultSet result = checkCode.executeQuery();

            if (!result.next()) {
                player.sendMessage("Invalid code!");
                connection.rollback();
                return;
            }

            String discordId = result.getString("discord_id");
            PreparedStatement checkLink = connection.prepareStatement("SELECT minecraft_username FROM linked_accounts WHERE discord_id = ?");
            checkLink.setString(1, discordId);
            ResultSet linkResult = checkLink.executeQuery();

            if (linkResult.next()) {
                player.sendMessage("Account already linked!");
                connection.rollback();
                return;
            }

            PreparedStatement deleteCode = connection.prepareStatement("DELETE FROM codes WHERE code = ?");
            deleteCode.setString(1, args[0]);
            deleteCode.executeUpdate();

            PreparedStatement insertLink = connection.prepareStatement("INSERT INTO linked_accounts (discord_id, minecraft_username) VALUES (?, ?)");
            insertLink.setString(1, discordId);
            insertLink.setString(2, player.getName());
            insertLink.executeUpdate();

            connection.commit();
            player.sendMessage("§aLink successful!");
        } catch (SQLException e) {
            try {
                connection.rollback();
                player.sendMessage("§cError: " + e.getMessage());
            } catch (SQLException ex) {
                plugin.getLogger().severe("Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Auto-commit reset failed");
            }
        }
    }

    private void handleLinkAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("link.admin")) {
            sender.sendMessage("§cNo permission!");
            return;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /linkadmin <player> <discord_id>");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO linked_accounts (discord_id, minecraft_username) VALUES (?, ?)");
            ps.setString(1, args[1]);
            ps.setString(2, args[0]);
            ps.executeUpdate();
            sender.sendMessage("§aLinked: " + args[0] + " → " + args[1]);
        } catch (SQLException e) {
            sender.sendMessage("§cError: " + e.getMessage());
        }
    }
}