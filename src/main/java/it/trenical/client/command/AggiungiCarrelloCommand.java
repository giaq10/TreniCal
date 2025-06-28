package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import it.trenical.client.carrello.GestoreCarrello;
import it.trenical.client.carrello.CarrelloItem;
import it.trenical.grpc.ViaggioDTO;

public class AggiungiCarrelloCommand implements Command {
    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final int quantita;
    private final String emailUtente;
    private final ViaggioDTO viaggioDTO;

    public AggiungiCarrelloCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                                   int quantita, String emailUtente, ViaggioDTO viaggioDTO) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.quantita = quantita;
        this.emailUtente = emailUtente;
        this.viaggioDTO = viaggioDTO;
    }

    @Override
    public void execute() {
        try {
            System.out.println("Aggiungiamo al carrello " + quantita + " biglietti del viaggio " + viaggioDTO.getId());
            ControllerTrenical.RisultatoCarrello risultato =
                    controllerTrenical.aggiungiAlCarrello(viaggioDTO.getId(), quantita, emailUtente);
            if (risultato.isSuccesso()) {
                System.out.println("Server: " + risultato.getMessaggio());

                for (CarrelloItem item : risultato.getCarrelloItems()) {
                    GestoreCarrello.getInstance().aggiungiItem(item, clientApp, emailUtente);
                }

                String messaggio = String.format(
                        "I tuoi %d biglietti per il viaggio %s-%s sono stati aggiunti al carrello.",
                        quantita, viaggioDTO.getStazionePartenza(), viaggioDTO.getStazioneArrivo()
                );

                clientApp.mostraSuccesso("Biglietti Aggiunti", messaggio);
                try {
                    clientApp.aggiornaTabCarrello();
                    System.out.println("GUI carrello aggiornata");
                } catch (Exception e) {
                    System.err.println("Errore aggiornamento GUI carrello: " + e.getMessage());
                }
            } else {
                System.out.println("Errore server: " + risultato.getMessaggio());
                clientApp.mostraErrore("Errore Carrello", risultato.getMessaggio());
            }

        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
            clientApp.mostraErrore("Errore di Sistema",
                    "Errore durante l'aggiunta al carrello:\n" + e.getMessage());
        }
    }
}