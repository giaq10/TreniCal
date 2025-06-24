package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;

import java.util.logging.Logger;


public class VisualizzaBigliettiCommand implements Command {
    private static final Logger log = Logger.getLogger(VisualizzaBigliettiCommand.class.getName());
    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final String emailUtente;

    public VisualizzaBigliettiCommand(ControllerTrenical controllerTrenical, ClientApp clientApp, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            ControllerTrenical.RisultatoBiglietti risultato =
                    controllerTrenical.visualizzaBiglietti(emailUtente);

            if (risultato.isSuccesso()) {
                if (risultato.getBiglietti() != null && !risultato.getBiglietti().isEmpty()) {
                    clientApp.getBigliettiListView().getItems().clear();
                    clientApp.getBigliettiListView().getItems().addAll(risultato.getBiglietti());

                    System.out.println("ListView aggiornata con " + clientApp.getBigliettiListView().getItems().size() + " elementi");

                } else {
                    clientApp.getBigliettiListView().getItems().clear();
                    clientApp.mostraErrore("Nessun Biglietto", "Non hai ancora acquistato biglietti.");
                }
            } else {
                clientApp.getBigliettiListView().getItems().clear();
                clientApp.mostraErrore("Errore Caricamento", risultato.getMessaggio());
            }

        } catch (Exception e) {
            log.severe("ECCEZIONE NEL COMMAND: " + e.getMessage());
            e.printStackTrace();
            clientApp.mostraErrore("Errore di Sistema",
                    "Errore durante il caricamento dei biglietti:\n" + e.getMessage());
        }
    }
}