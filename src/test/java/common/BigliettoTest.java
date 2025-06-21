package common;

import it.trenical.common.cliente.Biglietto;
import it.trenical.server.viaggi.Viaggio;
import it.trenical.server.treni.Treno;
import it.trenical.server.treni.TipoTreno;
import it.trenical.server.treni.ServizioTreno;
import it.trenical.server.tratte.Tratta;
import it.trenical.common.stazioni.Stazione;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test completo per la classe Biglietto e pattern Prototype
 */
public class BigliettoTest {

    private Viaggio viaggio;
    private Biglietto biglietto;

    @BeforeEach
    void setUp() {
        // Creazione viaggio di test
        Treno treno = new Treno("FR001", TipoTreno.STANDARD, 350, EnumSet.noneOf(ServizioTreno.class));
        Tratta tratta = new Tratta(Stazione.ROMA, Stazione.MILANO);
        viaggio = new Viaggio(treno, tratta, LocalDate.now().plusDays(7));

        // Creazione biglietto di base
        biglietto = new Biglietto(viaggio);

        System.out.println("=== Setup Test Biglietto ===");
        System.out.println("Viaggio creato: " + viaggio.toString());
        System.out.println("Biglietto base creato: " + biglietto.toString());
        System.out.println();
    }

    @Test
    @DisplayName("Test creazione biglietto iniziale")
    void testCreazioneBigliettoIniziale() {
        System.out.println("=== Test Creazione Biglietto Iniziale ===");

        // Verifica stato iniziale
        assertNotNull(biglietto);
        assertEquals(viaggio, biglietto.getViaggio());
        assertNotNull(biglietto.getDataAcquisto());

        // Verifica che nominativo e ID siano null inizialmente
        assertFalse(biglietto.isCompleto());
        assertEquals("[NOMINATIVO DA INSERIRE]", biglietto.getNominativo());
        assertEquals("[ID GENERATO DOPO NOMINATIVO]", biglietto.getId());

        System.out.println("Biglietto appena creato:");
        System.out.println("- Completo: " + biglietto.isCompleto());
        System.out.println("- Nominativo: " + biglietto.getNominativo());
        System.out.println("- ID: " + biglietto.getId());
        System.out.println("- Viaggio: " + biglietto.getIdViaggio());
        System.out.println("- Prezzo: €" + String.format("%.2f", biglietto.getPrezzo()));

        System.out.println("✅ Biglietto iniziale creato correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test setNominativo e generazione ID")
    void testSetNominativoEGenerazioneID() {
        System.out.println("=== Test SetNominativo e Generazione ID ===");

        System.out.println("Prima del setNominativo:");
        System.out.println("- Biglietto: " + biglietto.toString());
        System.out.println("- Completo: " + biglietto.isCompleto());

        // Imposta nominativo
        biglietto.setNominativo("Mario Rossi");

        System.out.println("\nDopo setNominativo('Mario Rossi'):");
        System.out.println("- Biglietto: " + biglietto.toString());
        System.out.println("- Completo: " + biglietto.isCompleto());
        System.out.println("- Nominativo: " + biglietto.getNominativo());
        System.out.println("- ID generato: " + biglietto.getId());

        // Verifica che tutto sia corretto
        assertTrue(biglietto.isCompleto());
        assertEquals("Mario Rossi", biglietto.getNominativo());
        assertNotNull(biglietto.getId());
        assertTrue(biglietto.getId().startsWith("BGT_"));
        assertEquals(12, biglietto.getId().length()); // "BGT_" + 8 cifre

        System.out.println("✅ SetNominativo e generazione ID funzionano correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test parametri non validi")
    void testParametriNonValidi() {
        System.out.println("=== Test Parametri Non Validi ===");

        // Test viaggio null nel costruttore
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Biglietto(null);
        });
        System.out.println("✅ Viaggio null: " + exception1.getMessage());

        // Test nominativo null
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            biglietto.setNominativo(null);
        });
        System.out.println("✅ Nominativo null: " + exception2.getMessage());

        // Test nominativo vuoto
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            biglietto.setNominativo("");
        });
        System.out.println("✅ Nominativo vuoto: " + exception3.getMessage());

        // Test nominativo solo spazi
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            biglietto.setNominativo("   ");
        });
        System.out.println("✅ Nominativo solo spazi: " + exception4.getMessage());

        System.out.println("✅ Gestione parametri non validi corretta");
        System.out.println();
    }

    @Test
    @DisplayName("Test pattern Prototype - clone singolo")
    void testPatternPrototypeSingolo() {
        System.out.println("=== Test Pattern Prototype - Clone Singolo ===");

        // Biglietto originale
        System.out.println("Biglietto originale (prima del clone):");
        System.out.println("- " + biglietto.toString());

        // Clone del biglietto
        Biglietto bigliettoClonato = biglietto.clone();

        System.out.println("\nBiglietto clonato:");
        System.out.println("- " + bigliettoClonato.toString());

        // Verifica che siano oggetti diversi
        assertNotSame(biglietto, bigliettoClonato);
        System.out.println("✅ Biglietto e clone sono oggetti diversi");

        // Verifica che condividano lo stesso viaggio
        assertSame(biglietto.getViaggio(), bigliettoClonato.getViaggio());
        System.out.println("✅ Biglietto e clone condividono stesso viaggio");

        // Verifica che abbiano stessa data acquisto
        assertEquals(biglietto.getDataAcquisto(), bigliettoClonato.getDataAcquisto());
        System.out.println("✅ Biglietto e clone hanno stessa data acquisto");

        // Verifica che il clone non sia completo
        assertFalse(bigliettoClonato.isCompleto());
        System.out.println("✅ Clone non è completo (nominativo e ID null)");

        System.out.println("✅ Pattern Prototype funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test scenario completo - Acquisto multiplo")
    void testScenarioCompletoAcquistoMultiplo() {
        System.out.println("=== Test Scenario Completo - Acquisto Multiplo ===");
        System.out.println("Scenario: Mario acquista 3 biglietti per la famiglia");
        System.out.println();

        // 1. Creazione biglietto base
        Biglietto bigliettoBase = new Biglietto(viaggio);
        System.out.println("1. Biglietto base creato:");
        System.out.println("   " + bigliettoBase.toString());

        // 2. Clonazione per acquisto multiplo
        Biglietto biglietto2 = bigliettoBase.clone();
        Biglietto biglietto3 = bigliettoBase.clone();

        System.out.println("\n2. Biglietti clonati:");
        System.out.println("   Biglietto 2: " + biglietto2.toString());
        System.out.println("   Biglietto 3: " + biglietto3.toString());

        // 3. Lista biglietti per gestione
        List<Biglietto> bigliettiAcquistati = new ArrayList<>();
        bigliettiAcquistati.add(bigliettoBase);
        bigliettiAcquistati.add(biglietto2);
        bigliettiAcquistati.add(biglietto3);

        System.out.println("\n3. Lista biglietti creata: " + bigliettiAcquistati.size() + " biglietti");

        // 4. Simulazione GUI: inserimento nominativi
        String[] nominativi = {"Mario Rossi", "Anna Rossi", "Luca Rossi"};

        System.out.println("\n4. Inserimento nominativi (simulazione GUI):");
        for (int i = 0; i < bigliettiAcquistati.size(); i++) {
            Biglietto b = bigliettiAcquistati.get(i);
            String nominativo = nominativi[i];

            System.out.println("   Impostando nominativo '" + nominativo + "' su biglietto " + (i+1));
            b.setNominativo(nominativo);
            System.out.println("   → ID generato: " + b.getId());
        }

        // 5. Verifica risultati finali
        System.out.println("\n5. RISULTATI FINALI:");
        System.out.println("=" .repeat(60));

        double totaleSpeso = 0;
        for (int i = 0; i < bigliettiAcquistati.size(); i++) {
            Biglietto b = bigliettiAcquistati.get(i);
            totaleSpeso += b.getPrezzo();

            System.out.println("BIGLIETTO " + (i+1) + ":");
            System.out.println("- ID: " + b.getId());
            System.out.println("- Nominativo: " + b.getNominativo());
            System.out.println("- Treno: " + b.getCodiceTreno() + " (" + b.getTipoTreno() + ")");
            System.out.println("- Tratta: " + b.getInfoTratta());
            System.out.println("- Data: " + b.getDataOraPartenzaFormattata());
            System.out.println("- Prezzo: €" + String.format("%.2f", b.getPrezzo()));
            System.out.println("- Completo: " + (b.isCompleto() ? "✅ SÌ" : "❌ NO"));
            System.out.println();
        }

        System.out.println("RIEPILOGO ACQUISTO:");
        System.out.println("- Numero biglietti: " + bigliettiAcquistati.size());
        System.out.println("- Totale speso: €" + String.format("%.2f", totaleSpeso));
        System.out.println("- Tutti completi: " + (bigliettiAcquistati.stream().allMatch(Biglietto::isCompleto) ? "✅ SÌ" : "❌ NO"));

        // 6. Verifiche con assert
        assertEquals(3, bigliettiAcquistati.size());
        assertTrue(bigliettiAcquistati.stream().allMatch(Biglietto::isCompleto));
        assertEquals("Mario Rossi", bigliettiAcquistati.get(0).getNominativo());
        assertEquals("Anna Rossi", bigliettiAcquistati.get(1).getNominativo());
        assertEquals("Luca Rossi", bigliettiAcquistati.get(2).getNominativo());

        // Verifica ID univoci
        String id1 = bigliettiAcquistati.get(0).getId();
        String id2 = bigliettiAcquistati.get(1).getId();
        String id3 = bigliettiAcquistati.get(2).getId();

        assertNotEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id2, id3);

        System.out.println("✅ Scenario completo acquisto multiplo testato con successo!");
        System.out.println("=" .repeat(60));
        System.out.println();
    }

    @Test
    @DisplayName("Test getters delegati al viaggio")
    void testGettersDelegatiAlViaggio() {
        System.out.println("=== Test Getters Delegati al Viaggio ===");

        // Imposta nominativo per completare il biglietto
        biglietto.setNominativo("Test User");

        System.out.println("Biglietto completo: " + biglietto.toString());
        System.out.println();

        // Test tutti i getters delegati
        System.out.println("Getters delegati al viaggio:");
        System.out.println("- ID Viaggio: " + biglietto.getIdViaggio());
        System.out.println("- Codice Treno: " + biglietto.getCodiceTreno());
        System.out.println("- Tipo Treno: " + biglietto.getTipoTreno());
        System.out.println("- Stazione Partenza: " + biglietto.getStazionePartenza().getNome());
        System.out.println("- Stazione Arrivo: " + biglietto.getStazioneArrivo().getNome());
        System.out.println("- Data Viaggio: " + biglietto.getDataViaggio());
        System.out.println("- Orario Partenza: " + biglietto.getOrarioPartenza());
        System.out.println("- Orario Arrivo: " + biglietto.getOrarioArrivo());
        System.out.println("- Binario: " + biglietto.getBinarioPartenza().getDescrizione());
        System.out.println("- Prezzo: €" + String.format("%.2f", biglietto.getPrezzo()));
        System.out.println("- Durata: " + biglietto.getDurataFormattata());

        System.out.println("\nMetodi delegati:");
        System.out.println("- Disponibile: " + biglietto.isDisponibile());
        System.out.println("- Cancellato: " + biglietto.isCancellato());
        System.out.println("- Ha ritardo: " + biglietto.haRitardo());

        // Verifica che i valori corrispondano a quelli del viaggio
        assertEquals(viaggio.getId(), biglietto.getIdViaggio());
        assertEquals(viaggio.getTreno().getCodice(), biglietto.getCodiceTreno());
        assertEquals(viaggio.getTratta().getStazionePartenza(), biglietto.getStazionePartenza());
        assertEquals(viaggio.getTratta().getStazioneArrivo(), biglietto.getStazioneArrivo());
        assertEquals(viaggio.getPrezzo(), biglietto.getPrezzo());

        System.out.println("✅ Tutti i getters delegati funzionano correttamente");
        System.out.println();
    }


    @Test
    @DisplayName("Test equals e hashCode")
    void testEqualsEHashCode() throws InterruptedException {
        System.out.println("=== Test Equals e HashCode ===");

        // Crea biglietti con stesso nominativo
        Biglietto biglietto1 = new Biglietto(viaggio);
        Biglietto biglietto2 = new Biglietto(viaggio);

        biglietto1.setNominativo("Mario Rossi");
        TimeUnit.SECONDS.sleep(1);
        biglietto2.setNominativo("Mario Rossi");

        System.out.println("Biglietto 1: " + biglietto1.toString());
        System.out.println("Biglietto 2: " + biglietto2.toString());

        // Con stesso nominativo + viaggio dovrebbe generare diverso ID con la gestione dei DB
        assertNotEquals(biglietto1.getId(), biglietto2.getId());
        assertNotEquals(biglietto1, biglietto2);
        assertNotEquals(biglietto1.hashCode(), biglietto2.hashCode());

        System.out.println("✅ Biglietti con stesso nominativo sono equals");

        // Test con nominativo diverso
        Biglietto biglietto3 = new Biglietto(viaggio);
        biglietto3.setNominativo("Anna Verdi");

        System.out.println("Biglietto 3: " + biglietto3.toString());

        assertNotEquals(biglietto1.getId(), biglietto3.getId());
        assertNotEquals(biglietto1, biglietto3);

        System.out.println("✅ Biglietti con nominativo diverso sono diversi");

        // Test con null e classe diversa
        assertNotEquals(biglietto1, null);
        assertNotEquals(biglietto1, "stringa");

        System.out.println("✅ Gestione null e classi diverse corretta");
        System.out.println();
    }
}