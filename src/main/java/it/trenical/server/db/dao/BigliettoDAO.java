package it.trenical.server.db.dao;

import it.trenical.common.cliente.Biglietto;
import it.trenical.server.viaggi.Viaggio;
import it.trenical.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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

    public List<Biglietto> findByClienteEmail(String clienteEmail) {
        List<Biglietto> biglietti = new ArrayList<>();

        if (clienteEmail == null || clienteEmail.trim().isEmpty()) {
            logger.warning("Email cliente null o vuota per findByClienteEmail");
            return biglietti;
        }

        List<BigliettoDatabase> bigliettiDatabase = new ArrayList<>();

        String sql = """
                    SELECT id, nominativo, viaggio_id, data_acquisto 
                    FROM biglietti 
                    WHERE cliente_email = ? 
                    """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clienteEmail);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BigliettoDatabase data = new BigliettoDatabase(
                        rs.getString("id"),
                        rs.getString("nominativo"),
                        rs.getString("viaggio_id"),
                        rs.getTimestamp("data_acquisto").toLocalDateTime()
                );
                bigliettiDatabase.add(data);
            }

        } catch (SQLException e) {
            logger.severe("Errore sql " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        for (BigliettoDatabase data : bigliettiDatabase) {
            try {
                Optional<Viaggio> viaggioOpt = viaggioDAO.findById(data.viaggioId);

                Viaggio viaggio = viaggioOpt.get();
                Biglietto biglietto = new Biglietto(viaggio, data.id, data.nominativo, data.dataAcquisto);
                biglietti.add(biglietto);

            } catch (Exception e) {
                logger.severe("Errore ricostruzione" + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("Trovati " + biglietti.size() + " biglietti per cliente " + clienteEmail);
        return biglietti;
    }

    private static class BigliettoDatabase {
        final String id;
        final String nominativo;
        final String viaggioId;
        final LocalDateTime dataAcquisto;

        BigliettoDatabase(String id, String nominativo, String viaggioId, LocalDateTime dataAcquisto) {
            this.id = id;
            this.nominativo = nominativo;
            this.viaggioId = viaggioId;
            this.dataAcquisto = dataAcquisto;
        }
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

    public boolean updateViaggioId(String bigliettoId, String nuovoViaggioId) {
        String sql = "UPDATE biglietti SET viaggio_id = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoViaggioId);
            stmt.setString(2, bigliettoId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Biglietto aggiornato: " + bigliettoId + " -> nuovo viaggio: " + nuovoViaggioId);
                return true;
            } else {
                logger.warning("Nessun biglietto trovato con ID: " + bigliettoId);
                return false;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento biglietto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}