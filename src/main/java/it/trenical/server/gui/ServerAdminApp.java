package it.trenical.server.gui;

import it.trenical.server.db.dao.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


public class ServerAdminApp extends Application {

    private AdminViaggi adminViaggi;
    private AdminPromozioni adminPromozioni;
    private AdminVisualizzaDB adminVisualizzaDB;

    private ViaggioDAO viaggioDAO;
    private ClienteDAO clienteDAO;
    private PromozioneDAO promozioneDAO;

    private TabPane mainTabPane;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Avvio GUI Amministratore TreniCal...");

        try {
            viaggioDAO = new ViaggioDAO();
            clienteDAO = new ClienteDAO();
            promozioneDAO = new PromozioneDAO();

            System.out.println("DAO inizializzati");
        } catch (Exception e) {
            System.err.println("Errore inizializzazione DAO: " + e.getMessage());
            mostraErrore("Errore inizializzazione database", e.getMessage());
        }
        try {
            adminViaggi = new AdminViaggi(viaggioDAO, this);
            adminPromozioni = new AdminPromozioni(promozioneDAO, viaggioDAO, this);
            adminVisualizzaDB = new AdminVisualizzaDB(clienteDAO, viaggioDAO, promozioneDAO, this);

            System.out.println("Controllers inizializzati");
        } catch (Exception e) {
            System.err.println("Errore inizializzazione controllers: " + e.getMessage());
            mostraErrore("Errore inizializzazione controllers", e.getMessage());
        }

