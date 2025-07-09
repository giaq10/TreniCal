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
    private final String codicePromozione;

    public ConfermaAcquistoCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                                   List<CarrelloItem> carrelloItems, List<String> nominativi,
                                   String modalitaPagamento, String emailUtente,
                                   String codicePromozione) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.carrelloItems = carrelloItems;
        this.nominativi = nominativi;
        this.modalitaPagamento = modalitaPagamento;
        this.emailUtente = emailUtente;
        this.codicePromozione = codicePromozione;
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
                return;
            }

            ControllerTrenical.RisultatoAcquisto risultato = controllerTrenical.confermaAcquisto(
                    carrelloItems, nominativi, modalitaPagamento, emailUtente, codicePromozione);

            if (risultato.isSuccesso()) {
                GestoreCarrello.getInstance().svuotaCarrello();
                clientApp.aggiornaTabCarrello();

                String messaggio = costruisciMessaggioSuccesso(risultato);
                clientApp.mostraSuccesso("Acquisto Completato", messaggio);
            } else {
                clientApp.mostraErrore("Errore Acquisto", risultato.getMessaggio());
            }
        } catch (Exception e) {
            clientApp.mostraErrore("Errore", "Errore durante l'acquisto: " + e.getMessage());
        }
    }

    private String costruisciMessaggioSuccesso(ControllerTrenical.RisultatoAcquisto risultato) {
        StringBuilder messaggio = new StringBuilder();

        messaggio.append(String.format("%d biglietti acquistati",
                risultato.getBigliettiAcquistati()));

        if (risultato.getScontoApplicato()!=0) {
            messaggio.append(String.format("Promozione applicata: %s",
                    risultato.getNomePromozione()));
            messaggio.append(String.format("\nSconto: %.2f%%",
                    risultato.getScontoApplicato()));
            messaggio.append(String.format("\nTotale pagato: €%.2f",
                    risultato.getPrezzoTotale()));
        } else {
            messaggio.append(String.format("\nTotale: €%.2f",
                    risultato.getPrezzoTotale()));
        }

        return messaggio.toString();
    }
}