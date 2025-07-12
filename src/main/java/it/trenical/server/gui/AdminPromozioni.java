package it.trenical.server.gui;

import it.trenical.server.cliente.Cliente;
import it.trenical.server.db.dao.ClienteDAO;
import it.trenical.server.promozioni.Promozione;
import it.trenical.server.promozioni.factoryMethod.PromozioneFactory;
import it.trenical.server.db.DatabaseManager;
import it.trenical.server.db.dao.PromozioneDAO;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.grpc.TrenicalServiceImpl;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class AdminPromozioni {

    private static final Logger logger = Logger.getLogger(AdminPromozioni.class.getName());

    private final PromozioneDAO promozioneDAO;
    private final ViaggioDAO viaggioDAO;
    private final ClienteDAO clienteDAO;
    private final ServerAdminApp gui;
    private final TrenicalServiceImpl trenicalService;

    public AdminPromozioni(PromozioneDAO promozioneDAO, ViaggioDAO viaggioDAO, ServerAdminApp gui) {
        this.promozioneDAO = promozioneDAO;
        this.viaggioDAO = viaggioDAO;
        this.clienteDAO = new ClienteDAO();
        this.gui = gui;
        this.trenicalService = new TrenicalServiceImpl();

        logger.info("AdminPromozioni inizializzato");
    }

    public void creaPromozione(String nome, String tipo, double percentualeSconto) {
        logger.info("Crea promozione " + nome);
        try {
            if (tipo == null) {
                gui.mostraErrore("Errore Input", "Tipo promozione deve essere 'Standard' o 'Fedelta'");
                return;
            }

            List<Promozione> promozioniEsistenti = promozioneDAO.findAll();
            for (Promozione p : promozioniEsistenti) {
                if (p.getNome().equalsIgnoreCase(nome.trim())) {
                    gui.mostraErrore("Promozione Già Esistente",
                            "Esiste già una promozione con il nome: " + nome);
                    return;
                }
            }

            PromozioneFactory factory = PromozioneFactory.getFactory(tipo.toLowerCase());
            Promozione nuovaPromozione = factory.creaPromozione(nome.trim(), percentualeSconto);

            boolean salvata = promozioneDAO.save(nuovaPromozione);
            if (salvata) {
                String messaggio = String.format(
                        "ID: %s\n" +
                        "Nome: %s\n" +
                        "Tipo: %s\n" +
                        "Sconto: %.1f%%\n\n",
                        nuovaPromozione.getId(),
                        nuovaPromozione.getNome(),
                        nuovaPromozione.getTipo(),
                        nuovaPromozione.getSconto()
                );

                if ("Fedelta".equals(nuovaPromozione.getTipo())) {
                    notificaClientiPromozioneFedelta(nuovaPromozione);
                }

                gui.mostraSuccesso("Promozione Creata", messaggio);
                logger.info("Promozione creata: " + nuovaPromozione);
            }

        } catch (Exception e) {
            logger.severe("Errore creazione promozione: " + e.getMessage());
            gui.mostraErrore("Errore Sistema",
                    "Errore durante la creazione della promozione: " + e.getMessage());
        }
    }

    private void notificaClientiPromozioneFedelta(Promozione promozione) {
        try {
            logger.info("Invio notifiche per nuova promozione fedeltà: " + promozione.getNome());

            List<Cliente> clientiFedelta = clienteDAO.findClientiFedeltaConNotificheAttive();
            if (clientiFedelta.isEmpty()) {
                logger.info("Nessun cliente fedeltà con notifiche attive trovato");
                return;
            }

            String messaggioNotifica = String.format(
                    "Nuova promozione per gli abbonati! '%s' - Sconto del %.1f%%.",
                    promozione.getNome(),
                    promozione.getSconto()
            );

            for (Cliente cliente : clientiFedelta) {
                String notificaCompleta = cliente.getEmail() + "|" + messaggioNotifica;
                AdminViaggi.aggiungiNotificaStatica(notificaCompleta);
            }

            logger.info("Notifiche promozione inviate a " + clientiFedelta.size() +
                    " clienti fedeltà con notifiche attive");

        } catch (Exception e) {
            logger.severe("Errore nell'invio notifiche promozione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void eliminaPromozione(String promozioneId) {
        logger.info("Elimina promozione " + promozioneId);

        try {
            Optional<Promozione> promozioneOpt = promozioneDAO.findById(promozioneId);
            if (promozioneOpt.isEmpty()) {
                gui.mostraErrore("Promozione Non Trovata",
                        "Nessuna promozione trovata con ID: " + promozioneId);
                return;
            }

            Promozione promozione = promozioneOpt.get();
            boolean eliminata = promozioneDAO.delete(promozioneId);
            if (eliminata) {
                String messaggio = String.format(
                        "Promozione eliminata %s", promozione.getNome()
                );
                gui.mostraSuccesso("Promozione Eliminata", messaggio);
                logger.info("Promozione eliminata definitivamente: " + promozione.getNome());
            }
        } catch (Exception e) {
            logger.severe("Errore eliminazione promozione: " + e.getMessage());
            gui.mostraErrore("Errore Sistema",
                    "Errore durante l'eliminazione della promozione: " + e.getMessage());
        }
    }

    public void eliminaTutteLePromozioni() {
        logger.warning("Eliminazione di TUTTE le promozioni dal database");

        try {
            String countSql = "SELECT COUNT(*) FROM promozioni";
            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement countStmt = conn.createStatement();
            ResultSet rs = countStmt.executeQuery(countSql);

            int promozioniTotali = 0;
            if (rs.next()) {
                promozioniTotali = rs.getInt(1);
            }
            countStmt.close();

            if (promozioniTotali == 0) {
                gui.mostraSuccesso("Database Già Pulito", "Il database non contiene promozioni da eliminare.");
                return;
            }

            String deleteSql = "DELETE FROM promozioni";
            Statement deleteStmt = conn.createStatement();
            int promozioniEliminate = deleteStmt.executeUpdate(deleteSql);
            deleteStmt.close();

            String messaggio = String.format("Promozioni eliminate: %,d\n", promozioniEliminate);

            gui.mostraSuccesso("Eliminazione Totale Completata", messaggio);
            logger.warning("Eliminate tutte le " + promozioniEliminate + " promozioni dal database");

        } catch (Exception e) {
            String errorMsg = "Errore durante l'eliminazione totale: " + e.getMessage();
            logger.severe(errorMsg);
            gui.mostraErrore("Errore Eliminazione Totale", errorMsg);
        }
    }
}