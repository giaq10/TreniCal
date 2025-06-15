package it.trenical.server.gui;

import it.trenical.common.viaggi.StatoViaggio;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.server.db.dao.ViaggioDAO;

import java.time.LocalDate;
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

}