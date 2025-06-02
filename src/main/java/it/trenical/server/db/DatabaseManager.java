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
            // Carica il driver SQLite
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);

            // Abilita foreign keys in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            logger.info("Connessione al database SQLite stabilita: " + DB_PATH);
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("Errore nella connessione al database: " + e.getMessage());
            throw new RuntimeException("Impossibile connettersi al database", e);
        }
    }

    /**
     * Ottiene l'istanza singleton del DatabaseManager
     */
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
            // Verifica se la connessione è ancora valida
            if (connection == null || connection.isClosed()) {
                logger.warning("Connessione chiusa, riconnessione in corso...");
                this.connection = DriverManager.getConnection(DB_URL);

                // Riabilita foreign keys
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

    /**
     * Chiude la connessione al database
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Connessione al database chiusa");
            }
        } catch (SQLException e) {
            logger.severe("Errore nella chiusura della connessione: " + e.getMessage());
        }
    }

    /**
     * Esegue una query di test per verificare la connessione
     */
    public boolean testConnection() {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM clienti")) {

            if (rs.next()) {
                int count = rs.getInt(1);
                logger.info("Test connessione riuscito. Clienti nel database: " + count);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Test connessione fallito: " + e.getMessage());
        }
        return false;
    }

    /**
     * Esegue backup del database (opzionale)
     */
    public void backup(String backupPath) {
        // Implementazione backup se necessaria
        logger.info("Backup del database non implementato");
    }

    /**
     * Ottiene metadati del database
     */
    public void printDatabaseInfo() {
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            logger.info("Database: " + metaData.getDatabaseProductName() +
                    " v" + metaData.getDatabaseProductVersion());
            logger.info("Driver: " + metaData.getDriverName() +
                    " v" + metaData.getDriverVersion());
        } catch (SQLException e) {
            logger.warning("Impossibile ottenere metadati database: " + e.getMessage());
        }
    }
}