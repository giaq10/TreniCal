
import it.trenical.common.model.tratte.*;
import it.trenical.common.model.treni.TipoTreno;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("Test Dettagliato Sistema Tratte")
class TrattaTest {

    @BeforeEach
    void setUp() {
        System.out.println("\n--- Inizio Test ---");
    }

    @AfterEach
    void tearDown() {
        System.out.println("--- Fine Test ---\n");
    }

    @Test
    @DisplayName("Test Creazione Singola Tratta")
    void testCreazioneSingolaTratta() {
        System.out.println("=== TEST CREAZIONE SINGOLA TRATTA ===");

        // Arrange
        Stazione partenza = Stazione.ROMA;
        Stazione arrivo = Stazione.MILANO;
        TipoTreno tipo = TipoTreno.STANDARD;

        System.out.println("Input:");
        System.out.println("- Partenza: " + partenza.getNome() + " (valore: " + partenza.getValore() + ")");
        System.out.println("- Arrivo: " + arrivo.getNome() + " (valore: " + arrivo.getValore() + ")");
        System.out.println("- Tipo Treno: " + tipo);

        // Act
        Tratta tratta = new Tratta(partenza, arrivo, tipo);

        // Assert e Output
        System.out.println("\nRisultati:");
        System.out.println("- Stazione Partenza: " + tratta.getStazionePartenza().getNome());
        System.out.println("- Stazione Arrivo: " + tratta.getStazioneArrivo().getNome());
        System.out.println("- Tipo Treno: " + tratta.getTipoTreno());
        System.out.println("- Distanza calcolata: " + tratta.getDistanzaKm() + " km");
        System.out.println("- Durata: " + tratta.getDurataMinuti() + " minuti");
        System.out.println("- Durata formattata: " + tratta.getDurataFormattata());
        System.out.println("- Prezzo: €" + tratta.getPrezzo());
        System.out.println("- ToString completo: " + tratta.toString());

        assertEquals(partenza, tratta.getStazionePartenza());
        assertEquals(arrivo, tratta.getStazioneArrivo());
        assertEquals(tipo, tratta.getTipoTreno());
        assertTrue(tratta.getDistanzaKm() > 0);
        assertTrue(tratta.getDurataMinuti() > 0);
        assertTrue(tratta.getPrezzo() > 0);
    }

    @Test
    @DisplayName("Test Tutti i Tipi di Treno sulla Stessa Tratta")
    void testTuttiTipiTreno() {
        System.out.println("=== TEST CONFRONTO TUTTI I TIPI DI TRENO ===");

        Stazione partenza = Stazione.REGGIO_CALABRIA;
        Stazione arrivo = Stazione.VENEZIA;

        System.out.println("Tratta fissa: " + partenza.getNome() + " → " + arrivo.getNome());
        System.out.println("Differenza valori stazioni: " + Math.abs(partenza.getValore() - arrivo.getValore()));
        System.out.println();

        Tratta trattaEconomy = new Tratta(partenza, arrivo, TipoTreno.ECONOMY);
        Tratta trattaStandard = new Tratta(partenza, arrivo, TipoTreno.STANDARD);
        Tratta trattaBusiness = new Tratta(partenza, arrivo, TipoTreno.BUSINESS);

        System.out.println("ECONOMY:");
        System.out.println("  - Distanza: " + trattaEconomy.getDistanzaKm() + " km");
        System.out.println("  - Durata: " + trattaEconomy.getDurataMinuti() + " min (" + trattaEconomy.getDurataFormattata() + ")");
        System.out.println("  - Prezzo: €" + trattaEconomy.getPrezzo());
        System.out.println("  - Velocità media: " + String.format("%.1f", (double)trattaEconomy.getDistanzaKm() * 60 / trattaEconomy.getDurataMinuti()) + " km/h");

        System.out.println("STANDARD:");
        System.out.println("  - Distanza: " + trattaStandard.getDistanzaKm() + " km");
        System.out.println("  - Durata: " + trattaStandard.getDurataMinuti() + " min (" + trattaStandard.getDurataFormattata() + ")");
        System.out.println("  - Prezzo: €" + trattaStandard.getPrezzo());
        System.out.println("  - Velocità media: " + String.format("%.1f", (double)trattaStandard.getDistanzaKm() * 60 / trattaStandard.getDurataMinuti()) + " km/h");

        System.out.println("BUSINESS:");
        System.out.println("  - Distanza: " + trattaBusiness.getDistanzaKm() + " km");
        System.out.println("  - Durata: " + trattaBusiness.getDurataMinuti() + " min (" + trattaBusiness.getDurataFormattata() + ")");
        System.out.println("  - Prezzo: €" + trattaBusiness.getPrezzo());
        System.out.println("  - Velocità media: " + String.format("%.1f", (double)trattaBusiness.getDistanzaKm() * 60 / trattaBusiness.getDurataMinuti()) + " km/h");

        // Verifica che Business sia più veloce ed Economy più economico
        assertTrue(trattaBusiness.getDurataMinuti() < trattaStandard.getDurataMinuti());
        assertTrue(trattaStandard.getDurataMinuti() < trattaEconomy.getDurataMinuti());
        assertTrue(trattaEconomy.getPrezzo() < trattaStandard.getPrezzo());
        assertTrue(trattaStandard.getPrezzo() < trattaBusiness.getPrezzo());
    }

