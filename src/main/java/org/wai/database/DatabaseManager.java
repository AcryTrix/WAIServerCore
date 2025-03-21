package org.wai.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {
    private Connection linksConnection;
    private Connection altsConnection;
    private Connection playerInfoConnection;
    private final Logger logger;

    public DatabaseManager(Logger logger) {
        this.logger = logger;
        initializeDatabases();
    }

    private void initializeDatabases() {
        new File("plugins/WAIServerCore").mkdirs();
        try {
            linksConnection = DriverManager.getConnection("jdbc:sqlite:plugins/WAIServerCore/link_data.db");
            altsConnection = DriverManager.getConnection("jdbc:sqlite:plugins/WAIServerCore/alts_data.db");
            playerInfoConnection = DriverManager.getConnection("jdbc:sqlite:plugins/WAIServerCore/player_info.db");
            createTables();
        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmtLinks = linksConnection.createStatement();
             Statement stmtAlts = altsConnection.createStatement()) {

            stmtLinks.execute("CREATE TABLE IF NOT EXISTS codes (code TEXT PRIMARY KEY, discord_id TEXT)");
            stmtLinks.execute("CREATE TABLE IF NOT EXISTS linked_accounts (discord_id TEXT PRIMARY KEY, minecraft_username TEXT)");
            stmtAlts.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, name TEXT, ip TEXT)");

        } catch (SQLException e) {
            logger.severe("Table creation error: " + e.getMessage());
        }
    }

    public Connection getLinksConnection() {
        return linksConnection;
    }

    public Connection getAltsConnection() {
        return altsConnection;
    }

    public Connection getPlayerInfoConnection() {
        return playerInfoConnection;
    }

    public void closeConnections() {
        closeConnection(linksConnection);
        closeConnection(altsConnection);
        closeConnection(playerInfoConnection);
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.severe("Connection error: " + e.getMessage());
        }
    }
}