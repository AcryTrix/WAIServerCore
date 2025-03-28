package org.wai.modules;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkingModule {
    private final JavaPlugin plugin;
    private final Connection connection;

    public LinkingModule(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    public void registerCommands() {
        plugin.getCommand("link").setExecutor((sender, command, label, args) -> {
            handleLinkCommand(sender, args);
            return true;
        });
        plugin.getCommand("linkadmin").setExecutor((sender, command, label, args) -> {
            handleLinkAdminCommand(sender, args);
            return true;
        });
    }

    private void handleLinkCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return;
        }

        if (args.length != 1) {
            player.sendMessage("§cИспользуйте: /link <код>");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                connection.setAutoCommit(false);
                String discordId = verifyCode(args[0]);
                if (discordId == null) {
                    player.sendMessage("§cНеверный или использованный код!");
                    connection.rollback();
                    return;
                }

                if (isAccountLinked(discordId)) {
                    player.sendMessage("§cЭтот Discord-аккаунт уже привязан!");
                    connection.rollback();
                    return;
                }

                linkAccount(discordId, player.getName());
                connection.commit();
                player.sendMessage("§aВаш аккаунт успешно привязан к Discord!");
            } catch (SQLException e) {
                try {
                    connection.rollback();
                    player.sendMessage("§cОшибка при привязке: " + e.getMessage());
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Ошибка отката транзакции: " + ex.getMessage());
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    plugin.getLogger().severe("Ошибка сброса auto-commit: " + e.getMessage());
                }
            }
        });
    }

    private void handleLinkAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("link.admin")) {
            sender.sendMessage("§cУ вас нет прав на эту команду!");
            return;
        }

        if (args.length != 2) {
            sender.sendMessage("§cИспользуйте: /linkadmin <игрок> <discord_id>");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                linkAccount(args[1], args[0]);
                sender.sendMessage("§aАккаунт " + args[0] + " привязан к Discord ID " + args[1]);
            } catch (SQLException e) {
                sender.sendMessage("§cОшибка при привязке: " + e.getMessage());
            }
        });
    }

    private String verifyCode(String code) throws SQLException {
        try (PreparedStatement checkCode = connection.prepareStatement(
                "SELECT discord_id FROM codes WHERE code = ?")) {
            checkCode.setString(1, code);
            ResultSet result = checkCode.executeQuery();
            if (result.next()) {
                String discordId = result.getString("discord_id");
                try (PreparedStatement deleteCode = connection.prepareStatement(
                        "DELETE FROM codes WHERE code = ?")) {
                    deleteCode.setString(1, code);
                    deleteCode.executeUpdate();
                }
                return discordId;
            }
            return null;
        }
    }

    private boolean isAccountLinked(String discordId) throws SQLException {
        try (PreparedStatement checkLink = connection.prepareStatement(
                "SELECT minecraft_username FROM linked_accounts WHERE discord_id = ?")) {
            checkLink.setString(1, discordId);
            ResultSet result = checkLink.executeQuery();
            return result.next();
        }
    }

    private void linkAccount(String discordId, String username) throws SQLException {
        try (PreparedStatement insertLink = connection.prepareStatement(
                "INSERT INTO linked_accounts (discord_id, minecraft_username) VALUES (?, ?)")) {
            insertLink.setString(1, discordId);
            insertLink.setString(2, username);
            insertLink.executeUpdate();
        }
    }
}