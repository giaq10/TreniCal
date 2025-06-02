package server;

import it.trenical.common.stazioni.Stazione;
import it.trenical.common.viaggi.StatoViaggio;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.treni.TipoTreno;
import it.trenical.server.treni.Treno;
import it.trenical.server.treni.builder.TrenoDirector;
import it.trenical.server.tratte.Tratta;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Test JUnit completo per ViaggioDAO
 * Verifica operazioni CRUD e ricerche specifiche per viaggi
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test ViaggioDAO - Gestione Viaggi Database")
class ViaggioDAOTest {

    private static ViaggioDAO viaggioDAO;
    private static TrenoDirector trenoDirector;
    private static Viaggio viaggioTest;
    private static String viaggioTestId;

    @BeforeAll
    static void setUpAll() {
        System.out.println("=== SETUP TEST VIAGGIO DAO ===");
        viaggioDAO = new ViaggioDAO();
        trenoDirector = new TrenoDirector();

        assertNotNull(viaggioDAO, "ViaggioDAO non dovrebbe essere null");
        assertNotNull(trenoDirector, "TrenoDirector non dovrebbe essere null");

        // Crea viaggio di test
        creaViaggioTest();
    }

    static void creaViaggioTest() {
        System.out.println("Creazione viaggio di test...");

        // Componenti del viaggio
        Treno trenoTest = trenoDirector.costruisciTrenoStandard("TEST001");
        Tratta trattaTest = new Tratta(Stazione.ROMA, Stazione.MILANO);
        LocalDate dataTest = LocalDate.of(2025, 7, 15);

        // Viaggio completo
        viaggioTest = new Viaggio(trenoTest, trattaTest, dataTest);
        viaggioTestId = viaggioTest.getId();

        System.out.println("Viaggio test creato: " + viaggioTest.toString());
        System.out.println("ID viaggio test: " + viaggioTestId);
    }

    @BeforeEach
    void setUp() {
        // Pulizia prima di ogni test
        if (viaggioDAO.findById(viaggioTestId).isPresent()) {
            viaggioDAO.delete(viaggioTestId);
        }
    }

