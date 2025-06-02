package server;

import it.trenical.common.cliente.Cliente;
import it.trenical.server.db.dao.ClienteDAO;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

/**
 * Test JUnit completo per ClienteDAO
 * Verifica tutte le operazioni CRUD e funzionalità specifiche
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test ClienteDAO - Operazioni Database")
class ClienteDAOTest {

    private static ClienteDAO clienteDAO;
    private static Cliente clienteTest;
    private static final String EMAIL_TEST = "junit.test@trenical.com";
    private static final String NOME_TEST = "Cliente JUnit Test";

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST CLIENTE DAO ===");
        clienteDAO = new ClienteDAO();
        assertNotNull(clienteDAO, "ClienteDAO non dovrebbe essere null");

        // Cliente per i test
        clienteTest = new Cliente(EMAIL_TEST, NOME_TEST, false);
    }

    @BeforeEach
    void setUp() {
        // Assicurati che il cliente test non esista prima di ogni test
        if (clienteDAO.exists(EMAIL_TEST)) {
            clienteDAO.delete(EMAIL_TEST);
        }
    }

    @AfterEach
    void tearDown() {
        // Pulizia dopo ogni test
        if (clienteDAO.exists(EMAIL_TEST)) {
            clienteDAO.delete(EMAIL_TEST);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test conteggio clienti esistenti")
    void testConteggioClientiEsistenti() {
        System.out.println("Test: Conteggio clienti esistenti");

        int count = clienteDAO.count();

        assertTrue(count >= 0, "Il conteggio non dovrebbe essere negativo");
        assertTrue(count >= 3, "Dovrebbero esserci almeno i 3 clienti di test iniziali");

        System.out.printf("✅ Trovati %d clienti nel database%n", count);
    }

    @Test
    @Order(2)
    @DisplayName("Test recupero tutti i clienti")
    void testRecuperoTuttiClienti() {
        System.out.println("Test: Recupero tutti i clienti");

        List<Cliente> tuttiClienti = clienteDAO.findAll();

        assertNotNull(tuttiClienti, "La lista non dovrebbe essere null");
        assertFalse(tuttiClienti.isEmpty(), "La lista non dovrebbe essere vuota");
        assertTrue(tuttiClienti.size() >= 3, "Dovrebbero esserci almeno 3 clienti");

        // Verifica che tutti i clienti abbiano email e nome validi
        for (Cliente cliente : tuttiClienti) {
            assertNotNull(cliente.getEmail(), "Email cliente non dovrebbe essere null");
            assertNotNull(cliente.getNome(), "Nome cliente non dovrebbe essere null");
            assertFalse(cliente.getEmail().trim().isEmpty(), "Email non dovrebbe essere vuota");
            assertFalse(cliente.getNome().trim().isEmpty(), "Nome non dovrebbe essere vuoto");
        }

        System.out.printf("✅ Recuperati %d clienti correttamente%n", tuttiClienti.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test ricerca cliente per email")
    void testRicercaClientePerEmail() {
        System.out.println("Test: Ricerca cliente per email");

        // Test ricerca cliente esistente (dai dati di test)
        Optional<Cliente> marioOpt = clienteDAO.findByEmail("mario.rossi@email.com");

        assertTrue(marioOpt.isPresent(), "Mario Rossi dovrebbe essere trovato");

        Cliente mario = marioOpt.get();
        assertEquals("mario.rossi@email.com", mario.getEmail());
        assertEquals("Mario Rossi", mario.getNome());
        assertTrue(mario.hasAbbonamentoFedelta(), "Mario dovrebbe avere abbonamento fedeltà");

        // Test ricerca cliente inesistente
        Optional<Cliente> inesistenteOpt = clienteDAO.findByEmail("inesistente@email.com");
        assertFalse(inesistenteOpt.isPresent(), "Cliente inesistente non dovrebbe essere trovato");

        System.out.println("✅ Ricerca per email funziona correttamente");
    }

    @Test
    @Order(4)
    @DisplayName("Test inserimento nuovo cliente")
    void testInserimentoNuovoCliente() {
        System.out.println("Test: Inserimento nuovo cliente");

        // Verifica che il cliente non esista
        assertFalse(clienteDAO.exists(EMAIL_TEST), "Cliente test non dovrebbe esistere");

        // Inserimento
        boolean risultato = clienteDAO.save(clienteTest);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica che ora esista
        assertTrue(clienteDAO.exists(EMAIL_TEST), "Cliente test dovrebbe esistere dopo il salvataggio");

        // Verifica recupero
        Optional<Cliente> clienteSalvatoOpt = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteSalvatoOpt.isPresent(), "Cliente salvato dovrebbe essere trovato");

        Cliente clienteSalvato = clienteSalvatoOpt.get();
        assertEquals(EMAIL_TEST, clienteSalvato.getEmail());
        assertEquals(NOME_TEST, clienteSalvato.getNome());
        assertFalse(clienteSalvato.hasAbbonamentoFedelta(), "Abbonamento fedeltà dovrebbe essere false");

        System.out.println("✅ Inserimento nuovo cliente riuscito");
    }

    @Test
    @Order(5)
    @DisplayName("Test aggiornamento cliente esistente")
    void testAggiornamentoClienteEsistente() {
        System.out.println("Test: Aggiornamento cliente esistente");

        // Prima inserisci il cliente
        boolean salvato = clienteDAO.save(clienteTest);
        assertTrue(salvato, "Il cliente dovrebbe essere salvato");

        // Modifica il cliente
        clienteTest.setNome("Nome Aggiornato JUnit");
        clienteTest.attivaAbbonamentoFedelta();

        // Aggiorna nel database
        boolean aggiornato = clienteDAO.update(clienteTest);
        assertTrue(aggiornato, "L'aggiornamento dovrebbe riuscire");

        // Verifica le modifiche
        Optional<Cliente> clienteAggiornato = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteAggiornato.isPresent(), "Cliente aggiornato dovrebbe essere trovato");

        Cliente cliente = clienteAggiornato.get();
        assertEquals("Nome Aggiornato JUnit", cliente.getNome());
        assertTrue(cliente.hasAbbonamentoFedelta(), "Abbonamento fedeltà dovrebbe essere attivo");

        System.out.println("✅ Aggiornamento cliente riuscito");
    }

    @Test
    @Order(6)
    @DisplayName("Test eliminazione cliente")
    void testEliminazioneCliente() {
        System.out.println("Test: Eliminazione cliente");

        // Prima inserisci il cliente
        boolean salvato = clienteDAO.save(clienteTest);
        assertTrue(salvato, "Il cliente dovrebbe essere salvato");
        assertTrue(clienteDAO.exists(EMAIL_TEST), "Cliente dovrebbe esistere");

        // Elimina il cliente
        boolean eliminato = clienteDAO.delete(EMAIL_TEST);
        assertTrue(eliminato, "L'eliminazione dovrebbe riuscire");

        // Verifica che non esista più
        assertFalse(clienteDAO.exists(EMAIL_TEST), "Cliente non dovrebbe più esistere");

        Optional<Cliente> clienteEliminato = clienteDAO.findByEmail(EMAIL_TEST);
        assertFalse(clienteEliminato.isPresent(), "Cliente eliminato non dovrebbe essere trovato");

        System.out.println("✅ Eliminazione cliente riuscita");
    }

    @Test
    @Order(7)
    @DisplayName("Test ricerca clienti fedeltà")
    void testRicercaClientiFedelta() {
        System.out.println("Test: Ricerca clienti fedeltà");

        List<Cliente> clientiFedelta = clienteDAO.findClientiFedelta();

        assertNotNull(clientiFedelta, "Lista clienti fedeltà non dovrebbe essere null");
        assertFalse(clientiFedelta.isEmpty(), "Dovrebbero esserci clienti fedeltà");

        // Verifica che tutti abbiano l'abbonamento fedeltà
        for (Cliente cliente : clientiFedelta) {
            assertTrue(cliente.hasAbbonamentoFedelta(),
                    "Tutti i clienti nella lista dovrebbero avere abbonamento fedeltà: " + cliente.getEmail());
        }

        // Dovrebbero esserci almeno Mario e Luca dai dati di test
        assertTrue(clientiFedelta.size() >= 2, "Dovrebbero esserci almeno 2 clienti fedeltà");

        System.out.printf("✅ Trovati %d clienti con abbonamento fedeltà%n", clientiFedelta.size());
    }

    @Test
    @Order(8)
    @DisplayName("Test operazioni con email non valide")
    void testOperazioniEmailNonValide() {
        System.out.println("Test: Operazioni con email non valide");

        // Test inserimento con email non valida
        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("email-non-valida", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email non valida");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email vuota");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente(null, "Nome Test");
        }, "Dovrebbe lanciare eccezione per email null");

        System.out.println("✅ Validazione email funziona correttamente");
    }

    @Test
    @Order(9)
    @DisplayName("Test operazioni con nomi non validi")
    void testOperazioniNomiNonValidi() {
        System.out.println("Test: Operazioni con nomi non validi");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", null);
        }, "Dovrebbe lanciare eccezione per nome null");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "");
        }, "Dovrebbe lanciare eccezione per nome vuoto");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "   ");
        }, "Dovrebbe lanciare eccezione per nome solo spazi");

        System.out.println("✅ Validazione nome funziona correttamente");
    }

    @Test
    @Order(10)
    @DisplayName("Test scenario completo - Ciclo di vita cliente")
    void testScenarioCompleto() {
        System.out.println("Test: Scenario completo ciclo di vita cliente");

        String emailScenario = "scenario.completo@trenical.com";

        // 1. Cliente inizialmente non esiste
        assertFalse(clienteDAO.exists(emailScenario), "Cliente non dovrebbe esistere inizialmente");
        assertEquals(0, clienteDAO.findByEmail(emailScenario).map(c -> 1).orElse(0));

        // 2. Registrazione nuovo cliente
        Cliente nuovoCliente = new Cliente(emailScenario, "Scenario Completo", false);
        assertTrue(clienteDAO.save(nuovoCliente), "Registrazione dovrebbe riuscire");

        // 3. Verifica registrazione
        assertTrue(clienteDAO.exists(emailScenario), "Cliente dovrebbe esistere dopo registrazione");
        Optional<Cliente> clienteRegistrato = clienteDAO.findByEmail(emailScenario);
        assertTrue(clienteRegistrato.isPresent(), "Cliente registrato dovrebbe essere trovato");
        assertFalse(clienteRegistrato.get().hasAbbonamentoFedelta(), "Cliente non dovrebbe avere fedeltà inizialmente");

        // 4. Upgrade a fedeltà
        Cliente cliente = clienteRegistrato.get();
        cliente.attivaAbbonamentoFedelta();
        assertTrue(clienteDAO.update(cliente), "Aggiornamento fedeltà dovrebbe riuscire");

        // 5. Verifica upgrade
        Optional<Cliente> clienteFedelta = clienteDAO.findByEmail(emailScenario);
        assertTrue(clienteFedelta.isPresent(), "Cliente dovrebbe essere trovato");
        assertTrue(clienteFedelta.get().hasAbbonamentoFedelta(), "Cliente dovrebbe avere fedeltà");

        // 6. Verifica presenza in lista fedeltà
        List<Cliente> clientiFedelta = clienteDAO.findClientiFedelta();
        assertTrue(clientiFedelta.stream().anyMatch(c -> c.getEmail().equals(emailScenario)),
                "Cliente dovrebbe essere nella lista fedeltà");

        // 7. Cancellazione account
        assertTrue(clienteDAO.delete(emailScenario), "Eliminazione dovrebbe riuscire");
        assertFalse(clienteDAO.exists(emailScenario), "Cliente non dovrebbe più esistere");

        System.out.println("✅ Scenario completo ciclo di vita cliente superato");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== PULIZIA TEST CLIENTE DAO ===");

        // Pulizia finale
        if (clienteDAO != null && clienteDAO.exists(EMAIL_TEST)) {
            clienteDAO.delete(EMAIL_TEST);
        }

        // Pulizia scenario completo se rimasto
        if (clienteDAO != null && clienteDAO.exists("scenario.completo@trenical.com")) {
            clienteDAO.delete("scenario.completo@trenical.com");
        }

        System.out.println("Test ClienteDAO completati con successo!");
    }
}
