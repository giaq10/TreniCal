import it.trenical.common.cliente.Cliente;
import it.trenical.common.cliente.Biglietto;
import it.trenical.common.viaggi.Viaggio;
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
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Test per la classe Cliente (inclusi i nuovi test per gestione biglietti)
 */
public class ClienteTest {

    // Variabili per i test biglietti
    private Cliente cliente;
    private Viaggio viaggio1;
    private Viaggio viaggio2;
    private Biglietto biglietto1;
    private Biglietto biglietto2;

    @BeforeEach
    void setUp() {
        // Setup per test biglietti
        cliente = new Cliente("mario@test.com", "Mario Rossi");

        // Crea viaggi di test
        Treno treno1 = new Treno("FR001", TipoTreno.STANDARD, 350, EnumSet.noneOf(ServizioTreno.class));
        Tratta tratta1 = new Tratta(Stazione.ROMA, Stazione.MILANO);
        viaggio1 = new Viaggio(treno1, tratta1, LocalDate.now().plusDays(7));

        Treno treno2 = new Treno("FB002", TipoTreno.BUSINESS, 250, EnumSet.noneOf(ServizioTreno.class));
        Tratta tratta2 = new Tratta(Stazione.MILANO, Stazione.VENEZIA);
        viaggio2 = new Viaggio(treno2, tratta2, LocalDate.now().plusDays(10));

        // Crea biglietti di test
        biglietto1 = new Biglietto(viaggio1);
        biglietto1.setNominativo("Mario Rossi");

        biglietto2 = new Biglietto(viaggio2);
        biglietto2.setNominativo("Anna Rossi");
    }

    // ===== TEST ORIGINALI CLIENTE (mantenuti identici) =====