    @AfterEach
    void tearDown() {
        // Pulizia dopo ogni test
        if (viaggioDAO.findById(viaggioTestId).isPresent()) {
            viaggioDAO.delete(viaggioTestId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test conteggio viaggi esistenti")
    void testConteggioViaggiEsistenti() {
        System.out.println("Test: Conteggio viaggi esistenti");

        int count = viaggioDAO.count();

        assertTrue(count >= 0, "Il conteggio non dovrebbe essere negativo");
        assertTrue(count >= 3, "Dovrebbero esserci almeno i 3 viaggi di test iniziali");

        System.out.printf("✅ Trovati %d viaggi nel database%n", count);
    }

    @Test
    @Order(2)
    @DisplayName("Test salvataggio nuovo viaggio")
    void testSalvataggioNuovoViaggio() {
        System.out.println("Test: Salvataggio nuovo viaggio");

        // Verifica che il viaggio non esista
        assertFalse(viaggioDAO.findById(viaggioTestId).isPresent(),
                "Viaggio test non dovrebbe esistere inizialmente");

        // Salvataggio
        boolean risultato = viaggioDAO.save(viaggioTest);
        assertTrue(risultato, "Il salvataggio dovrebbe riuscire");

        // Verifica salvataggio
        Optional<Viaggio> viaggioSalvato = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioSalvato.isPresent(), "Viaggio salvato dovrebbe essere trovato");

        Viaggio viaggio = viaggioSalvato.get();
        assertEquals(viaggioTestId, viaggio.getId());
        assertEquals("TEST001", viaggio.getTreno().getCodice());
        assertEquals(TipoTreno.STANDARD, viaggio.getTreno().getTipoTreno());
        assertEquals(Stazione.ROMA, viaggio.getTratta().getStazionePartenza());
        assertEquals(Stazione.MILANO, viaggio.getTratta().getStazioneArrivo());
        assertEquals(LocalDate.of(2025, 7, 15), viaggio.getDataViaggio());

        System.out.println("✅ Salvataggio viaggio riuscito");
        System.out.println("Viaggio salvato: " + viaggio.toString());
    }

    @Test
    @Order(3)
    @DisplayName("Test ricerca viaggio per ID")
    void testRicercaViaggioPerID() {
        System.out.println("Test: Ricerca viaggio per ID");

        // Prima salva il viaggio
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Test ricerca esistente
        Optional<Viaggio> viaggioTrovato = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioTrovato.isPresent(), "Viaggio dovrebbe essere trovato");

        Viaggio viaggio = viaggioTrovato.get();
        assertEquals(viaggioTestId, viaggio.getId());
        assertNotNull(viaggio.getTreno());
        assertNotNull(viaggio.getTratta());

        // Test ricerca inesistente
        Optional<Viaggio> viaggioInesistente = viaggioDAO.findById("ID_INESISTENTE");
        assertFalse(viaggioInesistente.isPresent(), "Viaggio inesistente non dovrebbe essere trovato");

        System.out.println("✅ Ricerca per ID funziona correttamente");
    }

    @Test
    @Order(4)
    @DisplayName("Test ricerca viaggi per tratta e data")
    void testRicercaViaggiPerTrattaEData() {
        System.out.println("Test: Ricerca viaggi per tratta e data");

        // Salva il viaggio test
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Ricerca per tratta e data (stesso giorno del viaggio test)
        List<Viaggio> viaggiTrovati = viaggioDAO.findByTrattaEData(
                Stazione.ROMA,
                Stazione.MILANO,
                LocalDate.of(2025, 7, 15)  // Stessa data del viaggioTest
        );

        assertNotNull(viaggiTrovati, "Lista non dovrebbe essere null");
        assertFalse(viaggiTrovati.isEmpty(), "Dovrebbe trovare almeno 1 viaggio");

        // Verifica che il nostro viaggio sia nella lista
        boolean viaggioTestTrovato = viaggiTrovati.stream()
                .anyMatch(v -> v.getId().equals(viaggioTestId));
        assertTrue(viaggioTestTrovato, "Il viaggio test dovrebbe essere nella lista");

        // Test ricerca senza risultati
        List<Viaggio> viaggiVuoti = viaggioDAO.findByTrattaEData(
                Stazione.REGGIO_CALABRIA,
                Stazione.VENEZIA,
                LocalDate.of(2030, 1, 1)
        );
        assertTrue(viaggiVuoti.isEmpty(), "Non dovrebbero esserci viaggi per questa tratta/data");

        System.out.printf("✅ Trovati %d viaggi per Roma → Milano del 15/07/2025%n", viaggiTrovati.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test ricerca viaggi per tipo treno")
    void testRicercaViaggiPerTipoTreno() {
        System.out.println("Test: Ricerca viaggi per tipo treno");

        // Salva il viaggio test (Standard)
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Ricerca viaggi Standard
        List<Viaggio> viaggiStandard = viaggioDAO.findByTipoTreno(TipoTreno.STANDARD);
        assertNotNull(viaggiStandard, "Lista non dovrebbe essere null");
        assertFalse(viaggiStandard.isEmpty(), "Dovrebbero esserci viaggi Standard");

        // Verifica che tutti i viaggi siano effettivamente Standard
        for (Viaggio viaggio : viaggiStandard) {
            assertEquals(TipoTreno.STANDARD, viaggio.getTreno().getTipoTreno(),
                    "Tutti i viaggi dovrebbero essere Standard");
        }

        // Verifica che il nostro viaggio sia nella lista
        boolean viaggioTestTrovato = viaggiStandard.stream()
                .anyMatch(v -> v.getId().equals(viaggioTestId));
        assertTrue(viaggioTestTrovato, "Il viaggio test dovrebbe essere nella lista Standard");

        System.out.printf("✅ Trovati %d viaggi Standard%n", viaggiStandard.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test aggiornamento stato viaggio")
    void testAggiornamentoStatoViaggio() {
        System.out.println("Test: Aggiornamento stato viaggio");

        // Salva il viaggio
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Verifica stato iniziale
        Optional<Viaggio> viaggioIniziale = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioIniziale.isPresent());
        assertEquals(StatoViaggio.PROGRAMMATO, viaggioIniziale.get().getStato());

        // Aggiorna stato
        boolean aggiornato = viaggioDAO.updateStato(viaggioTestId, StatoViaggio.CONFERMATO);
        assertTrue(aggiornato, "Aggiornamento stato dovrebbe riuscire");

        // Verifica aggiornamento
        Optional<Viaggio> viaggioAggiornato = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioAggiornato.isPresent());
        assertEquals(StatoViaggio.CONFERMATO, viaggioAggiornato.get().getStato());

        // Test aggiornamento stato inesistente
        boolean nonAggiornato = viaggioDAO.updateStato("ID_INESISTENTE", StatoViaggio.CANCELLATO);
        assertFalse(nonAggiornato, "Aggiornamento di viaggio inesistente dovrebbe fallire");

        System.out.println("✅ Aggiornamento stato viaggio riuscito");
    }

    @Test
    @Order(7)
    @DisplayName("Test aggiornamento posti disponibili")
    void testAggiornamentoPostiDisponibili() {
        System.out.println("Test: Aggiornamento posti disponibili");

        // Salva il viaggio
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Verifica posti iniziali
        Optional<Viaggio> viaggioIniziale = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioIniziale.isPresent());
        int postiIniziali = viaggioIniziale.get().getPostiDisponibili();
        assertEquals(350, postiIniziali, "Treno Standard dovrebbe avere 350 posti");

        // Simula prenotazione (riduci posti)
        int nuoviPosti = postiIniziali - 10;
        boolean aggiornato = viaggioDAO.updatePostiDisponibili(viaggioTestId, nuoviPosti);
        assertTrue(aggiornato, "Aggiornamento posti dovrebbe riuscire");

        // Verifica aggiornamento
        Optional<Viaggio> viaggioAggiornato = viaggioDAO.findById(viaggioTestId);
        assertTrue(viaggioAggiornato.isPresent());
        assertEquals(nuoviPosti, viaggioAggiornato.get().getPostiDisponibili());

        System.out.printf("✅ Posti aggiornati da %d a %d%n", postiIniziali, nuoviPosti);
    }

    @Test
    @Order(8)
    @DisplayName("Test ricerca viaggi disponibili")
    void testRicercaViaggiDisponibili() {
        System.out.println("Test: Ricerca viaggi disponibili");

        // Salva il viaggio test (dovrebbe essere disponibile)
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");

        // Ricerca viaggi disponibili
        List<Viaggio> viaggiDisponibili = viaggioDAO.findViaggiDisponibili();
        assertNotNull(viaggiDisponibili, "Lista non dovrebbe essere null");
        assertFalse(viaggiDisponibili.isEmpty(), "Dovrebbero esserci viaggi disponibili");

        // Verifica che tutti abbiano posti disponibili e non siano cancellati
        for (Viaggio viaggio : viaggiDisponibili) {
            assertTrue(viaggio.getPostiDisponibili() > 0,
                    "Viaggio dovrebbe avere posti disponibili: " + viaggio.getId());
            assertNotEquals(StatoViaggio.CANCELLATO, viaggio.getStato(),
                    "Viaggio non dovrebbe essere cancellato: " + viaggio.getId());
        }

        // Il nostro viaggio test dovrebbe essere nella lista
        boolean viaggioTestTrovato = viaggiDisponibili.stream()
                .anyMatch(v -> v.getId().equals(viaggioTestId));
        assertTrue(viaggioTestTrovato, "Il viaggio test dovrebbe essere disponibile");

        System.out.printf("✅ Trovati %d viaggi disponibili%n", viaggiDisponibili.size());
    }

    @Test
    @Order(9)
    @DisplayName("Test eliminazione viaggio")
    void testEliminazioneViaggio() {
        System.out.println("Test: Eliminazione viaggio");

        // Salva il viaggio
        assertTrue(viaggioDAO.save(viaggioTest), "Viaggio dovrebbe essere salvato");
        assertTrue(viaggioDAO.findById(viaggioTestId).isPresent(), "Viaggio dovrebbe esistere");

        // Elimina il viaggio
        boolean eliminato = viaggioDAO.delete(viaggioTestId);
        assertTrue(eliminato, "Eliminazione dovrebbe riuscire");

        // Verifica eliminazione
        Optional<Viaggio> viaggioEliminato = viaggioDAO.findById(viaggioTestId);
        assertFalse(viaggioEliminato.isPresent(), "Viaggio non dovrebbe più esistere");

        // Test eliminazione viaggio inesistente
        boolean nonEliminato = viaggioDAO.delete("ID_INESISTENTE");
        assertFalse(nonEliminato, "Eliminazione di viaggio inesistente dovrebbe fallire");

        System.out.println("✅ Eliminazione viaggio riuscita");
    }

    @Test
    @Order(10)
    @DisplayName("Test verifica dati di test esistenti")
    void testDatiDiTestEsistenti() {
        System.out.println("Test: Verifica dati di test esistenti nel database");

        // Dovrebbero esserci viaggi dai dati di setup iniziale
        List<Viaggio> tuttiIViaggi = viaggioDAO.findViaggiDisponibili();
        assertFalse(tuttiIViaggi.isEmpty(), "Dovrebbero esserci viaggi di test nel database");

        System.out.println("Viaggi di test trovati:");
        for (Viaggio viaggio : tuttiIViaggi) {
            System.out.printf("- %s: %s %s → %s (%s)%n",
                    viaggio.getId(),
                    viaggio.getTreno().getCodice(),
                    viaggio.getTratta().getStazionePartenza().getNome(),
                    viaggio.getTratta().getStazioneArrivo().getNome(),
                    viaggio.getTreno().getTipoTreno().getNome());
        }

        // Test ricerca per tipo specifico
        List<Viaggio> viaggiBusiness = viaggioDAO.findByTipoTreno(TipoTreno.BUSINESS);
        List<Viaggio> viaggiStandard = viaggioDAO.findByTipoTreno(TipoTreno.STANDARD);
        List<Viaggio> viaggiEconomy = viaggioDAO.findByTipoTreno(TipoTreno.ECONOMY);

        System.out.printf("Viaggi per tipo: Business=%d, Standard=%d, Economy=%d%n",
                viaggiBusiness.size(), viaggiStandard.size(), viaggiEconomy.size());

        // Dovrebbe esserci almeno un viaggio per ogni tipo
        assertTrue(viaggiBusiness.size() >= 1, "Dovrebbe esserci almeno 1 viaggio Business");
        assertTrue(viaggiStandard.size() >= 1, "Dovrebbe esserci almeno 1 viaggio Standard");
        assertTrue(viaggiEconomy.size() >= 1, "Dovrebbe esserci almeno 1 viaggio Economy");

        System.out.println("✅ Dati di test verificati correttamente");
    }

    @Test
    @Order(11)
    @DisplayName("Test scenario completo - Prenotazione viaggio")
    void testScenarioCompletoPrenotazione() {
        System.out.println("Test: Scenario completo prenotazione viaggio");

        // 1. Ricerca viaggi disponibili per una tratta (data presente nel database)
        List<Viaggio> viaggiDisponibili = viaggioDAO.findByTrattaEData(
                Stazione.NAPOLI,
                Stazione.TORINO,
                LocalDate.of(2025, 6, 16)  // Data dal database di test
        );

        if (!viaggiDisponibili.isEmpty()) {
            Viaggio viaggioScelto = viaggiDisponibili.get(0);
            String idViaggio = viaggioScelto.getId();
            int postiIniziali = viaggioScelto.getPostiDisponibili();

            System.out.printf("Viaggio scelto: %s con %d posti disponibili%n",
                    idViaggio, postiIniziali);

            // 2. Simula prenotazione (riduce posti)
            int nuoviPosti = postiIniziali - 1;
            boolean prenotato = viaggioDAO.updatePostiDisponibili(idViaggio, nuoviPosti);
            assertTrue(prenotato, "Prenotazione dovrebbe riuscire");

            // 3. Verifica prenotazione
            Optional<Viaggio> viaggioPrenotato = viaggioDAO.findById(idViaggio);
            assertTrue(viaggioPrenotato.isPresent());
            assertEquals(nuoviPosti, viaggioPrenotato.get().getPostiDisponibili());

            // 4. Aggiorna stato viaggio
            boolean confermato = viaggioDAO.updateStato(idViaggio, StatoViaggio.CONFERMATO);
            assertTrue(confermato, "Conferma dovrebbe riuscire");

            // 5. Verifica stato finale
            Optional<Viaggio> viaggioFinale = viaggioDAO.findById(idViaggio);
            assertTrue(viaggioFinale.isPresent());
            assertEquals(StatoViaggio.CONFERMATO, viaggioFinale.get().getStato());
            assertEquals(nuoviPosti, viaggioFinale.get().getPostiDisponibili());

            System.out.println("✅ Scenario completo prenotazione superato");
        } else {
            System.out.println("⚠️ Nessun viaggio disponibile per Napoli → Torino, creo viaggio test");

            // Crea e testa con il nostro viaggio
            assertTrue(viaggioDAO.save(viaggioTest));
            Optional<Viaggio> viaggioCreato = viaggioDAO.findById(viaggioTestId);
            assertTrue(viaggioCreato.isPresent());
            System.out.println("✅ Scenario con viaggio test creato");
        }
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== PULIZIA TEST VIAGGIO DAO ===");

        if (viaggioDAO != null && viaggioTestId != null) {
            if (viaggioDAO.findById(viaggioTestId).isPresent()) {
                viaggioDAO.delete(viaggioTestId);
            }
        }

        System.out.println("Test ViaggioDAO completati con successo!");
    }
}