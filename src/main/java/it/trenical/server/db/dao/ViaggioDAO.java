package it.trenical.server.db.dao;

import it.trenical.server.stazioni.Stazione;
import it.trenical.server.stazioni.Binario;
import it.trenical.server.viaggi.StatoViaggio;
import it.trenical.server.viaggi.Viaggio;
import it.trenical.server.treni.TipoTreno;
import it.trenical.server.treni.Treno;
import it.trenical.server.treni.builder.TrenoDirector;
import it.trenical.server.tratte.Tratta;
import it.trenical.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO per la gestione dei viaggi nel database
 * Gestisce la persistenza dei viaggi con tutte le loro proprietà
 */
public class ViaggioDAO {
    private static final Logger logger = Logger.getLogger(ViaggioDAO.class.getName());
    private final DatabaseManager dbManager;
    private final TrenoDirector trenoDirector;

    public ViaggioDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.trenoDirector = new TrenoDirector();
    }

    /**
     * Salva un viaggio nel database
     * @param viaggio Viaggio da salvare
     * @return true se salvato con successo
     */
    public boolean save(Viaggio viaggio) {
        String sql = """
        INSERT INTO viaggi (
            id, codice_treno, tipo_treno, stazione_partenza, stazione_arrivo,
            data_viaggio, orario_partenza, orario_arrivo, data_arrivo,
            prezzo, durata_minuti, posti_totali, posti_disponibili,
            stato, binario_partenza, ritardo_minuti, distanza_km
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, viaggio.getId());
            stmt.setString(2, viaggio.getTreno().getCodice());
            stmt.setString(3, viaggio.getTreno().getTipoTreno().name());
            stmt.setString(4, viaggio.getTratta().getStazionePartenza().getNome());
            stmt.setString(5, viaggio.getTratta().getStazioneArrivo().getNome());
            stmt.setString(6, viaggio.getDataViaggio().toString());
            stmt.setString(7, viaggio.getOrarioPartenza().toString());
            stmt.setString(8, viaggio.getOrarioArrivo().toString());
            stmt.setString(9, viaggio.getDataArrivo().toString());
            stmt.setDouble(10, viaggio.getPrezzo());
            stmt.setInt(11, viaggio.getDurataMinuti());
            stmt.setInt(12, viaggio.getTreno().getPostiTotali());
            stmt.setInt(13, viaggio.getPostiDisponibili());
            stmt.setString(14, viaggio.getStato().name());
            stmt.setString(15, "Binario " + viaggio.getBinarioPartenza().getNumero());
            stmt.setInt(16, viaggio.getRitardoMinuti());
            stmt.setInt(17, viaggio.getTratta().getDistanzaKm());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Viaggio salvato: " + viaggio.getId());
                return true;
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("chiave UNIQUE fallita")) {
                String errorMsg = e.getMessage().toLowerCase();
                if (errorMsg.contains("stazione_partenza") ||
                        errorMsg.contains("data_viaggio") ||
                        errorMsg.contains("orario_partenza") ||
                        errorMsg.contains("binario_partenza")
                ) {
                    logger.warning("Il binario " +
                            viaggio.getBinarioPartenza() + " è già occupato da " +
                            viaggio.getDataViaggio() + " alle " + viaggio.getOrarioPartenza() +
                            " dalla stazione " + viaggio.getTratta().getStazionePartenza().getNome());

                } else if (errorMsg.contains("codice_treno")) {
                    logger.warning("Il treno " +
                            viaggio.getTreno().getCodice() + " è già in viaggio il " +
                            viaggio.getDataViaggio() + " alle " + viaggio.getOrarioPartenza());
                } else {
                    logger.warning("CONFLITTO GENERICO " + e.getMessage());
                }
                return false;
            } else {
                logger.severe("Errore nel salvare viaggio: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public Optional<Viaggio> findById(String id) {
        String sql = "SELECT * FROM viaggi WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Viaggio viaggio = mapResultSetToViaggio(rs);
                logger.info("Viaggio trovato: " + id);
                return Optional.of(viaggio);
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca viaggio: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Viaggio> findByTrattaEData(Stazione partenza, Stazione arrivo, LocalDate data) {
        List<Viaggio> viaggi = new ArrayList<>();
        String sql = """
            SELECT * FROM viaggi 
            WHERE stazione_partenza = ? AND stazione_arrivo = ? AND data_viaggio = ?
            ORDER BY orario_partenza
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, partenza.getNome());
            stmt.setString(2, arrivo.getNome());
            stmt.setString(3, data.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                viaggi.add(mapResultSetToViaggio(rs));
            }

            logger.info("Trovati " + viaggi.size() + " viaggi per " +
                    partenza.getNome() + " → " + arrivo.getNome() + " del " + data);

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca viaggi per tratta: " + e.getMessage());
        }

        return viaggi;
    }

    public List<Viaggio> findByTratta(Stazione partenza, Stazione arrivo) {
        List<Viaggio> viaggi = new ArrayList<>();
        String sql = """
            SELECT * FROM viaggi 
            WHERE stazione_partenza = ? AND stazione_arrivo = ? 
            ORDER BY data_viaggio, orario_partenza
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, partenza.getNome());
            stmt.setString(2, arrivo.getNome());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                viaggi.add(mapResultSetToViaggio(rs));
            }

            logger.info("Trovati " + viaggi.size() + " viaggi per " +
                    partenza.getNome() + " => " + arrivo.getNome());

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca viaggi per tratta: " + e.getMessage());
        }

        return viaggi;
    }

    public List<Viaggio> findByData(LocalDate data){
        List<Viaggio> viaggi = new ArrayList<>();
        String sql = """
                SELECT * FROM viaggi 
                WHERE data_viaggio = ? 
                """;

        try{
            Connection connection = dbManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setString(1, data.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                viaggi.add(mapResultSetToViaggio(rs));
            }

            logger.info("Trovati " + viaggi.size() + " viaggi in partenza il "+data);

        }catch (SQLException e) {
            logger.severe("Errore nella ricerca viaggi per data: " + e.getMessage());
        }
        return viaggi;
    }

    public boolean updatePostiDisponibili(String viaggioId, int nuoviPostiDisponibili) {
        String sql = "UPDATE viaggi SET posti_disponibili = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuoviPostiDisponibili);
            stmt.setString(2, viaggioId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Posti aggiornati per viaggio " + viaggioId + ": " + nuoviPostiDisponibili);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento posti: " + e.getMessage());
        }

        return false;
    }

    public boolean updateViaggioCompleto(Viaggio viaggio) {
        String sql = """
                    UPDATE viaggi SET 
                        prezzo = ?,
                        posti_disponibili = ?,
                        stato = ?,
                        ritardo_minuti = ?,
                        motivo_cancellazione = ?,
                        binario_partenza = ?,
                        orario_partenza = ?,
                        orario_arrivo = ?,
                        data_arrivo = ?
                    WHERE id = ?
                    """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, viaggio.getPrezzo());
            stmt.setInt(2, viaggio.getPostiDisponibili());
            stmt.setString(3, viaggio.getStato().name());
            stmt.setInt(4, viaggio.getRitardoMinuti());
            stmt.setString(5, viaggio.getMotivoCancellazione());
            stmt.setString(6, "Binario " + viaggio.getBinarioPartenza().getNumero());
            stmt.setString(7, viaggio.getOrarioPartenzaEffettivo().toString());
            stmt.setString(8, viaggio.getOrarioArrivoEffettivo().toString());
            stmt.setString(9, viaggio.getDataArrivoEffettiva().toString());
            stmt.setString(10, viaggio.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Viaggio aggiornato completamente: " + viaggio.getId() +
                        " (Ritardo: " + viaggio.getRitardoMinuti() + " min, " +
                        "Partenza: " + viaggio.getOrarioPartenzaEffettivo() + ", " +
                        "Arrivo: " + viaggio.getOrarioArrivoEffettivo() + ")");
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento completo viaggio: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Trova tutti i viaggi disponibili (con posti e non cancellati)
     * @return Lista viaggi disponibili
     */
    public List<Viaggio> findViaggiDisponibili() {
        List<Viaggio> viaggi = new ArrayList<>();
        String sql = """
            SELECT * FROM viaggi 
            WHERE posti_disponibili > 0 AND stato != 'CANCELLATO'
            """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                viaggi.add(mapResultSetToViaggio(rs));
            }

            logger.info("Trovati " + viaggi.size() + " viaggi disponibili");

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca viaggi disponibili: " + e.getMessage());
        }

        return viaggi;
    }

    /**
     * Elimina un viaggio
     * @param viaggioId ID del viaggio da eliminare
     * @return true se eliminato
     */
    public boolean delete(String viaggioId) {
        String sql = "DELETE FROM viaggi WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, viaggioId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Viaggio eliminato: " + viaggioId);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione viaggio: " + e.getMessage());
        }

        return false;
    }

    /**
     * Conta il numero totale di viaggi
     * @return Numero di viaggi
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM viaggi";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Errore nel conteggio viaggi: " + e.getMessage());
        }

        return 0;
    }

    private Viaggio mapResultSetToViaggio(ResultSet rs) throws SQLException {
        try {
            String idDatabase = rs.getString("id");
            String codiceTreno = rs.getString("codice_treno");
            TipoTreno tipoTreno = TipoTreno.valueOf(rs.getString("tipo_treno"));
            String nomePartenza = rs.getString("stazione_partenza");
            String nomeArrivo = rs.getString("stazione_arrivo");
            LocalDate dataViaggio = LocalDate.parse(rs.getString("data_viaggio"));
            LocalTime orarioPartenza = LocalTime.parse(rs.getString("orario_partenza"));
            LocalTime orarioArrivo = LocalTime.parse(rs.getString("orario_arrivo"));
            LocalDate dataArrivo = LocalDate.parse(rs.getString("data_arrivo"));
            double prezzo = rs.getDouble("prezzo");
            int durataMinuti = rs.getInt("durata_minuti");
            int postiTotali = rs.getInt("posti_totali");
            int postiDisponibili = rs.getInt("posti_disponibili");
            StatoViaggio stato = StatoViaggio.valueOf(rs.getString("stato"));
            String binarioStr = rs.getString("binario_partenza");
            int ritardoMinuti = rs.getInt("ritardo_minuti");
            String motivoCancellazione = rs.getString("motivo_cancellazione");

            Stazione stazionePartenza = Stazione.fromNome(nomePartenza);
            Stazione stazioneArrivo = Stazione.fromNome(nomeArrivo);
            Tratta tratta = new Tratta(stazionePartenza, stazioneArrivo);
            Treno treno = trenoDirector.costruisciTrenoPerTipo(tipoTreno, codiceTreno);

            int numeroBinario = Integer.parseInt(binarioStr.replace("Binario ", ""));
            Binario binario = Binario.values()[numeroBinario - 1];

            Viaggio viaggio = new Viaggio(
                    idDatabase, treno, tratta, dataViaggio,
                    orarioPartenza, orarioArrivo, dataArrivo,
                    prezzo, durataMinuti, postiDisponibili,
                    stato, binario, motivoCancellazione
            );

            return viaggio;

        } catch (Exception e) {
            logger.severe("Errore nel mapping viaggio: " + e.getMessage());
            throw new SQLException("Errore nella ricostruzione viaggio dal database", e);
        }
    }
}