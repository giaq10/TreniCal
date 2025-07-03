package it.trenical.client;

import it.trenical.client.gui.Login;
import it.trenical.client.proxy.*;
import javafx.application.Application;

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
            Login loginDialog = new Login(controllerTrenical);
            loginDialog.show();
        } catch (Exception e) {
            logger.severe("Errore nell'avvio del client: " + e.getMessage());
            e.printStackTrace();
        }
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