package it.trenical.client.gui;

import it.trenical.client.command.LoginCommand;
import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.grpc.ClienteDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Logger;

public class Login {
    private static final Logger logger = Logger.getLogger(Login.class.getName());

    private Stage dialogStage;
    private ControllerTrenical controllerTrenical;

    private TextField emailField;
    private PasswordField passwordField;
    private TextField nomeField;
    private Button loginButton;
    private Label statusLabel;


    public Login(ControllerTrenical controllerTrenical) {
        this.controllerTrenical = controllerTrenical;
        creaDialog();
    }

    private void creaDialog() {
        dialogStage = new Stage();
        dialogStage.setTitle("TreniCal - Accesso");
        dialogStage.setResizable(false);

        VBox mainLayout = creaLayoutPrincipale();
        Scene scene = new Scene(mainLayout);

        dialogStage.setScene(scene);
    }

    private VBox creaLayoutPrincipale() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white;");

        VBox headerBox = creaHeader();

        VBox formBox = creaFormLogin();

        VBox actionBox = creaAzioniBox();

        statusLabel = new Label();
        statusLabel.setVisible(false);

        layout.getChildren().addAll(headerBox, formBox, actionBox, statusLabel);
        return layout;
    }

    private VBox creaHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        InputStream imageStream = getClass().getResourceAsStream("/trenical.png");
        Image image = new Image(imageStream);
        ImageView imageView = new ImageView(image);

        Label titleLabel = new Label("TreniCal");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        headerBox.getChildren().addAll(imageView, titleLabel);
        return headerBox;
    }

    private VBox creaFormLogin() {
        VBox formBox = new VBox(15);
        formBox.setStyle("-fx-background-color: white;");

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        emailField = new TextField();
        emailField.setPromptText("inserisci la tua email...");
        emailField.setPrefHeight(35);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("inserisci la password...");
        passwordField.setPrefHeight(35);

        Label nomeLabel = new Label("Nome completo:");
        nomeLabel.setStyle("-fx-font-weight: bold;");
        nomeField = new TextField();
        nomeField.setPromptText("inserisci il tuo nome...");
        nomeField.setPrefHeight(35);

        Label infoLabel = new Label("Se l'email non esiste, verrai registrato automaticamente");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: black; ");
        infoLabel.setWrapText(true);

        formBox.getChildren().addAll(
                emailLabel, emailField,
                passwordLabel, passwordField,
                nomeLabel, nomeField,
                infoLabel
        );

        return formBox;
    }

    private VBox creaAzioniBox() {
        VBox actionBox = new VBox(15);
        actionBox.setAlignment(Pos.CENTER);

        loginButton = new Button("Accedi / Registrati");
        loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; ");

        loginButton.setOnAction(e -> eseguiLogin());
        passwordField.setOnAction(e -> eseguiLogin());
        nomeField.setOnAction(e -> eseguiLogin());

        actionBox.getChildren().addAll(loginButton);
        return actionBox;
    }

    private void eseguiLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String nome = nomeField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || nome.isEmpty()) {
            mostraErrore("Tutti i campi sono obbligatori");
            return;
        }

        LoginCommand command = new LoginCommand(controllerTrenical, this, email, password, nome);
        command.execute();
    }

    public void mostraErrore(String messaggio) {
        Platform.runLater(() -> {
            statusLabel.setText( messaggio);
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            statusLabel.setVisible(true);
        });
    }

    public void mostraErrorePassword(String messaggio) {
        Platform.runLater(() -> {
            mostraErrore(messaggio);
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        });
    }

    public void loginRiuscito(ClienteDTO cliente) {
        Platform.runLater(() -> {
            logger.info("Login riuscito per: " + cliente.getEmail());
            dialogStage.close();
            try {
                ClientApp clientApp = new ClientApp(cliente);
                clientApp.setTrenicalService(this.controllerTrenical);
                clientApp.start(new Stage());
            } catch (Exception e) {
                logger.severe("Errore avvio ClientApp: " + e.getMessage());
            }
        });
    }

    public void show() {
        dialogStage.showAndWait();
    }
}