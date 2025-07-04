package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import it.trenical.grpc.PromozioneDTO;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.logging.Logger;

public class VisualizzaPromozioniCommand implements Command {
    private static final Logger logger = Logger.getLogger(VisualizzaPromozioniCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final String emailUtente;
    private final TextArea targetTextArea;

    public VisualizzaPromozioniCommand(ControllerTrenical controllerTrenical,
                                       ClientApp clientApp,
                                       String emailUtente,
                                       TextArea targetTextArea) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.emailUtente = emailUtente;
        this.targetTextArea = targetTextArea;
    }

    @Override
    public void execute() {
        try {
            logger.info("Visualizzazione promozioni per: " + emailUtente);

            if (emailUtente == null || emailUtente.trim().isEmpty()) {
                clientApp.mostraErrore("Errore", "Email utente non specificata");
                return;
            }

            ControllerTrenical.RisultatoPromozioni risultato =
                    controllerTrenical.visualizzaPromozioni(emailUtente);

            if (risultato.isSuccesso()) {
                List<PromozioneDTO> promozioni = risultato.getPromozioni();
                logger.info("Promozioni ricevute con successo: " + promozioni.size() + " elementi");

                String testoPromozioni = generaTestoPromozioni(promozioni);
                targetTextArea.setText(testoPromozioni);


            } else {
                logger.warning("Errore visualizzazione promozioni: " + risultato.getMessaggio());
                targetTextArea.setText("Errore nel caricamento delle promozioni: " + risultato.getMessaggio());
                clientApp.mostraErrore("Errore Promozioni", risultato.getMessaggio());
            }

        } catch (Exception e) {
            logger.severe("Errore nel command visualizzazione promozioni: " + e.getMessage());
            e.printStackTrace();
            targetTextArea.setText("Errore di sistema durante il caricamento delle promozioni.");
            clientApp.mostraErrore("Errore di Sistema",
                    "Errore durante il caricamento delle promozioni:\n" + e.getMessage());
        }
    }

    private String generaTestoPromozioni(List<PromozioneDTO> promozioni) {
        if (promozioni == null || promozioni.isEmpty()) {
            return "Nessuna promozione disponibile al momento.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Totale: %d promozioni\n\n", promozioni.size()));

        for (int i = 0; i < promozioni.size(); i++) {
            PromozioneDTO p = promozioni.get(i);
            sb.append(String.format("%d. ID: %s\n", i + 1, p.getId()));
            sb.append(String.format("Nome: %s\n", p.getNome()));
            sb.append(String.format("Tipo: %s\n", p.getTipo()));
            sb.append(String.format("Sconto: %.1f%%\n\n", p.getPercentualeSconto()));
        }

        return sb.toString();
    }
}