        VBox mainLayout = creaInterfacciaPrincipale();
        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setTitle("Pannello Amministratore");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Chiusura GUI Amministratore");
        });

        primaryStage.show();

        System.out.println("GUI Amministratore avviata con successo!");
    }

    private VBox creaInterfacciaPrincipale() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));

        VBox header = creaHeader();
        mainTabPane = creaTabPane();
        mainLayout.getChildren().addAll(header, mainTabPane);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);

        return mainLayout;
    }

    private VBox creaHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");

        Label titleLabel = new Label("Pannello Amministratore TreniCal");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        header.getChildren().addAll(titleLabel);
        return header;
    }

    private TabPane creaTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabViaggi = new Tab("Gestione Viaggi");
        tabViaggi.setContent(creaGestioneViaggi());

        Tab tabPromozioni = new Tab("Gestione Promozioni");
        tabPromozioni.setContent(creaGestionePromozioni());

        Tab tabViaggiView = new Tab("Visualizza Viaggi");
        tabViaggiView.setContent(creaVisualizzaViaggi());

        Tab tabPromozioniView = new Tab("Visualizza Promozioni");
        tabPromozioniView.setContent(creaVisualizzaPromozioni());

        Tab tabClienti = new Tab("Visualizza Clienti");
        tabClienti.setContent(creaVisualizzaClienti());
        tabPane.getTabs().addAll(tabViaggi, tabPromozioni, tabViaggiView, tabPromozioniView, tabClienti);
        return tabPane;
    }

    private VBox creaGestioneViaggi() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Operazione sui Viaggi");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        VBox ritardiBox = creaSezioneRitardi();
        grid.add(ritardiBox, 0, 0);

        VBox cancellazioniBox = creaSezioneCancellazioni();
        grid.add(cancellazioniBox, 1, 0);

        VBox binariBox = creaSezioneBinari();
        grid.add(binariBox, 2, 0);

        VBox creazioneMassivaViaggiBox = creaSezioneCreazioneViaggi();
        grid.add(creazioneMassivaViaggiBox,3,0);

        layout.getChildren().addAll(title, grid);
        return layout;
    }

    private VBox creaSezioneRitardi() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Imposta Ritardo Partenza");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField viaggioIdField = new TextField();
        viaggioIdField.setPromptText("ID Viaggio");

        TextField ritardoField = new TextField();
        ritardoField.setPromptText("Ritardo (minuti)");

        Button impostaRitardoBtn = new Button("Imposta Ritardo");
        impostaRitardoBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        impostaRitardoBtn.setOnAction(e -> {
            String viaggioId = viaggioIdField.getText().trim();
            String ritardoStr = ritardoField.getText().trim();
            try {
                int ritardoMinuti = Integer.parseInt(ritardoStr);
                adminViaggi.impostaRitardo(viaggioId, ritardoMinuti);

                viaggioIdField.clear();
                ritardoField.clear();
            } catch (NumberFormatException ex) {
                mostraErrore("Errore formato", "Inserire un numero valido per i minuti");
            }
        });

        box.getChildren().addAll(title, viaggioIdField, ritardoField, impostaRitardoBtn);
        return box;
    }

    private VBox creaSezioneCancellazioni() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Cancella Viaggi");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField viaggioIdField = new TextField();
        viaggioIdField.setPromptText("ID Viaggio");

        TextField motivoField = new TextField();
        motivoField.setPromptText("Motivo cancellazione");

        Button cancellaBtn = new Button("Cancella Viaggio");
        cancellaBtn.setStyle("-fx-background-color: darkorange; -fx-text-fill: white;");
        cancellaBtn.setOnAction(e -> {
            String viaggioId = viaggioIdField.getText().trim();
            String motivo = motivoField.getText().trim();

            if (viaggioId.isEmpty()) {
                mostraErrore("Campi obbligatori", "Inserire ID viaggio");
                return;
            }

            adminViaggi.cancellaViaggio(viaggioId, motivo);

            viaggioIdField.clear();
            motivoField.clear();
        });

        Button eliminaPassatiBtn = new Button("Elimina Viaggi Terminati");
        eliminaPassatiBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        eliminaPassatiBtn.setOnAction(e -> {
            adminViaggi.eliminaViaggiTerminati();
        });

        Button pulisciBtn = new Button("Cancella Tutti i Viaggi Esistenti");
        pulisciBtn.setStyle("-fx-background-color: darkred; -fx-text-fill: white; -fx-font-weight: bold;");
        pulisciBtn.setOnAction(e -> {
            Alert conferma = new Alert(Alert.AlertType.WARNING);
            conferma.setHeaderText("Eliminare TUTTI i viaggi?");
            conferma.setContentText("Questa operazione eliminerà definitivamente tutti i viaggi dal database.");
            Optional<ButtonType> result = conferma.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                adminViaggi.eliminaTuttiIViaggi();
            }
        });

        box.getChildren().addAll(title,viaggioIdField,motivoField,cancellaBtn,eliminaPassatiBtn, pulisciBtn);

        return box;
    }

    private VBox creaSezioneBinari() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Cambia Binari");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField viaggioIdField = new TextField();
        viaggioIdField.setPromptText("ID Viaggio");

        ComboBox<String> binarioCombo = new ComboBox<>();
        binarioCombo.getItems().addAll("1", "2", "3");
        binarioCombo.setPromptText("Nuovo Binario");

        Button cambiaBinarioBtn = new Button("Cambia Binario");
        cambiaBinarioBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        cambiaBinarioBtn.setOnAction(e -> {
            String viaggioId = viaggioIdField.getText().trim();
            String binario = binarioCombo.getValue();

            if (viaggioId.isEmpty() || binario == null) {
                mostraErrore("Campi obbligatori", "Inserire ID viaggio e selezionare binario");
                return;
            }

            adminViaggi.cambiaBinario(viaggioId, Integer.parseInt(binario));

            viaggioIdField.clear();
            binarioCombo.setValue(null);
        });

        box.getChildren().addAll(title, viaggioIdField, binarioCombo, cambiaBinarioBtn);
        return box;
    }

    private VBox creaSezioneCreazioneViaggi() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Generazione Viaggi");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Date picker per range
        DatePicker dataInizio = new DatePicker();
        dataInizio.setPromptText("Data Inizio");
        dataInizio.setValue(LocalDate.now().plusDays(1)); // Default: domani

        DatePicker dataFine = new DatePicker();
        dataFine.setPromptText("Data Fine");
        dataFine.setValue(LocalDate.now().plusDays(7)); // Default: +1 settimana

        ComboBox<Integer> viaggiPerTratta = new ComboBox<>();
        viaggiPerTratta.getItems().addAll(1, 2, 3, 4, 5, 6);
        viaggiPerTratta.setValue(4);
        viaggiPerTratta.setPromptText("Viaggi per tratta");

        Button generaBtn = new Button("Genera Viaggi");
        generaBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        generaBtn.setOnAction(e -> {
            LocalDate inizio = dataInizio.getValue();
            LocalDate fine = dataFine.getValue();

            if (inizio == null || fine == null) {
                mostraErrore("Date Obbligatorie", "Selezionare entrambe le date");
                return;
            }

            if (inizio.isAfter(fine)) {
                mostraErrore("Date Non Valide", "Data inizio deve essere precedente alla data fine");
                return;
            }

            int viaggiTratta = viaggiPerTratta.getValue();

            long giorni = ChronoUnit.DAYS.between(inizio, fine) + 1;
            long viaggiTotali = 132 * viaggiTratta * giorni;

            Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
            conferma.setTitle("Conferma Generazione");
            conferma.setHeaderText("Generare " + String.format("%,d", viaggiTotali) + " viaggi?");

            Optional<ButtonType> result = conferma.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                adminViaggi.generaViaggi(inizio, fine, viaggiTratta);
            }
        });

        box.getChildren().addAll(
                title,
                new Label("Periodo Generazione:"),
                dataInizio,
                dataFine,
                new Label("Configurazione:"),
                viaggiPerTratta,
                generaBtn
        );

        return box;
    }

    private VBox creaGestionePromozioni() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Operazioni sulle Promozioni");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        VBox creazioneBox = creaSezioneCreazionePromozioni();
        grid.add(creazioneBox, 0, 0);

        VBox applicazioneBox = creaSezioneApplicazionePromozioni();
        grid.add(applicazioneBox, 1, 0);

        VBox eliminazioneBox = creaEliminaPromozioni();
        grid.add(eliminazioneBox, 2, 0);

        layout.getChildren().addAll(title, grid);
        return layout;
    }

    private VBox creaSezioneCreazionePromozioni() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Crea Promozioni");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome Promozione");

        ComboBox<String> tipoPromo = new ComboBox<>();
        tipoPromo.getItems().addAll("Standard", "Fedelta");
        tipoPromo.setPromptText("Tipo Promozione");

        TextField scontoField = new TextField();
        scontoField.setPromptText("Sconto (%)");

        Button creaBtn = new Button("Crea Promozione");
        creaBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        creaBtn.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String tipo = tipoPromo.getValue();
            String scontoStr = scontoField.getText().trim();

            try {
                double percentualeSconto = Double.parseDouble(scontoStr);

                adminPromozioni.creaPromozione(nome, tipo, percentualeSconto);

                nomeField.clear();
                tipoPromo.setValue("Tipo Promozione");
                scontoField.clear();

            } catch (NumberFormatException ex) {
                mostraErrore("Errore formato", "Inserire un numero valido per la percentuale");
            }
        });

        box.getChildren().addAll(title, nomeField, tipoPromo, scontoField, creaBtn);
        return box;
    }

    private VBox creaSezioneApplicazionePromozioni() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Applica Promozioni");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField promozioneIdField = new TextField();
        promozioneIdField.setPromptText("ID Promozione");

        TextField viaggioIdField = new TextField();
        viaggioIdField.setPromptText("ID Viaggio");

        Button applicaBtn = new Button("Applica Promozione");
        applicaBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        applicaBtn.setOnAction(e -> {
            String promozioneId = promozioneIdField.getText().trim();
            String viaggioId = viaggioIdField.getText().trim();



            adminPromozioni.applicaPromozioneAViaggio(promozioneId, viaggioId);

            promozioneIdField.clear();
            viaggioIdField.clear();
        });

        box.getChildren().addAll(title, promozioneIdField, viaggioIdField, applicaBtn);
        return box;
    }

    public VBox creaEliminaPromozioni() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label title = new Label("Elimina Promozioni");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField promozioneIdField = new TextField();
        promozioneIdField.setPromptText("ID Promozione");

        Button applicaBtn = new Button("Elimina Promozione");
        applicaBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        applicaBtn.setOnAction(e -> {
            String promozioneId = promozioneIdField.getText().trim();

            if (promozioneId.isEmpty() ) {
                mostraErrore("Campi obbligatori", "Inserire ID promozione");
                return;
            }

            adminPromozioni.eliminaPromozione(promozioneId);

            promozioneIdField.clear();
        });

        Button pulisciBtn = new Button("Cancella Tutte Le Promozioni");
        pulisciBtn.setStyle("-fx-background-color: darkred; -fx-text-fill: white; -fx-font-weight: bold;");
        pulisciBtn.setOnAction(e -> {
            Alert conferma = new Alert(Alert.AlertType.WARNING);
            conferma.setHeaderText("Eliminare TUTTE le Promozioni?");
            conferma.setContentText("Questa operazione eliminerà definitivamente tutte le promozioni dal database.");
            Optional<ButtonType> result = conferma.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                adminPromozioni.eliminaTutteLePromozioni();
            }
        });

        box.getChildren().addAll(title, promozioneIdField, applicaBtn, pulisciBtn);

        return box;
    }

    private VBox creaVisualizzaViaggi() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Visualizzazione Viaggi");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        VBox viaggiBox = new VBox(10);
        viaggiBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label viaggiTitle = new Label("Elenco Viaggi Database");
        viaggiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea viaggiArea = new TextArea();
        viaggiArea.setPrefHeight(400);
        viaggiArea.setPrefWidth(500);
        viaggiArea.setEditable(false);
        viaggiArea.setWrapText(true);
        viaggiArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Button caricaViaggiBtn = new Button("Carica Viaggi");
        caricaViaggiBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        caricaViaggiBtn.setOnAction(e -> {
            String viaggi = adminVisualizzaDB.getTuttiIViaggi();
            viaggiArea.setText(viaggi);
        });

        viaggiBox.getChildren().addAll(viaggiTitle, viaggiArea, caricaViaggiBtn);

        grid.add(viaggiBox, 0, 0, 2, 1);

        layout.getChildren().addAll(title, grid);
        return layout;
    }

    private VBox creaVisualizzaPromozioni() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Visualizzazione Promozioni");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        VBox promoBox = new VBox(10);
        promoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label promoTitle = new Label("Elenco Promozioni Database");
        promoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea promoArea = new TextArea();
        promoArea.setPrefHeight(400);
        promoArea.setPrefWidth(500);
        promoArea.setEditable(false);
        promoArea.setWrapText(true);
        promoArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Button caricaPromoBtn = new Button("Carica Promozioni");
        caricaPromoBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        caricaPromoBtn.setOnAction(e -> {
            String promozioni = adminVisualizzaDB.getTutteLePromozioni();
            promoArea.setText(promozioni);
        });

        promoBox.getChildren().addAll(promoTitle, promoArea, caricaPromoBtn);

        grid.add(promoBox, 0, 0, 2, 1);

        layout.getChildren().addAll(title, grid);
        return layout;
    }

    private VBox creaVisualizzaClienti() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Visualizza Clienti");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        VBox clientiBox = new VBox(10);
        clientiBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label clientiTitle = new Label("Elenco Clienti Database");
        clientiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea clientiArea = new TextArea();
        clientiArea.setPrefHeight(400);
        clientiArea.setPrefWidth(500);
        clientiArea.setEditable(false);
        clientiArea.setWrapText(true);
        clientiArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Button caricaClientiBtn = new Button("Carica Clienti");
        caricaClientiBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        caricaClientiBtn.setOnAction(e -> {
            String promozioni = adminVisualizzaDB.getTuttiIClienti();
            clientiArea.setText(promozioni);
        });

        clientiBox.getChildren().addAll(clientiTitle, clientiArea, caricaClientiBtn);

        grid.add(clientiBox, 0, 0, 2, 1);

        layout.getChildren().addAll(title, grid);
        return layout;
    }

    public void mostraSuccesso(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();

    }

    public void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();

    }

    public static void main(String[] args) {
        System.out.println("Avvio GUI Amministrativa");
        launch(args);
    }
}