    @Test
    @DisplayName("Test Distanze Diverse")
    void testDistanzeDiverse() {
        System.out.println("=== TEST DISTANZE DIVERSE ===");

        // Tratta corta
        Tratta trattaCorta = new Tratta(Stazione.ROMA, Stazione.FIRENZE, TipoTreno.STANDARD);
        System.out.println("TRATTA CORTA - " + trattaCorta.getStazionePartenza().getNome() + " → " + trattaCorta.getStazioneArrivo().getNome());
        System.out.println("  Differenza valori: " + Math.abs(trattaCorta.getStazionePartenza().getValore() - trattaCorta.getStazioneArrivo().getValore()));
        System.out.println("  Distanza: " + trattaCorta.getDistanzaKm() + " km");
        System.out.println("  Durata: " + trattaCorta.getDurataFormattata());
        System.out.println("  Prezzo: €" + trattaCorta.getPrezzo());

        // Tratta lunga
        Tratta trattaLunga = new Tratta(Stazione.REGGIO_CALABRIA, Stazione.MILANO, TipoTreno.STANDARD);
        System.out.println("TRATTA LUNGA - " + trattaLunga.getStazionePartenza().getNome() + " → " + trattaLunga.getStazioneArrivo().getNome());
        System.out.println("  Differenza valori: " + Math.abs(trattaLunga.getStazionePartenza().getValore() - trattaLunga.getStazioneArrivo().getValore()));
        System.out.println("  Distanza: " + trattaLunga.getDistanzaKm() + " km");
        System.out.println("  Durata: " + trattaLunga.getDurataFormattata());
        System.out.println("  Prezzo: €" + trattaLunga.getPrezzo());

        assertTrue(trattaLunga.getDistanzaKm() > trattaCorta.getDistanzaKm());
        assertTrue(trattaLunga.getDurataMinuti() > trattaCorta.getDurataMinuti());
        assertTrue(trattaLunga.getPrezzo() > trattaCorta.getPrezzo());
    }

    @Test
    @DisplayName("Test TrattaFactory")
    void testTrattaFactory() {
        System.out.println("=== TEST TRATTA FACTORY ===");

        // Test creazione con stringhe
        System.out.println("Test creazione con stringhe:");
        System.out.println("Input: partenza='Napoli', arrivo='Torino', tipo='business'");

        Tratta tratta = TrattaFactory.creaTratta("Napoli", "Torino", "business");

        System.out.println("Output:");
        System.out.println("  - Partenza convertita: " + tratta.getStazionePartenza().getNome());
        System.out.println("  - Arrivo convertito: " + tratta.getStazioneArrivo().getNome());
        System.out.println("  - Tipo convertito: " + tratta.getTipoTreno());
        System.out.println("  - Risultato completo: " + tratta);

        assertEquals(Stazione.NAPOLI, tratta.getStazionePartenza());
        assertEquals(Stazione.TORINO, tratta.getStazioneArrivo());
        assertEquals(TipoTreno.BUSINESS, tratta.getTipoTreno());

        // Test creazione di tutte le tratte
        System.out.println("\nTest creazione tutte le tratte per una coppia di stazioni:");
        List<Tratta> tutteLeTratte = TrattaFactory.creaTutteLeTratte(Stazione.ROMA, Stazione.VENEZIA);

        System.out.println("Stazioni: ROMA → VENEZIA");
        System.out.println("Numero tratte create: " + tutteLeTratte.size());

        for (Tratta t : tutteLeTratte) {
            System.out.println("  - " + t.getTipoTreno() + ": " + t.getDurataFormattata() + ", €" + t.getPrezzo());
        }

        assertEquals(TipoTreno.values().length, tutteLeTratte.size());
    }

