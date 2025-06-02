package it.trenical.server.db.dao;

import it.trenical.common.cliente.Cliente;
import it.trenical.server.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO per la gestione dei clienti nel database
 */
public class ClienteDAO {
    private static final Logger logger = Logger.getLogger(ClienteDAO.class.getName());
    private final DatabaseManager dbManager;

    public ClienteDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Salva un nuovo cliente nel database
     * @param cliente Cliente da salvare
     * @return true se salvato con successo
     */
    public boolean save(Cliente cliente) {
        String sql = """
            INSERT INTO clienti (email, nome, abbonamento_fedelta) 
            VALUES (?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getEmail());
            stmt.setString(2, cliente.getNome());
            stmt.setBoolean(3, cliente.hasAbbonamentoFedelta());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Cliente salvato: " + cliente.getEmail());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nel salvare cliente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Trova un cliente per email
     * @param email Email del cliente
     * @return Optional con il cliente se trovato
     */
    public Optional<Cliente> findByEmail(String email) {
        String sql = "SELECT * FROM clienti WHERE email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                logger.info("Cliente trovato: " + email);
                return Optional.of(cliente);
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca cliente: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Ottiene tutti i clienti
     * @return Lista di tutti i clienti
     */
    public List<Cliente> findAll() {
        List<Cliente> clienti = new ArrayList<>();
        String sql = "SELECT * FROM clienti ORDER BY nome";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clienti.add(mapResultSetToCliente(rs));
            }

            logger.info("Trovati " + clienti.size() + " clienti");

        } catch (SQLException e) {
            logger.severe("Errore nel recupero clienti: " + e.getMessage());
        }

        return clienti;
    }

    /**
     * Aggiorna un cliente esistente
     * @param cliente Cliente con dati aggiornati
     * @return true se aggiornato con successo
     */
    public boolean update(Cliente cliente) {
        String sql = """
            UPDATE clienti 
            SET nome = ?, abbonamento_fedelta = ? 
            WHERE email = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNome());
            stmt.setBoolean(2, cliente.hasAbbonamentoFedelta());
            stmt.setString(3, cliente.getEmail());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Cliente aggiornato: " + cliente.getEmail());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento cliente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina un cliente
     * @param email Email del cliente da eliminare
     * @return true se eliminato con successo
     */
    public boolean delete(String email) {
        String sql = "DELETE FROM clienti WHERE email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Cliente eliminato: " + email);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione cliente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Trova tutti i clienti con abbonamento fedeltà
     * @return Lista clienti fedeltà
     */
    public List<Cliente> findClientiFedelta() {
        List<Cliente> clienti = new ArrayList<>();
        String sql = "SELECT * FROM clienti WHERE abbonamento_fedelta = 1 ORDER BY nome";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clienti.add(mapResultSetToCliente(rs));
            }

            logger.info("Trovati " + clienti.size() + " clienti fedeltà");

        } catch (SQLException e) {
            logger.severe("Errore nel recupero clienti fedeltà: " + e.getMessage());
        }

        return clienti;
    }

    /**
     * Verifica se esiste un cliente con la email specificata
     * @param email Email da verificare
     * @return true se esiste
     */
    public boolean exists(String email) {
        String sql = "SELECT COUNT(*) FROM clienti WHERE email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.severe("Errore nella verifica esistenza cliente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Conta il numero totale di clienti
     * @return Numero di clienti
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM clienti";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Errore nel conteggio clienti: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mappa un ResultSet a un oggetto Cliente
     * @param rs ResultSet da mappare
     * @return Cliente mappato
     * @throws SQLException se errore SQL
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        String email = rs.getString("email");
        String nome = rs.getString("nome");
        boolean abbonamentoFedelta = rs.getBoolean("abbonamento_fedelta");

        return new Cliente(email, nome, abbonamentoFedelta);
    }
}