import it.trenical.common.model.treni.*;
import it.trenical.common.model.treni.builder.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test essenziali per i 3 tipi di Treno: Economy, Standard, Business
 */
class TrenoTest {

    private TrenoDirector director;

    @BeforeEach
    void setUp() {
        director = new TrenoDirector();
    }

    @Test
    @DisplayName("Creazione Treno Economy con builder")
    void testCreazioneTrenoEconomy() {
        // Act
        Treno trenoEconomy = director.costruisciTrenoEconomy("EC001");
        System.out.println(trenoEconomy);
        System.out.println(trenoEconomy.getServizi()
        );

        // Assert
        assertNotNull(trenoEconomy);
        assertEquals("EC001", trenoEconomy.getCodice());
        assertEquals(TipoTreno.ECONOMY, trenoEconomy.getTipoTreno());
        assertEquals(450, trenoEconomy.getPostiTotali());
        assertEquals(1, trenoEconomy.getServizi().size());
        assertTrue(trenoEconomy.hasServizio(ServizioTreno.ARIA_CONDIZIONATA));
    }

    @Test
    @DisplayName("Creazione Treno Standard con builder")
    void testCreazioneTrenoStandard() {
        // Act
        Treno trenoStandard = director.costruisciTrenoStandard("ST001");
        System.out.println(trenoStandard);
        System.out.println(trenoStandard.getServizi());
        // Assert
        assertNotNull(trenoStandard);
        assertEquals("ST001", trenoStandard.getCodice());
        assertEquals(TipoTreno.STANDARD, trenoStandard.getTipoTreno());
        assertEquals(350, trenoStandard.getPostiTotali());
        assertEquals(3, trenoStandard.getServizi().size());
        assertTrue(trenoStandard.hasServizio(ServizioTreno.ARIA_CONDIZIONATA));
        assertTrue(trenoStandard.hasServizio(ServizioTreno.WIFI));
        assertTrue(trenoStandard.hasServizio(ServizioTreno.PRESE_ELETTRICHE));
    }

    @Test
    @DisplayName("Creazione Treno Business con builder")
    void testCreazioneTrenoBusiness() {
        // Act
        Treno trenoBusiness = director.costruisciTrenoBusiness("BS001");
        System.out.println(trenoBusiness);
        System.out.println(trenoBusiness.getServizi());
        // Assert
        assertNotNull(trenoBusiness);
        assertEquals("BS001", trenoBusiness.getCodice());
        assertEquals(TipoTreno.BUSINESS, trenoBusiness.getTipoTreno());
        assertEquals(250, trenoBusiness.getPostiTotali());
        assertEquals(7, trenoBusiness.getServizi().size());
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.ALTA_VELOCITA));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.RISTORAZIONE));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.WIFI));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.ARIA_CONDIZIONATA));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.PRESE_ELETTRICHE));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.SILENZIOSO));
        assertTrue(trenoBusiness.hasServizio(ServizioTreno.BUSINESS_LOUNGE));
    }

    @Test
    @DisplayName("Verifica differenze tra i 3 tipi di treno")
    void testDifferenzeTipiTreno() {
        // Arrange
        Treno economy = director.costruisciTrenoEconomy("EC001");
        Treno standard = director.costruisciTrenoStandard("ST001");
        Treno business = director.costruisciTrenoBusiness("BS001");

        // Assert - Posti
        assertTrue(economy.getPostiTotali() > standard.getPostiTotali());
        assertTrue(standard.getPostiTotali() > business.getPostiTotali());

        // Assert - Servizi
        assertTrue(economy.getServizi().size() < standard.getServizi().size());
        assertTrue(standard.getServizi().size() < business.getServizi().size());

        // Assert - Servizi specifici
        assertFalse(economy.hasServizio(ServizioTreno.WIFI));
        assertTrue(standard.hasServizio(ServizioTreno.WIFI));
        assertTrue(business.hasServizio(ServizioTreno.WIFI));

        assertFalse(economy.hasServizio(ServizioTreno.RISTORAZIONE));
        assertFalse(standard.hasServizio(ServizioTreno.RISTORAZIONE));
        assertTrue(business.hasServizio(ServizioTreno.RISTORAZIONE));
    }
}