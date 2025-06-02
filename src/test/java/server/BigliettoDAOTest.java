package server;

import it.trenical.common.cliente.Biglietto;
import it.trenical.common.cliente.Cliente;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.db.dao.BigliettoDAO;
import it.trenical.server.db.dao.ClienteDAO;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.treni.TipoTreno;
import it.trenical.server.treni.Treno;
import it.trenical.server.treni.ServizioTreno;
import it.trenical.server.treni.builder.TrenoDirector;
import it.trenical.server.tratte.Tratta;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test JUnit completo per BigliettoDAO - Versione Aggiornata
 * Testa solo i metodi effettivamente presenti nella classe BigliettoDAO
 * Verifica che i biglietti siano completi prima del salvataggio
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test BigliettoDAO - Gestione Biglietti Database (Aggiornato)")
class BigliettoDAOTest {

    private static BigliettoDAO bigliettoDAO;
    private static ClienteDAO clienteDAO;
    private static ViaggioDAO viaggioDAO;
    private static TrenoDirector trenoDirector;

    // Dati di test
    private static Cliente clienteTest;
    private static Viaggio viaggioTest;
    private static Biglietto bigliettoTest;
    private static String emailClienteTest = "biglietto.test@junit.com";

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST BIGLIETTO DAO (AGGIORNATO) ===");

        // Inizializza DAO
        bigliettoDAO = new BigliettoDAO();
        clienteDAO = new ClienteDAO();
        viaggioDAO = new ViaggioDAO();
        trenoDirector = new TrenoDirector();

        assertNotNull(bigliettoDAO, "BigliettoDAO non dovrebbe essere null");
        assertNotNull(clienteDAO, "ClienteDAO non dovrebbe essere null");
        assertNotNull(viaggioDAO, "ViaggioDAO non dovrebbe essere null");

