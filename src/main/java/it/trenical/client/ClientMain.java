package it.trenical.client;

import it.trenical.client.gui.ClientApp;
import it.trenical.client.proxy.*;
import javafx.application.Application;
import javafx.scene.control.Alert;

import java.util.logging.Logger;


public class ClientMain extends Application {

    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());
    private static final String SERVER_ADDRESS = "localhost:1010";

    private ControllerTrenical controllerTrenical;

    @Override
    public void init() throws Exception {
        super.init();

        try {
            controllerTrenical = new ControllerTrenical(SERVER_ADDRESS);
            logger.info("Connessione al server stabilita: " + SERVER_ADDRESS);

        } catch (Exception e) {
            logger.severe("Impossibile connettersi al server: " + e.getMessage());
        }
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) {
        try {
            ClientApp clientApp = new ClientApp();
            clientApp.setTrenicalService(controllerTrenical);
            clientApp.start(primaryStage);

            logger.info("GUI Client avviata con successo");

        } catch (Exception e) {
            logger.severe("Errore nell'avvio del client: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void mostraErroreConnessione() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di Connessione");
        alert.setHeaderText("Impossibile connettersi al server");
        alert.setContentText(
                "Il server TreniCal non è raggiungibile.\n\n" +
                        "Assicurati che:\n" +
                        "• Il server sia avviato\n" +
                        "• La porta 50051 sia disponibile\n" +
                        "• Non ci siano problemi di rete\n\n" +
                        "Server: " + SERVER_ADDRESS
        );
        alert.showAndWait();
    }

    private void mostraErroreGenerico(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Errore nell'avvio del client");
        alert.setContentText("Si è verificato un errore:\n\n" + messaggio);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        System.out.println("Avvio Client TreniCal...");
        try {
            launch(args);

        } catch (Exception e) {
            logger.severe("Errore nel client: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}