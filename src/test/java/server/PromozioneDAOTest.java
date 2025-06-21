package server;

import it.trenical.server.promozioni.Promozione;
import it.trenical.server.promozioni.PromozioneStandard;
import it.trenical.server.promozioni.factoryMethod.PromozioneFactory;

import it.trenical.server.db.dao.PromozioneDAO;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

/**
 * Test JUnit completo per PromozioneDAO
 * Verifica tutte le operazioni CRUD e funzionalità specifiche per promozioni
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test PromozioneDAO - Gestione Promozioni Database")
class PromozioneDAOTest {

    private static PromozioneDAO promozioneDAO;
    private static Promozione promozioneTestStandard;
    private static Promozione promozioneTestFedelta;
    private static String promoTestStandardId;
    private static String promoTestFedeltaId;

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST PROMOZIONE DAO ===");
        promozioneDAO = new PromozioneDAO();

        assertNotNull(promozioneDAO, "PromozioneDAO non dovrebbe essere null");

        // Crea promozioni di test usando factory
        PromozioneFactory factoryStandard = PromozioneFactory.getFactory("standard");
        PromozioneFactory factoryFedelta = PromozioneFactory.getFactory("fedelta");

        promozioneTestStandard = factoryStandard.creaPromozione("Test JUnit Standard", 25.0);
        promozioneTestFedelta = factoryFedelta.creaPromozione("Test JUnit Fedeltà", 35.0);

        promoTestStandardId = promozioneTestStandard.getId();
        promoTestFedeltaId = promozioneTestFedelta.getId();

        System.out.println("Promozione Standard test: " + promozioneTestStandard.toString());
        System.out.println("Promozione Fedeltà test: " + promozioneTestFedelta.toString());
    }

    @BeforeEach
    void setUp() {
        // Pulizia prima di ogni test
        if (promozioneDAO.exists(promoTestStandardId)) {
            promozioneDAO.delete(promoTestStandardId);
        }
        if (promozioneDAO.exists(promoTestFedeltaId)) {
            promozioneDAO.delete(promoTestFedeltaId);
        }
    }

    @AfterEach
    void tearDown() {
        // Pulizia dopo ogni test
        if (promozioneDAO.exists(promoTestStandardId)) {
            promozioneDAO.delete(promoTestStandardId);
        }
        if (promozioneDAO.exists(promoTestFedeltaId)) {
            promozioneDAO.delete(promoTestFedeltaId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test conteggio promozioni esistenti")
    void testConteggioPromozioniEsistenti() {
        System.out.println("Test: Conteggio promozioni esistenti");

        int count = promozioneDAO.count();

        assertTrue(count >= 0, "Il conteggio non dovrebbe essere negativo");
        assertTrue(count >= 3, "Dovrebbero esserci almeno le 3 promozioni di test iniziali");

        System.out.printf("✅ Trovate %d promozioni totali, %d attive%n", count);
    }

    @Test
    @Order(2)
    @DisplayName("Test recupero tutte le promozioni")
    void testRecuperoTuttePromozioni() {
        System.out.println("Test: Recupero tutte le promozioni");

        List<Promozione> tuttePromozioni = promozioneDAO.findAll();

        assertNotNull(tuttePromozioni, "La lista non dovrebbe essere null");
        assertFalse(tuttePromozioni.isEmpty(), "La lista non dovrebbe essere vuota");
        assertTrue(tuttePromozioni.size() >= 3, "Dovrebbero esserci almeno 3 promozioni");

        // Verifica che tutte le promozioni abbiano dati validi
        for (Promozione promo : tuttePromozioni) {
            assertNotNull(promo.getId(), "ID promozione non dovrebbe essere null");
            assertNotNull(promo.getNome(), "Nome promozione non dovrebbe essere null");
            assertNotNull(promo.getTipo(), "Tipo promozione non dovrebbe essere null");
            assertTrue(promo.getSconto() > 0, "Sconto dovrebbe essere positivo");
            assertTrue(promo.getSconto() <= 100, "Sconto non dovrebbe superare 100%");
        }

        System.out.printf("✅ Recuperate %d promozioni correttamente%n", tuttePromozioni.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test salvataggio promozione Standard")
    void testSalvataggioPromozioneStandard() {
        System.out.println("Test: Salvataggio promozione Standard");

        // Verifica che la promozione non esista
        assertFalse(promozioneDAO.exists(promoTestStandardId),
                "Promozione test non dovrebbe esistere inizialmente");

        // Salvataggio
        boolean risultato = promozioneDAO.save(promozioneTestStandard);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica salvataggio
        assertTrue(promozioneDAO.exists(promoTestStandardId),
                "Promozione test dovrebbe esistere dopo il salvataggio");

        Optional<Promozione> promoSalvata = promozioneDAO.findById(promoTestStandardId);
        assertTrue(promoSalvata.isPresent(), "Promozione salvata dovrebbe essere trovata");

        Promozione promo = promoSalvata.get();
        assertEquals(promoTestStandardId, promo.getId());
        assertEquals("Test JUnit Standard", promo.getNome());
        assertEquals("Standard", promo.getTipo());
        assertEquals(25.0, promo.getSconto(), 0.01);

        System.out.println("✅ Salvataggio promozione Standard riuscito");
        System.out.println("Promozione salvata: " + promo.toString());
    }

    @Test
    @Order(4)
    @DisplayName("Test salvataggio promozione Fedeltà")
    void testSalvataggioPromozioneFedelta() {
        System.out.println("Test: Salvataggio promozione Fedeltà");

        // Salvataggio
        boolean risultato = promozioneDAO.save(promozioneTestFedelta);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica salvataggio
        Optional<Promozione> promoSalvata = promozioneDAO.findById(promoTestFedeltaId);
        assertTrue(promoSalvata.isPresent(), "Promozione salvata dovrebbe essere trovata");

        Promozione promo = promoSalvata.get();
        assertEquals(promoTestFedeltaId, promo.getId());
        assertEquals("Test JUnit Fedeltà", promo.getNome());
        assertEquals("Fedelta", promo.getTipo());
        assertEquals(35.0, promo.getSconto(), 0.01);

        System.out.println("✅ Salvataggio promozione Fedeltà riuscito");
        System.out.println("Promozione salvata: " + promo.toString());
    }

    @Test
    @Order(5)
    @DisplayName("Test ricerca promozioni per tipo")
    void testRicercaPromozioniPerTipo() {
        System.out.println("Test: Ricerca promozioni per tipo");

        // Salva entrambe le promozioni
        assertTrue(promozioneDAO.save(promozioneTestStandard));
        assertTrue(promozioneDAO.save(promozioneTestFedelta));

        // Ricerca promozioni Standard
        List<Promozione> promozioniStandard = promozioneDAO.findByTipo("Standard");
        assertNotNull(promozioniStandard);
        assertFalse(promozioniStandard.isEmpty());

        // Verifica che tutte siano Standard
        for (Promozione promo : promozioniStandard) {
            assertEquals("Standard", promo.getTipo());
        }

        // Ricerca promozioni Fedeltà
        List<Promozione> promozioniFedelta = promozioneDAO.findByTipo("Fedelta");
        assertNotNull(promozioniFedelta);
        assertFalse(promozioniFedelta.isEmpty());

        // Verifica che tutte siano Fedeltà
        for (Promozione promo : promozioniFedelta) {
            assertEquals("Fedelta", promo.getTipo());
        }

        System.out.printf("✅ Trovate %d promozioni Standard e %d Fedeltà%n",
                promozioniStandard.size(), promozioniFedelta.size());
    }


    @Test
    @Order(7)
    @DisplayName("Test aggiornamento promozione")
    void testAggiornamentoPromozione() {
        System.out.println("Test: Aggiornamento promozione");

        // Salva promozione iniziale
        assertTrue(promozioneDAO.save(promozioneTestStandard));

        // Crea promozione aggiornata
        Promozione promoAggiornata = new PromozioneStandard("Test JUnit Standard AGGIORNATO", 40.0);
        // Simula stesso ID
        try {
            java.lang.reflect.Field idField = Promozione.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(promoAggiornata, promoTestStandardId);
        } catch (Exception e) {
            fail("Errore nella preparazione test aggiornamento");
        }

        // Aggiornamento
        boolean aggiornato = promozioneDAO.update(promoAggiornata);
        assertTrue(aggiornato, "L'aggiornamento dovrebbe riuscire");

        // Verifica aggiornamento
        Optional<Promozione> promoVerifica = promozioneDAO.findById(promoTestStandardId);
        assertTrue(promoVerifica.isPresent());

        Promozione promo = promoVerifica.get();
        assertEquals("Test JUnit Standard AGGIORNATO", promo.getNome());
        assertEquals(40.0, promo.getSconto(), 0.01);

        System.out.println("✅ Aggiornamento promozione riuscito");
    }

    @Test
    @Order(9)
    @DisplayName("Test eliminazione promozione")
    void testEliminazionePromozione() {
        System.out.println("Test: Eliminazione promozione");

        // Salva promozione
        assertTrue(promozioneDAO.save(promozioneTestStandard));
        assertTrue(promozioneDAO.exists(promoTestStandardId));

        // Eliminazione
        boolean eliminata = promozioneDAO.delete(promoTestStandardId);
        assertTrue(eliminata, "L'eliminazione dovrebbe riuscire");

        // Verifica eliminazione
        assertFalse(promozioneDAO.exists(promoTestStandardId),
                "Promozione non dovrebbe più esistere");

        Optional<Promozione> promoEliminata = promozioneDAO.findById(promoTestStandardId);
        assertFalse(promoEliminata.isPresent(),
                "Promozione eliminata non dovrebbe essere trovata");

        System.out.println("✅ Eliminazione promozione riuscita");
    }

    @Test
    @Order(10)
    @DisplayName("Test metodi di utilità")
    void testMetodiUtilita() {
        System.out.println("Test: Metodi di utilità");

        // Test metodi specifici per tipo
        List<Promozione> standardAttive = promozioneDAO.findPromozioniStandardAttive();
        List<Promozione> fedeltaAttive = promozioneDAO.findPromozioniFedeltaAttive();

        assertNotNull(standardAttive);
        assertNotNull(fedeltaAttive);

        // Verifica che i metodi specifici restituiscano solo il tipo corretto
        for (Promozione promo : standardAttive) {
            assertEquals("Standard", promo.getTipo(),
                    "Metodo findPromozioniStandardAttive dovrebbe restituire solo Standard");
        }

        for (Promozione promo : fedeltaAttive) {
            assertEquals("Fedelta", promo.getTipo(),
                    "Metodo findPromozioniFedeltaAttive dovrebbe restituire solo Fedeltà");
        }

        System.out.printf("✅ Metodi utilità: %d Standard attive, %d Fedeltà attive%n",
                standardAttive.size(), fedeltaAttive.size());
    }

    @Test
    @Order(11)
    @DisplayName("Test verifica dati di test esistenti")
    void testDatiDiTestEsistenti() {
        System.out.println("Test: Verifica dati di test esistenti nel database");

        List<Promozione> tuttePromozioni = promozioneDAO.findAll();
        assertFalse(tuttePromozioni.isEmpty(), "Dovrebbero esserci promozioni di test nel database");

        System.out.println("Promozioni di test trovate:");
        for (Promozione promo : tuttePromozioni) {
            System.out.printf("- %s: %s (%s, %.1f%% sconto)%n",
                    promo.getId(), promo.getNome(), promo.getTipo(), promo.getSconto());
        }

        // Verifica che ci siano almeno promozioni Standard e Fedeltà
        boolean hasStandard = tuttePromozioni.stream()
                .anyMatch(p -> "Standard".equals(p.getTipo()));
        boolean hasFedelta = tuttePromozioni.stream()
                .anyMatch(p -> "Fedelta".equals(p.getTipo()));

        assertTrue(hasStandard, "Dovrebbe esserci almeno una promozione Standard");
        assertTrue(hasFedelta, "Dovrebbe esserci almeno una promozione Fedeltà");

        System.out.println("✅ Dati di test verificati correttamente");
    }

    @Test
    @Order(12)
    @DisplayName("Test scenario completo - Gestione promozioni")
    void testScenarioCompletoGestionePromozioni() {
        System.out.println("Test: Scenario completo gestione promozioni");

        // 1. Creazione nuove promozioni tramite Factory
        PromozioneFactory factoryStandard = PromozioneFactory.getFactory("standard");
        PromozioneFactory factoryFedelta = PromozioneFactory.getFactory("fedelta");

        Promozione promoBlackFriday = factoryStandard.creaPromozione("Black Friday 2025", 50.0);
        Promozione promoVipNatale = factoryFedelta.creaPromozione("VIP Natale", 30.0);

        System.out.println("1. Promozioni create:");
        System.out.println("   - " + promoBlackFriday.toString());
        System.out.println("   - " + promoVipNatale.toString());

        // 2. Salvataggio nel database
        assertTrue(promozioneDAO.save(promoBlackFriday), "Black Friday dovrebbe essere salvata");
        assertTrue(promozioneDAO.save(promoVipNatale), "VIP Natale dovrebbe essere salvata");

        // 4. Test applicazione sconti
        double prezzoTest = 100.0;
        double prezzoConBlackFriday = promoBlackFriday.applicaSconto(prezzoTest);
        double prezzoConVipNatale = promoVipNatale.applicaSconto(prezzoTest);

        assertEquals(50.0, prezzoConBlackFriday, 0.01, "Black Friday 50% su €100 = €50");
        assertEquals(70.0, prezzoConVipNatale, 0.01, "VIP Natale 30% su €100 = €70");

        System.out.printf("4. Test applicazione sconti su €%.2f:%n", prezzoTest);
        System.out.printf("   - Con Black Friday: €%.2f%n", prezzoConBlackFriday);
        System.out.printf("   - Con VIP Natale: €%.2f%n", prezzoConVipNatale);


        promozioneDAO.delete(promoBlackFriday.getId());
        promozioneDAO.delete(promoVipNatale.getId());

        System.out.println("✅ Scenario completo gestione promozioni superato");
    }

    @Test
    @Order(13)
    @DisplayName("Test operazioni con dati non validi")
    void testOperazioniDatiNonValidi() {
        System.out.println("Test: Operazioni con dati non validi");

        // Test ricerca con ID inesistente
        Optional<Promozione> inesistente = promozioneDAO.findById("ID_INESISTENTE");
        assertFalse(inesistente.isPresent(), "Promozione inesistente non dovrebbe essere trovata");

        // Test aggiornamento promozione inesistente
        Promozione promoInesistente = new PromozioneStandard("Test Inesistente", 10.0);
        boolean aggiornatoInesistente = promozioneDAO.update(promoInesistente);
        assertFalse(aggiornatoInesistente, "Aggiornamento di promozione inesistente dovrebbe fallire");

        // Test eliminazione ID inesistente
        boolean eliminatoInesistente = promozioneDAO.delete("ID_INESISTENTE");
        assertFalse(eliminatoInesistente, "Eliminazione ID inesistente dovrebbe fallire");

        // Test ricerca tipo inesistente
        List<Promozione> tipoInesistente = promozioneDAO.findByTipo("TIPO_INESISTENTE");
        assertTrue(tipoInesistente.isEmpty(), "Ricerca tipo inesistente dovrebbe restituire lista vuota");

        System.out.println("✅ Gestione dati non validi corretta");
    }

    @Test
    @Order(14)
    @DisplayName("Test performance e limiti")
    void testPerformanceELimiti() {
        System.out.println("Test: Performance e limiti");

        long startTime = System.currentTimeMillis();

        // Test performance conteggi
        int count1 = promozioneDAO.count();

        // Test performance ricerche
        List<Promozione> all = promozioneDAO.findAll();
        List<Promozione> standard = promozioneDAO.findPromozioniStandardAttive();
        List<Promozione> fedelta = promozioneDAO.findPromozioniFedeltaAttive();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Le operazioni dovrebbero essere veloci (< 1 secondo)
        assertTrue(duration < 1000, "Operazioni dovrebbero essere veloci: " + duration + "ms");

        // Verifica coerenza dati
        assertEquals(count1, all.size(), "Count() dovrebbe corrispondere a findAll().size()");

        System.out.printf("✅ Performance test: %dms per %d operazioni%n", duration, 7);
        System.out.printf("   Totali: %d, Standard: %d, Fedeltà: %d%n",
                count1, standard.size(), fedelta.size());
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== PULIZIA TEST PROMOZIONE DAO ===");

        // Pulizia finale delle promozioni test
        if (promozioneDAO != null) {
            if (promozioneDAO.exists(promoTestStandardId)) {
                promozioneDAO.delete(promoTestStandardId);
            }
            if (promozioneDAO.exists(promoTestFedeltaId)) {
                promozioneDAO.delete(promoTestFedeltaId);
            }
        }

        System.out.println("Test PromozioneDAO completati con successo!");
    }
}
