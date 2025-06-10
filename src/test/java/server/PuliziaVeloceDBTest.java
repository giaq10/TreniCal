package server;

import it.trenical.server.db.dao.BigliettoDAO;
import it.trenical.server.db.dao.ClienteDAO;
import it.trenical.server.db.dao.PromozioneDAO;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.db.*;
import java.sql.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PuliziaVeloceDBTest {

    private static ViaggioDAO viaggioDAO;
    private static BigliettoDAO bigliettoDAO;
    private static ClienteDAO clienteDAO;
    private static PromozioneDAO promozioneDAO;

    @BeforeAll
    static void setUpAll() {
        viaggioDAO = new ViaggioDAO();
        bigliettoDAO = new BigliettoDAO();
        clienteDAO = new ClienteDAO();
        promozioneDAO = new PromozioneDAO();
    }

    @Test
    @DisplayName("Pulizia Database Viaggi")
    void testPuliziaViaggi() {

        int viaggiPrima = viaggioDAO.count();
        System.out.println("Viaggi presenti prima della pulizia: " + viaggiPrima);

        if (viaggiPrima == 0) {
            System.out.println("Database viaggi già pulito, nessuna operazione necessaria");
            return;
        }

        String sqlDelete = "DELETE FROM viaggi";
        int viaggiEliminati = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            viaggiEliminati = stmt.executeUpdate(sqlDelete);

            System.out.println("Eliminazione viaggi completata");
            System.out.printf("Viaggi eliminati: %d%n", viaggiEliminati);

            int viaggiDopo = viaggioDAO.count();
            assertEquals(0, viaggiDopo, "Dopo l'eliminazione non dovrebbero esserci viaggi");
            assertEquals(viaggiPrima, viaggiEliminati, "Il numero di viaggi eliminati dovrebbe corrispondere a quelli presenti");

            System.out.printf("Verifica: viaggi rimanenti = %d (atteso: 0)%n", viaggiDopo);

        } catch (SQLException e) {
            fail("Errore durante l'eliminazione dei viaggi: " + e.getMessage());
        }

        System.out.println("PULIZIA VIAGGI COMPLETATA CON SUCCESSO!");
    }

    @Test
    @DisplayName("Pulizia Database Clienti")
    void testPuliziaClienti() {

        int clientiPrima = clienteDAO.count();
        System.out.println("Clienti presenti prima della pulizia: " + clientiPrima);

        if (clientiPrima == 0) {
            System.out.println("Database clienti già pulito, nessuna operazione necessaria");
            return;
        }

        String sqlDelete = "DELETE FROM clienti";
        int clientiEliminati = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            clientiEliminati = stmt.executeUpdate(sqlDelete);

            System.out.println("Eliminazione clienti completata");
            System.out.printf("Clienti eliminati: %d%n", clientiEliminati);

            int clientiDopo = clienteDAO.count();
            assertEquals(0, clientiDopo, "Dopo l'eliminazione non dovrebbero esserci clienti");
            assertEquals(clientiPrima, clientiEliminati, "Il numero di clienti eliminati dovrebbe corrispondere a quelli presenti");

            System.out.printf("✅ Verifica: clienti rimanenti = %d (atteso: 0)%n", clientiDopo);

        } catch (SQLException e) {
            fail("Errore durante l'eliminazione dei clienti: " + e.getMessage());
        }

        System.out.println("PULIZIA CLIENTI COMPLETATA CON SUCCESSO!");
    }

    @Test
    @DisplayName("Pulizia Database Biglietti")
    void testPuliziaBiglietti() {

        int bigliettiPrima = bigliettoDAO.count();
        System.out.println("Biglietti presenti prima della pulizia: " + bigliettiPrima);

        if (bigliettiPrima == 0) {
            System.out.println("Database biglietti già pulito, nessuna operazione necessaria");
            return;
        }

        String sqlDelete = "DELETE FROM biglietti";
        int bigliettiEliminati = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            bigliettiEliminati = stmt.executeUpdate(sqlDelete);

            System.out.println("Eliminazione biglietti completata");
            System.out.printf("Biglietti eliminati: %d%n", bigliettiEliminati);

            int bigliettiDopo = bigliettoDAO.count();
            assertEquals(0, bigliettiDopo, "Dopo l'eliminazione non dovrebbero esserci biglietti");
            assertEquals(bigliettiPrima, bigliettiEliminati, "Il numero di biglietti eliminati dovrebbe corrispondere a quelli presenti");

            System.out.printf("Verifica: biglietti rimanenti = %d (atteso: 0)%n", bigliettiDopo);

        } catch (SQLException e) {
            fail("Errore durante l'eliminazione dei biglietti: " + e.getMessage());
        }

        System.out.println("PULIZIA BIGLIETTI COMPLETATA CON SUCCESSO!");
    }

    @Test
    @DisplayName("Pulizia Database Promozioni")
    void testPuliziaPromozioni() {

        int promozioniPrima = promozioneDAO.count();
        System.out.println("Promozioni presenti prima della pulizia: " + promozioniPrima);

        if (promozioniPrima == 0) {
            System.out.println("Database promozioni già pulito, nessuna operazione necessaria");
            return;
        }

        String sqlDelete = "DELETE FROM promozioni";
        int promozioniEliminate = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            promozioniEliminate = stmt.executeUpdate(sqlDelete);

            System.out.println("Eliminazione promozioni completata");
            System.out.printf("Promozioni eliminate: %d%n", promozioniEliminate);

            int promozioniDopo = promozioneDAO.count();
            assertEquals(0, promozioniDopo, "Dopo l'eliminazione non dovrebbero esserci promozioni");
            assertEquals(promozioniPrima, promozioniEliminate, "Il numero di promozioni eliminate dovrebbe corrispondere a quelle presenti");

            System.out.printf("Verifica: promozioni rimanenti = %d (atteso: 0)%n", promozioniDopo);

        } catch (SQLException e) {
            fail("Errore durante l'eliminazione delle promozioni: " + e.getMessage());
        }

        System.out.println("PULIZIA PROMOZIONI COMPLETATA CON SUCCESSO!");
    }

    @Test
    @DisplayName("Pulizia Completa Tutte le Tabelle")
    void testPuliziaCompleta() {
        int viaggiPrima = viaggioDAO.count();
        int clientiPrima = clienteDAO.count();
        int bigliettiPrima = bigliettoDAO.count();
        int promozioniPrima = promozioneDAO.count();

        int totalePrima = viaggiPrima + clientiPrima + bigliettiPrima + promozioniPrima;

        if (totalePrima == 0) {
            System.out.println("Database completamente pulito");
            return;
        }

        int totaleEliminato = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            if (bigliettiPrima > 0) {
                int biglietti = stmt.executeUpdate("DELETE FROM biglietti");
                totaleEliminato += biglietti;
                System.out.printf("🗑Eliminati %d biglietti%n", biglietti);
            }
            if (viaggiPrima > 0) {
                int viaggi = stmt.executeUpdate("DELETE FROM viaggi");
                totaleEliminato += viaggi;
                System.out.printf("🗑Eliminati %d viaggi%n", viaggi);
            }
            if (clientiPrima > 0) {
                int clienti = stmt.executeUpdate("DELETE FROM clienti");
                totaleEliminato += clienti;
                System.out.printf("🗑Eliminati %d clienti%n", clienti);
            }
            if (promozioniPrima > 0) {
                int promozioni = stmt.executeUpdate("DELETE FROM promozioni");
                totaleEliminato += promozioni;
                System.out.printf("🗑Eliminate %d promozioni%n", promozioni);
            }

            int viaggiDopo = viaggioDAO.count();
            int clientiDopo = clienteDAO.count();
            int bigliettiDopo = bigliettoDAO.count();
            int promozioniDopo = promozioneDAO.count();
            int totaleDopo = viaggiDopo + clientiDopo + bigliettiDopo + promozioniDopo;

            assertEquals(0, totaleDopo, "Dopo la pulizia completa non dovrebbero esserci record");
            assertEquals(totalePrima, totaleEliminato, "Il numero di record eliminati dovrebbe corrispondere a quelli presenti");

        } catch (SQLException e) {
            fail("Errore durante la pulizia completa: " + e.getMessage());
        }

        System.out.println("PULIZIA COMPLETA TERMINATA CON SUCCESSO!");
    }


}