        // Crea dati di test
        creaDatiDiTest();
    }

    static void creaDatiDiTest() {
        System.out.println("Creazione dati di test...");

        // Cliente di test
        clienteTest = new Cliente(emailClienteTest, "Cliente Test Biglietti", false);

        // Viaggio di test
        Treno trenoTest = trenoDirector.costruisciTrenoStandard("BIGTEST001");
        Tratta trattaTest = new Tratta(Stazione.ROMA, Stazione.NAPOLI);
        viaggioTest = new Viaggio(trenoTest, trattaTest, LocalDate.of(2025, 8, 1));

        // Biglietto di test COMPLETO (con nominativo)
        bigliettoTest = new Biglietto(viaggioTest);
        bigliettoTest.setNominativo("Mario Test");

        assertTrue(bigliettoTest.isCompleto(), "Biglietto test deve essere completo");

        System.out.println("Cliente test: " + clienteTest.toString());
        System.out.println("Viaggio test: " + viaggioTest.toString());
        System.out.println("Biglietto test: " + bigliettoTest.toString());
        System.out.println("Biglietto ID: " + bigliettoTest.getId());
    }

    @BeforeEach
    void setUp() {
        System.out.println("\n--- Setup test ---");

        // Pulizia dati test
        pulisciDatiTest();

        // Inserisci dati prerequisiti
        assertTrue(clienteDAO.save(clienteTest), "Cliente test dovrebbe essere salvato");
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio test dovrebbe essere salvato");
    }

    @AfterEach
    void tearDown() {
        System.out.println("--- Cleanup test ---");
        pulisciDatiTest();
    }

    void pulisciDatiTest() {
        // Elimina biglietti, viaggi, clienti di test
        bigliettoDAO.deleteByClienteEmail(emailClienteTest);
        viaggioDAO.delete(viaggioTest.getId());
        clienteDAO.delete(emailClienteTest);
    }

    // ===== TEST METODI PRESENTI NELLA CLASSE =====

    @Test
    @Order(1)
    @DisplayName("Test conteggio biglietti - count()")
    void testCount() {
        System.out.println("Test: count()");

        int countIniziale = bigliettoDAO.count();
        assertTrue(countIniziale >= 0, "Il conteggio non dovrebbe essere negativo");

        // Salva biglietto e verifica incremento
        assertTrue(bigliettoDAO.save(bigliettoTest, emailClienteTest));

        int countDopoSalvataggio = bigliettoDAO.count();
        assertEquals(countIniziale + 1, countDopoSalvataggio,
                "Il conteggio dovrebbe aumentare di 1 dopo il salvataggio");

        System.out.printf("✅ Count iniziale: %d, dopo salvataggio: %d%n",
                countIniziale, countDopoSalvataggio);
    }

    @Test
    @Order(2)
    @DisplayName("Test conteggio biglietti per cliente - countByClienteEmail()")
    void testCountByClienteEmail() {
        System.out.println("Test: countByClienteEmail()");

        // Verifica conteggio iniziale
        int countIniziale = bigliettoDAO.countByClienteEmail(emailClienteTest);
        assertEquals(0, countIniziale, "Cliente test non dovrebbe avere biglietti inizialmente");

        // Salva biglietto
        assertTrue(bigliettoDAO.save(bigliettoTest, emailClienteTest));

        // Verifica incremento
        int countDopoSalvataggio = bigliettoDAO.countByClienteEmail(emailClienteTest);
        assertEquals(1, countDopoSalvataggio, "Cliente dovrebbe avere 1 biglietto");

        // Test cliente inesistente
        int countInesistente = bigliettoDAO.countByClienteEmail("cliente.inesistente@test.com");
        assertEquals(0, countInesistente, "Cliente inesistente dovrebbe avere 0 biglietti");

        System.out.printf("✅ Count cliente: iniziale=%d, dopo salvataggio=%d, inesistente=%d%n",
                countIniziale, countDopoSalvataggio, countInesistente);
    }

    @Test
    @Order(3)
    @DisplayName("Test verifica esistenza - exists()")
    void testExists() {
        System.out.println("Test: exists()");

        // Verifica che il biglietto non esista inizialmente
        assertFalse(bigliettoDAO.exists(bigliettoTest.getId()),
                "Biglietto non dovrebbe esistere inizialmente");

        // Salva biglietto
        assertTrue(bigliettoDAO.save(bigliettoTest, emailClienteTest));

        // Verifica che ora esista
        assertTrue(bigliettoDAO.exists(bigliettoTest.getId()),
                "Biglietto dovrebbe esistere dopo il salvataggio");

        // Test ID inesistente
        assertFalse(bigliettoDAO.exists("ID_INESISTENTE"),
                "ID inesistente non dovrebbe esistere");

        System.out.println("✅ Verifica esistenza funziona correttamente");
    }

    @Test
    @Order(4)
    @DisplayName("Test salvataggio singolo - save()")
    void testSave() {
        System.out.println("Test: save()");

        // Verifica stato iniziale
        assertFalse(bigliettoDAO.exists(bigliettoTest.getId()));

        // Salvataggio
        boolean risultato = bigliettoDAO.save(bigliettoTest, emailClienteTest);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica che sia stato salvato
        assertTrue(bigliettoDAO.exists(bigliettoTest.getId()));
        assertEquals(1, bigliettoDAO.countByClienteEmail(emailClienteTest));

        System.out.println("✅ Salvataggio singolo riuscito");
        System.out.println("Biglietto salvato: " + bigliettoTest.toString());
    }

    @Test
    @Order(5)
    @DisplayName("Test salvataggio biglietto incompleto")
    void testSaveBigliettoIncompleto() {
        System.out.println("Test: save() con biglietto incompleto");

        // Crea biglietto SENZA nominativo (incompleto)
        Biglietto bigliettoIncompleto = new Biglietto(viaggioTest);
        // NON chiamiamo setNominativo()

        assertFalse(bigliettoIncompleto.isCompleto(),
                "Biglietto senza nominativo dovrebbe essere incompleto");

        // Tentativo di salvataggio
        boolean risultato = bigliettoDAO.save(bigliettoIncompleto, emailClienteTest);
        assertFalse(risultato, "Salvataggio di biglietto incompleto dovrebbe fallire");

        // Verifica che non sia stato salvato
        assertEquals(0, bigliettoDAO.countByClienteEmail(emailClienteTest),
                "Nessun biglietto dovrebbe essere salvato");

        System.out.println("✅ Validazione biglietto incompleto funziona correttamente");
        System.out.println("Biglietto incompleto: " + bigliettoIncompleto.toString());
    }

    @Test
    @Order(6)
    @DisplayName("Test salvataggio multiplo - saveAll()")
    void testSaveAll() {
        System.out.println("Test: saveAll()");

        // Crea biglietti multipli (scenario famiglia)
        List<Biglietto> bigliettiFamiglia = new ArrayList<>();

        Biglietto bigliettoPadre = new Biglietto(viaggioTest);
        bigliettoPadre.setNominativo("Mario Rossi");

        Biglietto bigliettoMadre = bigliettoTest.clone();  // Pattern Prototype
        bigliettoMadre.setNominativo("Anna Rossi");

        Biglietto bigliettoFiglio = bigliettoTest.clone(); // Pattern Prototype
        bigliettoFiglio.setNominativo("Luca Rossi");

        bigliettiFamiglia.add(bigliettoPadre);
        bigliettiFamiglia.add(bigliettoMadre);
        bigliettiFamiglia.add(bigliettoFiglio);

        // Verifica che tutti siano completi
        assertTrue(bigliettiFamiglia.stream().allMatch(Biglietto::isCompleto),
                "Tutti i biglietti dovrebbero essere completi");

        System.out.println("Biglietti da salvare:");
        bigliettiFamiglia.forEach(b -> System.out.println("- " + b.toString()));

        // Salvataggio multiplo
        int salvati = bigliettoDAO.saveAll(bigliettiFamiglia, emailClienteTest);
        assertEquals(3, salvati, "Dovrebbero essere salvati tutti e 3 i biglietti");

        // Verifica salvataggio
        assertEquals(3, bigliettoDAO.countByClienteEmail(emailClienteTest),
                "Cliente dovrebbe avere 3 biglietti");

        System.out.println("✅ Salvataggio multiplo riuscito");
        System.out.printf("Salvati %d/%d biglietti per %s%n",
                salvati, bigliettiFamiglia.size(), emailClienteTest);
    }

    @Test
    @Order(7)
    @DisplayName("Test salvataggio multiplo con biglietti incompleti")
    void testSaveAllConBigliettiIncompleti() {
        System.out.println("Test: saveAll() con mix di biglietti completi e incompleti");

        List<Biglietto> bigliettiMisti = new ArrayList<>();

        // Biglietto completo
        Biglietto completoUno = new Biglietto(viaggioTest);
        completoUno.setNominativo("Completo Uno");
        bigliettiMisti.add(completoUno);

        // Biglietto incompleto (senza nominativo)
        Biglietto incompleto = new Biglietto(viaggioTest);
        // Non chiamiamo setNominativo()
        bigliettiMisti.add(incompleto);

        // Altro biglietto completo
        Biglietto completoDue = new Biglietto(viaggioTest);
        completoDue.setNominativo("Completo Due");
        bigliettiMisti.add(completoDue);

        System.out.println("Biglietti da salvare (mix completi/incompleti):");
        bigliettiMisti.forEach(b -> System.out.println("- " + b.toString() +
                " (Completo: " + b.isCompleto() + ")"));

        // Salvataggio: dovrebbero essere salvati solo quelli completi
        int salvati = bigliettoDAO.saveAll(bigliettiMisti, emailClienteTest);
        assertEquals(2, salvati, "Dovrebbero essere salvati solo 2 biglietti completi");

        // Verifica
        assertEquals(2, bigliettoDAO.countByClienteEmail(emailClienteTest),
                "Cliente dovrebbe avere solo 2 biglietti");

        System.out.printf("✅ Salvati %d/3 biglietti (solo quelli completi)%n", salvati);
    }

    @Test
    @Order(8)
    @DisplayName("Test ricerca biglietti per cliente - findByClienteEmail()")
    void testFindByClienteEmail() {
        System.out.println("Test: findByClienteEmail()");

        // Verifica lista vuota inizialmente
        List<Biglietto> vuota = bigliettoDAO.findByClienteEmail(emailClienteTest);
        assertTrue(vuota.isEmpty(), "Lista dovrebbe essere vuota inizialmente");

        // Salva biglietto
        assertTrue(bigliettoDAO.save(bigliettoTest, emailClienteTest));

        // Ricerca
        List<Biglietto> bigliettiCliente = bigliettoDAO.findByClienteEmail(emailClienteTest);
        assertNotNull(bigliettiCliente);
        assertEquals(1, bigliettiCliente.size());

        Biglietto biglietto = bigliettiCliente.get(0);
        assertEquals(bigliettoTest.getId(), biglietto.getId());
        assertEquals("Mario Test", biglietto.getNominativo());
        assertEquals(viaggioTest.getId(), biglietto.getIdViaggio());

        // Test cliente inesistente
        List<Biglietto> bigliettiInesistenti = bigliettoDAO.findByClienteEmail("inesistente@test.com");
        assertTrue(bigliettiInesistenti.isEmpty(), "Cliente inesistente non dovrebbe avere biglietti");

        System.out.printf("✅ Trovati %d biglietti per cliente %s%n",
                bigliettiCliente.size(), emailClienteTest);
    }

    @Test
    @Order(9)
    @DisplayName("Test eliminazione biglietti per cliente - deleteByClienteEmail()")
    void testDeleteByClienteEmail() {
        System.out.println("Test: deleteByClienteEmail()");

        // Salva più biglietti per lo stesso cliente
        List<Biglietto> biglietti = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Biglietto b = bigliettoTest.clone();
            b.setNominativo("Test " + i);
            biglietti.add(b);
        }

        int salvati = bigliettoDAO.saveAll(biglietti, emailClienteTest);
        assertEquals(3, salvati, "Dovrebbero essere salvati 3 biglietti");

        // Verifica che siano stati salvati
        assertEquals(3, bigliettoDAO.countByClienteEmail(emailClienteTest));

        // Eliminazione per cliente
        int eliminati = bigliettoDAO.deleteByClienteEmail(emailClienteTest);
        assertEquals(3, eliminati, "Dovrebbero essere eliminati 3 biglietti");

        // Verifica eliminazione
        assertEquals(0, bigliettoDAO.countByClienteEmail(emailClienteTest),
                "Cliente non dovrebbe più avere biglietti");

        // Test eliminazione cliente senza biglietti
        int eliminatiVuoti = bigliettoDAO.deleteByClienteEmail("cliente.vuoto@test.com");
        assertEquals(0, eliminatiVuoti, "Cliente senza biglietti dovrebbe restituire 0 eliminazioni");

        System.out.printf("✅ Eliminati %d biglietti per cliente %s%n", eliminati, emailClienteTest);
    }

    @Test
    @Order(10)
    @DisplayName("Test recupero cliente proprietario - getClienteProprietario()")
    void testGetClienteProprietario() {
        System.out.println("Test: getClienteProprietario()");

        // Salva biglietto
        assertTrue(bigliettoDAO.save(bigliettoTest, emailClienteTest));

        // Recupera proprietario
        Optional<String> proprietario = bigliettoDAO.getClienteProprietario(bigliettoTest.getId());
        assertTrue(proprietario.isPresent(), "Dovrebbe trovare il proprietario");
        assertEquals(emailClienteTest, proprietario.get());

        // Test biglietto inesistente
        Optional<String> proprietarioInesistente = bigliettoDAO.getClienteProprietario("ID_INESISTENTE");
        assertFalse(proprietarioInesistente.isPresent(),
                "Non dovrebbe trovare proprietario per ID inesistente");

        System.out.printf("✅ Proprietario trovato: %s per biglietto %s%n",
                proprietario.get(), bigliettoTest.getId());
    }

    @Test
    @Order(11)
    @DisplayName("Test scenario completo - Acquisto famiglia con Pattern Prototype")
    void testScenarioCompletoAcquistoFamiglia() {
        System.out.println("Test: Scenario completo acquisto famiglia");

        // 1. Cliente Mario acquista biglietti per la famiglia
        System.out.println("1. Scenario: Mario acquista biglietti per la famiglia");

        // 2. Crea biglietto base e usa Pattern Prototype per clonarlo
        Biglietto bigliettoBase = new Biglietto(viaggioTest);
        // Il biglietto base NON ha nominativo (sarà settato individualmente)

        Biglietto bigliettoMario = bigliettoBase.clone(); // Pattern Prototype
        bigliettoMario.setNominativo("Mario Rossi");

        Biglietto bigliettoAnna = bigliettoBase.clone();  // Pattern Prototype
        bigliettoAnna.setNominativo("Anna Rossi");

        Biglietto bigliettoLuca = bigliettoBase.clone();  // Pattern Prototype
        bigliettoLuca.setNominativo("Luca Rossi");

        List<Biglietto> bigliettiFamiglia = List.of(bigliettoMario, bigliettoAnna, bigliettoLuca);

        System.out.println("2. Biglietti famiglia creati usando Pattern Prototype:");
        bigliettiFamiglia.forEach(b -> System.out.println("   - " + b.toString()));

        // 3. Verifica che tutti siano completi
        assertTrue(bigliettiFamiglia.stream().allMatch(Biglietto::isCompleto),
                "Tutti i biglietti dovrebbero essere completi");

        // 4. Salvataggio acquisto
        int salvati = bigliettoDAO.saveAll(bigliettiFamiglia, emailClienteTest);
        assertEquals(3, salvati, "Dovrebbero essere salvati tutti e 3 i biglietti");

        // 5. Verifica acquisto
        List<Biglietto> bigliettiSalvati = bigliettoDAO.findByClienteEmail(emailClienteTest);
        assertEquals(3, bigliettiSalvati.size(), "Mario dovrebbe avere 3 biglietti");

        double totaleSpeso = bigliettiSalvati.stream()
                .mapToDouble(Biglietto::getPrezzo)
                .sum();

        System.out.printf("3. Acquisto completato: %d biglietti per €%.2f%n",
                bigliettiSalvati.size(), totaleSpeso);

        // 6. Verifica ID univoci (importante per Pattern Prototype)
        List<String> ids = bigliettiSalvati.stream().map(Biglietto::getId).toList();
        assertEquals(3, ids.stream().distinct().count(),
                "Tutti i biglietti dovrebbero avere ID univoci");

        // 7. Verifica che ogni biglietto abbia nominativo diverso
        List<String> nominativi = bigliettiSalvati.stream().map(Biglietto::getNominativo).toList();
        assertEquals(3, nominativi.stream().distinct().count(),
                "Tutti i biglietti dovrebbero avere nominativi diversi");
        assertTrue(nominativi.contains("Mario Rossi"));
        assertTrue(nominativi.contains("Anna Rossi"));
        assertTrue(nominativi.contains("Luca Rossi"));

        // 8. Statistiche finali
        System.out.println("4. STATISTICHE FINALI:");
        System.out.printf("   - Cliente: %s%n", emailClienteTest);
        System.out.printf("   - Biglietti acquistati: %d%n", bigliettiSalvati.size());
        System.out.printf("   - Viaggio: %s%n", viaggioTest.getTratta());
        System.out.printf("   - Data viaggio: %s%n", viaggioTest.getDataViaggio());
        System.out.printf("   - Totale speso: €%.2f%n", totaleSpeso);
        System.out.printf("   - ID univoci: %s%n", ids);
        System.out.printf("   - Nominativi: %s%n", nominativi);

        System.out.println("✅ Scenario completo acquisto famiglia con Pattern Prototype superato!");
    }

    @Test
    @Order(12)
    @DisplayName("Test operazioni con dati non validi")
    void testOperazioniDatiNonValidi() {
        System.out.println("Test: Operazioni con dati non validi");

        // Test exists con ID null/vuoto
        assertFalse(bigliettoDAO.exists(null), "ID null non dovrebbe esistere");
        assertFalse(bigliettoDAO.exists(""), "ID vuoto non dovrebbe esistere");
        assertFalse(bigliettoDAO.exists("ID_INESISTENTE"), "ID inesistente non dovrebbe esistere");

        // Test countByClienteEmail con email null/vuota
        assertEquals(0, bigliettoDAO.countByClienteEmail(null),
                "Email null dovrebbe restituire 0");
        assertEquals(0, bigliettoDAO.countByClienteEmail(""),
                "Email vuota dovrebbe restituire 0");
        assertEquals(0, bigliettoDAO.countByClienteEmail("inesistente@test.com"),
                "Email inesistente dovrebbe restituire 0");

        // Test findByClienteEmail con email null/vuota
        assertTrue(bigliettoDAO.findByClienteEmail(null).isEmpty(),
                "Email null dovrebbe restituire lista vuota");
        assertTrue(bigliettoDAO.findByClienteEmail("").isEmpty(),
                "Email vuota dovrebbe restituire lista vuota");

        // Test deleteByClienteEmail con email null/vuota
        assertEquals(0, bigliettoDAO.deleteByClienteEmail(null),
                "Delete con email null dovrebbe restituire 0");
        assertEquals(0, bigliettoDAO.deleteByClienteEmail(""),
                "Delete con email vuota dovrebbe restituire 0");

        // Test getClienteProprietario con ID inesistente
        Optional<String> proprietarioInesistente = bigliettoDAO.getClienteProprietario("ID_INESISTENTE");
        assertFalse(proprietarioInesistente.isPresent(),
                "Proprietario per ID inesistente non dovrebbe essere trovato");

        System.out.println("✅ Gestione dati non validi corretta");
    }

    @Test
    @Order(13)
    @DisplayName("Test performance operazioni base")
    void testPerformanceOperazioniBase() {
        System.out.println("Test: Performance operazioni base");

        long startTime = System.currentTimeMillis();

        // Test performance operazioni disponibili
        int count = bigliettoDAO.count();
        int countCliente = bigliettoDAO.countByClienteEmail(emailClienteTest);
        boolean exists = bigliettoDAO.exists(bigliettoTest.getId());
        List<Biglietto> byCliente = bigliettoDAO.findByClienteEmail(emailClienteTest);
        Optional<String> proprietario = bigliettoDAO.getClienteProprietario("ID_TEST");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Le operazioni dovrebbero essere veloci (< 1 secondo)
        assertTrue(duration < 1000, "Operazioni dovrebbero essere veloci: " + duration + "ms");

        // Verifica coerenza dati
        assertTrue(count >= 0, "Count dovrebbe essere >= 0");
        assertTrue(countCliente >= 0, "Count cliente dovrebbe essere >= 0");
        assertTrue(byCliente.size() == countCliente, "findByClienteEmail size dovrebbe corrispondere a count");

        System.out.printf("✅ Performance test: %dms per 5 operazioni%n", duration);
        System.out.printf("   Count: %d, CountCliente: %d, Exists: %s, ByCliente: %d, Proprietario: %s%n",
                count, countCliente, exists, byCliente.size(), proprietario.isPresent());
    }

    @Test
    @Order(14)
    @DisplayName("Test validazione completa biglietti")
    void testValidazioneCompletaBiglietti() {
        System.out.println("Test: Validazione completa biglietti");

        // Test 1: Biglietto senza nominativo
        Biglietto senzaNominativo = new Biglietto(viaggioTest);
        assertFalse(senzaNominativo.isCompleto(), "Biglietto senza nominativo non dovrebbe essere completo");
        assertFalse(bigliettoDAO.save(senzaNominativo, emailClienteTest),
                "Non dovrebbe salvare biglietto senza nominativo");

        // Test 2: Biglietto con nominativo vuoto
        Biglietto nominativoVuoto = new Biglietto(viaggioTest);
        try {
            nominativoVuoto.setNominativo("");
            fail("Dovrebbe lanciare eccezione per nominativo vuoto");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Eccezione corretta per nominativo vuoto: " + e.getMessage());
        }

        // Test 3: Biglietto con nominativo null
        Biglietto nominativoNull = new Biglietto(viaggioTest);
        try {
            nominativoNull.setNominativo(null);
            fail("Dovrebbe lanciare eccezione per nominativo null");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Eccezione corretta per nominativo null: " + e.getMessage());
        }

        // Test 4: Biglietto completo (controllo positivo)
        Biglietto completo = new Biglietto(viaggioTest);
        completo.setNominativo("Mario Completo");
        assertTrue(completo.isCompleto(), "Biglietto con nominativo dovrebbe essere completo");
        assertTrue(bigliettoDAO.save(completo, emailClienteTest),
                "Dovrebbe salvare biglietto completo");

        // Verifica che sia stato salvato
        assertTrue(bigliettoDAO.exists(completo.getId()));
        assertEquals(1, bigliettoDAO.countByClienteEmail(emailClienteTest));

        System.out.println("✅ Validazione completa biglietti superata");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("\n=== CLEANUP FINALE TEST BIGLIETTO DAO ===");

        // Pulizia finale
        if (bigliettoDAO != null) {
            bigliettoDAO.deleteByClienteEmail(emailClienteTest);
        }
        if (viaggioDAO != null && viaggioTest != null) {
            viaggioDAO.delete(viaggioTest.getId());
        }
        if (clienteDAO != null) {
            clienteDAO.delete(emailClienteTest);
        }

        System.out.println("✅ Test BigliettoDAO completati con successo!");
    }
}