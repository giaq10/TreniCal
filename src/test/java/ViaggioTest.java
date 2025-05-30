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
    private LocalTime orarioPartenza;

    @BeforeEach
    void setUp() {
        director = new TrenoDirector();
        trenoEconomy = director.costruisciTrenoEconomy("EC001");
        trenoStandard = director.costruisciTrenoStandard("ST001");
        trenoBusiness = director.costruisciTrenoBusiness("BS001");
        tratta = new Tratta(Stazione.REGGIO_CALABRIA, Stazione.BOLOGNA);
        dataViaggio = LocalDate.of(2025, 6, 15);
        orarioPartenza = LocalTime.of(9, 30);
    }

    // ===== TEST CREAZIONE VIAGGIO CON I 3 TIPI DI TRENO =====

    @Test
    @DisplayName("Creazione Viaggio con Treno Economy")
    void testCreazioneViaggioEconomy() {
        // Act
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);

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

        // Assert
        assertNotNull(viaggio);
        assertNotNull(viaggio.getId());
        assertEquals(trenoEconomy, viaggio.getTreno());
        assertEquals(TipoTreno.ECONOMY, viaggio.getTreno().getTipoTreno());
        assertEquals(tratta, viaggio.getTratta());
        assertEquals(dataViaggio, viaggio.getDataViaggio());
        assertEquals(orarioPartenza, viaggio.getOrarioPartenza());
        assertEquals(450, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
        assertEquals(StatoViaggio.PROGRAMMATO, viaggio.getStato());
    }

    @Test
    @DisplayName("Creazione Viaggio con Treno Standard")
    void testCreazioneViaggioStandard() {
        // Act
        Viaggio viaggio = new Viaggio(trenoStandard, tratta, dataViaggio, orarioPartenza);

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

        // Assert
        assertNotNull(viaggio);
        assertEquals(trenoStandard, viaggio.getTreno());
        assertEquals(TipoTreno.STANDARD, viaggio.getTreno().getTipoTreno());
        assertEquals(350, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
    }

    @Test
    @DisplayName("Creazione Viaggio con Treno Business")
    void testCreazioneViaggioBusiness() {
        // Act
        Viaggio viaggio = new Viaggio(trenoBusiness, tratta, dataViaggio, orarioPartenza);

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

        // Assert
        assertNotNull(viaggio);
        assertEquals(trenoBusiness, viaggio.getTreno());
        assertEquals(TipoTreno.BUSINESS, viaggio.getTreno().getTipoTreno());
        assertEquals(250, viaggio.getPostiDisponibili());
        assertTrue(viaggio.getPrezzo() > 0);
        assertTrue(viaggio.getDurataMinuti() > 0);
    }

    // ===== TEST STRATEGY PATTERN =====

    @Test
    @DisplayName("Strategy Pattern: Business più costoso di Standard, Standard più costoso di Economy")
    void testStrategyPrezziCorretti() {
        // Arrange
        Viaggio viaggioEconomy = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
        Viaggio viaggioStandard = new Viaggio(trenoStandard, tratta, dataViaggio, orarioPartenza);
        Viaggio viaggioBusiness = new Viaggio(trenoBusiness, tratta, dataViaggio, orarioPartenza);

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
        Viaggio viaggioEconomy = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
        Viaggio viaggioStandard = new Viaggio(trenoStandard, tratta, dataViaggio, orarioPartenza);
        Viaggio viaggioBusiness = new Viaggio(trenoBusiness, tratta, dataViaggio, orarioPartenza);

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
        System.out.println(economyStrategy);
        assertTrue(economyStrategy instanceof CalcoloViaggioEconomy);

        CalcoloViaggioStrategy standardStrategy = StrategyFactory.getStrategy(TipoTreno.STANDARD);
        assertTrue(standardStrategy instanceof CalcoloViaggioStandard);

        CalcoloViaggioStrategy businessStrategy = StrategyFactory.getStrategy(TipoTreno.BUSINESS);
        assertTrue(businessStrategy instanceof CalcoloViaggioBusiness);
    }

    // ===== TEST GESTIONE POSTI =====

    @Test
    @DisplayName("Prenotazione e liberazione posti")
    void testGestionePosti() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
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
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);

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
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
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
        // Assert
        log.error("e: ", assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(null, tratta, dataViaggio, orarioPartenza)));
        log.error("e: ",assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(trenoEconomy, null, dataViaggio, orarioPartenza)));
        log.error("e: ",assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(trenoEconomy, tratta, null, orarioPartenza)));
        log.error("e: ",assertThrows(IllegalArgumentException.class,
                () -> new Viaggio(trenoEconomy, tratta, dataViaggio, null)));
    }

    // ===== TEST METODI UTILITY =====

    @Test
    @DisplayName("Test metodi formattazione date e durate")
    void testMetodiFormattazione() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
        String vi = viaggio.toString();
        System.out.println(vi);

        // Act & Assert
        String durataFormattata = viaggio.getDurataFormattata();
        System.out.println(durataFormattata);
        assertNotNull(durataFormattata);
        assertTrue(durataFormattata.contains("h"));
        assertTrue(durataFormattata.contains("m"));

        String dataPartenza = viaggio.getDataOraPartenzaFormattata();
        System.out.println(dataPartenza);
        assertTrue(dataPartenza.contains("15/06/2025"));
        assertTrue(dataPartenza.contains("09:30"));

        String infoBinari = viaggio.getInfoBinari();
        System.out.println(infoBinari);
        assertTrue(infoBinari.startsWith("Binario"));
    }

    @Test
    @DisplayName("Test LocalDateTime getters")
    void testLocalDateTimeGetters() {
        // Arrange
        Viaggio viaggio = new Viaggio(trenoEconomy, tratta, dataViaggio, orarioPartenza);
        System.out.println(viaggio);

        // Act & Assert
        LocalDateTime dataOraPartenza = viaggio.getDataOraPartenza();
        System.out.println(dataOraPartenza);
        assertEquals(dataViaggio, dataOraPartenza.toLocalDate());
        assertEquals(orarioPartenza, dataOraPartenza.toLocalTime());

        LocalDateTime dataOraArrivo = viaggio.getDataOraArrivo();
        System.out.println(dataOraArrivo);
        assertEquals(dataViaggio, dataOraArrivo.toLocalDate());
        assertTrue(dataOraArrivo.toLocalTime().isAfter(orarioPartenza));
    }
}