package it.trenical.server.db.dao;

import it.trenical.common.cliente.Biglietto;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO per la gestione dei biglietti nel database
 * Gestisce la relazione tra clienti, viaggi e biglietti
 */
public class BigliettoDAO {
    private static final Logger logger = Logger.getLogger(BigliettoDAO.class.getName());
    private final DatabaseManager dbManager;
    private final ViaggioDAO viaggioDAO;

    public BigliettoDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.viaggioDAO = new ViaggioDAO();
    }

    public BigliettoDAO(ViaggioDAO viaggioDAO) {
        this.dbManager = DatabaseManager.getInstance();
        this.viaggioDAO = viaggioDAO;
    }

    /**
     * Salva un biglietto nel database
     * @param biglietto Biglietto da salvare
     * @param clienteEmail Email del cliente proprietario
     * @return true se salvato con successo
     */
    public boolean save(Biglietto biglietto, String clienteEmail) {
        if (!biglietto.isCompleto()) {
            logger.warning("Tentativo di salvare biglietto incompleto: " + biglietto.getId());
            return false;
        }

        String sql = """
            INSERT INTO biglietti (id, cliente_email, viaggio_id, nominativo, data_acquisto) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, biglietto.getId());
            stmt.setString(2, clienteEmail);
            stmt.setString(3, biglietto.getIdViaggio());
            stmt.setString(4, biglietto.getNominativo());

            // Converti LocalDateTime a Timestamp per SQLite
            Timestamp timestamp = Timestamp.valueOf(biglietto.getDataAcquisto());
            stmt.setTimestamp(5, timestamp);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Biglietto salvato: " + biglietto.getId() + " per cliente " + clienteEmail);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nel salvare biglietto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Salva una lista di biglietti per lo stesso cliente
     * @param biglietti Lista biglietti da salvare
     * @param clienteEmail Email del cliente
     * @return numero di biglietti salvati con successo
     */
    public int saveAll(List<Biglietto> biglietti, String clienteEmail) {
        int salvatiConSuccesso = 0;

        for (Biglietto biglietto : biglietti) {
            if (save(biglietto, clienteEmail)) {
                salvatiConSuccesso++;
            }
        }

        logger.info("Salvati " + salvatiConSuccesso + "/" + biglietti.size() +
                " biglietti per cliente " + clienteEmail);
        return salvatiConSuccesso;
    }

    /**
     * Trova tutti i biglietti di un cliente
     * @param clienteEmail Email del cliente
     * @return Lista biglietti del cliente
     */
    public List<Biglietto> findByClienteEmail(String clienteEmail) {
        List<Biglietto> biglietti = new ArrayList<>();

        if (clienteEmail == null || clienteEmail.trim().isEmpty()) {
            logger.warning("Email cliente null o vuota");
            return biglietti;
        }

        // STEP 1: Raccogli tutti gli ID e dati dei biglietti PRIMA
        List<String> bigliettiIds = new ArrayList<>();
        List<String> nominativi = new ArrayList<>();
        List<String> viaggiIds = new ArrayList<>();
        List<LocalDateTime> dateAcquisto = new ArrayList<>();

        String sql = """
        SELECT id, viaggio_id, nominativo, data_acquisto
        FROM biglietti
        WHERE cliente_email = ?
        ORDER BY data_acquisto DESC
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clienteEmail);
            ResultSet rs = stmt.executeQuery();

            logger.info("Eseguendo query per cliente: " + clienteEmail);

            while (rs.next()) {
                bigliettiIds.add(rs.getString("id"));
                nominativi.add(rs.getString("nominativo"));
                viaggiIds.add(rs.getString("viaggio_id"));
                dateAcquisto.add(rs.getTimestamp("data_acquisto").toLocalDateTime());
            }

            logger.info("Raccolti " + bigliettiIds.size() + " biglietti per cliente " + clienteEmail);

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca biglietti per cliente: " + e.getMessage());
            e.printStackTrace();
            return biglietti;
        }

        for (int i = 0; i < bigliettiIds.size(); i++) {
            String bigliettoId = bigliettiIds.get(i);
            String nominativo = nominativi.get(i);
            String viaggioId = viaggiIds.get(i);
            LocalDateTime dataAcquisto = dateAcquisto.get(i);

            logger.info("Mappando biglietto " + (i+1) + "/" + bigliettiIds.size() + ": " + bigliettoId);

            try {
                Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
                if (viaggioOpt.isEmpty()) {
                    logger.warning("Viaggio non trovato per ID: " + viaggioId + " (biglietto " + bigliettoId + ")");
                    continue;
                }

                Viaggio viaggio = viaggioOpt.get();
                Biglietto biglietto = new Biglietto(viaggio);

                biglietto.setNominativo(nominativo);

                java.lang.reflect.Field idField = Biglietto.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(biglietto, bigliettoId);

                java.lang.reflect.Field dataField = Biglietto.class.getDeclaredField("dataAcquisto");
                dataField.setAccessible(true);
                dataField.set(biglietto, dataAcquisto);

                biglietti.add(biglietto);
                logger.info("Biglietto mappato: " + biglietto.toString());

            } catch (Exception e) {
                logger.severe("Errore nel mapping biglietto " + bigliettoId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("Trovati " + biglietti.size() + " biglietti per cliente " + clienteEmail);
        return biglietti;
    }

    public int deleteByClienteEmail(String clienteEmail) {
        String sql = "DELETE FROM biglietti WHERE cliente_email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clienteEmail);
            int rowsAffected = stmt.executeUpdate();

            logger.info("Eliminati " + rowsAffected + " biglietti per cliente " + clienteEmail);
            return rowsAffected;

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione biglietti cliente: " + e.getMessage());
        }

        return 0;
    }

    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM biglietti WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.severe("Errore nella verifica esistenza biglietto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Conta il numero totale di biglietti
     * @return Numero di biglietti
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM biglietti";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Errore nel conteggio biglietti: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Conta i biglietti di un cliente
     * @param clienteEmail Email del cliente
     * @return Numero di biglietti del cliente
     */
    public int countByClienteEmail(String clienteEmail) {
        String sql = "SELECT COUNT(*) FROM biglietti WHERE cliente_email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clienteEmail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Errore nel conteggio biglietti cliente: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Ottiene il cliente proprietario di un biglietto
     * @param bigliettoId ID del biglietto
     * @return Email del cliente proprietario
     */
    public Optional<String> getClienteProprietario(String bigliettoId) {
        String sql = "SELECT cliente_email FROM biglietti WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bigliettoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("cliente_email"));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel recupero cliente proprietario: " + e.getMessage());
        }

        return Optional.empty();
    }
}