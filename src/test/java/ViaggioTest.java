import it.trenical.common.model.viaggi.*;
import it.trenical.common.model.viaggi.strategy.*;
import it.trenical.common.model.treni.*;
import it.trenical.common.model.treni.builder.TrenoDirector;
import it.trenical.common.model.tratte.*;
import it.trenical.common.model.stazioni.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Test per la classe Viaggio e Strategy Pattern
 */
class ViaggioTest {

    private static final Logger log = LoggerFactory.getLogger(ViaggioTest.class);
    private TrenoDirector director;
    private Treno trenoEconomy;
    private Treno trenoStandard;
    private Treno trenoBusiness;
    private Tratta tratta;
    private LocalDate dataViaggio;

    @BeforeEach
    void setUp() {
        director = new TrenoDirector();
        trenoEconomy = director.costruisciTrenoEconomy("EC001");
        trenoStandard = director.costruisciTrenoStandard("ST001");
        trenoBusiness = director.costruisciTrenoBusiness("BS001");
        tratta = new Tratta(Stazione.REGGIO_CALABRIA, Stazione.BOLOGNA);
        dataViaggio = LocalDate.of(2025, 6, 15);
    }

    // ===== TEST CREAZIONE VIAGGIO CON I 3 TIPI DI TRENO =====

