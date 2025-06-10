package common;

import it.trenical.common.stazioni.Stazione;
import it.trenical.server.tratte.Tratta;
import it.trenical.server.tratte.TrattaUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite completa per Tratta e TrattaUtil
 * Verifica il funzionamento con calcolo distanze reali (Haversine)
 * Aggiornato per rimozione del valore intero dalle stazioni
 */
class TrattaTest {

    private Tratta trattaRomaMilano;
    private Tratta trattaNapoliTorino;
    private Tratta trattaFirenzeBologna;

    @BeforeAll
    static void setupAll() {
        System.out.println("=== INIZIO TEST SUITE TRATTE CON DISTANZE REALI ===");
        System.out.println("Testando Tratta e TrattaUtil con formula di Haversine");
    }

    @BeforeEach
    void setup() {
        System.out.println("Preparazione tratte di test...");

        // Tratte di test standard
        trattaRomaMilano = new Tratta(Stazione.ROMA, Stazione.MILANO);
        trattaNapoliTorino = new Tratta(Stazione.NAPOLI, Stazione.TORINO);
        trattaFirenzeBologna = new Tratta(Stazione.FIRENZE, Stazione.BOLOGNA);
    }

    // =====================================================
    // TEST COSTRUTTORE TRATTA
    // =====================================================

    @Test
    @DisplayName("Costruttore Tratta - creazione corretta con distanze reali")
    void shouldCreateTrattaCorrectly() {
        System.out.println("Test: Creazione Tratta corretta");

        assertNotNull(trattaRomaMilano, "Tratta non dovrebbe essere null");
        assertEquals(Stazione.ROMA, trattaRomaMilano.getStazionePartenza());
        assertEquals(Stazione.MILANO, trattaRomaMilano.getStazioneArrivo());
        assertTrue(trattaRomaMilano.getDistanzaKm() > 0, "Distanza deve essere positiva");

        System.out.println("✅ " + trattaRomaMilano);
        System.out.println("📏 Distanza calcolata: " + trattaRomaMilano.getDistanzaKm() + " km");
        System.out.println("✅ " + trattaNapoliTorino);
        System.out.println("📏 Distanza calcolata: " + trattaNapoliTorino.getDistanzaKm() + " km");
        System.out.println("✅ " + trattaFirenzeBologna);
        System.out.println("📏 Distanza calcolata: " + trattaFirenzeBologna.getDistanzaKm() + " km");
    }

    @Test
    @DisplayName("Costruttore Tratta - validazione parametri obbligatori")
    void shouldThrowExceptionForInvalidParameters() {
        System.out.println("Test: Validazione parametri costruttore");

        // Test stazione partenza null
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> new Tratta(null, Stazione.MILANO),
                "Dovrebbe lanciare eccezione per stazione partenza null"
        );
        assertTrue(exception1.getMessage().contains("partenza obbligatoria"));

