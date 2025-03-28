package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfoModule implements Listener {
    private final JavaPlugin plugin;
    private final Connection connection;
    private final Map<UUID, Long> sessionStarts = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public PlayerInfoModule(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    public void registerCommandsAndEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("lc").setExecutor((sender, command, label, args) -> {
            if (args.length != 1) {
                sender.sendMessage("§cИспользуйте: /lc <ник>");
                return true;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM player_info WHERE username = ?")) {
                    stmt.setString(1, args[0]);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        sendPlayerInfo(sender, rs, args[0]);
                    } else {
                        sender.sendMessage("§cИгрок " + args[0] + " не найден!");
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Ошибка БД: " + e.getMessage());
                    sender.sendMessage("§cОшибка при запросе данных!");
                }
            });
            return true;
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO player_info (uuid, username, first_join, last_join, total_playtime, last_ip) " +
                            "VALUES (?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET username = ?, last_join = ?, last_ip = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, player.getName());
                stmt.setLong(3, currentTime);
                stmt.setLong(4, currentTime);
                stmt.setLong(5, 0);
                stmt.setString(6, player.getAddress().getAddress().getHostAddress());
                stmt.setString(7, player.getName());
                stmt.setLong(8, currentTime);
                stmt.setString(9, player.getAddress().getAddress().getHostAddress());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка записи данных игрока: " + e.getMessage());
            }
        });
        sessionStarts.put(uuid, currentTime);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Long sessionStart = sessionStarts.remove(uuid);
        if (sessionStart != null) {
            long sessionDuration = System.currentTimeMillis() - sessionStart;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE player_info SET total_playtime = total_playtime + ? WHERE uuid = ?")) {
                    stmt.setLong(1, sessionDuration);
                    stmt.setString(2, uuid.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Ошибка обновления времени игры: " + e.getMessage());
                }
            });
        }
    }

    private void sendPlayerInfo(org.bukkit.command.CommandSender sender, ResultSet rs, String username) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        long firstJoin = rs.getLong("first_join");
        long lastJoin = rs.getLong("last_join");
        long totalPlaytime = rs.getLong("total_playtime");
        String lastIp = rs.getString("last_ip");
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            lastIp = onlinePlayer.getAddress().getAddress().getHostAddress();
        }
        String message = "§aИнформация об игроке §f" + username + "§a:\n" +
                "§eПервый вход: §f" + dateFormat.format(firstJoin) + "\n" +
                "§eПоследний вход: §f" + dateFormat.format(lastJoin) + "\n" +
                "§eВремя в игре: §f" + (totalPlaytime / 3600000) + " ч.\n" +
                "§eПоследний IP: §f" + lastIp;
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }
}