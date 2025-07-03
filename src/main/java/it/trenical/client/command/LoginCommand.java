package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.Login;
import it.trenical.grpc.ClienteDTO;

import java.util.logging.Logger;

public class LoginCommand implements Command {
    private static final Logger logger = Logger.getLogger(LoginCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final Login loginDialog;
    private final String email;
    private final String password;
    private final String nome;

    public LoginCommand(ControllerTrenical controllerTrenical, Login loginDialog,
                        String email, String password, String nome) {
        this.controllerTrenical = controllerTrenical;
        this.loginDialog = loginDialog;
        this.email = email;
        this.password = password;
        this.nome = nome;
    }

    @Override
    public void execute() {
        try {
            logger.info("Esecuzione comando login per: " + email);

            ControllerTrenical.RisultatoLogin risultato =
                    controllerTrenical.login(email.trim(), password, nome.trim());
            if (risultato.isSuccesso()) {
                ClienteDTO cliente = risultato.getCliente();
                logger.info("Login completato per: " + cliente.getEmail());
                loginDialog.loginRiuscito(cliente);
            } else {
                String messaggio = risultato.getMessaggio();
                if (messaggio.toLowerCase().contains("password")) {
                    loginDialog.mostraErrorePassword(messaggio);
                } else {
                    loginDialog.mostraErrore(messaggio);
                }
                logger.warning("Login fallito: " + messaggio);
            }
        } catch (Exception e) {
            logger.severe("Errore durante esecuzione comando login: " + e.getMessage());
            e.printStackTrace();
            loginDialog.mostraErrore("Errore di sistema durante il login:\n" + e.getMessage());
        }
    }
}