    @Test
    @DisplayName("Creazione Viaggio con Treno Economy")
    void testCreazioneViaggioEconomy() {
        // Act
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST VIAGGIO ECONOMY ===");
        System.out.println("Treno: " + viaggio.getTreno());
        System.out.println("Tratta: " + viaggio.getTratta());
        System.out.println("Viaggio: " + viaggio);
        System.out.println("ID Viaggio: " + viaggio.getId());
        System.out.println("Prezzo: €" + viaggio.getPrezzo());
        System.out.println("Durata: " + viaggio.getDurataFormattata());
        System.out.println("Binario: " + viaggio.getInfoBinari());
        System.out.println("Partenza: " + viaggio.getDataOraPartenzaFormattata());
        System.out.println("Arrivo: " + viaggio.getDataOraArrivoFormattata());
        System.out.println("Orario generato: " + viaggio.getOrarioPartenza());

        // Assert
        assertNotNull(viaggio);
        assertNotNull(viaggio.getId());
        assertEquals(trenoEconomy, viaggio.getTreno());
        assertEquals(TipoTreno.ECONOMY, viaggio.getTreno().getTipoTreno());
        assertEquals(tratta, viaggio.getTratta());
        assertEquals(dataViaggio, viaggio.getDataViaggio());
        // Verifica che l'orario sia uno di quelli disponibili
        assertTrue(Viaggio.getOrariDisponibili().contains(viaggio.getOrarioPartenza()));
        assertEquals(450, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
    }

    @Test
    @DisplayName("Creazione Viaggio con Treno Standard")
    void testCreazioneViaggioStandard() {
        // Act
        Viaggio viaggio = new Viaggio(trenoStandard, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST VIAGGIO STANDARD ===");
        System.out.println("Treno: " + viaggio.getTreno());
        System.out.println("Tratta: " + viaggio.getTratta());
        System.out.println("Viaggio: " + viaggio);
        System.out.println("ID Viaggio: " + viaggio.getId());
        System.out.println("Prezzo: €" + viaggio.getPrezzo());
        System.out.println("Durata: " + viaggio.getDurataFormattata());
        System.out.println("Binario: " + viaggio.getInfoBinari());
        System.out.println("Partenza: " + viaggio.getDataOraPartenzaFormattata());
        System.out.println("Arrivo: " + viaggio.getDataOraArrivoFormattata());
        System.out.println("Orario generato: " + viaggio.getOrarioPartenza());

        // Assert
        assertNotNull(viaggio);
        assertEquals(trenoStandard, viaggio.getTreno());
        assertEquals(TipoTreno.STANDARD, viaggio.getTreno().getTipoTreno());
        assertEquals(350, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
        // Verifica orario valido
        assertTrue(Viaggio.getOrariDisponibili().contains(viaggio.getOrarioPartenza()));
    }

    @Test
    @DisplayName("Creazione Viaggio con Treno Business")
    void testCreazioneViaggioBusiness() {
        // Act
        Viaggio viaggio = new Viaggio(trenoBusiness, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST VIAGGIO BUSINESS ===");
        System.out.println("Treno: " + viaggio.getTreno());
        System.out.println("Tratta: " + viaggio.getTratta());
        System.out.println("Viaggio: " + viaggio);
        System.out.println("ID Viaggio: " + viaggio.getId());
        System.out.println("Prezzo: €" + viaggio.getPrezzo());
        System.out.println("Durata: " + viaggio.getDurataFormattata());
        System.out.println("Binario: " + viaggio.getInfoBinari());
        System.out.println("Partenza: " + viaggio.getDataOraPartenzaFormattata());
        System.out.println("Arrivo: " + viaggio.getDataOraArrivoFormattata());
        System.out.println("Orario generato: " + viaggio.getOrarioPartenza());

        // Assert
        assertNotNull(viaggio);
        assertEquals(trenoBusiness, viaggio.getTreno());
        assertEquals(TipoTreno.BUSINESS, viaggio.getTreno().getTipoTreno());
        assertEquals(250, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
        // Verifica orario valido
        assertTrue(Viaggio.getOrariDisponibili().contains(viaggio.getOrarioPartenza()));
    }

    // ===== TEST ORARI DISPONIBILI =====

    @Test
    @DisplayName("Test lista orari disponibili")
    void testOrariDisponibili() {
        // Act
        var orariDisponibili = Viaggio.getOrariDisponibili();

        // Print
        System.out.println("\n=== TEST ORARI DISPONIBILI ===");
        System.out.println("Numero orari disponibili: " + orariDisponibili.size());
        System.out.println("Orari disponibili:");
        orariDisponibili.forEach(orario -> System.out.println("  " + orario));

        // Assert
        assertEquals(12, orariDisponibili.size());
        assertTrue(orariDisponibili.contains(LocalTime.of(4, 0)));
        assertTrue(orariDisponibili.contains(LocalTime.of(22, 0)));
        assertTrue(orariDisponibili.contains(LocalTime.of(7, 30)));
        assertTrue(orariDisponibili.contains(LocalTime.of(15, 30)));
    }

    @Test
    @DisplayName("Test generazione orari casuali diversi")
    void testGenerazioneOrariCasuali() {
        // Arrange & Act - Crea molti viaggi per vedere la varietà degli orari
        System.out.println("\n=== TEST GENERAZIONE ORARI CASUALI ===");

        for (int i = 0; i < 10; i++) {
            Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);
            System.out.println("Viaggio " + (i+1) + " - Orario: " + viaggio.getOrarioPartenza() +
                    " - Binario: " + viaggio.getBinarioPartenza().getNumero());

            // Assert - Ogni viaggio deve avere un orario valido
            assertTrue(Viaggio.getOrariDisponibili().contains(viaggio.getOrarioPartenza()));
        }
    }

    // ===== TEST STRATEGY PATTERN =====

    @Test
    @DisplayName("Strategy Pattern: Business più costoso di Standard, Standard più costoso di Economy")
    void testStrategyPrezziCorretti() {
        // Arrange
        Viaggio viaggioEconomy = new Viaggio(trenoEconomy, tratta, dataViaggio);
        Viaggio viaggioStandard = new Viaggio(trenoStandard, tratta, dataViaggio);
        Viaggio viaggioBusiness = new Viaggio(trenoBusiness, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST STRATEGY PATTERN - PREZZI ===");
        System.out.println("Viaggio Economy - Prezzo: €" + viaggioEconomy.getPrezzo());
        System.out.println("Viaggio Standard - Prezzo: €" + viaggioStandard.getPrezzo());
        System.out.println("Viaggio Business - Prezzo: €" + viaggioBusiness.getPrezzo());
        System.out.println("Incremento Standard vs Economy: +" +
                String.format("%.2f", (viaggioStandard.getPrezzo() - viaggioEconomy.getPrezzo())) + "€");
        System.out.println("Incremento Business vs Standard: +" +
                String.format("%.2f", (viaggioBusiness.getPrezzo() - viaggioStandard.getPrezzo())) + "€");

        // Assert - Prezzi crescenti
        assertTrue(viaggioStandard.getPrezzo() > viaggioEconomy.getPrezzo());
        assertTrue(viaggioBusiness.getPrezzo() > viaggioStandard.getPrezzo());
    }

    @Test
    @DisplayName("Strategy Pattern: Business più veloce di Standard, Standard più veloce di Economy")
    void testStrategyDurateCorrette() {
        // Arrange
        Viaggio viaggioEconomy = new Viaggio(trenoEconomy, tratta, dataViaggio);
        Viaggio viaggioStandard = new Viaggio(trenoStandard, tratta, dataViaggio);
        Viaggio viaggioBusiness = new Viaggio(trenoBusiness, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST STRATEGY PATTERN - DURATE ===");
        System.out.println("Viaggio Economy - Durata: " + viaggioEconomy.getDurataFormattata() +
                " (" + viaggioEconomy.getDurataMinuti() + " min)");
        System.out.println("Viaggio Standard - Durata: " + viaggioStandard.getDurataFormattata() +
                " (" + viaggioStandard.getDurataMinuti() + " min)");
        System.out.println("Viaggio Business - Durata: " + viaggioBusiness.getDurataFormattata() +
                " (" + viaggioBusiness.getDurataMinuti() + " min)");
        System.out.println("Risparmio Standard vs Economy: -" +
                (viaggioEconomy.getDurataMinuti() - viaggioStandard.getDurataMinuti()) + " min");
        System.out.println("Risparmio Business vs Standard: -" +
                (viaggioStandard.getDurataMinuti() - viaggioBusiness.getDurataMinuti()) + " min");

        // Assert - Durate decrescenti (Business più veloce)
        assertTrue(viaggioStandard.getDurataMinuti() < viaggioEconomy.getDurataMinuti());
        assertTrue(viaggioBusiness.getDurataMinuti() < viaggioStandard.getDurataMinuti());
    }

    @Test
    @DisplayName("StrategyFactory restituisce strategy corretta per ogni tipo")
    void testStrategyFactory() {
        // Act & Assert
        CalcoloViaggioStrategy economyStrategy = StrategyFactory.getStrategy(TipoTreno.ECONOMY);
        System.out.println("\n=== TEST STRATEGY FACTORY ===");
        System.out.println("Economy Strategy: " + economyStrategy.getClass().getSimpleName());
        assertTrue(economyStrategy instanceof CalcoloViaggioEconomy);

        CalcoloViaggioStrategy standardStrategy = StrategyFactory.getStrategy(TipoTreno.STANDARD);
        System.out.println("Standard Strategy: " + standardStrategy.getClass().getSimpleName());
        assertTrue(standardStrategy instanceof CalcoloViaggioStandard);

        CalcoloViaggioStrategy businessStrategy = StrategyFactory.getStrategy(TipoTreno.BUSINESS);
        System.out.println("Business Strategy: " + businessStrategy.getClass().getSimpleName());
        assertTrue(businessStrategy instanceof CalcoloViaggioBusiness);
    }

    // ===== TEST GESTIONE POSTI =====

    @Test
    @DisplayName("Prenotazione e liberazione posti")
    void testGestionePosti() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);
        int postiIniziali = viaggio.getPostiDisponibili();

        // Print
        System.out.println("\n=== TEST GESTIONE POSTI ===");
        System.out.println("Viaggio: " + viaggio.getId());
        System.out.println("Posti iniziali: " + postiIniziali);

        // Act & Assert - Prenotazione
        assertTrue(viaggio.prenotaPosto());
        System.out.println("Dopo prenotazione - Posti disponibili: " + viaggio.getPostiDisponibili() +
                ", Posti occupati: " + viaggio.getPostiOccupati());
        assertEquals(postiIniziali - 1, viaggio.getPostiDisponibili());
        assertEquals(1, viaggio.getPostiOccupati());

        // Act & Assert - Liberazione
        assertTrue(viaggio.liberaPosto());
        System.out.println("Dopo liberazione - Posti disponibili: " + viaggio.getPostiDisponibili() +
                ", Posti occupati: " + viaggio.getPostiOccupati());
        assertEquals(postiIniziali, viaggio.getPostiDisponibili());
        assertEquals(0, viaggio.getPostiOccupati());
    }

    // ===== TEST GESTIONE STATO E RITARDI =====

    @Test
    @DisplayName("Gestione stato viaggio")
    void testGestioneStato() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST GESTIONE STATO ===");
        System.out.println("Viaggio: " + viaggio.getId());
        System.out.println("Stato iniziale: " + viaggio.getStato() + " - Disponibile: " + viaggio.isDisponibile());

        // Act & Assert
        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
        assertTrue(viaggio.isDisponibile());

        viaggio.aggiornaStato(StatoViaggio.CONFERMATO);
        System.out.println("Dopo conferma: " + viaggio.getStato());
        assertEquals(StatoViaggio.CONFERMATO, viaggio.getStato());

        viaggio.cancellaViaggio("Problemi tecnici");
        System.out.println("Dopo cancellazione: " + viaggio.getStato() + " - Motivo: " + viaggio.getMotivoCancellazione());
        System.out.println("Cancellato: " + viaggio.isCancellato() + " - Disponibile: " + viaggio.isDisponibile());
        assertEquals(StatoViaggio.CANCELLATO, viaggio.getStato());
        assertTrue(viaggio.isCancellato());
        assertFalse(viaggio.isDisponibile());
    }

    @Test
    @DisplayName("Gestione ritardi")
    void testGestioneRitardi() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);
        LocalTime orarioArrivoOriginale = viaggio.getOrarioArrivo();

        // Print
        System.out.println("\n=== TEST GESTIONE RITARDI ===");
        System.out.println("Viaggio: " + viaggio.getId());
        System.out.println("Orario arrivo programmato: " + orarioArrivoOriginale);
        System.out.println("Stato iniziale: " + viaggio.getStato() + " - Ritardo: " + viaggio.getRitardoMinuti() + " min");

        // Act
        viaggio.impostaRitardo(30);

        // Print
        System.out.println("Dopo impostazione ritardo:");
        System.out.println("Stato: " + viaggio.getStato() + " - Ritardo: " + viaggio.getRitardoMinuti() + " min");
        System.out.println("Orario arrivo effettivo: " + viaggio.getOrarioArrivoEffettivo());
        System.out.println("Ha ritardo: " + viaggio.haRitardo());

        // Assert
        assertEquals(30, viaggio.getRitardoMinuti());
        assertEquals(StatoViaggio.RITARDO, viaggio.getStato());
        assertTrue(viaggio.haRitardo());
        assertEquals(orarioArrivoOriginale.plusMinutes(30), viaggio.getOrarioArrivoEffettivo());
    }

    // ===== TEST VALIDAZIONE PARAMETRI =====

    @Test
    @DisplayName("Creazione viaggio con parametri null deve lanciare eccezione")
    void testCreazioneViaggioParametriNull() {
        // Print
        System.out.println("\n=== TEST VALIDAZIONE PARAMETRI ===");

        // Assert
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(null, tratta, dataViaggio));
        System.out.println("Eccezione treno null: " + e1.getMessage());

        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(trenoEconomy, null, dataViaggio));
        System.out.println("Eccezione tratta null: " + e2.getMessage());

        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(trenoEconomy, tratta, null));
        System.out.println("Eccezione data null: " + e3.getMessage());
    }

    // ===== TEST METODI UTILITY =====

    @Test
    @DisplayName("Test metodi formattazione date e durate")
    void testMetodiFormattazione() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST FORMATTAZIONE ===");
        System.out.println("Viaggio completo: " + viaggio.toString());

        // Act & Assert
        String durataFormattata = viaggio.getDurataFormattata();
        System.out.println("Durata formattata: " + durataFormattata);
        assertNotNull(durataFormattata);
        assertTrue(durataFormattata.contains("h"));
        assertTrue(durataFormattata.contains("m"));

        String dataPartenza = viaggio.getDataOraPartenzaFormattata();
        System.out.println("Data partenza formattata: " + dataPartenza);
        assertTrue(dataPartenza.contains("15/06/2025"));
        assertTrue(dataPartenza.contains("alle"));

        String infoBinari = viaggio.getInfoBinari();
        System.out.println("Info binari: " + infoBinari);
        assertTrue(infoBinari.startsWith("Binario"));
    }

    @Test
    @DisplayName("Test LocalDateTime getters")
    void testLocalDateTimeGetters() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio);

        // Print
        System.out.println("\n=== TEST LOCAL DATE TIME ===");
        System.out.println("Viaggio: " + viaggio.getId());

        // Act & Assert
        LocalDateTime dataOraPartenza = viaggio.getDataOraPartenza();
        System.out.println("Data/ora partenza: " + dataOraPartenza);
        assertEquals(dataViaggio, dataOraPartenza.toLocalDate());
        assertTrue(Viaggio.getOrariDisponibili().contains(dataOraPartenza.toLocalTime()));

        LocalDateTime dataOraArrivo = viaggio.getDataOraArrivo();
        System.out.println("Data/ora arrivo: " + dataOraArrivo);

        assertTrue(dataOraArrivo.toLocalDate().equals(dataViaggio) ||
                        dataOraArrivo.toLocalDate().equals(dataViaggio.plusDays(1)),
                "La data di arrivo deve essere lo stesso giorno o il giorno successivo");
        assertTrue(dataOraArrivo.isAfter(dataOraPartenza),
                "L'orario di arrivo deve essere successivo a quello di partenza");
        long minutiEffettivi = java.time.Duration.between(dataOraPartenza, dataOraArrivo).toMinutes();
        assertEquals(viaggio.getDurataMinuti(), minutiEffettivi,
                "La durata calcolata deve corrispondere a quella del viaggio");
    }

    @Test
    @DisplayName("Test ID univoci per viaggi diversi")
    void testIdUnivoci() {
        // Arrange & Act
        Viaggio viaggio1 = new Viaggio(trenoEconomy, tratta, dataViaggio);
        Viaggio viaggio2 = new Viaggio(trenoStandard, tratta, dataViaggio);
        Viaggio viaggio3 = new Viaggio(trenoEconomy, tratta, dataViaggio.plusDays(1));

        // Print
        System.out.println("\n=== TEST ID UNIVOCI ===");
        System.out.println("ID Viaggio 1: " + viaggio1.getId());
        System.out.println("ID Viaggio 2: " + viaggio2.getId());
        System.out.println("ID Viaggio 3: " + viaggio3.getId());

        // Assert
        assertNotEquals(viaggio1.getId(), viaggio2.getId());
        assertNotEquals(viaggio1.getId(), viaggio3.getId());
        assertNotEquals(viaggio2.getId(), viaggio3.getId());
    }
}