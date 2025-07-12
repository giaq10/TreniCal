package it.trenical.server.db.dao;

import it.trenical.server.cliente.Biglietto;
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
            INSERT INTO biglietti (id, cliente_email, viaggio_id, nominativo, prezzo, data_acquisto) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, biglietto.getId());
            stmt.setString(2, clienteEmail);
            stmt.setString(3, biglietto.getIdViaggio());
            stmt.setString(4, biglietto.getNominativo());
            stmt.setDouble(5, biglietto.getPrezzo());

            Timestamp timestamp = Timestamp.valueOf(biglietto.getDataAcquisto());
            stmt.setTimestamp(6, timestamp);

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
                    SELECT id, nominativo, viaggio_id, data_acquisto, prezzo
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
                        rs.getDouble("prezzo"),
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
                Biglietto biglietto = new Biglietto(viaggio, data.id, data.nominativo, data.prezzo, data.dataAcquisto);
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
        final double prezzo;
        final LocalDateTime dataAcquisto;

        BigliettoDatabase(String id, String nominativo, String viaggioId, double prezzo, LocalDateTime dataAcquisto) {
            this.id = id;
            this.nominativo = nominativo;
            this.viaggioId = viaggioId;
            this.prezzo = prezzo;
            this.dataAcquisto = dataAcquisto;
        }
    }

    public List<String> findEmailClientiByViaggioId(String viaggioId) {
        List<String> emailClienti = new ArrayList<>();

        if (viaggioId == null || viaggioId.trim().isEmpty()) {
            logger.warning("ViaggioId null o vuoto per findEmailClientiByViaggioId");
            return emailClienti;
        }

        String sql = "SELECT DISTINCT cliente_email FROM biglietti WHERE viaggio_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, viaggioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("cliente_email");
                if (email != null && !email.trim().isEmpty()) {
                    emailClienti.add(email);
                }
            }

        } catch (SQLException e) {
            logger.severe("Errore nel recupero email clienti per viaggio " + viaggioId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return emailClienti;
    }

    public boolean updateViaggioIdEPrezzo(String bigliettoId, String nuovoViaggioId, double nuovoPrezzo) {
        String sql = "UPDATE biglietti SET viaggio_id = ?, prezzo = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoViaggioId);
            stmt.setDouble(2, nuovoPrezzo);
            stmt.setString(3, bigliettoId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Biglietto aggiornato: " + bigliettoId +
                        " -> nuovo viaggio: " + nuovoViaggioId +
                        ", nuovo prezzo: €" + nuovoPrezzo);
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