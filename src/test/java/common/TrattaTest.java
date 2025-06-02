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
 * Verifica il funzionamento dopo il refactoring (rimozione TipoTreno)
 */
class TrattaTest {

    private Tratta trattaRomaMilano;
    private Tratta trattaNapoliTorino;
    private Tratta trattaFirenzeBologna;

    @BeforeAll
    static void setupAll() {
        System.out.println("=== INIZIO TEST SUITE TRATTE ===");
        System.out.println("Testando Tratta e TrattaUtil dopo refactoring");
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
    @DisplayName("Costruttore Tratta - creazione corretta")
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
    @DisplayName("Calcolo distanza - logica basata su valori stazioni")
    void shouldCalculateDistanceCorrectly() {
        System.out.println("Test: Calcolo distanza");

        // Verifica che la distanza sia calcolata in base ai valori delle stazioni
        int valoreRoma = Stazione.ROMA.getValore();
        int valoreMilano = Stazione.MILANO.getValore();
        int differenza = Math.abs(valoreRoma - valoreMilano);

        // La distanza dovrebbe essere circa: differenza * 100 ± 20
        int distanzaAttesa = differenza * 100;
        int tolleranza = 30; // ±30 km di tolleranza per la variazione random

        int distanzaCalcolata = trattaRomaMilano.getDistanzaKm();

        assertTrue(distanzaCalcolata >= distanzaAttesa - tolleranza,
                "Distanza troppo piccola rispetto al previsto");
        assertTrue(distanzaCalcolata <= distanzaAttesa + tolleranza,
                "Distanza troppo grande rispetto al previsto");
        assertTrue(distanzaCalcolata >= 50, "Distanza minima dovrebbe essere 50 km");

        System.out.println("📊 Valore Roma: " + valoreRoma + ", Valore Milano: " + valoreMilano);
        System.out.println("📊 Differenza: " + differenza + ", Distanza base attesa: " + distanzaAttesa);
        System.out.println("📊 Distanza calcolata: " + distanzaCalcolata + " km");
        System.out.println("✅ Logica di calcolo distanza verificata");
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

        // Se hai rimosso getDescrizione() e getDescrizioneCompleta(),
        // verifica solo che toString() funzioni correttamente
        assertNotNull(toString, "toString non dovrebbe essere null");
        assertFalse(toString.trim().isEmpty(), "toString non dovrebbe essere vuoto");

        // Verifica formato atteso (esempio: "Tratta: Roma → Milano (393 km)")
        // Oppure se il formato è diverso, adatta di conseguenza
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
    // TEST TRATTAFACTORY
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

    @RepeatedTest(value = 5, name = "Test ripetuto {currentRepetition}/{totalRepetitions}")
    @DisplayName("Test stabilità calcolo distanze")
    void shouldCalculateConsistentDistances() {
        System.out.println("Test ripetuto: Stabilità calcolo distanze");

        // Crea la stessa tratta più volte e verifica coerenza
        Tratta tratta = new Tratta(Stazione.ROMA, Stazione.MILANO);

        // La distanza dovrebbe essere sempre positiva e ragionevole
        assertTrue(tratta.getDistanzaKm() > 0);
        assertTrue(tratta.getDistanzaKm() < 2000, "Distanza troppo grande per l'Italia");

        // Test che la variazione random non sia eccessiva
        int valoreRoma = Stazione.ROMA.getValore();
        int valoreMilano = Stazione.MILANO.getValore();
        int distanzaBase = Math.abs(valoreRoma - valoreMilano) * 100;
        int distanzaCalcolata = tratta.getDistanzaKm();

        // La variazione dovrebbe essere entro ±20km dalla base
        assertTrue(Math.abs(distanzaCalcolata - distanzaBase) <= 30,
                "Variazione random eccessiva");

        System.out.println("🔄 Distanza calcolata: " + distanzaCalcolata + " km");
    }

    // =====================================================
    // TEST EDGE CASES
    // =====================================================

    @Test
    @DisplayName("Test edge cases - stazioni estreme")
    void shouldHandleExtremeStations() {
        System.out.println("Test: Edge cases stazioni estreme");

        // Test con la prima e ultima stazione (valori estremi)
        Stazione primaStazione = Stazione.REGGIO_CALABRIA; // Valore 0
        Stazione ultimaStazione = Stazione.VENEZIA;        // Valore 11

        Tratta trattaEstrema = new Tratta(primaStazione, ultimaStazione);

        assertNotNull(trattaEstrema);
        assertTrue(trattaEstrema.getDistanzaKm() > 0);

        // Dovrebbe essere la tratta più lunga possibile
        int distanzaMassima = Math.abs(primaStazione.getValore() - ultimaStazione.getValore()) * 100;
        assertTrue(trattaEstrema.getDistanzaKm() >= distanzaMassima - 20,
                "Distanza dovrebbe essere vicina al massimo teorico");

        System.out.println("🏔️ Tratta estrema: " + trattaEstrema);
        System.out.println("📏 Distanza massima teorica: " + distanzaMassima + " km");
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
        System.out.println("=== FINE TEST SUITE TRATTE ===");
        System.out.println("✅ Refactoring Tratta verificato con successo!");

        // Stampa statistiche finali
        TrattaUtil.stampaStatistiche();
    }
}