    @Test
    @DisplayName("Test Validazione Tratte")
    void testValidazioneTratte() {
        System.out.println("=== TEST VALIDAZIONE TRATTE ===");

        // Test tratta valida
        boolean valida = TrattaFactory.isTrattaValida(Stazione.ROMA, Stazione.MILANO);
        System.out.println("ROMA → MILANO: " + (valida ? "VALIDA" : "NON VALIDA"));
        assertTrue(valida);

        // Test tratta con stazioni uguali
        boolean invalida = TrattaFactory.isTrattaValida(Stazione.ROMA, Stazione.ROMA);
        System.out.println("ROMA → ROMA: " + (invalida ? "VALIDA" : "NON VALIDA"));
        assertFalse(invalida);

        // Test con null
        boolean nullTest = TrattaFactory.isTrattaValida(null, Stazione.MILANO);
        System.out.println("null → MILANO: " + (nullTest ? "VALIDA" : "NON VALIDA"));
        assertFalse(nullTest);
    }

    @Test
    @DisplayName("Test Equals e HashCode")
    void testEqualsEHashCode() {
        System.out.println("=== TEST EQUALS E HASHCODE ===");

        Tratta tratta1 = new Tratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.STANDARD);
        Tratta tratta2 = new Tratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.STANDARD);
        Tratta tratta3 = new Tratta(Stazione.ROMA, Stazione.MILANO, TipoTreno.BUSINESS);

        System.out.println("Tratta1: " + tratta1);
        System.out.println("Tratta2: " + tratta2);
        System.out.println("Tratta3: " + tratta3);

        System.out.println("HashCode Tratta1: " + tratta1.hashCode());
        System.out.println("HashCode Tratta2: " + tratta2.hashCode());
        System.out.println("HashCode Tratta3: " + tratta3.hashCode());

        System.out.println("Tratta1.equals(Tratta2): " + tratta1.equals(tratta2));
        System.out.println("Tratta1.equals(Tratta3): " + tratta1.equals(tratta3));

        assertTrue(tratta1.equals(tratta2));
        assertFalse(tratta1.equals(tratta3));
        assertEquals(tratta1.hashCode(), tratta2.hashCode());
    }

    @Test
    @DisplayName("Test Eccezioni")
    void testEccezioni() {
        System.out.println("=== TEST GESTIONE ECCEZIONI ===");

        // Test stazioni uguali
        System.out.println("Test creazione tratta con stazioni uguali...");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Tratta(Stazione.ROMA, Stazione.ROMA, TipoTreno.STANDARD);
        });
        System.out.println("Eccezione catturata: " + exception.getMessage());

        // Test stazione non trovata
        System.out.println("Test ricerca stazione inesistente...");
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            Stazione.fromNome("StazioneInesistente");
        });
        System.out.println("Eccezione catturata: " + exception2.getMessage());
    }

    @Test
    @DisplayName("Test Metodo fromNome di Stazione")
    void testStazioneFromNome() {
        System.out.println("=== TEST METODO fromNome STAZIONE ===");

        // Test case sensitive
        String[] nomiTest = {"Roma", "ROMA", "roma", "Milano", "Reggio Calabria"};

        for (String nome : nomiTest) {
            try {
                Stazione stazione = Stazione.fromNome(nome);
                System.out.println("Input: '" + nome + "' → Output: " + stazione.getNome() + " (valore: " + stazione.getValore() + ")");
            } catch (Exception e) {
                System.out.println("Input: '" + nome + "' → Errore: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Test Calcolo Dettagliato Strategy")
    void testCalcoloStrategy() {
        System.out.println("=== TEST DETTAGLIATO STRATEGY PATTERN ===");

        // Creiamo più tratte con la stessa distanza per vedere le variazioni
        Stazione partenza = Stazione.ROMA;
        Stazione arrivo = Stazione.FIRENZE;

        System.out.println("Analisi multiple creazioni stessa tratta: " + partenza.getNome() + " → " + arrivo.getNome());
        System.out.println("(Per verificare le variazioni casuali nei calcoli)");

        for (int i = 1; i <= 3; i++) {
            System.out.println("\n--- Iterazione " + i + " ---");

            for (TipoTreno tipo : TipoTreno.values()) {
                Tratta tratta = new Tratta(partenza, arrivo, tipo);
                double velocitaMedia = (double)tratta.getDistanzaKm() * 60 / tratta.getDurataMinuti();
                double prezzoPerKm = tratta.getPrezzo() / tratta.getDistanzaKm();

                System.out.printf("%s: %d km, %d min, €%.2f (%.1f km/h, €%.3f/km)%n",
                        tipo, tratta.getDistanzaKm(), tratta.getDurataMinuti(),
                        tratta.getPrezzo(), velocitaMedia, prezzoPerKm);
            }
        }
    }
}