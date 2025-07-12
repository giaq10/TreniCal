package it.trenical.server.db;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Gestisce la connessione al database SQLite
 * Pattern Singleton per garantire una sola connessione
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_PATH = "trenical.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            logger.info("Connessione al database SQLite stabilita: " + DB_PATH);
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("Errore nella connessione al database: " + e.getMessage());
            throw new RuntimeException("Impossibile connettersi al database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Ottiene la connessione al database
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                logger.warning("Connessione chiusa, riconnessione in corso...");
                this.connection = DriverManager.getConnection(DB_URL);

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
            }
            return connection;
        } catch (SQLException e) {
            logger.severe("Errore nel recupero della connessione: " + e.getMessage());
            throw new RuntimeException("Errore connessione database", e);
        }
    }
}