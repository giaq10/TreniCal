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

/**
 * DAO per la gestione delle promozioni nel database
 */
public class PromozioneDAO {
    private static final Logger logger = Logger.getLogger(PromozioneDAO.class.getName());
    private final DatabaseManager dbManager;

    public PromozioneDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Salva una promozione nel database
     * @param promozione Promozione da salvare
     * @return true se salvata con successo
     */
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

    /**
     * Trova una promozione per ID
     * @param id ID della promozione
     * @return Optional con la promozione se trovata
     */
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

    /**
     * Ottiene tutte le promozioni
     * @return Lista di tutte le promozioni
     */
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

    /**
     * Trova promozioni per tipo
     * @param tipo Tipo promozione ("Standard" o "Fedelta")
     * @return Lista promozioni del tipo specificato
     */
    public List<Promozione> findByTipo(String tipo) {
        List<Promozione> promozioni = new ArrayList<>();
        String sql = "SELECT * FROM promozioni WHERE tipo = ? AND attiva = 1 ORDER BY percentuale_sconto DESC";

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

    /**
     * Trova promozioni Standard attive
     * @return Lista promozioni Standard attive
     */
    public List<Promozione> findPromozioniStandardAttive() {
        return findByTipo("Standard");
    }

    /**
     * Trova promozioni Fedeltà attive
     * @return Lista promozioni Fedeltà attive
     */
    public List<Promozione> findPromozioniFedeltaAttive() {
        return findByTipo("Fedelta");
    }

    /**
     * Aggiorna una promozione esistente
     * @param promozione Promozione con dati aggiornati
     * @return true se aggiornata con successo
     */
    public boolean update(Promozione promozione) {
        String sql = """
            UPDATE promozioni 
            SET nome = ?, percentuale_sconto = ?
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promozione.getNome());
            stmt.setDouble(2, promozione.getSconto());
            stmt.setString(3, promozione.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Promozione aggiornata: " + promozione.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento promozione: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina definitivamente una promozione dal database
     * @param id ID della promozione da eliminare
     * @return true se eliminata con successo
     */
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

    /**
     * Verifica se esiste una promozione con l'ID specificato
     * @param id ID da verificare
     * @return true se esiste
     */
    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM promozioni WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.severe("Errore nella verifica esistenza promozione: " + e.getMessage());
        }

        return false;
    }

    /**
     * Conta il numero totale di promozioni
     * @return Numero di promozioni
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM promozioni";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Errore nel conteggio promozioni: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mappa un ResultSet a un oggetto Promozione
     * @param rs ResultSet da mappare
     * @return Promozione mappata
     * @throws SQLException se errore SQL
     */
    private Promozione mapResultSetToPromozione(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nome = rs.getString("nome");
        String tipo = rs.getString("tipo");
        double sconto = rs.getDouble("percentuale_sconto");

        // Crea il tipo corretto di promozione
        Promozione promozione;
        if ("Standard".equals(tipo)) {
            promozione = new PromozioneStandard(nome, sconto);
        } else if ("Fedelta".equals(tipo)) {
            promozione = new PromozioneFedelta(nome, sconto);
        } else {
            throw new SQLException("Tipo promozione non riconosciuto: " + tipo);
        }

        // Usa reflection per impostare l'ID dal database (se necessario)
        try {
            java.lang.reflect.Field idField = Promozione.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(promozione, id);
        } catch (Exception e) {
            logger.warning("Impossibile impostare ID via reflection: " + e.getMessage());
        }

        return promozione;
    }
}