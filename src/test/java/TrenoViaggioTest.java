import it.trenical.common.model.stazioni.Stazione;
import it.trenical.common.model.stazioni.Binario;
import it.trenical.common.model.tratte.Tratta;
import it.trenical.common.model.tratte.TrattaFactory;
import it.trenical.common.model.treni.*;
import it.trenical.common.model.treni.builder.*;
import it.trenical.common.model.viaggi.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TrenoViaggioTest {

    private Treno trenoEconomy;
    private Treno trenoStandard;
    private Treno trenoBusiness;
    private Tratta trattaRomaMilano;
    private Tratta trattaNapoliTorino;

    @BeforeAll
    static void setupAll() {
        System.out.println("=== INIZIO TEST SUITE TRENO-VIAGGIO ===");
    }

    @BeforeEach
    void setup() {
        System.out.println("Preparazione test...");

        // Creazione treni di test
        trenoEconomy = new BuilderEconomy()
                .codice("REG001")
                .build();

        trenoStandard = new BuilderStandard()
                .codice("IC001")
                .postiTotali(300)
                .build();

        trenoBusiness = new BuilderBusiness()
                .codice("FR001")
                .build();

        // Creazione tratte di test
        trattaRomaMilano = TrattaFactory.creaTratta(
                Stazione.ROMA, Stazione.MILANO, TipoTreno.BUSINESS);
        trattaNapoliTorino = TrattaFactory.creaTratta(
                Stazione.NAPOLI, Stazione.TORINO, TipoTreno.STANDARD);
    }

    @Test
    @DisplayName("Creazione Treno Economy con Builder")
    void shouldCreateTrenoEconomy() {
        System.out.println("Test: Creazione Treno Economy");

        assertNotNull(trenoEconomy, "Treno Economy non dovrebbe essere null");
        assertEquals("REG001", trenoEconomy.getCodice(), "Codice treno non corretto");
        assertEquals(TipoTreno.ECONOMY, trenoEconomy.getTipoTreno(), "Tipo treno non corretto");
        assertEquals(450, trenoEconomy.getPostiTotali(), "Numero posti non corretto");

        // Verifica servizi predefiniti per Economy
        assertTrue(trenoEconomy.hasServizio(ServizioTreno.ARIA_CONDIZIONATA),
                "Treno Economy dovrebbe avere aria condizionata");
        assertFalse(trenoEconomy.hasServizio(ServizioTreno.RISTORAZIONE),
                "Treno Economy non dovrebbe avere ristorazione");

        System.out.println("✅ " + trenoEconomy);
        System.out.println("🔧 Servizi: " + trenoEconomy.getServizi());
    }

    @Test
    @DisplayName("Creazione Treno Business con tutti i servizi")
    void shouldCreateTrenoBusiness() {
        System.out.println("Test: Creazione Treno Business");

        assertNotNull(trenoBusiness, "Treno Business non dovrebbe essere null");
        assertEquals("FR001", trenoBusiness.getCodice());
        assertEquals(TipoTreno.BUSINESS, trenoBusiness.getTipoTreno());
        assertEquals(250, trenoBusiness.getPostiTotali());

        // Verifica tutti i servizi premium
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.ALTA_VELOCITA));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.RISTORAZIONE));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.WIFI));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.BUSINESS_LOUNGE));

        // Verifica numero servizi
        assertEquals(7, trenoBusiness.getServizi().size(),
                "Treno Business dovrebbe avere 7 servizi");

        System.out.println("✅ " + trenoBusiness);
        System.out.println("🔧 Servizi completi: " + trenoBusiness.getServizi().size());
    }

    @Test
    @DisplayName("Builder personalizzato con servizi custom")
    void shouldCreateTrenoCustom() {
        System.out.println("Test: Treno Custom Builder");

        Treno trenoCustom = new Builder()
                .codice("CUSTOM01")
                .tipo(TipoTreno.STANDARD)
                .postiTotali(400)
                .aggiungiServizio(ServizioTreno.WIFI)
                .servizi(ServizioTreno.PRESE_ELETTRICHE, ServizioTreno.SILENZIOSO)
                .build();

        assertNotNull(trenoCustom);
        assertEquals("CUSTOM01", trenoCustom.getCodice());
        assertEquals(TipoTreno.STANDARD, trenoCustom.getTipoTreno());
        assertEquals(400, trenoCustom.getPostiTotali());

        // Verifica servizi custom
        assertTrue(trenoCustom.hasServizio(ServizioTreno.WIFI));
        assertTrue(trenoCustom.hasServizio(ServizioTreno.PRESE_ELETTRICHE));
        assertTrue(trenoCustom.hasServizio(ServizioTreno.SILENZIOSO));
        assertEquals(3, trenoCustom.getServizi().size());

        System.out.println("✅ " + trenoCustom);
        System.out.println("🔧 Servizi custom: " + trenoCustom.getServizi());
    }

    @Test
    @DisplayName("Validazione costruttore - campi obbligatori")
    void shouldThrowExceptionWhenRequiredFieldsMissing() {
        System.out.println("Test: Validazione campi obbligatori");

        // Test codice null
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> new Builder().tipo(TipoTreno.ECONOMY).build(),
                "Dovrebbe lanciare eccezione per codice null"
        );

