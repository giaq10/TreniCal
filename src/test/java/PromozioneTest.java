import it.trenical.common.model.bigliettiEpromozioni.*;
import it.trenical.common.model.bigliettiEpromozioni.factoryMethod.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class PromozioneTest {

    private PromozioneStandard promoStandard;
    private PromozioneFedelta promoFedelta;

    @BeforeEach
    void setUp() {
        promoStandard = new PromozioneStandard( "Sconto Estate", 20.0);
        promoFedelta = new PromozioneFedelta( "Sconto Fedelta", 15.0);
    }

    @Test
    @DisplayName("Test creazione PromozioneStandard")
    void testCreazionePromozioneStandard() {
        System.out.println("=== Test Creazione PromozioneStandard ===");

        assertEquals("Sconto Estate", promoStandard.getNome());
        assertEquals(20.0, promoStandard.getSconto());
        assertEquals("Standard", promoStandard.getTipo());

        System.out.println("ID: " + promoStandard.getId());
        System.out.println("Nome: " + promoStandard.getNome());
        System.out.println("Percentuale: " + promoStandard.getSconto() + "%");
        System.out.println("Tipo: " + promoStandard.getTipo());
        System.out.println("✅ PromozioneStandard creata correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test creazione PromozioneFedelta")
    void testCreazionePromozioneFedelta() {
        System.out.println("=== Test Creazione PromozioneFedelta ===");

        assertEquals("Sconto Fedelta", promoFedelta.getNome());
        assertEquals(15.0, promoFedelta.getSconto());
        assertEquals("Fedelta", promoFedelta.getTipo());

        System.out.println("ID: " + promoFedelta.getId());
        System.out.println("Nome: " + promoFedelta.getNome());
        System.out.println("Percentuale: " + promoFedelta.getSconto() + "%");
        System.out.println("Tipo: " + promoFedelta.getTipo());
        System.out.println("✅ PromozioneFedelta creata correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test parametri non validi PromozioneStandard")
    void testParametriNonValidiStandard() {
        System.out.println("=== Test Parametri Non Validi PromozioneStandard ===");

        // Test nome null
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new PromozioneStandard(null, 10.0);
        });
        System.out.println("✅ Nome null: " + exception3.getMessage());

        // Test nome vuoto
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new PromozioneStandard("", 10.0);
        });
        System.out.println("✅ Nome vuoto: " + exception4.getMessage());

        // Test percentuale zero
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> {
            new PromozioneStandard("Test", 0.0);
        });
        System.out.println("✅ Percentuale zero: " + exception5.getMessage());

        // Test percentuale negativa
        Exception exception6 = assertThrows(IllegalArgumentException.class, () -> {
            new PromozioneStandard("Test", -5.0);
        });
        System.out.println("✅ Percentuale negativa: " + exception6.getMessage());

        // Test percentuale maggiore di 100
        Exception exception7 = assertThrows(IllegalArgumentException.class, () -> {
            new PromozioneStandard("Test", 150.0);
        });
        System.out.println("✅ Percentuale > 100: " + exception7.getMessage());
        System.out.println();
    }

    @Test
    @DisplayName("Test calcolo sconto PromozioneStandard")
    void testCalcoloScontoStandard() {
        System.out.println("=== Test Calcolo Sconto PromozioneStandard ===");

        double prezzoTest = 100.0;
        double prezzoScontato = promoStandard.applicaSconto(prezzoTest);
        double scontoApplicato = prezzoTest - prezzoScontato;

        System.out.println("Prezzo originale: €" + prezzoTest);
        System.out.println("Sconto applicato: " + promoStandard.getSconto() + "%");
        System.out.println("Importo sconto: €" + scontoApplicato);
        System.out.println("Prezzo finale: €" + prezzoScontato);

        assertEquals(80.0, prezzoScontato, 0.01);
        System.out.println("✅ Calcolo sconto corretto (€100 - 20% = €80)");
        System.out.println();
    }

    @Test
    @DisplayName("Test calcolo sconto PromozioneFedelta")
    void testCalcoloScontoFedelta() {
        System.out.println("=== Test Calcolo Sconto PromozioneFedelta ===");

        double prezzoTest = 200.0;
        double prezzoScontato = promoFedelta.applicaSconto(prezzoTest);
        double scontoApplicato = prezzoTest - prezzoScontato;

        System.out.println("Prezzo originale: €" + prezzoTest);
        System.out.println("Sconto applicato: " + promoFedelta.getSconto() + "%");
        System.out.println("Importo sconto: €" + scontoApplicato);
        System.out.println("Prezzo finale: €" + prezzoScontato);

        assertEquals(170.0, prezzoScontato, 0.01);
        System.out.println("✅ Calcolo sconto corretto (€200 - 15% = €170)");
        System.out.println();
    }

    @Test
    @DisplayName("Test Factory PromozioneStandard")
    void testFactoryStandard() {
        System.out.println("=== Test Factory PromozioneStandard ===");

        PromozioneFactory factory = PromozioneFactory.getFactory("standard");
        Promozione promo = factory.creaPromozione("Test Factory", 25.0);

        System.out.println("Factory utilizzata: " + factory.getClass().getSimpleName());
        System.out.println("Promozione creata: " + promo.toString());

        assertNotNull(promo);
        assertTrue(promo instanceof PromozioneStandard);
        assertEquals("Test Factory", promo.getNome());
        assertEquals(25.0, promo.getSconto());
        assertEquals("Standard", promo.getTipo());

        System.out.println("✅ Factory PromozioneStandard funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test Factory PromozioneFedelta")
    void testFactoryFedelta() {
        System.out.println("=== Test Factory PromozioneFedelta ===");

        PromozioneFactory factory = PromozioneFactory.getFactory("fedelta");
        Promozione promo = factory.creaPromozione("Test Factory Fedeltà", 12.0);

        System.out.println("Factory utilizzata: " + factory.getClass().getSimpleName());
        System.out.println("Promozione creata: " + promo.toString());

        assertNotNull(promo);
        assertTrue(promo instanceof PromozioneFedelta);
        assertEquals("Test Factory Fedeltà", promo.getNome());
        assertEquals(12.0, promo.getSconto());
        assertEquals("Fedelta", promo.getTipo());

        System.out.println("✅ Factory PromozioneFedelta funziona correttamente");
        System.out.println();
    }

    @Test
    @DisplayName("Test Factory case insensitive")
    void testFactoryCaseInsensitive() {
        System.out.println("=== Test Factory Case Insensitive ===");

        // Test con diverse varianti
        PromozioneFactory factory1 = PromozioneFactory.getFactory("STANDARD");
        PromozioneFactory factory2 = PromozioneFactory.getFactory("Standard");
        PromozioneFactory factory3 = PromozioneFactory.getFactory("standard");

        assertNotNull(factory1);
        assertNotNull(factory2);
        assertNotNull(factory3);
        System.out.println("✅ Factory 'STANDARD', 'Standard', 'standard' tutte funzionanti");

        // Test fedeltà con accenti
        PromozioneFactory factory4 = PromozioneFactory.getFactory("fedelta");
        PromozioneFactory factory5 = PromozioneFactory.getFactory("FEDELTA");

        assertNotNull(factory4);
        assertNotNull(factory5);
        System.out.println("✅ Factory 'fedelta' e 'FEDELTA' funzionanti");
        System.out.println();
    }

    @Test
    @DisplayName("Test Factory tipo non supportato")
    void testFactoryTipoNonSupportato() {
        System.out.println("=== Test Factory Tipo Non Supportato ===");

        // Test tipo inesistente
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            PromozioneFactory.getFactory("premium");
        });
        System.out.println("✅ Tipo 'premium': " + exception1.getMessage());

        // Test stringa vuota
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            PromozioneFactory.getFactory("");
        });
        System.out.println("✅ Stringa vuota: " + exception2.getMessage());

        // Test null
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            PromozioneFactory.getFactory(null);
        });
        System.out.println("✅ Null: " + exception3.getMessage());
        System.out.println();
    }

    @Test
    @DisplayName("Test equals e hashCode")
    void testEqualsEHashCode() {
        System.out.println("=== Test Equals e HashCode ===");

        PromozioneStandard promo1 = new PromozioneStandard("Nome1", 10.0);
        PromozioneStandard promo2 = new PromozioneStandard("Nome2", 20.0);
        PromozioneStandard promo3 = new PromozioneStandard("Nome1", 10.0);

        System.out.println("Promo1: " + promo1.toString());
        System.out.println("Promo2: " + promo2.toString() );
        System.out.println("Promo3: " + promo3.toString() );

        // Test equals con stesso ID
        assertEquals(promo1, promo3);
        assertEquals(promo1.hashCode(), promo3.hashCode());
        System.out.println("✅ Promo1 e Promo3 sono equals (stesso ID perche uguali in nome e sconto)");

        // Test equals con ID diverso
        assertNotEquals(promo1, promo2);
        System.out.println("✅ Promo1 e Promo2 sono diversi (ID diverso perche diversi in nome e sconto)");

        // Test con null
        assertNotEquals(promo1, null);
        System.out.println("✅ Promo1 diverso da null");

        // Test con classe diversa
        assertNotEquals(promo1, "stringa");
        System.out.println("✅ Promo1 diverso da oggetto di classe diversa");
        System.out.println();
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        System.out.println("=== Test ToString ===");

        String stringStandard = promoStandard.toString();
        String stringFedelta = promoFedelta.toString();

        System.out.println("PromozioneStandard toString: " + stringStandard);
        System.out.println("PromozioneFedelta toString: " + stringFedelta);

        // Verifica che le stringhe contengano le informazioni corrette
        assertTrue(stringStandard.contains("Standard"));
        assertTrue(stringStandard.contains("Sconto Estate"));
        assertTrue(stringStandard.contains("20,0"));
        assertTrue(stringFedelta.contains("Fedelta"));
        assertTrue(stringFedelta.contains("Sconto Fedelta"));
        assertTrue(stringFedelta.contains("15,0"));

        System.out.println("✅ ToString contiene tutte le informazioni corrette");
        System.out.println();
    }

    @Test
    @DisplayName("Test scenario completo promozioni")
    void testScenarioCompleto() {
        System.out.println("=== Test Scenario Completo ===");

        // Creazione di diverse promozioni tramite factory
        PromozioneFactory factoryStandard = PromozioneFactory.getFactory("standard");
        PromozioneFactory factoryFedelta = PromozioneFactory.getFactory("fedelta");

        Promozione promoNatale = factoryStandard.creaPromozione("Sconto Natale", 30.0);
        Promozione promoVip = factoryFedelta.creaPromozione("Sconto VIP", 25.0);

        System.out.println("Promozioni create:");
        System.out.println("- " + promoNatale.toString());
        System.out.println("- " + promoVip.toString());

        // Test applicazione sconti su prezzi diversi
        double[] prezziTest = {50.0, 150.0, 300.0};

        for (double prezzo : prezziTest) {
            double prezzoScontatoNatale = promoNatale.applicaSconto(prezzo);
            double prezzoScontatoVip = promoVip.applicaSconto(prezzo);

            System.out.println("\nPrezzo base: €" + prezzo);
            System.out.println("Con sconto Natale (30%): €" + String.format("%.2f", prezzoScontatoNatale));
            System.out.println("Con sconto VIP (25%): €" + String.format("%.2f", prezzoScontatoVip));

            // Verifica che gli sconti siano corretti
            assertEquals(prezzo * 0.7, prezzoScontatoNatale, 0.01);
            assertEquals(prezzo * 0.75, prezzoScontatoVip, 0.01);
        }

        System.out.println("\n✅ Scenario completo testato con successo");
        System.out.println();
    }
}