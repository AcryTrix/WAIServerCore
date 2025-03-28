package org.wai.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final String databasePath;
    private Connection altsConnection;
    private Connection linksConnection;
    private Connection playerInfoConnection;

    public DatabaseManager(JavaPlugin plugin, String databasePath) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.databasePath = databasePath;
        initializeDatabases();
    }

    private void initializeDatabases() {
        File dbFolder = new File(databasePath);
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }

        try {
            altsConnection = createConnection("alts.db");
            linksConnection = createConnection("links.db");
            playerInfoConnection = createConnection("player_info.db");

            setupAltsDatabase();
            setupLinksDatabase();
            setupPlayerInfoDatabase();
        } catch (SQLException e) {
            logger.severe("Ошибка инициализации баз данных: " + e.getMessage());
        }
    }

    private Connection createConnection(String dbName) throws SQLException {
        String url = "jdbc:sqlite:" + databasePath + File.separator + dbName;
        return DriverManager.getConnection(url);
    }

    private void setupAltsDatabase() throws SQLException {
        try (var stmt = altsConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "ip TEXT NOT NULL)");
        }
    }

    private void setupLinksDatabase() throws SQLException {
        try (var stmt = linksConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS codes (" +
                    "code TEXT PRIMARY KEY, " +
                    "discord_id TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS linked_accounts (" +
                    "discord_id TEXT PRIMARY KEY, " +
                    "minecraft_username TEXT NOT NULL)");
        }
    }

    private void setupPlayerInfoDatabase() throws SQLException {
        try (var stmt = playerInfoConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS player_info (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "first_join BIGINT NOT NULL, " +
                    "last_join BIGINT NOT NULL, " +
                    "total_playtime BIGINT NOT NULL, " +
                    "last_ip TEXT)");
        }
    }

    public Connection getAltsConnection() {
        return altsConnection;
    }

    public Connection getLinksConnection() {
        return linksConnection;
    }

    public Connection getPlayerInfoConnection() {
        return playerInfoConnection;
    }

    public void closeConnections() {
        try {
            if (altsConnection != null && !altsConnection.isClosed()) altsConnection.close();
            if (linksConnection != null && !linksConnection.isClosed()) linksConnection.close();
            if (playerInfoConnection != null && !playerInfoConnection.isClosed()) playerInfoConnection.close();
            logger.info("Соединения с базами данных закрыты");
        } catch (SQLException e) {
            logger.severe("Ошибка при закрытии соединений: " + e.getMessage());
        }
    }
}