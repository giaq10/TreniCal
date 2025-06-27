package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;

import java.util.logging.Logger;

public class ModificaBigliettoCommand implements Command {
    private static final Logger logger = Logger.getLogger(ModificaBigliettoCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final String idBiglietto;
    private final String nuovoIdViaggio;
    private final String emailUtente;

    public ModificaBigliettoCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                                    String idBiglietto, String nuovoIdViaggio, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.idBiglietto = idBiglietto;
        this.nuovoIdViaggio = nuovoIdViaggio;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            logger.info("Esecuzione modifica biglietto: " + idBiglietto + " -> " + nuovoIdViaggio);

            ControllerTrenical.RisultatoModificaBiglietto risultato =
                    controllerTrenical.modificaBiglietto(idBiglietto, nuovoIdViaggio, emailUtente);

            if (risultato.isSuccesso()) {
                String messaggioSuccesso = risultato.getMessaggio() + "\n" +
                        risultato.getDettaglioPrezzo();

                clientApp.mostraSuccesso("Modifica Completata", messaggioSuccesso);
                logger.info("Modifica biglietto completata con successo");
            } else {
                clientApp.mostraErrore("Modifica Fallita", risultato.getMessaggio());
                logger.warning("Modifica biglietto fallita: " + risultato.getMessaggio());
            }

        } catch (Exception e) {
            logger.severe("Errore nell'esecuzione modifica: " + e.getMessage());
            e.printStackTrace();
            clientApp.mostraErrore("Errore Sistema", "Errore durante la modifica: " + e.getMessage());
        }
    }
}