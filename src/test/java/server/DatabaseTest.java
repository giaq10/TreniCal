package server;

import it.trenical.server.db.DatabaseManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test JUnit per la connessione al database SQLite
 * Verifica che il DatabaseManager funzioni correttamente
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test Connessione Database SQLite")
class DatabaseTest {

    private static DatabaseManager dbManager;

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST DATABASE ===");
        dbManager = DatabaseManager.getInstance();
        assertNotNull(dbManager, "DatabaseManager non dovrebbe essere null");
    }

    @Test
    @Order(1)
    @DisplayName("Test connessione al database")
    void testConnection() {
        System.out.println("Test: Connessione database");

        // Test che la connessione non sia null
        Connection connection = dbManager.getConnection();
        assertNotNull(connection, "La connessione non dovrebbe essere null");

        // Test che la connessione sia valida
        assertDoesNotThrow(() -> {
            boolean isValid = connection.isValid(5); // timeout 5 secondi
            assertTrue(isValid, "La connessione dovrebbe essere valida");
        });

        System.out.println("✅ Connessione al database stabilita correttamente");
    }

    @Test
    @Order(2)
    @DisplayName("Test esistenza tabelle")
    void testTabelleEsistenti() {
        System.out.println("Test: Verifica esistenza tabelle");

        String[] tabelleAttese = {"clienti", "viaggi", "promozioni", "biglietti"};

        assertDoesNotThrow(() -> {
            Connection conn = dbManager.getConnection();
            for (String tabella : tabelleAttese) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabella + "'")) {

                    assertTrue(rs.next(), "La tabella '" + tabella + "' dovrebbe esistere");
                    assertEquals(tabella, rs.getString("name"), "Nome tabella non corretto");
                    System.out.println("✅ Tabella '" + tabella + "' trovata");
                }
            }
        });
    }

    @Test
    @Order(3)
    @DisplayName("Test dati di test presenti")
    void testDatiDiTest() {
        System.out.println("Test: Verifica dati di test");

        assertDoesNotThrow(() -> {
            Connection conn = dbManager.getConnection();

            // Test clienti
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM clienti")) {
                assertTrue(rs.next());
                int countClienti = rs.getInt("count");
                assertTrue(countClienti >= 3, "Dovrebbero esserci almeno 3 clienti di test");
                System.out.println("✅ Trovati " + countClienti + " clienti");
            }

            // Test promozioni
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM promozioni")) {
                assertTrue(rs.next());
                int countPromozioni = rs.getInt("count");
                assertTrue(countPromozioni >= 3, "Dovrebbero esserci almeno 3 promozioni di test");
                System.out.println("✅ Trovate " + countPromozioni + " promozioni");
            }

            // Test viaggi
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM viaggi")) {
                assertTrue(rs.next());
                int countViaggi = rs.getInt("count");
                assertTrue(countViaggi >= 3, "Dovrebbero esserci almeno 3 viaggi di test");
                System.out.println("✅ Trovati " + countViaggi + " viaggi");
            }
        });
    }

    @Test
    @Order(4)
    @DisplayName("Test metadati database")
    void testMetadatiDatabase() {
        System.out.println("Test: Metadati database");

        assertDoesNotThrow(() -> {
            Connection conn = dbManager.getConnection();
            var metaData = conn.getMetaData();

            // Verifica che sia SQLite
            String dbName = metaData.getDatabaseProductName();
            assertTrue(dbName.toLowerCase().contains("sqlite"),
                    "Il database dovrebbe essere SQLite, trovato: " + dbName);

            // Verifica versione
            String dbVersion = metaData.getDatabaseProductVersion();
            assertNotNull(dbVersion, "La versione del database non dovrebbe essere null");

            System.out.println("✅ Database: " + dbName + " v" + dbVersion);
            System.out.println("✅ Driver: " + metaData.getDriverName() + " v" + metaData.getDriverVersion());
        });
    }

    @Test
    @Order(5)
    @DisplayName("Test operazioni CRUD di base")
    void testOperazioniCRUD() {
        System.out.println("Test: Operazioni CRUD di base");

        assertDoesNotThrow(() -> {
            Connection conn = dbManager.getConnection();

            // Test INSERT temporaneo
            try (Statement stmt = conn.createStatement()) {
                int result = stmt.executeUpdate(
                        "INSERT INTO clienti (email, nome, abbonamento_fedelta) " +
                                "VALUES ('test.crud@junit.com', 'Test CRUD', 0)"
                );
                assertEquals(1, result, "L'inserimento dovrebbe influenzare 1 riga");
            }

            // Test SELECT
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM clienti WHERE email = 'test.crud@junit.com'")) {
                assertTrue(rs.next(), "Il cliente inserito dovrebbe essere trovato");
                assertEquals("Test CRUD", rs.getString("nome"));
                assertFalse(rs.getBoolean("abbonamento_fedelta"));
            }

            // Test UPDATE
            try (Statement stmt = conn.createStatement()) {
                int result = stmt.executeUpdate(
                        "UPDATE clienti SET nome = 'Test CRUD Aggiornato' WHERE email = 'test.crud@junit.com'"
                );
                assertEquals(1, result, "L'aggiornamento dovrebbe influenzare 1 riga");
            }

            // Test DELETE (cleanup)
            try (Statement stmt = conn.createStatement()) {
                int result = stmt.executeUpdate("DELETE FROM clienti WHERE email = 'test.crud@junit.com'");
                assertEquals(1, result, "L'eliminazione dovrebbe influenzare 1 riga");
            }

            System.out.println("✅ Operazioni CRUD funzionano correttamente");
        });
    }

    @Test
    @Order(6)
    @DisplayName("Test foreign keys attive")
    void testForeignKeys() {
        System.out.println("Test: Foreign keys");

        assertDoesNotThrow(() -> {
            Connection conn = dbManager.getConnection();

            // Verifica che le foreign keys siano attive
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(rs.next());
                boolean foreignKeysEnabled = rs.getBoolean(1);
                assertTrue(foreignKeysEnabled, "Le foreign keys dovrebbero essere attive");
                System.out.println("✅ Foreign keys attive");
            }
        });
    }

    @Test
    @Order(7)
    @DisplayName("Test performance connessione")
    void testPerformanceConnessione() {
        System.out.println("Test: Performance connessione");

        // Test che ottenere la connessione sia veloce (< 1 secondo)
        long startTime = System.currentTimeMillis();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                Connection conn = dbManager.getConnection();
                assertNotNull(conn);
            }
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 1000,
                "10 connessioni dovrebbero richiedere meno di 1 secondo, richieste: " + duration + "ms");

        System.out.println("✅ Performance test superato: " + duration + "ms per 10 connessioni");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== PULIZIA TEST DATABASE ===");
        if (dbManager != null) {
            System.out.println("DatabaseManager test completati con successo");
        }
    }
}
