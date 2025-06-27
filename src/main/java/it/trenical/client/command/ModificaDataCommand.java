package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import it.trenical.grpc.BigliettoDTO;
import javafx.scene.control.TextInputDialog;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;

public class ModificaDataCommand implements Command {
    private static final Logger logger = Logger.getLogger(ModificaDataCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final BigliettoDTO biglietto;
    private final String emailUtente;

    public ModificaDataCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                               BigliettoDTO biglietto, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.biglietto = biglietto;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            logger.info("Modifica data biglietto: " + biglietto.getId());

            LocalDate nuovaData = richiediNuovaData();
            if (nuovaData == null) {
                return;
            }

            ControllerTrenical.RisultatoRicerca risultato = controllerTrenical.cercaViaggi(
                    biglietto.getStazionePartenza(),
                    biglietto.getStazioneArrivo(),
                    nuovaData
            );

            if (risultato.isSuccesso() && risultato.getViaggi() != null && !risultato.getViaggi().isEmpty()) {
                String titolo = "Seleziona Viaggio per " + nuovaData + " - " +
                        biglietto.getStazionePartenza() + " -> " + biglietto.getStazioneArrivo();
                clientApp.mostraSelezionaNuovoViaggio(risultato.getViaggi(), biglietto, emailUtente, titolo);
            } else {
                clientApp.mostraErrore("Nessun Viaggio", "Non ci sono viaggi disponibili per la data " + nuovaData);
            }
        } catch (Exception e) {
            logger.severe("Errore modifica data: " + e.getMessage());
            clientApp.mostraErrore("Errore Sistema", "Errore durante la ricerca: " + e.getMessage());
        }
    }

    private LocalDate richiediNuovaData() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Modifica Data Viaggio");
        dialog.setHeaderText("Inserisci la nuova data di partenza");
        dialog.setContentText("Data (formato YYYY-MM-DD):");
        dialog.getEditor().setText(LocalDate.now().toString());

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                LocalDate nuovaData = LocalDate.parse(result.get());
                LocalDate dataBiglietto = LocalDate.parse(biglietto.getDataViaggio());
                if (nuovaData.isBefore(LocalDate.now())) {
                    clientApp.mostraErrore("Data Non Valida", "Non è possibile selezionare una data nel passato.");
                    return null;
                } else if (nuovaData.isEqual(dataBiglietto)) {
                    clientApp.mostraErrore("Data Non Valida", "Scegliere una data diversa da quella corrente.");
                    return null;
                }
                return nuovaData;
            } catch (DateTimeParseException e) {
                clientApp.mostraErrore("Formato Data Non Valido",
                        "Inserire la data nel formato YYYY-MM-DD");
                return null;
            }
        }
        return null;
    }

}