// Test tipo null
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> new Builder().codice("TEST").build(),
                "Dovrebbe lanciare eccezione per tipo null"
        );

// Test posti negativi
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> new Builder().codice("TEST").tipo(TipoTreno.ECONOMY).postiTotali(-1).build(),
                "Dovrebbe lanciare eccezione per posti negativi"
        );
        assertEquals("Numero posti deve essere positivo", exception3.getMessage());
        System.out.println("✅ Eccezione posti negativi: " + exception3.getMessage());
    }

    @Test
    @DisplayName("Creazione Viaggio con compatibilità treno-tratta")
    void shouldCreateViaggioCompatible() {
        System.out.println("Test: Creazione Viaggio compatibile");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoBusiness,
                trattaRomaMilano,
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 30)
        );

        assertNotNull(viaggio, "Viaggio non dovrebbe essere null");
        assertEquals(trenoBusiness, viaggio.getTreno());
        assertEquals(trattaRomaMilano, viaggio.getTratta());
        assertEquals(LocalDate.now().plusDays(1), viaggio.getDataViaggio());
        assertEquals(LocalTime.of(14, 30), viaggio.getOrarioPartenza());

        // Verifica calcolo orario arrivo dalla Strategy della tratta
        LocalTime orarioArrivoAtteso = LocalTime.of(14, 30)
                .plusMinutes(trattaRomaMilano.getDurataMinuti());
        assertEquals(orarioArrivoAtteso, viaggio.getOrarioArrivo());

        // Verifica stato iniziale
        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
        assertEquals(trenoBusiness.getPostiTotali(), viaggio.getPostiDisponibili());
        assertTrue(viaggio.isDisponibile());

        System.out.println("✅ " + viaggio);
        System.out.println("📅 Partenza: " + viaggio.getDataOraPartenzaFormattata());
        System.out.println("📅 Arrivo: " + viaggio.getDataOraArrivoFormattata());
    }

    @Test
    @DisplayName("Verifica integrazione Tratta-Strategy-Viaggio per calcoli")
    void shouldIntegrateTrattaStrategyWithViaggio() {
        System.out.println("Test: Integrazione Tratta Strategy → Viaggio");

        // Crea tratta Business specifica
        Tratta trattaTest = TrattaFactory.creaTratta(
                Stazione.ROMA,
                Stazione.NAPOLI,
                TipoTreno.BUSINESS
        );

        // Crea treno compatibile
        Treno trenoTest = new BuilderBusiness()
                .codice("FR_TEST")
                .build();

        // Crea viaggio
        LocalTime orarioPartenza = LocalTime.of(10, 15);
        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoTest, trattaTest,
                LocalDate.now().plusDays(1), orarioPartenza
        );

        // === VERIFICA PASSAGGIO DATI DALLA TRATTA ===

        // 1. Verifica che il prezzo del viaggio viene dalla Strategy della tratta
        double prezzoTratta = trattaTest.getPrezzo();
        double prezzoViaggio = viaggio.getPrezzo();
        assertEquals(prezzoTratta, prezzoViaggio, 0.01,
                "Il prezzo del viaggio deve essere uguale a quello calcolato dalla Strategy della tratta");

        // 2. Verifica che la durata viene dalla Strategy della tratta
        int durataTrattaMinuti = trattaTest.getDurataMinuti();
        LocalTime orarioArrivoCalcolato = orarioPartenza.plusMinutes(durataTrattaMinuti);
        assertEquals(orarioArrivoCalcolato, viaggio.getOrarioArrivo(),
                "L'orario di arrivo deve essere calcolato usando la durata della Strategy");

        // 3. Verifica stazioni passate correttamente
        assertEquals(trattaTest.getStazionePartenza(), viaggio.getTratta().getStazionePartenza(),
                "Stazione di partenza deve essere passata correttamente");
        assertEquals(trattaTest.getStazioneArrivo(), viaggio.getTratta().getStazioneArrivo(),
                "Stazione di arrivo deve essere passata correttamente");

        // 4. Verifica distanza e tipo treno
        assertEquals(trattaTest.getDistanzaKm(), viaggio.getTratta().getDistanzaKm(),
                "Distanza deve essere passata correttamente");
        assertEquals(trattaTest.getTipoTreno(), viaggio.getTratta().getTipoTreno(),
                "Tipo treno deve essere passato correttamente");

        // === LOG DETTAGLIATO ===
        System.out.println("🛤️ DATI TRATTA (da Strategy):");
        System.out.println("   📍 " + trattaTest.getStazionePartenza().getNome() +
                " → " + trattaTest.getStazioneArrivo().getNome());
        System.out.println("   📏 Distanza: " + trattaTest.getDistanzaKm() + " km");
        System.out.println("   ⏱️ Durata (Strategy): " + trattaTest.getDurataFormattata() +
                " (" + trattaTest.getDurataMinuti() + " min)");
        System.out.println("   💰 Prezzo (Strategy): €" + String.format("%.2f", trattaTest.getPrezzo()));
        System.out.println("   🚆 Tipo: " + trattaTest.getTipoTreno());

        System.out.println("🚄 DATI VIAGGIO (derivati da Tratta):");
        System.out.println("   🚆 Treno: " + viaggio.getTreno().getCodice());
        System.out.println("   📅 Data: " + viaggio.getDataViaggio());
        System.out.println("   🕐 Partenza: " + viaggio.getOrarioPartenza());
        System.out.println("   🕐 Arrivo calcolato: " + viaggio.getOrarioArrivo());
        System.out.println("   💰 Prezzo finale: €" + String.format("%.2f", viaggio.getPrezzo()));
        System.out.println("   🎫 Posti: " + viaggio.getPostiDisponibili());

        System.out.println("✅ INTEGRAZIONE VERIFICATA: Strategy durata (min) → Orario arrivo formattato");
        System.out.println("📅 Formato leggibile: " + viaggio.getDataOraPartenzaFormattata() +
                " → " + viaggio.getDataOraArrivoFormattata());
    }

    @Test
    @DisplayName("Incompatibilità treno-tratta deve lanciare eccezione")
    void shouldThrowExceptionWhenTrenoTrattaIncompatible() {
        System.out.println("Test: Incompatibilità treno-tratta");

        // Treno Economy su tratta Business - dovrebbe fallire
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Viaggio(
                        trenoEconomy,  // ECONOMY
                        trattaRomaMilano,  // BUSINESS
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                ),
                "Dovrebbe lanciare eccezione per incompatibilità"
        );

        assertTrue(exception.getMessage().contains("incompatibile"));
        System.out.println("✅ Incompatibilità rilevata: " + exception.getMessage());
    }

    @Test
    @DisplayName("Gestione prenotazione e liberazione posti")
    void shouldManagePostiCorrectly() {
        System.out.println("Test: Gestione posti");

        // Crea viaggio con pochi posti per il test
        Treno trenoTest = new BuilderEconomy()
                .codice("TEST001")
                .postiTotali(5)
                .build();
        Tratta trattaTest = TrattaFactory.creaTratta(
                Stazione.ROMA, Stazione.NAPOLI, TipoTreno.ECONOMY);

        Viaggio viaggio = new Viaggio(trenoTest, trattaTest,
                LocalDate.now().plusDays(1), LocalTime.of(12, 0));

        // Stato iniziale
        assertEquals(5, viaggio.getPostiDisponibili());
        assertEquals(0, viaggio.getPostiOccupati());
        assertTrue(viaggio.hasPostiDisponibili());

        // Prenota 3 posti
        assertTrue(viaggio.prenotaPosto(), "Prima prenotazione dovrebbe riuscire");
        assertTrue(viaggio.prenotaPosto(), "Seconda prenotazione dovrebbe riuscire");
        assertTrue(viaggio.prenotaPosto(), "Terza prenotazione dovrebbe riuscire");

        assertEquals(2, viaggio.getPostiDisponibili());
        assertEquals(3, viaggio.getPostiOccupati());

        System.out.println("📊 Dopo 3 prenotazioni: " + viaggio.getPostiDisponibili() + " disponibili");

        // Prenota tutti i posti rimanenti
        assertTrue(viaggio.prenotaPosto());
        assertTrue(viaggio.prenotaPosto());

        assertEquals(0, viaggio.getPostiDisponibili());
        assertEquals(5, viaggio.getPostiOccupati());
        assertFalse(viaggio.hasPostiDisponibili());

        // Tentativo prenotazione su viaggio pieno
        assertFalse(viaggio.prenotaPosto(), "Prenotazione su viaggio pieno dovrebbe fallire");

        System.out.println("📊 Viaggio pieno: " + viaggio.getPostiOccupati() + " occupati");

        // Libera un posto
        assertTrue(viaggio.liberaPosto(), "Liberazione posto dovrebbe riuscire");
        assertEquals(1, viaggio.getPostiDisponibili());
        assertTrue(viaggio.hasPostiDisponibili());

        System.out.println("📊 Dopo liberazione: " + viaggio.getPostiDisponibili() + " disponibili");
    }

    @Test
    @DisplayName("Creazione Viaggio con binari specificati")
    void shouldCreateViaggioWithBinari() {
        System.out.println("Test: Creazione Viaggio con binari");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoStandard, trattaNapoliTorino,
                LocalDate.now().plusDays(1), LocalTime.of(10, 15),
                Binario.BINARIO_1, Binario.BINARIO_2
        );

        assertNotNull(viaggio);
        assertEquals(Binario.BINARIO_1, viaggio.getBinarioPartenza());
        //assertEquals(Binario.BINARIO_2, viaggio.getBinarioArrivo());

        System.out.println("✅ " + viaggio);
        System.out.println("🚂 " + viaggio.getInfoBinari());
    }

    @Test
    @DisplayName("Cambio orario partenza con ricalcolo automatico arrivo")
    void shouldChangeOrarioPartenza() {
        System.out.println("Test: Cambio orario partenza");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoBusiness, trattaRomaMilano,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0)
        );

        LocalTime partenzaOriginale = LocalTime.of(10, 0);
        assertEquals(partenzaOriginale, viaggio.getOrarioPartenzaEffettivo());

        // Cambio orario partenza
        LocalTime nuovaPartenza = LocalTime.of(11, 30);
        viaggio.cambioOrarioPartenza(nuovaPartenza);

        assertEquals(nuovaPartenza, viaggio.getOrarioPartenzaEffettivo());
        LocalTime nuovoArrivoAtteso = nuovaPartenza.plusMinutes(trattaRomaMilano.getDurataMinuti());
        assertEquals(nuovoArrivoAtteso, viaggio.getOrarioArrivoEffettivo());

        System.out.println("✅ Cambio orario verificato");
    }

    @Test
    @DisplayName("Cambio binari")
    void shouldChangeBinari() {
        System.out.println("Test: Cambio binari");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoEconomy,
                TrattaFactory.creaTratta(Stazione.FIRENZE, Stazione.BOLOGNA, TipoTreno.ECONOMY),
                LocalDate.now().plusDays(1), LocalTime.of(13, 20)
        );

        // Assegna binario partenza
        viaggio.cambioBinarioPartenza(Binario.BINARIO_2);
        assertEquals(Binario.BINARIO_2, viaggio.getBinarioPartenza());

        // Assegna binario arrivo
