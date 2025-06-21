package it.trenical.server;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import it.trenical.server.grpc.TrenicalServiceImpl;
import it.trenical.server.gui.ServerAdminApp;
import javafx.application.Platform;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerMain {

    private static Logger logger = Logger.getLogger(ServerMain.class.getName());
    private static int port = 1010;

    private Server server;

    private void avviaServer() throws IOException {
        server = Grpc.newServerBuilderForPort(
                        port,
                        InsecureServerCredentials.create())
                .addService(new TrenicalServiceImpl())
                .build()
                .start();

        logger.info("Server gRPC in ascolto sulla porta: " + port);
    }

    private void avviaGUIAdmin() {
        Thread thread = new Thread(() -> {
            try {
                Platform.startup(() -> {
                });

                ServerAdminApp.main(new String[]{});

            } catch (Exception e) {
                logger.severe("Errore nell'avvio della GUI amministrativa: " + e.getMessage());
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void attendiTerminazione() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) {
        ServerMain serverMain = new ServerMain();
        try {
            serverMain.avviaServer();
            serverMain.avviaGUIAdmin();
            serverMain.attendiTerminazione();
        } catch (Exception e) {
            logger.severe("Errore nel server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}