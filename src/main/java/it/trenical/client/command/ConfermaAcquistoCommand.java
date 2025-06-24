package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import it.trenical.client.carrello.GestoreCarrello;
import it.trenical.client.carrello.CarrelloItem;

import java.util.List;

public class ConfermaAcquistoCommand implements Command {
    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final List<CarrelloItem> carrelloItems;
    private final List<String> nominativi;
    private final String modalitaPagamento;
    private final String emailUtente;

    public ConfermaAcquistoCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                                   List<CarrelloItem> carrelloItems, List<String> nominativi,
                                   String modalitaPagamento, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.carrelloItems = carrelloItems;
        this.nominativi = nominativi;
        this.modalitaPagamento = modalitaPagamento;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            GestoreCarrello carrello = GestoreCarrello.getInstance();
            if (carrello.getTimer().isTerminato()) {
                clientApp.mostraErrore("Timer Scaduto",
                        "Il tempo per l'acquisto è scaduto. Il carrello verrà svuotato.");
                carrello.svuotaCarrello();
                clientApp.aggiornaTabCarrello();
                return;  // BLOCCA L'ACQUISTO
            }

            ControllerTrenical.RisultatoAcquisto risultato = controllerTrenical.confermaAcquisto(
                    carrelloItems, nominativi, modalitaPagamento, emailUtente);

            if (risultato.isSuccesso()) {
                GestoreCarrello.getInstance().svuotaCarrello();
                clientApp.aggiornaTabCarrello();

                String messaggio = String.format("Acquisto completato!\n%d biglietti acquistati per €%.2f",
                        risultato.getBigliettiAcquistati(), risultato.getPrezzoTotale());

                clientApp.mostraSuccesso("Acquisto Completato", messaggio);
            } else {
                clientApp.mostraErrore("Errore Acquisto", risultato.getMessaggio());
            }
        } catch (Exception e) {
            clientApp.mostraErrore("Errore", "Errore durante l'acquisto: " + e.getMessage());
        }
    }
}