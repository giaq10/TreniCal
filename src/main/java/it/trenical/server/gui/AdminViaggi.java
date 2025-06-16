package it.trenical.server.gui;

import it.trenical.common.viaggi.StatoViaggio;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.server.db.DatabaseManager;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.tratte.*;
import it.trenical.server.treni.*;
import it.trenical.server.treni.builder.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class AdminViaggi {

    private static final Logger logger = Logger.getLogger(AdminViaggi.class.getName());

    private final ViaggioDAO viaggioDAO;
    private final ServerAdminApp gui;

    public AdminViaggi(ViaggioDAO viaggioDAO, ServerAdminApp gui) {
        this.viaggioDAO = viaggioDAO;
        this.gui = gui;

        logger.info("AdminViaggi inizializzato");
    }

    public void impostaRitardo(String viaggioId, int minuti) {
        logger.info("Imposta ritardo viaggio " + viaggioId);
        try {
            Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
            if (viaggioOpt.isEmpty()) {
                gui.mostraErrore("Viaggio Non Trovato",
                        "Nessun viaggio trovato con ID: " + viaggioId);
                return;
            }

            Viaggio viaggio = viaggioOpt.get();
            if (viaggio.getStato() != StatoViaggio.PROGRAMMATO && viaggio.getStato() != StatoViaggio.RITARDO) {
                gui.mostraErrore("Operazione Non Consentita",
                        "Stato del Viaggio illegale");
                return;
            }
            if (viaggio.getDataViaggio().isBefore(LocalDate.now())) {
                gui.mostraErrore("Operazione Non Consentita",
                        "Impossibile modificare un viaggio del passato");
                return;
            }

            viaggio.impostaRitardo(minuti);
            viaggio.aggiornaStato(StatoViaggio.RITARDO);
            boolean aggiornato = viaggioDAO.updateViaggioCompleto(viaggio);
            if(aggiornato) {
                String messaggio = String.format(
                        "Ritardo impostato per il viaggio %s. \n Clienti notificati automaticamente: %d",
                        viaggioId,
                        viaggio.getObserverCount()
                );
                gui.mostraSuccesso("Ritardo Impostato", messaggio);
                logger.info(String.format("Ritardo viaggio %s:", viaggioId)
                );
            }

        } catch (IllegalArgumentException e) {
            logger.warning("Validazione fallita impostazione ritardo: " + e.getMessage());
            gui.mostraErrore("Dati Non Validi", e.getMessage());

        } catch (Exception e) {
            logger.severe("Errore ritardo: " + e.getMessage());
            e.printStackTrace();
            gui.mostraErrore("Errore","Errore durante l'impostazione del ritardo: " + e.getMessage()
            );
        }
    }

    public void cancellaViaggio(String viaggioId, String motivo) {
        logger.info("Cancella viaggio " + viaggioId + " - Motivo: " + motivo);

        try {
            Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
            if (viaggioOpt.isEmpty()) {
                gui.mostraErrore("Viaggio Non Trovato",
                        "Nessun viaggio trovato con ID: " + viaggioId);
                return;
            }

            Viaggio viaggio = viaggioOpt.get();
            if (viaggio.getStato() == StatoViaggio.ARRIVATO || viaggio.getStato() == StatoViaggio.IN_VIAGGIO) {
                gui.mostraErrore("Operazione Non Consentita",
                        "Impossibile cancellare il viaggio");
                return;
            }

            viaggio.cancellaViaggio(motivo);
            boolean aggiornato = viaggioDAO.delete(viaggioId);
            if(aggiornato) {
                String messaggioSuccesso = String.format(
                        "Viaggio %s cancellato. \n Clienti notificati automaticamente: %d",
                        viaggioId,
                        viaggio.getObserverCount()
                );
                gui.mostraSuccesso("Viaggio Cancellato", messaggioSuccesso);
                logger.info("Viaggio cancellato");
            }
        } catch (Exception e) {
            logger.severe("Errore cancellazione viaggio: " + e.getMessage());
            gui.mostraErrore("Errore","Errore durante la cancellazione del viaggio: " + e.getMessage());
        }
    }

    public void cambiaBinario(String viaggioId, int nuovoBinario) {
        logger.info("Cambia binario viaggio " + viaggioId + " a binario " + nuovoBinario);

        try {
            Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
            if (viaggioOpt.isEmpty()) {
                gui.mostraErrore("Viaggio Non Trovato",
                        "Nessun viaggio trovato con ID: " + viaggioId);
                return;
            }

            Viaggio viaggio = viaggioOpt.get();
            if (viaggio.getStato() != StatoViaggio.PROGRAMMATO || viaggio.getStato() != StatoViaggio.RITARDO) {
                gui.mostraErrore("Operazione Non Consentita",
                        "Impossibile modificare il viaggio");
                return;
            }

            int binarioAttuale = viaggio.getBinarioPartenza().getNumero();
            if (binarioAttuale == nuovoBinario) {
                gui.mostraErrore("Operazione Non Necessaria",
                        "Il viaggio è già assegnato al binario " + nuovoBinario);
                return;
            }

            viaggio.cambioBinario(nuovoBinario-1);
            boolean aggiornato = viaggioDAO.updateViaggioCompleto(viaggio);
            if (aggiornato) {
                String messaggio = String.format(
                        "Binario cambiato con successo\n" +
                                "Viaggio: %s",viaggioId
                );
                gui.mostraSuccesso("Binario Cambiato", messaggio);
                logger.info("Binario cambiato: " + nuovoBinario);
            }

        } catch (Exception e) {
            logger.severe("Errore cambio binario: " + e.getMessage());
            gui.mostraErrore("Errore","Errore durante il cambio binario: " + e.getMessage());
        }
    }

    public void generaViaggi(LocalDate dataInizio, LocalDate dataFine, int viaggiPerTratta) {

        logger.info("Inizio generazione viaggi sistematica dal " + dataInizio + " al " + dataFine);
        try {
            List<Tratta> tutteLeTratte = TrattaUtil.creaTutteLeTratte();
            List<Treno> treniDisponibili = creaTreniBase();

            long giorni = ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
            long viaggiTotaliAttesi = tutteLeTratte.size() * viaggiPerTratta * giorni;

            logger.info("Viaggi totali attesi: " + viaggiTotaliAttesi);

            int viaggiCreati = 0;

            LocalDate dataCorrente = dataInizio;
            while (!dataCorrente.isAfter(dataFine)) {
                logger.info("Processando data: " + dataCorrente);
                for (Tratta tratta : tutteLeTratte) {
                    for (int i = 0; i < viaggiPerTratta; i++) {
                        int maxTentativi = 100;
                        boolean viaggioCreato = false;
                        for (int tentativo = 0; tentativo < maxTentativi && !viaggioCreato; tentativo++) {
                            try {
                                Treno trenoCasuale = treniDisponibili.get((int) (Math.random() * treniDisponibili.size()));
                                Viaggio nuovoViaggio = new Viaggio(trenoCasuale, tratta, dataCorrente);
                                boolean salvato = viaggioDAO.save(nuovoViaggio);
                                if (salvato) {
                                    viaggiCreati++;
                                    viaggioCreato = true;
                                }
                            } catch (IllegalArgumentException e) {
                                if (e.getMessage().contains("passato")) {
                                    logger.fine("Tentativo ignorato - orario nel passato per tratta: " + tratta);
                                }
                            } catch (Exception e) {
                                logger.warning("Errore imprevisto creazione viaggio: " + e.getMessage());
                            }
                        }
                        if (!viaggioCreato) {
                            logger.warning("ATTENZIONE: Impossibile creare viaggio per tratta " +
                                    tratta + " dopo " + maxTentativi + " tentativi");
                        }
                    }
                }
                dataCorrente = dataCorrente.plusDays(1);
            }
            String risultatiFinali = String.format(
                            "Viaggi attesi: %,d\n" +
                            "Viaggi creati con successo: %,d",
                            viaggiTotaliAttesi,
                            viaggiCreati
            );

            logger.info(risultatiFinali);
            if (viaggiCreati == viaggiTotaliAttesi) {
                gui.mostraSuccesso("Generazione Completata",
                        String.format("Creati tutti i %,d viaggi !", viaggiCreati));
            } else {
                gui.mostraSuccesso("Generazione Parzialmente Completata",
                        String.format("Creati %,d viaggi su %,d richiesti",
                                viaggiCreati, viaggiTotaliAttesi));
            }

            logger.info("Generazione completata");

        } catch (Exception e) {
            String errorMsg = "Errore durante la generazione: " + e.getMessage();
            logger.severe(errorMsg);
            e.printStackTrace();
            gui.mostraErrore("Errore Generazione", errorMsg);
        }
    }

    public void eliminaViaggiTerminati() {
        logger.info("Eliminazione viaggi passati (data < oggi)");

        try {
            String sql = "DELETE FROM viaggi WHERE data_viaggio < ?";

            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, LocalDate.now().toString());

            int viaggiEliminati = stmt.executeUpdate();
            stmt.close();

            String messaggio = String.format(
                    "Eliminati %,d viaggi del passato\n" +
                            "(con data precedente a %s)",
                    viaggiEliminati,
                    LocalDate.now()
            );

            gui.mostraSuccesso("Pulizia Viaggi Passati Completata", messaggio);
            logger.info("Viaggi passati eliminati: " + viaggiEliminati);

        } catch (Exception e) {
            String errorMsg = "Errore durante l'eliminazione viaggi passati: " + e.getMessage();
            logger.severe(errorMsg);
            gui.mostraErrore("Errore Eliminazione", errorMsg);
        }
    }

    public void eliminaTuttiIViaggi() {
        logger.warning("Eliminazione di TUTTI i viaggi dal database");

        try {
            String countSql = "SELECT COUNT(*) FROM viaggi";
            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement countStmt = conn.createStatement();
            ResultSet rs = countStmt.executeQuery(countSql);

            int viaggiTotali = 0;
            if (rs.next()) {
                viaggiTotali = rs.getInt(1);
            }
            countStmt.close();

            if (viaggiTotali == 0) {
                gui.mostraSuccesso("Database Già Pulito", "Il database non contiene viaggi da eliminare.");
                return;
            }

            String deleteSql = "DELETE FROM viaggi";
            Statement deleteStmt = conn.createStatement();
            int viaggiEliminati = deleteStmt.executeUpdate(deleteSql);
            deleteStmt.close();

            String messaggio = String.format("Viaggi eliminati: %,d\n" ,viaggiEliminati);

            gui.mostraSuccesso("Eliminazione Totale Completata", messaggio);
            logger.warning("Eliminati tutti i " + viaggiEliminati + " viaggi dal database");

        } catch (Exception e) {
            String errorMsg = "Errore durante l'eliminazione totale: " + e.getMessage();
            logger.severe(errorMsg);
            gui.mostraErrore("Errore Eliminazione Totale", errorMsg);
        }
    }

    private List<Treno> creaTreniBase() {
        TrenoDirector director = new TrenoDirector();
        List<Treno> treni = new ArrayList<>();

        treni.add(director.costruisciTrenoEconomy("Economy"));
        treni.add(director.costruisciTrenoStandard("Standard"));
        treni.add(director.costruisciTrenoBusiness("Business"));

        return treni;
    }


}