//        viaggio.cambioBinarioArrivo(Binario.BINARIO_1);
//        assertEquals(Binario.BINARIO_1, viaggio.getBinarioArrivo());

        System.out.println("✅ Cambio binari verificato: " + viaggio.getInfoBinari());
    }

    @Test
    @DisplayName("Cancellazione viaggio")
    void shouldCancelViaggio() {
        System.out.println("Test: Cancellazione viaggio");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoBusiness, trattaRomaMilano,
                LocalDate.now().plusDays(1), LocalTime.of(18, 45)
        );

        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
        assertFalse(viaggio.isCancellato());

        // Cancellazione
        String motivoCancellazione = "Sciopero del personale";
        viaggio.cancellaViaggio(motivoCancellazione);

        assertEquals(StatoViaggio.CANCELLATO, viaggio.getStato());
        assertEquals(motivoCancellazione, viaggio.getMotivoCancellazione());
        assertTrue(viaggio.isCancellato());

        System.out.println("✅ Cancellazione gestita: " + viaggio.getMotivoCancellazione());
    }

    @Test
    @DisplayName("Gestione stato viaggio e ritardi")
    void shouldManageStatoAndRitardi() {
        System.out.println("Test: Gestione stato e ritardi");

        Viaggio viaggio = ViaggioFactory.creaViaggio(
                trenoStandard, trattaNapoliTorino,
                LocalDate.now().plusDays(1), LocalTime.of(16, 0));

        // Stato iniziale
        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
        assertEquals(0, viaggio.getRitardoMinuti());
        //assertNull(viaggio.getMotivoRitardo());

        // Imposta ritardo
        viaggio.impostaRitardo(15);//, "Lavori in corso");
        assertEquals(StatoViaggio.RITARDO, viaggio.getStato());
        assertEquals(15, viaggio.getRitardoMinuti());
        //assertEquals("Lavori in corso", viaggio.getMotivoRitardo());

        // Verifica orario arrivo con ritardo
        LocalTime orarioArrivoOriginale = viaggio.getOrarioArrivo();
        LocalTime orarioArrivoEffettivo = viaggio.getOrarioArrivoEffettivo();
        assertEquals(orarioArrivoOriginale.plusMinutes(15), orarioArrivoEffettivo);

        System.out.println("⏰ Orario arrivo originale: " + orarioArrivoOriginale);
        System.out.println("⏰ Orario arrivo con ritardo: " + orarioArrivoEffettivo);
        //System.out.println("📝 Motivo ritardo: " + viaggio.getMotivoRitardo());

        // Cambia stato
        viaggio.aggiornaStato(StatoViaggio.IN_VIAGGIO);
        assertEquals(StatoViaggio.IN_VIAGGIO, viaggio.getStato());
        assertTrue(viaggio.getStato().isAttivo());

        viaggio.aggiornaStato(StatoViaggio.ARRIVATO);
        assertEquals(StatoViaggio.ARRIVATO, viaggio.getStato());
        assertTrue(viaggio.getStato().isConcluso());

        System.out.println("✅ Stati gestiti correttamente");
    }

    @Test
    @DisplayName("Confronto calcoli Strategy per tutti i tipi treno")
    void shouldCompareStrategyCalculationsForAllTypes() {
        System.out.println("Test: Confronto Strategy per tutti i tipi treno");

        // Stessa tratta con 3 tipi diversi
        Stazione partenza = Stazione.MILANO;
        Stazione arrivo = Stazione.NAPOLI;

        Tratta trattaEconomy = TrattaFactory.creaTratta(partenza, arrivo, TipoTreno.ECONOMY);
        Tratta trattaStandard = TrattaFactory.creaTratta(partenza, arrivo, TipoTreno.STANDARD);
        Tratta trattaBusiness = TrattaFactory.creaTratta(partenza, arrivo, TipoTreno.BUSINESS);

        // Crea viaggi corrispondenti
        LocalTime orarioBase = LocalTime.of(12, 0);
        LocalDate dataBase = LocalDate.now().plusDays(1);

        Viaggio viaggioEconomy = ViaggioFactory.creaViaggio(
                new BuilderEconomy().codice("ECO_001").build(),
                trattaEconomy, dataBase, orarioBase);

        Viaggio viaggioStandard = ViaggioFactory.creaViaggio(
                new BuilderStandard().codice("STD_001").build(),
                trattaStandard, dataBase, orarioBase);

        Viaggio viaggioBusiness = ViaggioFactory.creaViaggio(
                new BuilderBusiness().codice("BUS_001").build(),
                trattaBusiness, dataBase, orarioBase);

        // === VERIFICA LOGICA DELLE STRATEGY ===

        // 1. Business dovrebbe essere più costoso di Standard, che è più costoso di Economy
        assertTrue(viaggioBusiness.getPrezzo() > viaggioStandard.getPrezzo(),
                "Business dovrebbe costare più di Standard");
        assertTrue(viaggioStandard.getPrezzo() > viaggioEconomy.getPrezzo(),
                "Standard dovrebbe costare più di Economy");

        // 2. Business dovrebbe essere più veloce (arrivo prima)
        assertTrue(viaggioBusiness.getOrarioArrivo().isBefore(viaggioStandard.getOrarioArrivo()),
                "Business dovrebbe arrivare prima di Standard");
        assertTrue(viaggioStandard.getOrarioArrivo().isBefore(viaggioEconomy.getOrarioArrivo()),
                "Standard dovrebbe arrivare prima di Economy");

        // 3. Tutti dovrebbero partire alla stessa ora
        assertEquals(orarioBase, viaggioEconomy.getOrarioPartenza());
        assertEquals(orarioBase, viaggioStandard.getOrarioPartenza());
        assertEquals(orarioBase, viaggioBusiness.getOrarioPartenza());

        // === LOG DETTAGLIATO COMPARATIVO ===
        System.out.println("📊 CONFRONTO STRATEGY CALCULATIONS:");
        System.out.println("   Tratta: " + partenza.getNome() + " → " + arrivo.getNome());
        System.out.println("   Partenza comune: " + orarioBase);
        System.out.println();

        System.out.printf("🚂 ECONOMY   | Prezzo: €%6.2f | Durata: %s | Arrivo: %s%n",
                viaggioEconomy.getPrezzo(),
                trattaEconomy.getDurataFormattata(),
                viaggioEconomy.getOrarioArrivo());

        System.out.printf("🚄 STANDARD  | Prezzo: €%6.2f | Durata: %s | Arrivo: %s%n",
                viaggioStandard.getPrezzo(),
                trattaStandard.getDurataFormattata(),
                viaggioStandard.getOrarioArrivo());

        System.out.printf("🚅 BUSINESS  | Prezzo: €%6.2f | Durata: %s | Arrivo: %s%n",
                viaggioBusiness.getPrezzo(),
                trattaBusiness.getDurataFormattata(),
                viaggioBusiness.getOrarioArrivo());

        System.out.println("✅ STRATEGY INTEGRATION VERIFIED: Prezzi e durate calcolati correttamente");
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR001", "IC002", "REG003", "TEST123"})
    @DisplayName("Test creazione treni con codici diversi")
    void shouldCreateTreniWithDifferentCodes(String codice) {
        System.out.println("Test codice treno: " + codice);

        Treno treno = new BuilderStandard()
                .codice(codice)
                .build();

        assertNotNull(treno);
        assertEquals(codice, treno.getCodice());
        assertEquals(TipoTreno.STANDARD, treno.getTipoTreno());

        System.out.println("✅ Treno creato: " + treno);
    }

    @RepeatedTest(value = 3, name = "Test ripetuto {currentRepetition}/{totalRepetitions}")
    @DisplayName("Test calcoli random nelle tratte")
    void shouldHandleRandomCalculations() {
        System.out.println("Test calcoli randomici tratta");

        Tratta tratta1 = TrattaFactory.creaTratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.BUSINESS);
        Tratta tratta2 = TrattaFactory.creaTratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.BUSINESS);

        // Le tratte potrebbero avere valori leggermente diversi per la componente random
        assertTrue(tratta1.getDistanzaKm() > 0);
        assertTrue(tratta2.getDistanzaKm() > 0);
        assertTrue(tratta1.getPrezzo() > 0);
        assertTrue(tratta2.getPrezzo() > 0);

        System.out.println("📏 Tratta 1: " + tratta1.getDistanzaKm() + "km, €" + tratta1.getPrezzo());
        System.out.println("📏 Tratta 2: " + tratta2.getDistanzaKm() + "km, €" + tratta2.getPrezzo());
    }

    @Test
    @DisplayName("Test ID univoco dei viaggi")
    void shouldGenerateUniqueViaggioIds() {
        System.out.println("Test: ID univoci viaggi");

        Viaggio viaggio1 = ViaggioFactory.creaViaggio(trenoBusiness, trattaRomaMilano,
                LocalDate.of(2024, 6, 15), LocalTime.of(10, 0));
        Viaggio viaggio2 = ViaggioFactory.creaViaggio(trenoBusiness, trattaRomaMilano,
                LocalDate.of(2024, 6, 15), LocalTime.of(14, 0));

        assertNotEquals(viaggio1.getId(), viaggio2.getId(),
                "Viaggi diversi devono avere ID diversi");

        System.out.println("🆔 ID Viaggio 1: " + viaggio1.getId());
        System.out.println("🆔 ID Viaggio 2: " + viaggio2.getId());
    }

    @Test
    @DisplayName("Test filtri ViaggioFactory")
    void shouldFilterViaggi() {
        System.out.println("Test: Filtri viaggi");

        // Crea lista viaggi di test
        java.util.List<Viaggio> viaggi = new java.util.ArrayList<>();
        viaggi.add(ViaggioFactory.creaViaggio(trenoEconomy,
                TrattaFactory.creaTratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.ECONOMY),
                LocalDate.now().plusDays(1), LocalTime.of(8, 0)));
        viaggi.add(ViaggioFactory.creaViaggio(trenoBusiness, trattaRomaMilano,
                LocalDate.now().plusDays(1), LocalTime.of(14, 0)));

        // Test filtro per orario
        var viaggiFiltrati = ViaggioFactory.filtraViaggi(
                viaggi, LocalTime.of(12, 0), LocalTime.of(18, 0), null, false);

        assertEquals(1, viaggiFiltrati.size(), "Dovrebbe filtrare solo il viaggio delle 14:00");
        assertEquals(LocalTime.of(14, 0), viaggiFiltrati.get(0).getOrarioPartenza());

        System.out.println("✅ Filtri applicati correttamente");
        System.out.println("📊 Viaggi filtrati: " + viaggiFiltrati.size());
    }

    @Test
    @Disabled("Test da completare quando avremo il database")
    @DisplayName("Test persistenza database - TODO")
    void shouldPersistToDatabase() {
        // Questo test sarà implementato quando avremo il database
        System.out.println("Test database in attesa di implementazione");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Pulizia dopo test completata\n");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== FINE TEST SUITE TRENO-VIAGGIO ===");
    }
}