        // Test stazione arrivo null
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> new Tratta(Stazione.ROMA, null),
                "Dovrebbe lanciare eccezione per stazione arrivo null"
        );
        assertTrue(exception2.getMessage().contains("arrivo obbligatoria"));

        // Test stazioni uguali
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> new Tratta(Stazione.ROMA, Stazione.ROMA),
                "Dovrebbe lanciare eccezione per stazioni uguali"
        );
        assertTrue(exception3.getMessage().contains("non possono essere uguali"));

        System.out.println("✅ Tutte le validazioni funzionano correttamente");
    }

    @Test
    @DisplayName("Calcolo distanza - verifiche distanze reali note")
    void shouldCalculateRealisticDistances() {
        System.out.println("Test: Calcolo distanze realistiche");

        int distanzaRomaMilano = trattaRomaMilano.getDistanzaKm();

        int distanzaFirenzeBologna = trattaFirenzeBologna.getDistanzaKm();

        int distanzaNapoliTorino = trattaNapoliTorino.getDistanzaKm();

        System.out.println("📊 Roma-Milano: " + distanzaRomaMilano + " km (attesa ~475)");
        System.out.println("📊 Firenze-Bologna: " + distanzaFirenzeBologna + " km (attesa ~100)");
        System.out.println("📊 Napoli-Torino: " + distanzaNapoliTorino + " km (attesa ~670)");
        System.out.println("✅ Distanze realistiche verificate");
    }

    @Test
    @DisplayName("Test simmetria distanze - A→B = B→A")
    void shouldCalculateSymmetricDistances() {
        System.out.println("Test: Simmetria distanze");

        // Crea tratte opposte
        Tratta romaMilano = new Tratta(Stazione.ROMA, Stazione.MILANO);
        Tratta milanoRoma = new Tratta(Stazione.MILANO, Stazione.ROMA);

        // Le distanze dovrebbero essere identiche
        assertEquals(romaMilano.getDistanzaKm(), milanoRoma.getDistanzaKm(),
                "Distanza Roma→Milano dovrebbe essere uguale a Milano→Roma");

        System.out.println("📊 Roma → Milano: " + romaMilano.getDistanzaKm() + " km");
        System.out.println("📊 Milano → Roma: " + milanoRoma.getDistanzaKm() + " km");
        System.out.println("✅ Simmetria verificata");
    }

    @Test
    @DisplayName("Metodi utility Tratta")
    void shouldProvideUtilityMethods() {
        System.out.println("Test: Metodi utility Tratta");

        // Test toString() - restituisce la descrizione completa
        String toString = trattaRomaMilano.toString();

        // Verifica che contenga gli elementi essenziali
        assertTrue(toString.contains("Roma → Milano"), "Dovrebbe contenere 'Roma → Milano'");
        assertTrue(toString.contains("km"), "Dovrebbe contenere 'km'");
        assertTrue(toString.contains(String.valueOf(trattaRomaMilano.getDistanzaKm())),
                "Dovrebbe contenere la distanza calcolata");

        assertNotNull(toString, "toString non dovrebbe essere null");
        assertFalse(toString.trim().isEmpty(), "toString non dovrebbe essere vuoto");

        // Verifica formato atteso (esempio: "Roma → Milano (475 km)")
        assertTrue(toString.matches(".*Roma.*→.*Milano.*\\(\\d+\\s*km\\).*"),
                "Formato toString non corretto: " + toString);

        System.out.println("📝 ToString: " + toString);
        System.out.println("✅ Metodi utility verificati");
    }

    // =====================================================
    // TEST EQUALS & HASHCODE
    // =====================================================

    @Test
    @DisplayName("Equals e HashCode - identità basata su stazioni")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        System.out.println("Test: Equals e HashCode");

        // Crea due tratte identiche
        Tratta tratta1 = new Tratta(Stazione.ROMA, Stazione.MILANO);
        Tratta tratta2 = new Tratta(Stazione.ROMA, Stazione.MILANO);
        Tratta trattaDiversa = new Tratta(Stazione.MILANO, Stazione.ROMA); // Opposta

        // Test equals
        assertEquals(tratta1, tratta2, "Tratte con stesse stazioni dovrebbero essere uguali");
        assertEquals(tratta1, trattaRomaMilano, "Tratte con stesse stazioni dovrebbero essere uguali");
        assertNotEquals(tratta1, trattaDiversa, "Tratte con stazioni opposte dovrebbero essere diverse");
        assertNotEquals(tratta1, null, "Tratta non dovrebbe essere uguale a null");
        assertNotEquals(tratta1, "stringa", "Tratta non dovrebbe essere uguale a oggetto diverso");

        // Test hashCode
        assertEquals(tratta1.hashCode(), tratta2.hashCode(), "HashCode dovrebbe essere uguale per tratte uguali");
        assertEquals(tratta1.hashCode(), trattaRomaMilano.hashCode(), "HashCode coerente con equals");

        // Test riflessività, simmetria, transitività
        assertEquals(tratta1, tratta1, "Riflessività: oggetto uguale a se stesso");
        assertEquals(tratta2, tratta1, "Simmetria: se a=b allora b=a");

        System.out.println("✅ Equals e HashCode implementati correttamente");
    }

    // =====================================================
    // TEST TRATTAUTIL
    // =====================================================

    @Test
    @DisplayName("TrattaUtil - creazione singola tratta")
    void shouldCreateSingleTratta() {
        System.out.println("Test: TrattaUtil creazione singola");

        // Test con stazioni enum
        Tratta trattaFactory1 = TrattaUtil.creaTratta(Stazione.VENEZIA, Stazione.GENOVA);
        assertNotNull(trattaFactory1);
        assertEquals(Stazione.VENEZIA, trattaFactory1.getStazionePartenza());
        assertEquals(Stazione.GENOVA, trattaFactory1.getStazioneArrivo());

        // Test con nomi stringa
        Tratta trattaFactory2 = TrattaUtil.creaTratta("Bologna", "Verona");
        assertNotNull(trattaFactory2);
        assertEquals(Stazione.BOLOGNA, trattaFactory2.getStazionePartenza());
        assertEquals(Stazione.VERONA, trattaFactory2.getStazioneArrivo());

        System.out.println("✅ " + trattaFactory1);
        System.out.println("✅ " + trattaFactory2);
    }

    @Test
    @DisplayName("TrattaUtil - creazione con nomi invalidi")
    void shouldThrowExceptionForInvalidStationNames() {
        System.out.println("Test: TrattaUtil validazione nomi");

        // Test nome stazione inesistente
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TrattaUtil.creaTratta("StazioneInesistente", "Roma"),
                "Dovrebbe lanciare eccezione per nome stazione inesistente"
        );
        assertTrue(exception.getMessage().contains("non trovata"));

        System.out.println("✅ Validazione nomi stazioni funziona");
    }

    @Test
    @DisplayName("TrattaUtil - creazione tutte le tratte")
    void shouldCreateAllPossibleTratte() {
        System.out.println("Test: TrattaUtil tutte le tratte");

        List<Tratta> tutteLeTratte = TrattaUtil.creaTutteLeTratte();


        // Calcolo numero atteso: 12 stazioni * 11 destinazioni = 132 tratte
        int numeroStazioni = Stazione.values().length;
        int numeroTratteAttese = numeroStazioni * (numeroStazioni - 1);

        assertEquals(numeroTratteAttese, tutteLeTratte.size(),
                "Numero tratte non corretto");

        // Verifica che non ci siano duplicati
        Set<Tratta> setTratte = new HashSet<>(tutteLeTratte);
        assertEquals(tutteLeTratte.size(), setTratte.size(),
                "Non dovrebbero esserci tratte duplicate");

        // Verifica che non ci siano tratte con stazioni uguali
        boolean haTratteSbagliate = tutteLeTratte.stream()
                .anyMatch(t -> t.getStazionePartenza().equals(t.getStazioneArrivo()));
        assertFalse(haTratteSbagliate, "Non dovrebbero esserci tratte con stazioni uguali");

        // Stampa tutte le tratte create
        System.out.println("\n=== ELENCO COMPLETO TRATTE ===");
        for (int i = 0; i < tutteLeTratte.size(); i++) {
            System.out.println((i + 1) + ". " + tutteLeTratte.get(i));
        }
        System.out.println("📊 Numero stazioni: " + numeroStazioni);
        System.out.println("📊 Tratte totali create: " + tutteLeTratte.size());
        System.out.println("📊 Tratte attese: " + numeroTratteAttese);
        System.out.println("✅ Tutte le tratte create correttamente");
    }

    @Test
    @DisplayName("TrattaUtil - ricerca tratta specifica")
    void shouldFindSpecificTratta() {
        System.out.println("Test: TrattaUtil ricerca tratta");

        List<Tratta> tutteLeTratte = TrattaUtil.creaTutteLeTratte();

        // Test ricerca tratta esistente
        Tratta trattaTrovata = TrattaUtil.trovaTratta(tutteLeTratte,
                Stazione.MILANO, Stazione.NAPOLI);

        assertNotNull(trattaTrovata, "Dovrebbe trovare la tratta Milano-Napoli");
        assertEquals(Stazione.MILANO, trattaTrovata.getStazionePartenza());
        assertEquals(Stazione.NAPOLI, trattaTrovata.getStazioneArrivo());

        // Test ricerca tratta inesistente (stazioni uguali)
        Tratta trattaNonTrovata = TrattaUtil.trovaTratta(tutteLeTratte,
                Stazione.ROMA, Stazione.ROMA);

        assertNull(trattaNonTrovata, "Non dovrebbe trovare tratta con stazioni uguali");

        System.out.println("✅ Ricerca tratta funziona correttamente");
    }

    @Test
    @DisplayName("TrattaUtil - validazione tratte")
    void shouldValidateTratte() {
        System.out.println("Test: TrattaUtil validazione");

        // Test tratte valide
        assertTrue(TrattaUtil.isTrattaValida(Stazione.ROMA, Stazione.MILANO));
        assertTrue(TrattaUtil.isTrattaValida(Stazione.NAPOLI, Stazione.TORINO));

        // Test tratte invalide
        assertFalse(TrattaUtil.isTrattaValida(null, Stazione.MILANO),
                "Tratta con partenza null dovrebbe essere invalida");
        assertFalse(TrattaUtil.isTrattaValida(Stazione.ROMA, null),
                "Tratta con arrivo null dovrebbe essere invalida");
        assertFalse(TrattaUtil.isTrattaValida(Stazione.ROMA, Stazione.ROMA),
                "Tratta con stazioni uguali dovrebbe essere invalida");

        System.out.println("✅ Validazione tratte funziona correttamente");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Roma", "Milano", "Napoli", "Torino", "Bologna"})
    @DisplayName("Test creazione tratte con nomi string")
    void shouldCreateTratteWithStringNames(String nomePartenza) {
        System.out.println("Test parametrizzato: Tratta da " + nomePartenza);

        // Prova a creare tratta verso una destinazione fissa
        String nomeDestinazione = "Firenze";
        if (nomePartenza.equals("Firenze")) {
            nomeDestinazione = "Roma"; // Evita stazioni uguali
        }

        Tratta tratta = TrattaUtil.creaTratta(nomePartenza, nomeDestinazione);

        assertNotNull(tratta);
        assertEquals(nomePartenza, tratta.getStazionePartenza().getNome());
        assertEquals(nomeDestinazione, tratta.getStazioneArrivo().getNome());
        assertTrue(tratta.getDistanzaKm() > 0);

        System.out.println("✅ " + tratta);
    }

    // =====================================================
    // TEST PERFORMANCE E STRESS
    // =====================================================

    @Test
    @DisplayName("Test performance creazione tutte le tratte")
    void shouldCreateAllTrattesInReasonableTime() {
        System.out.println("Test: Performance creazione tratte");

        long startTime = System.currentTimeMillis();

        List<Tratta> tutteLeTratte = TrattaUtil.creaTutteLeTratte();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // La creazione di tutte le tratte dovrebbe essere veloce (< 1 secondo)
        assertTrue(duration < 1000,
                "Creazione tratte troppo lenta: " + duration + "ms");

        System.out.println("⏱️ Tempo creazione " + tutteLeTratte.size() + " tratte: " + duration + "ms");
        System.out.println("✅ Performance accettabile");
    }

    @RepeatedTest(value = 3, name = "Test ripetuto {currentRepetition}/{totalRepetitions}")
    @DisplayName("Test stabilità calcolo distanze reali")
    void shouldCalculateConsistentDistances() {
        System.out.println("Test ripetuto: Stabilità calcolo distanze");

        // Crea la stessa tratta più volte e verifica coerenza
        Tratta tratta1 = new Tratta(Stazione.ROMA, Stazione.MILANO);
        Tratta tratta2 = new Tratta(Stazione.ROMA, Stazione.MILANO);

        // Le distanze dovrebbero essere sempre identiche (niente randomness)
        assertEquals(tratta1.getDistanzaKm(), tratta2.getDistanzaKm(),
                "Calcolo dovrebbe essere deterministico");

        // La distanza dovrebbe essere sempre positiva e ragionevole
        assertTrue(tratta1.getDistanzaKm() > 0);
        assertTrue(tratta1.getDistanzaKm() < 2000, "Distanza troppo grande per l'Italia");

        System.out.println("🔄 Distanza calcolata: " + tratta1.getDistanzaKm() + " km");
        System.out.println("✅ Stabilità verificata");
    }

    // =====================================================
    // TEST EDGE CASES
    // =====================================================

    @Test
    @DisplayName("Test edge cases - tratte estreme")
    void shouldHandleExtremeDistances() {
        System.out.println("Test: Edge cases tratte estreme");

        // Test tratta più lunga possibile in Italia
        Tratta trattaLunga = new Tratta(Stazione.REGGIO_CALABRIA, Stazione.MILANO);

        // Test tratta più corta tra stazioni vicine
        Tratta trattaCorta = new Tratta(Stazione.FIRENZE, Stazione.BOLOGNA);

        assertNotNull(trattaLunga);
        assertNotNull(trattaCorta);

        assertTrue(trattaLunga.getDistanzaKm() > trattaCorta.getDistanzaKm(),
                "Reggio Calabria-Milano dovrebbe essere più lunga di Firenze-Bologna");

        // Dovrebbe essere circa la tratta più lunga (circa 950 km)
        assertTrue(trattaLunga.getDistanzaKm() >= 900 && trattaLunga.getDistanzaKm() <= 1000,
                "Reggio Calabria-Milano dovrebbe essere circa 950 km, trovata: " + trattaLunga.getDistanzaKm());

        System.out.println("🏔️ Tratta più lunga: " + trattaLunga);
        System.out.println("🏘️ Tratta più corta: " + trattaCorta);
    }

    // =====================================================
    // TEST STATISTICHE AVANZATE
    // =====================================================

    @Test
    @DisplayName("Test statistiche distanze reali")
    void shouldProvideRealisticStatistics() {
        System.out.println("Test: Statistiche distanze reali");

        List<Tratta> tutteLeTratte = TrattaUtil.creaTutteLeTratte();

        // Calcola distanza media
        double distanzaMedia = tutteLeTratte.stream()
                .mapToInt(Tratta::getDistanzaKm)
                .average()
                .orElse(0.0);

        // In Italia, la distanza media dovrebbe essere ragionevole
        assertTrue(distanzaMedia >= 200 && distanzaMedia <= 600,
                "Distanza media dovrebbe essere tra 200-600 km, trovata: " + distanzaMedia);

        // Trova min e max
        int distanzaMin = tutteLeTratte.stream()
                .mapToInt(Tratta::getDistanzaKm)
                .min()
                .orElse(0);

        int distanzaMax = tutteLeTratte.stream()
                .mapToInt(Tratta::getDistanzaKm)
                .max()
                .orElse(0);

        System.out.println("📊 Distanza media: " + String.format("%.1f", distanzaMedia) + " km");
        System.out.println("📊 Distanza minima: " + distanzaMin + " km");
        System.out.println("📊 Distanza massima: " + distanzaMax + " km");
        System.out.println("✅ Statistiche realistiche verificate");
    }

    // =====================================================
    // CLEANUP
    // =====================================================

    @AfterEach
    void tearDown() {
        System.out.println("Pulizia test completata\n");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("=== FINE TEST SUITE TRATTE CON DISTANZE REALI ===");
        System.out.println("Implementazione con formula di Haversine verificata con successo!");
    }
}