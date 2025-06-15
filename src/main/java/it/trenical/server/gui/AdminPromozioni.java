package it.trenical.server.gui;

import it.trenical.common.promozioni.Promozione;
import it.trenical.common.promozioni.factoryMethod.PromozioneFactory;
import it.trenical.common.viaggi.StatoViaggio;
import it.trenical.common.viaggi.Viaggio;
import it.trenical.server.db.dao.PromozioneDAO;
import it.trenical.server.db.dao.ViaggioDAO;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


public class AdminPromozioni {

    private static final Logger logger = Logger.getLogger(AdminPromozioni.class.getName());

    private final PromozioneDAO promozioneDAO;
    private final ViaggioDAO viaggioDAO;
    private final ServerAdminApp gui;

    public AdminPromozioni(PromozioneDAO promozioneDAO, ViaggioDAO viaggioDAO, ServerAdminApp gui) {
        this.promozioneDAO = promozioneDAO;
        this.viaggioDAO = viaggioDAO;
        this.gui = gui;

        logger.info("AdminPromozioni inizializzato");
    }

    public void creaPromozione(String nome, String tipo, double percentualeSconto) {
        logger.info("Crea promozione " + nome );
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
                                "Sconto: %.1f%%\n\n" ,
                        nuovaPromozione.getId(),
                        nuovaPromozione.getNome(),
                        nuovaPromozione.getTipo(),
                        nuovaPromozione.getSconto()
                );
                gui.mostraSuccesso("Promozione Creata", messaggio);
                logger.info("Promozione creata: " + nuovaPromozione);
            }

        } catch (Exception e) {
            logger.severe("Errore creazione promozione: " + e.getMessage());
            gui.mostraErrore("Errore Sistema",
                    "Errore durante la creazione della promozione: " + e.getMessage());
        }
    }

    public void applicaPromozioneAViaggio(String promozioneId, String viaggioId) {
        logger.info("Applica promozione " + promozioneId + " a viaggio " + viaggioId);

        try {
            Optional<Promozione> promozioneOpt = promozioneDAO.findById(promozioneId);
            if (promozioneOpt.isEmpty()) {
                gui.mostraErrore("Promozione Non Trovata",
                        "Nessuna promozione trovata con ID: " + promozioneId);
                return;
            }

            Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
            if (viaggioOpt.isEmpty()) {
                gui.mostraErrore("Viaggio Non Trovato",
                        "Nessun viaggio trovato con ID: " + viaggioId);
                return;
            }

            Promozione promozione = promozioneOpt.get();
            Viaggio viaggio = viaggioOpt.get();

            if (viaggio.getStato() != StatoViaggio.PROGRAMMATO) {
                gui.mostraErrore("Operazione Non Consentita",
                        "Impossibile applicare promozione al viaggio");
                return;
            }

            double prezzoOriginale = viaggio.getPrezzo();
            viaggio.applicaPromozione(promozione);
            double prezzoScontato = viaggio.getPrezzo();

            boolean aggiornato = viaggioDAO.updateViaggioCompleto(viaggio);
            if (aggiornato) {
                String messaggio = String.format(
                        "Promozione applicata con successo!\n\n" +
                                "PROMOZIONE:\n" +
                                "Nome: %s\n" +
                                "Tipo: %s\n" +
                                "Sconto: %.1f%%\n\n" +
                                "VIAGGIO:\n" +
                                "ID: %s\n" +
                                "Treno: %s\n" +
                                "Tratta: %s\n\n" +
                                "PREZZI:\n" +
                                "Prezzo originale: €%.2f\n" +
                                "Prezzo scontato: €%.2f",
                        promozione.getNome(),
                        promozione.getTipo(),
                        promozione.getSconto(),
                        viaggioId,
                        viaggio.getTreno().getCodice(),
                        viaggio.getTratta().toString(),
                        prezzoOriginale,
                        prezzoScontato
                );
                gui.mostraSuccesso("Promozione Applicata", messaggio);
                logger.info("Promozione applicata");
            }
        } catch (Exception e) {
            logger.severe("Errore applicazione promozione: " + e.getMessage());
            gui.mostraErrore("Errore Sistema",
                    "Errore durante l'applicazione della promozione: " + e.getMessage());
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
                        "Promozione eliminata %s" ,promozione.getNome()
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

}