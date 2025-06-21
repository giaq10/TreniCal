package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;

import java.time.LocalDate;

public class CercaViaggiCommand implements Command {
    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final String stazionePartenza;
    private final String stazioneArrivo;
    private final LocalDate dataViaggio;

    public CercaViaggiCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                              String stazionePartenza, String stazioneArrivo, LocalDate dataViaggio) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
        this.dataViaggio = dataViaggio;
    }

    @Override
    public void execute() {
        try {
            ControllerTrenical.RisultatoRicerca risultato =
                    controllerTrenical.cercaViaggi(stazionePartenza, stazioneArrivo, dataViaggio);

            if (risultato.isSuccesso()) {
                if (risultato.getViaggi() != null && !risultato.getViaggi().isEmpty()) {
                    clientApp.getViaggiListView().getItems().clear();
                    clientApp.getViaggiListView().getItems().addAll(risultato.getViaggi());
                } else {
                    clientApp.getViaggiListView().getItems().clear();
                }
            } else {
                clientApp.getViaggiListView().getItems().clear();
            }

        } catch (Exception e) {
            clientApp.mostraErrore("Errore di Sistema",
                    "Errore durante la ricerca: " + e.getMessage());
        }
    }
}