    @Test
    @DisplayName("Test creazione cliente valido")
    void testCreazioneClienteValido() {
        System.out.println("=== Test Creazione Cliente Valido ===");

        // Test costruttore completo
        Cliente cliente1 = new Cliente("mario.rossi@email.com", "Mario Rossi", true);

        assertEquals("mario.rossi@email.com", cliente1.getEmail());
        assertEquals("Mario Rossi", cliente1.getNome());
        assertTrue(cliente1.hasAbbonamentoFedelta());
        assertEquals("Abbonato Fedeltà", cliente1.getStatusAbbonamento());

        System.out.println("Cliente 1: " + cliente1.toString());

        // Test costruttore semplificato (senza abbonamento)
        Cliente cliente2 = new Cliente("anna.verdi@test.it", "Anna Verdi");

        assertEquals("anna.verdi@test.it", cliente2.getEmail());
        assertEquals("Anna Verdi", cliente2.getNome());
        assertFalse(cliente2.hasAbbonamentoFedelta());
        assertEquals("Cliente Standard", cliente2.getStatusAbbonamento());

        System.out.println("Cliente 2: " + cliente2.toString());
        System.out.println("✅ Creazione clienti validi funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test validazione email")
    void testValidazioneEmail() {
        System.out.println("=== Test Validazione Email ===");

        // Email valide - dovrebbero funzionare
        String[] emailValide = {
                "test@email.com",
                "mario.rossi@gmail.com",
                "anna_verdi@university.edu",
                "cliente123@azienda.it",
                "user+tag@domain.co.uk"
        };

        System.out.println("Test email valide:");
        for (String email : emailValide) {
            try {
                Cliente clienteTest = new Cliente(email, "Test User");
                System.out.println("✅ " + email + " - VALIDA");
                assertEquals(email.toLowerCase(), clienteTest.getEmail()); // Verifica normalizzazione
            } catch (Exception e) {
                fail("Email valida rifiutata: " + email + " - " + e.getMessage());
            }
        }

        // Email non valide - dovrebbero lanciare eccezione
        String[] emailNonValide = {
                "",                    // Vuota
                "   ",                 // Solo spazi
                "test",                // Senza @
                "@email.com",          // Senza parte locale
                "test@",               // Senza dominio
                "test@email",          // Senza TLD
                "test space@email.com", // Con spazi
                "test@@email.com",     // Doppia @
                "test@email..com",     // Punto doppio
                "  test@email.com  ",    // Con spazi
                " test@email.com",       // Spazio iniziale
                "test@email.com ",       // Spazio finale
        };

        System.out.println("\nTest email non valide:");
        for (String email : emailNonValide) {
            try {
                new Cliente(email, "Test User");
                fail("Email non valida accettata: " + email);
            } catch (IllegalArgumentException e) {
                System.out.println("✅ " + email + " - RIFIUTATA: " + e.getMessage());
            }
        }

        System.out.println("✅ Validazione email funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test parametri non validi")
    void testParametriNonValidi() {
        System.out.println("=== Test Parametri Non Validi ===");

        // Test email null
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente(null, "Test User");
        });
        System.out.println("✅ Email null: " + exception1.getMessage());

        // Test nome null
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", null);
        });
        System.out.println("✅ Nome null: " + exception2.getMessage());

        // Test nome vuoto
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "");
        });
        System.out.println("✅ Nome vuoto: " + exception3.getMessage());

        // Test nome solo spazi
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("test@email.com", "   ");
        });
        System.out.println("✅ Nome solo spazi: " + exception4.getMessage());

        System.out.println("✅ Gestione parametri non validi corretta");
        System.out.println();
    }

    @Test
    @DisplayName("Test gestione abbonamento fedeltà")
    void testGestioneAbbonamentoFedelta() {
        System.out.println("=== Test Gestione Abbonamento Fedeltà ===");

        // Cliente iniziale senza abbonamento
        Cliente clienteTest = new Cliente("test@email.com", "Test User", false);

        System.out.println("Cliente iniziale: " + clienteTest.toString());
        assertFalse(clienteTest.hasAbbonamentoFedelta());
        assertEquals("Cliente Standard", clienteTest.getStatusAbbonamento());

        // Attivazione abbonamento
        clienteTest.attivaAbbonamentoFedelta();
        System.out.println("Dopo attivazione: " + clienteTest.toString());
        assertTrue(clienteTest.hasAbbonamentoFedelta());
        assertEquals("Abbonato Fedeltà", clienteTest.getStatusAbbonamento());

        // Disattivazione abbonamento
        clienteTest.disattivaAbbonamentoFedelta();
        System.out.println("Dopo disattivazione: " + clienteTest.toString());
        assertFalse(clienteTest.hasAbbonamentoFedelta());
        assertEquals("Cliente Standard", clienteTest.getStatusAbbonamento());

        // Test cliente che inizia già abbonato
        Cliente clienteAbbonato = new Cliente("vip@email.com", "Cliente VIP", true);
        System.out.println("Cliente VIP: " + clienteAbbonato.toString());
        assertTrue(clienteAbbonato.hasAbbonamentoFedelta());

        System.out.println("✅ Gestione abbonamento fedeltà funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test normalizzazione email")
    void testNormalizzazioneEmail() {
        System.out.println("=== Test Normalizzazione Email ===");

        // Test varie forme della stessa email
        String[] emailVarianti = {
                "TEST@EMAIL.COM",
                "Test@Email.Com",
                "test@email.com"
        };

        System.out.println("Test normalizzazione email:");
        for (String emailInput : emailVarianti) {
            Cliente clienteTest = new Cliente(emailInput, "Test User", false);
            System.out.println("Input: '" + emailInput + "' → Output: '" + clienteTest.getEmail() + "'");
            assertEquals("test@email.com", clienteTest.getEmail());
        }

        System.out.println("✅ Normalizzazione email funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test equals e hashCode")
    void testEqualsEHashCode() {
        System.out.println("=== Test Equals e HashCode ===");

        Cliente cliente1 = new Cliente("test@email.com", "Mario Rossi", true);
        Cliente cliente2 = new Cliente("test@email.com", "Luigi Verdi", false); // Stessa email, dati diversi
        Cliente cliente3 = new Cliente("altro@email.com", "Mario Rossi", true); // Email diversa

        System.out.println("Cliente 1: " + cliente1.toString());
        System.out.println("Cliente 2: " + cliente2.toString() + " (stessa email)");
        System.out.println("Cliente 3: " + cliente3.toString() + " (email diversa)");

        // Test equals con stessa email
        assertEquals(cliente1, cliente2);
        assertEquals(cliente1.hashCode(), cliente2.hashCode());
        System.out.println("✅ Cliente1 e Cliente2 sono equals (stessa email)");

        // Test equals con email diversa
        assertNotEquals(cliente1, cliente3);
        System.out.println("✅ Cliente1 e Cliente3 sono diversi (email diversa)");

        // Test con null
        assertNotEquals(cliente1, null);
        System.out.println("✅ Cliente diverso da null");

        // Test con classe diversa
        assertNotEquals(cliente1, "stringa");
        System.out.println("✅ Cliente diverso da oggetto di classe diversa");

        System.out.println("✅ Test equals e hashCode superati");
        System.out.println();
    }

    @Test
    @DisplayName("Test scenario completo")
    void testScenarioCompleto() {
        System.out.println("=== Test Scenario Completo ===");

        // Creazione di diversi clienti
        Cliente clienteStandard = new Cliente("mario@email.com", "Mario Rossi");
        Cliente clienteVIP = new Cliente("anna@email.com", "Anna Verdi", true);

        System.out.println("Clienti creati:");
        System.out.println("- " + clienteStandard.toString());
        System.out.println("- " + clienteVIP.toString());

        // Simulazione upgrade a fedeltà
        System.out.println("\nUpgrade Mario a cliente fedeltà...");
        clienteStandard.attivaAbbonamentoFedelta();
        System.out.println("- " + clienteStandard.toString());

        // Verifica stati
        assertTrue(clienteStandard.hasAbbonamentoFedelta());
        assertTrue(clienteVIP.hasAbbonamentoFedelta());

        // Test che email sia sempre la chiave
        Cliente clienteDuplicato = new Cliente("mario@email.com", "Mario Bianchi");
        assertEquals(clienteStandard, clienteDuplicato);
        System.out.println("✅ Stesso cliente anche con nome diverso (email = chiave)");

        System.out.println("✅ Scenario completo testato con successo");
        System.out.println();
    }

    // ===== NUOVI TEST PER GESTIONE BIGLIETTI =====

    @Test
    @DisplayName("Test aggiunta singolo biglietto")
    void testAggiuntaSingoloBiglietto() {
        System.out.println("=== Test Aggiunta Singolo Biglietto ===");

        System.out.println("Cliente iniziale: " + cliente.toString());
        assertEquals(0, cliente.getNumeroBiglietti());
        assertFalse(cliente.hasBiglietti());

        // Aggiungi biglietto
        cliente.addBiglietto(biglietto1);

        System.out.println("Dopo aggiunta biglietto: " + cliente.toString());
        System.out.println("Biglietto aggiunto: " + biglietto1.toString());

        assertEquals(1, cliente.getNumeroBiglietti());
        assertTrue(cliente.hasBiglietti());
        assertEquals(1, cliente.getBiglietti().size());
        assertEquals(biglietto1, cliente.getBiglietti().get(0));

        System.out.println("✅ Aggiunta singolo biglietto funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test aggiunta biglietti multipli")
    void testAggiuntaBigliettiMultipli() {
        System.out.println("=== Test Aggiunta Biglietti Multipli ===");

        List<Biglietto> biglietti = new ArrayList<>();
        biglietti.add(biglietto1);
        biglietti.add(biglietto2);

        System.out.println("Cliente iniziale: " + cliente.toString());
        System.out.println("Biglietti da aggiungere:");
        biglietti.forEach(b -> System.out.println("- " + b.toString()));

        cliente.addBiglietti(biglietti);

        System.out.println("\nDopo aggiunta biglietti: " + cliente.toString());

        assertEquals(2, cliente.getNumeroBiglietti());
        assertTrue(cliente.hasBiglietti());
        assertEquals(2, cliente.getBiglietti().size());
        assertTrue(cliente.getBiglietti().contains(biglietto1));
        assertTrue(cliente.getBiglietti().contains(biglietto2));

        System.out.println("✅ Aggiunta biglietti multipli funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test rimozione biglietti")
    void testRimozioneBiglietti() {
        System.out.println("=== Test Rimozione Biglietti ===");

        // Setup: aggiungi biglietti
        cliente.addBiglietto(biglietto1);
        cliente.addBiglietto(biglietto2);

        System.out.println("Cliente con biglietti: " + cliente.toString());
        System.out.println("Biglietti presenti: " + cliente.getNumeroBiglietti());

        // Test rimozione per oggetto
        boolean rimosso1 = cliente.removeBiglietto(biglietto1);
        assertTrue(rimosso1);
        assertEquals(1, cliente.getNumeroBiglietti());
        assertFalse(cliente.getBiglietti().contains(biglietto1));

        System.out.println("Dopo rimozione biglietto1: " + cliente.getNumeroBiglietti() + " biglietti");

        // Test rimozione per ID
        boolean rimosso2 = cliente.removeBigliettoById(biglietto2.getId());
        assertTrue(rimosso2);
        assertEquals(0, cliente.getNumeroBiglietti());
        assertFalse(cliente.hasBiglietti());

        System.out.println("Dopo rimozione biglietto2: " + cliente.getNumeroBiglietti() + " biglietti");

        // Test rimozione biglietto inesistente
        boolean rimossoInesistente = cliente.removeBiglietto(biglietto1);
        assertFalse(rimossoInesistente);

        System.out.println("✅ Rimozione biglietti funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test ricerca biglietto per ID")
    void testRicercaBigliettoPerID() {
        System.out.println("=== Test Ricerca Biglietto per ID ===");

        cliente.addBiglietto(biglietto1);
        cliente.addBiglietto(biglietto2);

        System.out.println("Biglietti del cliente:");
        cliente.getBiglietti().forEach(b -> System.out.println("- " + b.getId() + ": " + b.getNominativo()));

        // Test ricerca esistente
        Biglietto trovato = cliente.getBigliettoById(biglietto1.getId());
        assertNotNull(trovato);
        assertEquals(biglietto1, trovato);

        System.out.println("Biglietto trovato con ID " + biglietto1.getId() + ": " + trovato.getNominativo());

        // Test ricerca inesistente
        Biglietto nonTrovato = cliente.getBigliettoById("ID_INESISTENTE");
        assertNull(nonTrovato);

        System.out.println("✅ Ricerca biglietto per ID funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test calcoli statistiche cliente")
    void testCalcoliStatisticheCliente() {
        System.out.println("=== Test Calcoli Statistiche Cliente ===");

        cliente.addBiglietto(biglietto1);
        cliente.addBiglietto(biglietto2);

        double totaleSpeso = cliente.getTotaleSpeso();
        double totaleAtteso = biglietto1.getPrezzo() + biglietto2.getPrezzo();

        System.out.println("Statistiche cliente:");
        System.out.println("- Numero biglietti: " + cliente.getNumeroBiglietti());
        System.out.println("- Totale speso: €" + String.format("%.2f", totaleSpeso));
        System.out.println("- Biglietti validi: " + cliente.getBigliettiValidi().size());

        assertEquals(totaleAtteso, totaleSpeso, 0.01);
        assertEquals(2, cliente.getNumeroBiglietti());
        assertEquals(2, cliente.getBigliettiValidi().size()); // Nessun biglietto cancellato

        // Test statistiche complete
        String statistiche = cliente.getStatisticheCliente();
        System.out.println("\nStatistiche complete:");
        System.out.println(statistiche);

        assertTrue(statistiche.contains("Mario Rossi"));
        assertTrue(statistiche.contains("mario@test.com"));
        assertTrue(statistiche.contains("2"));
        assertTrue(statistiche.contains(String.format("%.2f", totaleSpeso)));

        System.out.println("✅ Calcoli statistiche cliente funzionano correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test parametri non validi gestione biglietti")
    void testParametriNonValidiGestioneBiglietti() {
        System.out.println("=== Test Parametri Non Validi Gestione Biglietti ===");

        // Test aggiunta biglietto null
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            cliente.addBiglietto(null);
        });
        System.out.println("✅ Biglietto null: " + exception1.getMessage());

        // Test aggiunta lista null
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            cliente.addBiglietti(null);
        });
        System.out.println("✅ Lista biglietti null: " + exception2.getMessage());

        System.out.println("✅ Gestione parametri non validi per biglietti corretta");
        System.out.println();
    }

    @Test
    @DisplayName("Test scenario completo acquisto famiglia")
    void testScenarioCompletoAcquistoFamiglia() {
        System.out.println("=== Test Scenario Completo Acquisto Famiglia ===");
        System.out.println("Scenario: Mario acquista biglietti per tutta la famiglia");
        System.out.println();

        // 1. Creazione cliente
        Cliente mario = new Cliente("mario.famiglia@email.com", "Mario Rossi");
        mario.attivaAbbonamentoFedelta(); // Cliente fedeltà

        System.out.println("1. Cliente creato: " + mario.toString());

        // 2. Creazione biglietto base e cloni
        Biglietto bigliettoBase = new Biglietto(viaggio1);
        Biglietto bigliettoMoglie = bigliettoBase.clone();
        Biglietto bigliettoFiglio = bigliettoBase.clone();

        // 3. Impostazione nominativi
        bigliettoBase.setNominativo("Mario Rossi");
        bigliettoMoglie.setNominativo("Anna Rossi");
        bigliettoFiglio.setNominativo("Luca Rossi");

        System.out.println("\n2. Biglietti creati e nominativi impostati:");
        System.out.println("- " + bigliettoBase.toString());
        System.out.println("- " + bigliettoMoglie.toString());
        System.out.println("- " + bigliettoFiglio.toString());

        // 4. Aggiunta biglietti al cliente
        List<Biglietto> bigliettiFamiglia = List.of(bigliettoBase, bigliettoMoglie, bigliettoFiglio);
        mario.addBiglietti(bigliettiFamiglia);

        System.out.println("\n3. Cliente dopo acquisto: " + mario.toString());

        // 5. Statistiche finali
        System.out.println("\n4. STATISTICHE FINALI:");
        System.out.println("=" .repeat(50));

        // 6. Verifica biglietti del cliente
        System.out.println("5. BIGLIETTI ACQUISTATI:");
        System.out.println("=" .repeat(50));
        List<Biglietto> bigliettiCliente = mario.getBiglietti();
        for (int i = 0; i < bigliettiCliente.size(); i++) {
            Biglietto b = bigliettiCliente.get(i);
            System.out.println("Biglietto " + (i+1) + ":");
            System.out.println("- ID: " + b.getId());
            System.out.println("- Nominativo: " + b.getNominativo());
            System.out.println("- Tratta: " + b.getInfoTratta());
            System.out.println("- Prezzo: €" + String.format("%.2f", b.getPrezzo()));
            System.out.println();
        }

        // 7. Verifiche finali
        assertEquals(3, mario.getNumeroBiglietti());
        assertTrue(mario.hasAbbonamentoFedelta());
        assertTrue(mario.hasBiglietti());
        assertEquals(3, mario.getBigliettiValidi().size());

        double totaleAtteso = bigliettoBase.getPrezzo() + bigliettoMoglie.getPrezzo() + bigliettoFiglio.getPrezzo();
        assertEquals(totaleAtteso, mario.getTotaleSpeso(), 0.01);

        // Verifica che tutti i biglietti siano diversi (ID diversi)
        List<String> ids = mario.getBiglietti().stream().map(Biglietto::getId).toList();
        assertEquals(3, ids.stream().distinct().count()); // Tutti ID diversi

        System.out.println("✅ Scenario completo acquisto famiglia testato con successo!");
        System.out.println("=" .repeat(50));
        System.out.println();
    }
}