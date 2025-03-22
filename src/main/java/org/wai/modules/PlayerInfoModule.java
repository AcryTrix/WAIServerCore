package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;
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
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_info (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(16) NOT NULL, first_join BIGINT NOT NULL, last_join BIGINT NOT NULL, total_playtime BIGINT NOT NULL, last_ip VARCHAR(45))");
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка создания таблицы: " + e.getMessage());
        }
    }

    public void registerCommandsAndEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("lc").setExecutor(this::onLcCommand);
    }

    private boolean onLcCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cИспользуйте: /lc <ник>");
            return true;
        }
        String username = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM player_info WHERE username = ?")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long firstJoin = rs.getLong("first_join");
                    long lastJoin = rs.getLong("last_join");
                    long totalPlaytime = rs.getLong("total_playtime");
                    String lastIp = rs.getString("last_ip");
                    Player onlinePlayer = Bukkit.getPlayer(uuid);
                    if (onlinePlayer != null) {
                        lastIp = onlinePlayer.getAddress().getAddress().getHostAddress();
                    }
                    String message = "§6Информация об игроке:\n" +
                            "§eНик: §f" + username + "\n" +
                            "§eПервый вход: §f" + dateFormat.format(firstJoin) + "\n" +
                            "§eПоследний вход: §f" + dateFormat.format(lastJoin) + "\n" +
                            "§eЧасов наиграно: §f" + (totalPlaytime / 3600000) + "\n" +
                            "§eПоследний IP: §f" + lastIp;
                    Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(message));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("§cИгрок " + username + " не найден"));
                }
            } catch (SQLException e) {
                handleDatabaseError(sender, e);
            }
        });
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO player_info (uuid, username, first_join, last_join, total_playtime, last_ip) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(uuid) DO UPDATE SET username = excluded.username, last_join = excluded.last_join, last_ip = excluded.last_ip")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, player.getName());
                stmt.setLong(3, currentTime);
                stmt.setLong(4, currentTime);
                stmt.setLong(5, 0);
                stmt.setString(6, player.getAddress().getAddress().getHostAddress());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка обновления данных: " + e.getMessage());
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
                try (PreparedStatement stmt = connection.prepareStatement("UPDATE player_info SET total_playtime = total_playtime + ? WHERE uuid = ?")) {
                    stmt.setLong(1, sessionDuration);
                    stmt.setString(2, uuid.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Ошибка обновления времени: " + e.getMessage());
                }
            });
        }
    }

    private void handleDatabaseError(CommandSender sender, SQLException e) {
        plugin.getLogger().severe("Ошибка базы данных: " + e.getMessage());
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("§cПроизошла ошибка базы данных"));
    }
}