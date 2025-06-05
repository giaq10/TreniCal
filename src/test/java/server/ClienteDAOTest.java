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
    private static final String PASSWORD_TEST = "fedeMiao";
    private static final String NOME_TEST = "Cliente JUnit Test";

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST CLIENTE DAO ===");
        clienteDAO = new ClienteDAO();
        assertNotNull(clienteDAO, "ClienteDAO non dovrebbe essere null");

        // Cliente per i test
        clienteTest = new Cliente(EMAIL_TEST, PASSWORD_TEST,NOME_TEST, false);
        System.out.println("Cliente test creato: " + clienteTest.toString());
        System.out.println("Password test: " + PASSWORD_TEST);
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
            assertNotNull(cliente.getPassword(), "Password cliente non dovrebbe essere null");
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
    @DisplayName("Test inserimento nuovo cliente con password")
    void testInserimentoNuovoClienteConPassword() {
        System.out.println("Test: Inserimento nuovo cliente con password");

        // Verifica che il cliente non esista
        assertFalse(clienteDAO.exists(EMAIL_TEST), "Cliente test non dovrebbe esistere");

        // Inserimento
        boolean risultato = clienteDAO.save(clienteTest);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica che ora esista
        assertTrue(clienteDAO.exists(EMAIL_TEST), "Cliente test dovrebbe esistere dopo il salvataggio");

        // Verifica recupero completo
        Optional<Cliente> clienteSalvatoOpt = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteSalvatoOpt.isPresent(), "Cliente salvato dovrebbe essere trovato");

        Cliente clienteSalvato = clienteSalvatoOpt.get();
        assertEquals(EMAIL_TEST, clienteSalvato.getEmail());
        assertEquals(PASSWORD_TEST, clienteSalvato.getPassword());
        assertEquals(NOME_TEST, clienteSalvato.getNome());
        assertFalse(clienteSalvato.hasAbbonamentoFedelta(), "Abbonamento fedeltà dovrebbe essere false");

        System.out.println("Cliente salvato: " + clienteSalvato.toString());
        System.out.println("Password verificata: " + clienteSalvato.getPassword());

        System.out.println("✅ Inserimento nuovo cliente con password riuscito");
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
        clienteTest.modificaPassword("federicossss");
        clienteTest.attivaAbbonamentoFedelta();

        // Aggiorna nel database
        boolean aggiornato = clienteDAO.update(clienteTest);
        assertTrue(aggiornato, "L'aggiornamento dovrebbe riuscire");

        // Verifica le modifiche
        Optional<Cliente> clienteAggiornato = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteAggiornato.isPresent(), "Cliente aggiornato dovrebbe essere trovato");

        Cliente cliente = clienteAggiornato.get();
        assertEquals("Nome Aggiornato JUnit", cliente.getNome());
        assertEquals("federicossss", cliente.getPassword());
        assertTrue(cliente.hasAbbonamentoFedelta(), "Abbonamento fedeltà dovrebbe essere attivo");

        clienteTest.setNome(NOME_TEST);
        clienteTest.modificaPassword(PASSWORD_TEST);
        clienteTest.disattivaAbbonamentoFedelta();

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
            new Cliente("email-non-valida", "password123", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email non valida");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("", "password123", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email vuota");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente(null, "password123", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email null");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("emailsenzachiocciola", "federicchio", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email senza @");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@", "ricchiofede", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email senza dominio");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email", "ciccioooo", "Nome Test");
        }, "Dovrebbe lanciare eccezione per email senza TLD");

        System.out.println("✅ Validazione email funziona correttamente");
    }

    @Test
    @Order(9)
    @DisplayName("Test operazioni con nomi non validi")
    void testOperazioniNomiNonValidi() {
        System.out.println("Test: Operazioni con nomi non validi");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "federicchio", null);
        }, "Dovrebbe lanciare eccezione per nome null");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "federicchio", "");
        }, "Dovrebbe lanciare eccezione per nome vuoto");

        assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "federicchio", "   ");
        }, "Dovrebbe lanciare eccezione per nome solo spazi");

        System.out.println("✅ Validazione nome funziona correttamente");
    }

    @Test
    @Order(10)
    @DisplayName("Test operazioni con password non valide")
    void testOperazioniPasswordNonValide() {
        System.out.println("Test: Operazioni con password non valide");

        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test1@email.com", null, "Nome Test");
        }, "Dovrebbe lanciare eccezione per password null");
        assertTrue(exception1.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("Password null rifiutata: " + exception1.getMessage());

        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test2@email.com", "", "Nome Test");
        }, "Dovrebbe lanciare eccezione per password vuota");
        assertTrue(exception2.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("Password vuota rifiutata: " + exception2.getMessage());

        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test3@email.com", "a", "Nome Test");
        }, "Dovrebbe lanciare eccezione per password 1 carattere");
        assertTrue(exception3.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("Password 1 carattere rifiutata: " + exception3.getMessage());

        // Test password troppo corta (3 caratteri)
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test4@email.com", "abc", "Nome Test");
        }, "Dovrebbe lanciare eccezione per password 3 caratteri");
        assertTrue(exception4.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("Password 3 caratteri rifiutata: " + exception4.getMessage());

        // Test password troppo corta (5 caratteri)
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test5@email.com", "12345", "Nome Test");
        }, "Dovrebbe lanciare eccezione per password 5 caratteri");
        assertTrue(exception5.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("Password 5 caratteri rifiutata: " + exception5.getMessage());

        // Test password valida (esattamente 6 caratteri)
        assertDoesNotThrow(() -> {
            Cliente cliente6 = new Cliente("test6@email.com", "123456", "Nome Test");
            assertEquals("123456", cliente6.getPassword());
        }, "Password di 6 caratteri dovrebbe essere accettata");
        System.out.println("Password 6 caratteri accettata");

        // Test password valida (7 caratteri)
        assertDoesNotThrow(() -> {
            Cliente cliente7 = new Cliente("test7@email.com", "1234567", "Nome Test");
            assertEquals("1234567", cliente7.getPassword());
        }, "Password di 7 caratteri dovrebbe essere accettata");
        System.out.println("Password 7 caratteri accettata");

        // Test password valida (password lunga)
        assertDoesNotThrow(() -> {
            Cliente cliente8 = new Cliente("test8@email.com", "passwordmoltolunga123", "Nome Test");
            assertEquals("passwordmoltolunga123", cliente8.getPassword());
        }, "Password lunga dovrebbe essere accettata");
        System.out.println("Password lunga accettata");

        // Test password con spazi (dovrebbe essere accettata se >= 6 caratteri)
        assertDoesNotThrow(() -> {
            Cliente cliente9 = new Cliente("test9@email.com", "pass word", "Nome Test");
            assertEquals("pass word", cliente9.getPassword());
        }, "Password con spazi dovrebbe essere accettata se >= 6 caratteri");
        System.out.println("✅ Password con spazi accettata");

        // Test password con caratteri speciali
        assertDoesNotThrow(() -> {
            Cliente cliente10 = new Cliente("test10@email.com", "p@ss!123", "Nome Test");
            assertEquals("p@ss!123", cliente10.getPassword());
        }, "Password con caratteri speciali dovrebbe essere accettata");
        System.out.println("✅ Password con caratteri speciali accettata");

        System.out.println("✅ Validazione password funziona correttamente");
    }

    @Test
    @Order(11)
    @DisplayName("Test scenario completo - Registrazione e autenticazione")
    void testScenarioCompletoRegistrazioneAutenticazione() {
        System.out.println("Test: Scenario completo registrazione e autenticazione");

        String emailScenario = "scenario.completo@trenical.com";
        String passwordScenario = "scenariopass123";
        String nomeScenario = "Scenario Completo";

        // 1. Cliente inizialmente non esiste
        assertFalse(clienteDAO.exists(emailScenario), "Cliente non dovrebbe esistere inizialmente");

        // 2. Registrazione nuovo cliente
        Cliente nuovoCliente = new Cliente(emailScenario, passwordScenario, nomeScenario, false);
        assertTrue(clienteDAO.save(nuovoCliente), "Registrazione dovrebbe riuscire");

        System.out.println("1. Registrazione completata: " + nuovoCliente.toString());

        // 3. Verifica registrazione
        assertTrue(clienteDAO.exists(emailScenario), "Cliente dovrebbe esistere dopo registrazione");
        Optional<Cliente> clienteRegistrato = clienteDAO.findByEmail(emailScenario);
        assertTrue(clienteRegistrato.isPresent(), "Cliente registrato dovrebbe essere trovato");

        // 4. Test autenticazione
        Cliente cliente = clienteRegistrato.get();
        assertTrue(cliente.autenticaPassword(passwordScenario), "Autenticazione dovrebbe riuscire");
        assertFalse(cliente.autenticaPassword("passwordsbagliata"), "Password sbagliata dovrebbe fallire");

        System.out.println("2. Autenticazione testata con successo");

        // 5. Modifica password
        String passwordNuova = "nuovapass456";
        cliente.modificaPassword(passwordNuova);
        assertTrue(clienteDAO.update(cliente), "Aggiornamento password dovrebbe riuscire");

        // 6. Test autenticazione con nuova password
        Optional<Cliente> clienteConNuovaPassword = clienteDAO.findByEmail(emailScenario);
        assertTrue(clienteConNuovaPassword.isPresent());
        Cliente clienteAggiornato = clienteConNuovaPassword.get();

        assertTrue(clienteAggiornato.autenticaPassword(passwordNuova), "Nuova password dovrebbe funzionare");
        assertFalse(clienteAggiornato.autenticaPassword(passwordScenario), "Vecchia password non dovrebbe più funzionare");

        System.out.println("3. Cambio password completato");

        // 7. Upgrade a fedeltà
        clienteAggiornato.attivaAbbonamentoFedelta();
        assertTrue(clienteDAO.update(clienteAggiornato), "Upgrade fedeltà dovrebbe riuscire");

        // 8. Verifica finale
        Optional<Cliente> clienteFinale = clienteDAO.findByEmail(emailScenario);
        assertTrue(clienteFinale.isPresent());
        assertTrue(clienteFinale.get().hasAbbonamentoFedelta(), "Cliente dovrebbe avere fedeltà");

        System.out.println("4. Upgrade fedeltà completato");

        // 9. Cleanup
        assertTrue(clienteDAO.delete(emailScenario), "Eliminazione dovrebbe riuscire");
        assertFalse(clienteDAO.exists(emailScenario), "Cliente non dovrebbe più esistere");

        System.out.println("✅ Scenario completo registrazione e autenticazione superato");
    }

    @Test
    @Order(12)
    @DisplayName("Test autenticazione cliente")
    void testAutenticazioneCliente() {
        System.out.println("Test: Autenticazione cliente");

        // Prima inserisci il cliente
        assertTrue(clienteDAO.save(clienteTest), "Cliente dovrebbe essere salvato");

        assertTrue(clienteTest.autenticaPassword(PASSWORD_TEST), "Verifica password locale dovrebbe funzionare");

        Optional<Cliente> clienteAutenticato = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteAutenticato.isPresent(), "Cliente dovrebbe essere trovato");

        Cliente cliente = clienteAutenticato.get();
        assertTrue(cliente.autenticaPassword(PASSWORD_TEST), "Password dovrebbe essere corretta");

        System.out.println("✅ Autenticazione con password corretta: SUCCESSO");

        assertFalse(cliente.autenticaPassword("passwordsbagliata"), "Password sbagliata dovrebbe fallire");

        System.out.println("✅ Autenticazione con password sbagliata: FALLIMENTO (corretto)");

        assertFalse(cliente.autenticaPassword(null), "Password null dovrebbe fallire");

        System.out.println("✅ Autenticazione con password null: FALLIMENTO (corretto)");

        System.out.println("✅ Test autenticazione completato");
    }

    @Test
    @Order(13)
    @DisplayName("Test modifica password")
    void testModificaPassword() {
        System.out.println("Test: Modifica password");

        assertTrue(clienteDAO.save(clienteTest), "Cliente dovrebbe essere salvato");

        Optional<Cliente> clienteOpt = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteOpt.isPresent(), "Cliente dovrebbe essere trovato");

        Cliente cliente = clienteOpt.get();
        String passwordOriginale = cliente.getPassword();

        System.out.println("Password originale: " + passwordOriginale);

        String nuovaPassword = "newpass789";
        cliente.modificaPassword(nuovaPassword);

        System.out.println("Nuova password: " + cliente.getPassword());

        boolean aggiornato = clienteDAO.update(cliente);
        assertTrue(aggiornato, "L'aggiornamento dovrebbe riuscire");

        Optional<Cliente> clienteAggiornato = clienteDAO.findByEmail(EMAIL_TEST);
        assertTrue(clienteAggiornato.isPresent(), "Cliente aggiornato dovrebbe essere trovato");

        Cliente clienteVerifica = clienteAggiornato.get();
        assertEquals(nuovaPassword, clienteVerifica.getPassword());
        assertTrue(clienteVerifica.autenticaPassword(nuovaPassword), "Nuova password dovrebbe funzionare");
        assertFalse(clienteVerifica.autenticaPassword(passwordOriginale), "Vecchia password non dovrebbe più funzionare");

        System.out.println("✅ Modifica password riuscita");
    }

    @Test
    @Order(14)
    @DisplayName("Test validazione password nella creazione")
    void testValidazionePasswordCreazione() {
        System.out.println("Test: Validazione password nella creazione");

        // Test password troppo corta
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test1@email.com", "123", "Test User");
        });
        assertTrue(exception1.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("✅ Password troppo corta rifiutata: " + exception1.getMessage());

        // Test password null
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test2@email.com", null, "Test User");
        });
        assertTrue(exception2.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("✅ Password null rifiutata: " + exception2.getMessage());

        // Test password vuota
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test3@email.com", "", "Test User");
        });
        assertTrue(exception3.getMessage().contains("Password obbligatoria o troppo corta"));
        System.out.println("✅ Password vuota rifiutata: " + exception3.getMessage());

        // Test password valida (6 caratteri)
        Cliente clienteValido = new Cliente("test4@email.com", "123456", "Test User");
        assertEquals("123456", clienteValido.getPassword());
        System.out.println("✅ Password 6 caratteri accettata");

        // Test password valida (più di 6 caratteri)
        Cliente clienteValido2 = new Cliente("test5@email.com", "password123", "Test User");
        assertEquals("password123", clienteValido2.getPassword());
        System.out.println("✅ Password lunga accettata");

        System.out.println("✅ Validazione password funziona correttamente");
    }

    @Test
    @Order(15)
    @DisplayName("Test metodo autenticaCliente")
    void testMetodoAutenticaCliente() {
        System.out.println("Test: Metodo autenticaCliente");

        assertTrue(clienteDAO.save(clienteTest));

        Optional<Cliente> loginOK = clienteDAO.autenticaCliente(EMAIL_TEST, PASSWORD_TEST);
        assertTrue(loginOK.isPresent(), "Login con credenziali corrette dovrebbe riuscire");
        assertEquals(EMAIL_TEST, loginOK.get().getEmail());

        // Test password sbagliata
        Optional<Cliente> loginFailPassword = clienteDAO.autenticaCliente(EMAIL_TEST, "passwordsbagliata");
        assertFalse(loginFailPassword.isPresent(), "Login con password sbagliata dovrebbe fallire");

        // Test email inesistente
        Optional<Cliente> loginFailEmail = clienteDAO.autenticaCliente("inesistente@email.com", PASSWORD_TEST);
        assertFalse(loginFailEmail.isPresent(), "Login con email inesistente dovrebbe fallire");

        System.out.println("Metodo autenticaCliente funziona correttamente");
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
