package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.logging.Logger;

public class AbbonamentoCommand implements Command {
    private static final Logger logger = Logger.getLogger(AbbonamentoCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final String emailUtente;

    public AbbonamentoCommand(ControllerTrenical controllerTrenical,
                              ClientApp clientApp, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            logger.info("Esecuzione comando gestione abbonamento per: " + emailUtente);

            boolean vuoleNotifiche = false;

            if (!clientApp.abbonamentoCorrente()) {
                Alert confermaNotifiche = new Alert(Alert.AlertType.CONFIRMATION);
                confermaNotifiche.setTitle("Configurazione Notifiche");
                confermaNotifiche.setHeaderText("Vuoi ricevere notifiche esclusive?");

                ButtonType siButton = new ButtonType("Sì");
                ButtonType noButton = new ButtonType("No");

                confermaNotifiche.getButtonTypes().setAll(siButton, noButton);

                Optional<ButtonType> result = confermaNotifiche.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == siButton) {
                        vuoleNotifiche = true;
                    } else if (result.get() == noButton) {
                        vuoleNotifiche = false;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            ControllerTrenical.RisultatoAbbonamento risultato =
                    controllerTrenical.gestisciAbbonamento(emailUtente, vuoleNotifiche);

            if (risultato.isSuccesso()) {
                clientApp.aggiornaStatoAbbonamento(risultato.isAbbonato(), risultato.hasNotificheAttive());

            } else {
                logger.warning("Errore gestione abbonamento: " + risultato.getMessaggio());
                clientApp.mostraErrore("Errore Abbonamento", risultato.getMessaggio());
            }

        } catch (Exception e) {
            logger.severe("Errore nel command gestione abbonamento: " + e.getMessage());
            e.printStackTrace();
            clientApp.mostraErrore("Errore Sistema",
                    "Errore durante la gestione dell'abbonamento: " + e.getMessage());
        }
    }
}