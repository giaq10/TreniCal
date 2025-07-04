package it.trenical.server.db.dao;

import it.trenical.server.promozioni.Promozione;
import it.trenical.server.promozioni.PromozioneStandard;
import it.trenical.server.promozioni.PromozioneFedelta;
import it.trenical.server.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PromozioneDAO {
    private static final Logger logger = Logger.getLogger(PromozioneDAO.class.getName());
    private final DatabaseManager dbManager;

    public PromozioneDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public boolean save(Promozione promozione) {
        String sql = """
            INSERT INTO promozioni (id, nome, tipo, percentuale_sconto) 
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promozione.getId());
            stmt.setString(2, promozione.getNome());
            stmt.setString(3, promozione.getTipo());
            stmt.setDouble(4, promozione.getSconto());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Promozione salvata: " + promozione.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nel salvare promozione: " + e.getMessage());
        }

        return false;
    }

    public Optional<Promozione> findById(String id) {
        String sql = "SELECT * FROM promozioni WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Promozione promozione = mapResultSetToPromozione(rs);
                logger.info("Promozione trovata: " + id);
                return Optional.of(promozione);
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca promozione: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Promozione> findAll() {
        List<Promozione> promozioni = new ArrayList<>();
        String sql = "SELECT * FROM promozioni ";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                promozioni.add(mapResultSetToPromozione(rs));
            }

            logger.info("Trovate " + promozioni.size() + " promozioni");

        } catch (SQLException e) {
            logger.severe("Errore nel recupero promozioni: " + e.getMessage());
        }

        return promozioni;
    }

    public List<Promozione> findByTipo(String tipo) {
        List<Promozione> promozioni = new ArrayList<>();
        String sql = "SELECT * FROM promozioni WHERE tipo = ? ORDER BY percentuale_sconto DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                promozioni.add(mapResultSetToPromozione(rs));
            }

            logger.info("Trovate " + promozioni.size() + " promozioni " + tipo);

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca per tipo: " + e.getMessage());
        }

        return promozioni;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM promozioni WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Promozione eliminata: " + id);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione promozione: " + e.getMessage());
        }

        return false;
    }

    private Promozione mapResultSetToPromozione(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nome = rs.getString("nome");
        String tipo = rs.getString("tipo");
        double sconto = rs.getDouble("percentuale_sconto");

        Promozione promozione;
        if ("Standard".equals(tipo)) {
            promozione = new PromozioneStandard(id, nome, sconto);
        } else if ("Fedelta".equals(tipo)) {
            promozione = new PromozioneFedelta(id, nome, sconto);
        } else {
            throw new SQLException("Tipo promozione non riconosciuto: " + tipo);
        }
        return promozione;
    }
}