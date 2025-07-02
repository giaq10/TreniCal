package it.trenical.client.gui;

import it.trenical.client.carrello.CarrelloItem;
import it.trenical.client.carrello.GestoreCarrello;
import it.trenical.client.command.*;
import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.grpc.BigliettoDTO;
import it.trenical.grpc.ViaggioDTO;
import it.trenical.common.stazioni.Stazione;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class ClientApp extends Application {

    private String nomeUtente = "Davide Iaquinta";
    private String emailUtente = "giaq10@email.com";
    private boolean abbonamentoFedelta = true;

    private TabPane mainTabPane;
    private VBox layoutCarrello;

    private Timer timerNotifiche;

    private ListView<ViaggioDTO> viaggiListView;
    private ListView<BigliettoDTO> bigliettiListView;
    private ControllerTrenical controllerTrenical;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Avvio GUI Cliente TreniCal...");

        VBox mainLayout = creaInterfacciaPrincipale();
        configuraVisualizazioneViaggi();
        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setTitle("TreniCal " + nomeUtente);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Chiusura GUI Cliente");
        });

        primaryStage.show();
        avviaPollingNotifiche();
        System.out.println("GUI Cliente avviata con successo!");
    }

    public void avviaPollingNotifiche() {
        timerNotifiche = new Timer();
        timerNotifiche.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    ControllerTrenical.RisultatoNotifichePendenti risultato =
                            controllerTrenical.controllaNotifichePendenti(emailUtente);

                    if (risultato.ciSonoNotifiche()) {
                        Platform.runLater(() -> {
                            mostraNotifica(risultato.getMessaggio());
                        });
                    }

                } catch (Exception e) {
                    System.err.println("Errore polling notifiche: " + e.getMessage());
                }
            }
        }, 10000, 10000);
    }

    private VBox creaInterfacciaPrincipale() {
        VBox mainLayout = new VBox();
        mainLayout.setSpacing(0);
        HBox header = creaHeader();
        mainTabPane = creaTabPane();
        mainLayout.getChildren().addAll(header, mainTabPane);
        return mainLayout;
    }

    private HBox creaHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #2c3e50;");

        InputStream imageStream = getClass().getResourceAsStream("/trenical_logo.png");
        Image logoImage = new Image(imageStream);
        ImageView logoImageView = new ImageView(logoImage);

        logoImageView.setFitHeight(130);
        logoImageView.setFitWidth(130);
        logoImageView.setPreserveRatio(true);

        HBox logoBox = new HBox(5);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.getChildren().addAll(logoImageView);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label infoUtente = new Label( nomeUtente);
        infoUtente.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Label badgeFedelta = new Label(abbonamentoFedelta ? "Fedeltà" : "");
        badgeFedelta.setStyle("-fx-font-size: 12px; -fx-text-fill: gold; -fx-font-weight: bold;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> {
        });

        header.getChildren().addAll(logoBox, spacer, infoUtente, badgeFedelta, logoutBtn);
        return header;
    }

    private TabPane creaTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab ricercaTab = new Tab("Ricerca Viaggi");
        ricercaTab.setContent(creaTabRicercaViaggi());

        Tab bigliettiTab = new Tab("I Miei Biglietti");
        bigliettiTab.setContent(creaTabBiglietti());

        Tab carrelloTab = new Tab("Carrello");
        carrelloTab.setContent(creaTabCarrello());

        Tab profiloTab = new Tab("Profilo");
        profiloTab.setContent(creaTabProfilo());

        tabPane.getTabs().addAll(ricercaTab, bigliettiTab, carrelloTab, profiloTab);

        tabPane.getSelectionModel().select(0);

        return tabPane;
    }

    private VBox creaTabRicercaViaggi() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Ricerca Viaggi");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox ricercaBox = creaRicerca();

        VBox risultatiBox = creaAreaRisultatiRicerca();

        layout.getChildren().addAll(title, ricercaBox, risultatiBox);
        return layout;
    }

    private VBox creaRicerca() {
        VBox formBox = new VBox(15);
        formBox.setStyle("-fx-background-color: lightgray; -fx-padding: 20; -fx-background-radius: 8;");

        Label formTitle = new Label("Parametri di Ricerca");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        Label partenzaLabel = new Label("Stazione Partenza:");
        ComboBox<String> partenzaCombo = new ComboBox<>();
        partenzaCombo.setPromptText("Seleziona partenza");

        Label arrivoLabel = new Label("Stazione Arrivo:");
        ComboBox<String> arrivoCombo = new ComboBox<>();
        arrivoCombo.setPromptText("Seleziona arrivo");

        List<Stazione> stazioni = Stazione.getTutteLeStazioni();
        for(Stazione s : stazioni) {
            partenzaCombo.getItems().add(s.toString());
            arrivoCombo.getItems().add(s.toString());
        }

        Label dataLabel = new Label("Data Viaggio:");
        DatePicker dataPicker = new DatePicker(LocalDate.now());

        grid.add(partenzaLabel, 0, 0);
        grid.add(partenzaCombo, 0, 1);
        grid.add(arrivoLabel, 1, 0);
        grid.add(arrivoCombo, 1, 1);

        grid.add(dataLabel, 2, 0);
        grid.add(dataPicker, 2, 1);

        Button cercaBtn = new Button("Cerca Viaggi");
        cercaBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        cercaBtn.setOnAction(e -> {
            String partenza = partenzaCombo.getValue();
            String arrivo = arrivoCombo.getValue();
            LocalDate data = dataPicker.getValue();

            if (partenza == null || arrivo == null || data == null) {
                mostraErrore("Campi Mancanti", "Seleziona stazione di partenza, arrivo e data");
                return;
            }

            if (partenza.equals(arrivo)) {
                mostraErrore("Errore Selezione", "Stazione di partenza e arrivo devono essere diverse");
                return;
            }

            if (data.isBefore(LocalDate.now())) {
                mostraErrore("Data Non Valida", "Non puoi cercare viaggi nel passato");
                return;
            }

            Command cercaCommand = new CercaViaggiCommand(controllerTrenical, this,partenza, arrivo, data);
            cercaCommand.execute();
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.getChildren().add(cercaBtn);

        formBox.getChildren().addAll(formTitle, grid, buttonBox);
        return formBox;
    }

    public ListView<ViaggioDTO> getViaggiListView() {
        return viaggiListView;
    }

    private void configuraVisualizazioneViaggi() {
        viaggiListView.setCellFactory(listView -> new ListCell<ViaggioDTO>() {
            @Override
            protected void updateItem(ViaggioDTO viaggio, boolean empty) {
                super.updateItem(viaggio, empty);

                if (empty || viaggio == null) {
                    setText(null);
                } else {
                    String testoFormattato = String.format("%s | %s-%s | %s-%s | €%.2f",
                            viaggio.getTipoTreno(),
                            viaggio.getStazionePartenza(),
                            viaggio.getStazioneArrivo(),
                            viaggio.getOrarioPartenza(),
                            viaggio.getOrarioArrivo(),
                            viaggio.getPrezzo()
                    );
                    setText(testoFormattato);
                }
            }
        });
    }

    private VBox creaAreaRisultatiRicerca() {
        VBox risultatiBox = new VBox(10);
        risultatiBox.setStyle("-fx-background-color: lightgray; -fx-padding: 15; -fx-background-radius: 8;");

        Label risultatiTitle = new Label("Risultati Ricerca");
        risultatiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        viaggiListView = new ListView<>();
        viaggiListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold;");

        viaggiListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ViaggioDTO viaggioSelezionato = viaggiListView.getSelectionModel().getSelectedItem();
                if (viaggioSelezionato != null) {
                    apriDettaglioViaggio(viaggioSelezionato);
                }
            }
        });

        Label placeholderLabel = new Label("Nessun viaggio disponibile");
        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-font-style: italic;");
        viaggiListView.setPlaceholder(placeholderLabel);

        risultatiBox.getChildren().addAll(risultatiTitle, viaggiListView);
        return risultatiBox;
    }

    private void apriDettaglioViaggio(ViaggioDTO viaggio) {
        Stage dettaglioStage = new Stage();
        dettaglioStage.setTitle("Dettaglio Viaggio ");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        Label titoloDettaglio = new Label("Dettaglio Viaggio " + viaggio.getId());
        titoloDettaglio.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 8;");

        Label infoLabel = new Label("Informazioni Viaggio");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);

        int riga = 0;

        infoGrid.add(new Label("Tratta:"), 0, riga);
        Label trattaLabel = new Label(viaggio.getStazionePartenza() + " - " + viaggio.getStazioneArrivo());
        trattaLabel.setStyle("-fx-font-weight: bold;");
        infoGrid.add(trattaLabel, 1, riga++);

        infoGrid.add(new Label("Partenza:"), 0, riga);
        infoGrid.add(new Label(viaggio.getDataPartenza()+" alle "+viaggio.getOrarioPartenza()), 1, riga++);

        infoGrid.add(new Label("Arrivo:"), 0, riga);
        infoGrid.add(new Label(viaggio.getDataArrivo()+" alle "+viaggio.getOrarioArrivo()), 1, riga++);

        infoGrid.add(new Label("Treno:"), 0, riga);
        infoGrid.add(new Label(viaggio.getTipoTreno()), 1, riga++);

        infoGrid.add(new Label("Servizi:"), 0, riga);
        for(String servizio : viaggio.getServizi().split(",")) {
            Label servizioLabel = new Label(servizio);
            infoGrid.add(servizioLabel, 1, riga++);
        }

        infoGrid.add(new Label("Durata:"), 0, riga);
        infoGrid.add(new Label(viaggio.getDurataFormattata()), 1, riga++);

        infoGrid.add(new Label("Distanza:"), 0, riga);
        infoGrid.add(new Label(viaggio.getDistanzaKm() + " km"), 1, riga++);

        infoGrid.add(new Label("Posti disponibili:"), 0, riga);
        infoGrid.add(new Label(String.valueOf(viaggio.getPostiDisponibili())), 1, riga++);

        infoGrid.add(new Label("Binario:"), 0, riga);
        infoGrid.add(new Label(viaggio.getBinario()), 1, riga++);

        infoBox.getChildren().addAll(infoLabel, infoGrid);

        VBox acquistaBox = new VBox(10);
        acquistaBox.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15; -fx-background-radius: 8;");

        Label acquistaLabel = new Label("Acquista Biglietto");
        acquistaLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox prezzoBox = new HBox(10);
        prezzoBox.setAlignment(Pos.CENTER_LEFT);

        Label prezzoLabel = new Label("Prezzo :");
        prezzoLabel.setStyle("-fx-text-fill: white;");

        Label prezzoValue = new Label("€" + String.format("%.2f", viaggio.getPrezzo()));
        prezzoValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        prezzoBox.getChildren().addAll(prezzoLabel, prezzoValue);

        HBox contatoreBox = new HBox(10);
        contatoreBox.setAlignment(Pos.CENTER_LEFT);

        Label qntLabel = new Label("Quantità:");
        qntLabel.setStyle("-fx-text-fill: white;");

        Spinner<Integer> qntSpinner = new Spinner<>(1, viaggio.getPostiDisponibili(), 1);
        qntSpinner.setPrefWidth(80);
        qntSpinner.setStyle("-fx-background-color: white;");

        Label totaleLabel = new Label("Totale: €" + String.format("%.2f", viaggio.getPrezzo()));
        totaleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        qntSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            double totale = viaggio.getPrezzo() * newValue;
            totaleLabel.setText("Totale: €" + String.format("%.2f", totale));
        });

        contatoreBox.getChildren().addAll(qntLabel, qntSpinner, totaleLabel);

        HBox azioniBox = new HBox(10);
        azioniBox.setAlignment(Pos.CENTER);

        Button aggiungiCarrelloBtn = new Button("Aggiungi al Carrello");
        aggiungiCarrelloBtn.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold;");
        aggiungiCarrelloBtn.setOnAction(e -> {
            int quantita = qntSpinner.getValue();

            AggiungiCarrelloCommand command = new AggiungiCarrelloCommand(
                    controllerTrenical,
                    this,
                    quantita,
                    emailUtente,
                    viaggio
            );

            command.execute();
            dettaglioStage.close();
        });

        Button chiudiBtn = new Button("Chiudi");
        chiudiBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        chiudiBtn.setOnAction(e -> dettaglioStage.close());

        azioniBox.getChildren().addAll(aggiungiCarrelloBtn, chiudiBtn);

        acquistaBox.getChildren().addAll(acquistaLabel, prezzoBox, contatoreBox, azioniBox);

        mainLayout.getChildren().addAll(titoloDettaglio, infoBox, acquistaBox);

        Scene dettaglioScene = new Scene(new ScrollPane(mainLayout));
        dettaglioStage.setScene(dettaglioScene);
        dettaglioStage.setResizable(false);
        dettaglioStage.show();
    }

    private VBox creaTabBiglietti() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("I Miei Biglietti");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox risultatiBigliettiBox = creaAreaRisultatiBiglietti();

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        Button caricaBigliettiBtn = new Button("Carica Biglietti");
        caricaBigliettiBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        caricaBigliettiBtn.setOnAction(e -> {
            Command caricaCommand = new VisualizzaBigliettiCommand(controllerTrenical, this, emailUtente);
            caricaCommand.execute();
        });

        actionsBox.getChildren().addAll(caricaBigliettiBtn);
        risultatiBigliettiBox.getChildren().addAll(actionsBox);

        layout.getChildren().addAll(title, risultatiBigliettiBox);
        return layout;
    }

    private VBox creaAreaRisultatiBiglietti() {
        VBox risultatiBox = new VBox();
        risultatiBox.setStyle("-fx-background-color: lightgray; -fx-padding: 15; -fx-background-radius: 8;");

        bigliettiListView = new ListView<>();
        bigliettiListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold;");

        configuraBigliettiListView();

        bigliettiListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BigliettoDTO bigliettoSelezionato = bigliettiListView.getSelectionModel().getSelectedItem();
                apriDettaglioBiglietto(bigliettoSelezionato);
            }
        });

        Label placeholderLabel = new Label("Nessun biglietto caricato.");
        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-font-style: italic;");
        bigliettiListView.setPlaceholder(placeholderLabel);

        risultatiBox.getChildren().addAll(bigliettiListView);
        return risultatiBox;
    }

    private void configuraBigliettiListView() {
        bigliettiListView.setCellFactory(listView -> new ListCell<BigliettoDTO>() {
            @Override
            protected void updateItem(BigliettoDTO biglietto, boolean empty) {
                super.updateItem(biglietto, empty);

                if (empty || biglietto == null) {
                    setText(null);
                } else {
                    String testoFormattato = String.format("%s | %s->%s | %s %s | €%.2f",
                            biglietto.getNominativo(),
                            biglietto.getStazionePartenza(),
                            biglietto.getStazioneArrivo(),
                            biglietto.getDataViaggio(),
                            biglietto.getOrarioPartenza(),
                            biglietto.getPrezzo()
                    );
                    setText(testoFormattato);
                }
            }
        });
    }

    private void apriDettaglioBiglietto(BigliettoDTO biglietto) {
        Stage dettaglioStage = new Stage();
        dettaglioStage.setTitle("Dettaglio Biglietto - " + biglietto.getNominativo());

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        Label titolo = new Label("Dettaglio Biglietto");
        titolo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");

        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label infoBiglietto = new Label(String.format(
                "Nominativo: %s\n" +
                "Tratta: %s -> %s\n" +
                "Partenza: %s\n" +
                "Orario: %s - %s\n" +
                "Tipo Treno: %s\n" +
                "Binario: %s\n" +
                "Durata: %s\n" +
                "Prezzo: €%.2f",
                biglietto.getNominativo(),
                biglietto.getStazionePartenza(),
                biglietto.getStazioneArrivo(),
                biglietto.getDataViaggio(),
                biglietto.getOrarioPartenza(),
                biglietto.getOrarioArrivo(),
                biglietto.getTipoTreno(),
                biglietto.getBinario(),
                biglietto.getDurataFormattata(),
                biglietto.getPrezzo()
        ));

        infoBiglietto.setStyle("-fx-font-size: 12px; -fx-font-family: 'bold'; -fx-text-fill: black;");
        infoBox.getChildren().add(infoBiglietto);

        VBox modificaBox = new VBox(10);
        modificaBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");

        Label modificaLabel = new Label("Modifica Biglietto");
        modificaLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");

        HBox bottoniModificaBox = new HBox(10);
        bottoniModificaBox.setAlignment(Pos.CENTER);

        Button modificaClasseBtn = new Button("Modifica Classe");
        modificaClasseBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; ");
        modificaClasseBtn.setOnAction(e -> {
            Command modificaClasseCommand = new ModificaClasseCommand(controllerTrenical, this, biglietto, emailUtente);
            modificaClasseCommand.execute();
            dettaglioStage.close();
        });

        Button modificaOrarioBtn = new Button("Modifica Orario");
        modificaOrarioBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        modificaOrarioBtn.setOnAction(e -> {
            Command modificaOrarioCommand = new ModificaOrarioCommand(controllerTrenical, this, biglietto, emailUtente);
            modificaOrarioCommand.execute();
            dettaglioStage.close();
        });

        Button modificaDataBtn = new Button("Modifica Data");
        modificaDataBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        modificaDataBtn.setOnAction(e -> {
            Command modificaDataCommand = new ModificaDataCommand(controllerTrenical, this, biglietto, emailUtente);
            modificaDataCommand.execute();
            dettaglioStage.close();
        });

        bottoniModificaBox.getChildren().addAll(modificaClasseBtn, modificaOrarioBtn, modificaDataBtn);
        modificaBox.getChildren().addAll(modificaLabel, bottoniModificaBox);

        HBox azioniBox = new HBox(10);
        azioniBox.setAlignment(Pos.CENTER);

        Button chiudiBtn = new Button("Chiudi");
        chiudiBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        chiudiBtn.setOnAction(e -> dettaglioStage.close());

        azioniBox.getChildren().add(chiudiBtn);

        mainLayout.getChildren().addAll(titolo, infoBox, modificaBox, azioniBox);

        Scene dettaglioScene = new Scene(new ScrollPane(mainLayout));
        dettaglioStage.setScene(dettaglioScene);
        dettaglioStage.setResizable(false);
        dettaglioStage.show();
    }

    public void mostraSelezionaNuovoViaggio(List<ViaggioDTO> viaggiDisponibili,
                                            BigliettoDTO bigliettoCorrente,
                                            String emailUtente,
                                            String titolo) {
        Stage selezioneStage = new Stage();
        selezioneStage.setTitle(titolo);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        Label titoloLabel = new Label("Seleziona il nuovo viaggio");
        titoloLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        ListView<ViaggioDTO> viaggiListView = new ListView<>();
        viaggiListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold;");

        viaggiListView.setCellFactory(listView -> new ListCell<ViaggioDTO>() {
            @Override
            protected void updateItem(ViaggioDTO viaggio, boolean empty) {
                super.updateItem(viaggio, empty);

                if (empty || viaggio == null) {
                    setText(null);
                } else {
                    String testoViaggio = String.format(
                            "%s %s | %s-%s | Partenza: %s | Arrivo: %s | €%.2f | Posti: %d",
                            viaggio.getTipoTreno(),
                            viaggio.getCodiceTreno(),
                            viaggio.getStazionePartenza(),
                            viaggio.getStazioneArrivo(),
                            viaggio.getOrarioPartenza(),
                            viaggio.getOrarioArrivo(),
                            viaggio.getPrezzo(),
                            viaggio.getPostiDisponibili()
                    );
                    setText(testoViaggio);
                }
            }
        });

        viaggiListView.getItems().addAll(viaggiDisponibili);
        viaggiListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ViaggioDTO viaggioSelezionato = viaggiListView.getSelectionModel().getSelectedItem();
                if (viaggioSelezionato != null) {
                    selezioneStage.close();
                    confermaEEseguiModifica(bigliettoCorrente, viaggioSelezionato, emailUtente);
                }
            }
        });

        mainLayout.getChildren().addAll(titoloLabel, viaggiListView);

        Scene selezioneScene = new Scene(mainLayout, 650, 400);
        selezioneStage.setScene(selezioneScene);
        selezioneStage.show();
    }

    public void confermaEEseguiModifica(BigliettoDTO bigliettoCorrente, ViaggioDTO nuovoViaggio,
                                        String emailUtente) {
        try {
            double differenza = nuovoViaggio.getPrezzo() - bigliettoCorrente.getPrezzo();
            String messaggio;
            if(differenza > 0) {
                messaggio = String.format("Penale da pagare: €%.2f \n" +
                        "Confermi la modifica?", differenza);
            }
            else {
                messaggio = String.format("Differenza di: €%.2f.\n " +
                        "Non verrà effettuato nessun rimborso. Confermi la modifica?", differenza * -1);
            }

            Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
            conferma.setTitle("Conferma Modifica Biglietto");
            conferma.setContentText(messaggio);

            Optional<ButtonType> risultato = conferma.showAndWait();

            if (risultato.isPresent() && risultato.get() == ButtonType.OK) {
                Command eseguiModificaCommand = new ModificaBigliettoCommand(
                        controllerTrenical, this, bigliettoCorrente.getId(), nuovoViaggio.getId(), emailUtente
                );
                eseguiModificaCommand.execute();
            }

        } catch (Exception e) {
            mostraErrore("Errore", "Errore nella conferma modifica: " + e.getMessage());
        }
    }

    public ListView<BigliettoDTO> getBigliettiListView() {
        return bigliettiListView;
    }

    private VBox creaTabCarrello() {
        layoutCarrello = new VBox(15);
        layoutCarrello.setPadding(new Insets(20));

        Label title = new Label("Carrello");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        aggiornaContenutoCarrello();

        layoutCarrello.getChildren().add(title);
        return layoutCarrello;
    }

    public void aggiornaTabCarrello() {
        layoutCarrello.getChildren().clear();

        Label title = new Label("Carrello");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        layoutCarrello.getChildren().add(title);

        aggiornaContenutoCarrello();
    }

    private void aggiornaContenutoCarrello() {
        GestoreCarrello carrello = GestoreCarrello.getInstance();

        if (carrello.isVuoto()) {
            Label vuotoLabel = new Label("Il carrello è vuoto");
            vuotoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-font-weight: bold;");
            layoutCarrello.getChildren().add(vuotoLabel);
        } else {
            VBox carrelloBox = new VBox(10);
            carrelloBox.setStyle("-fx-background-color: lightgray; -fx-padding: 15; -fx-background-radius: 8;");

            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label bigliettiTitle = new Label("Biglietti nel Carrello (" + carrello.getTotaleBiglietti() + ")");
            bigliettiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label prezzoTotale = new Label("Totale: €" + String.format("%.2f", carrello.getPrezzoTotale()));
            prezzoTotale.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");

            VBox timerBox = new VBox(2);
            timerBox.setAlignment(Pos.CENTER);

            Label timerText = new Label("Tempo rimanente:");
            timerText.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

            Label timerLabel = carrello.getTimerLabel();

            timerBox.getChildren().addAll(timerText, timerLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            headerBox.getChildren().addAll(bigliettiTitle, spacer, timerBox, prezzoTotale);
            carrelloBox.getChildren().add(headerBox);

            for (CarrelloItem item : carrello.getCarrelloItems()) {
                HBox bigliettoBox = new HBox(10);
                bigliettoBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");
                bigliettoBox.setAlignment(Pos.CENTER_LEFT);

                VBox infoBox = new VBox(2);

                String infoBiglietto = String.format(
                        "%s, %s-%s, partenza: %s alle %s, arrivo: %s alle %s. Quantita:%d",
                        item.getViaggio().getTipoTreno(),
                        item.getViaggio().getStazionePartenza(),
                        item.getViaggio().getStazioneArrivo(),
                        item.getViaggio().getDataPartenza(),
                        item.getViaggio().getOrarioPartenza(),
                        item.getViaggio().getDataArrivo(),
                        item.getViaggio().getOrarioArrivo(),
                        item.getQuantita()
                );
                Label infoBigliettoLabel = new Label(infoBiglietto);
                infoBigliettoLabel.setStyle("-fx-font-weight: bold;");

                infoBox.getChildren().addAll(infoBigliettoLabel);

                Region spacerBiglietto = new Region();
                HBox.setHgrow(spacerBiglietto, Priority.ALWAYS);

                Label prezzoBiglietto = new Label("€" + String.format("%.2f", item.getPrezzo()));
                prezzoBiglietto.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");

                bigliettoBox.getChildren().addAll(infoBox, spacerBiglietto, prezzoBiglietto);

                carrelloBox.getChildren().add(bigliettoBox);
            }

            HBox azioniBox = new HBox(10);
            azioniBox.setAlignment(Pos.CENTER);

            Button svuotaBtn = new Button("Svuota Carrello");
            svuotaBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            svuotaBtn.setOnAction(e -> {
                carrello.svuotaCarrello();
                aggiornaTabCarrello();
            });

            Button acquistaBtn = new Button("Acquista Tutto");
            acquistaBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            acquistaBtn.setOnAction(e -> resocontoAcquisto());

            azioniBox.getChildren().addAll(svuotaBtn, acquistaBtn);
            carrelloBox.getChildren().add(azioniBox);

            layoutCarrello.getChildren().add(carrelloBox);
        }
    }

    private void resocontoAcquisto() {
        GestoreCarrello carrello = GestoreCarrello.getInstance();
        List<CarrelloItem> items = carrello.getCarrelloItems();
        int totaleBiglietti = carrello.getTotaleBiglietti();

        Stage resocontoStage = new Stage();
        resocontoStage.setTitle("Conferma Acquisto - " + totaleBiglietti + " biglietti");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Inserisci i nominativi per i biglietti");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: lightgray; -fx-padding: 10; -fx-background-radius: 5;");

        for (CarrelloItem item : items) {
            Label infoLabel = new Label(String.format("%dx %s-%s %s (€%.2f )",
                    item.getQuantita(),
                    item.getViaggio().getStazionePartenza(),
                    item.getViaggio().getStazioneArrivo(),
                    item.getViaggio().getDataPartenza(),
                    item.getPrezzo()));
            infoLabel.setStyle("-fx-font-size: 12px;");
            infoBox.getChildren().add(infoLabel);
        }

        Label totaleLabel = new Label("Totale: €" + String.format("%.2f", carrello.getPrezzoTotale()));
        totaleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");
        infoBox.getChildren().add(totaleLabel);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(200);
        scrollPane.setFitToWidth(true);

        VBox nominativiBox = new VBox(10);
        List<TextField> textFields = new ArrayList<>();

        int bigliettoNum = 1;
        for (CarrelloItem item : items) {
            for (int i = 0; i < item.getQuantita(); i++) {
                HBox bigliettoRow = new HBox(10);
                bigliettoRow.setAlignment(Pos.CENTER_LEFT);

                Label numeroLabel = new Label("Biglietto " + bigliettoNum + ":");
                numeroLabel.setPrefWidth(80);
                numeroLabel.setStyle("-fx-font-weight: bold;");

                Label viaggioLabel = new Label(item.getViaggio().getStazionePartenza() +
                        " -> " + item.getViaggio().getStazioneArrivo());
                viaggioLabel.setPrefWidth(150);
                viaggioLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                TextField nominativoField = new TextField();
                nominativoField.setPromptText("Inserisci nominativo...");
                nominativoField.setPrefWidth(200);
                textFields.add(nominativoField);

                bigliettoRow.getChildren().addAll(numeroLabel, viaggioLabel, nominativoField);
                nominativiBox.getChildren().add(bigliettoRow);
                bigliettoNum++;
            }
        }

        scrollPane.setContent(nominativiBox);

        HBox pagamentoBox = new HBox(10);
        pagamentoBox.setAlignment(Pos.CENTER_LEFT);

        Label pagamentoLabel = new Label("Modalità di pagamento:");
        pagamentoLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> pagamentoCombo = new ComboBox<>();
        pagamentoCombo.getItems().addAll("Carta di Credito", "Wallet", "Bonifico Bancario");
        pagamentoCombo.setValue("Carta di Credito");
        pagamentoCombo.setPrefWidth(150);

        pagamentoBox.getChildren().addAll(pagamentoLabel, pagamentoCombo);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button annullaBtn = new Button("Annulla");
        annullaBtn.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-pref-width: 100;");
        annullaBtn.setOnAction(e -> resocontoStage.close());

        Button confermaBtn = new Button("Conferma Acquisto");
        confermaBtn.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-pref-width: 150;");
        confermaBtn.setOnAction(e -> {
            List<String> nominativi = new ArrayList<>();
            boolean tuttiValidi = true;

            for (TextField field : textFields) {
                String nominativo = field.getText().trim();
                if (nominativo.isEmpty()) {
                    field.setStyle("-fx-border-color: red;");
                    tuttiValidi = false;
                } else {
                    nominativi.add(nominativo);
                }
            }
            if (!tuttiValidi) {
                mostraErrore("Errore", "Inserisci tutti i nominativi richiesti");
                return;
            }
            ConfermaAcquistoCommand command = new ConfermaAcquistoCommand(
                    controllerTrenical,
                    this,
                    items,
                    nominativi,
                    pagamentoCombo.getValue(),
                    emailUtente
            );

            command.execute();
            resocontoStage.close();
        });

        buttonsBox.getChildren().addAll(annullaBtn, confermaBtn);

        mainLayout.getChildren().addAll(titleLabel, infoBox,
                new Label("Nominativi:"), scrollPane,
                pagamentoBox, buttonsBox);

        Scene scene = new Scene(mainLayout, 500, 500);
        resocontoStage.setScene(scene);
        resocontoStage.showAndWait();
    }

    private VBox creaTabProfilo() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label title = new Label("Profilo Utente");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox profiloBox = new VBox(15);
        profiloBox.setStyle("-fx-background-color: lightgray; -fx-padding: 20; -fx-background-radius: 8;");

        Label profiloTitle = new Label("Dati Personali");
        profiloTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane profiloGrid = new GridPane();
        profiloGrid.setHgap(15);
        profiloGrid.setVgap(10);

        Label nomeLabel = new Label("Nome:");
        TextField nomeField = new TextField(nomeUtente);
        nomeField.setEditable(false);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(emailUtente);
        emailField.setEditable(false);

        profiloGrid.add(nomeLabel, 0, 0);
        profiloGrid.add(nomeField, 1, 0);
        profiloGrid.add(emailLabel, 0, 1);
        profiloGrid.add(emailField, 1, 1);

        HBox profiloActionsBox = new HBox(10);
        profiloActionsBox.setAlignment(Pos.CENTER);

        Button abbonatiBtn = new Button("Abbonati al Programma Fedeltà");
        abbonatiBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        abbonatiBtn.setOnAction(e -> {
        });

        profiloActionsBox.getChildren().addAll(abbonatiBtn);

        profiloBox.getChildren().addAll(profiloTitle, profiloGrid, profiloActionsBox);
        layout.getChildren().addAll(title, profiloBox);
        return layout;
    }

    public void mostraNotifica(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifica TreniCal");
        alert.setHeaderText("Notifica Importante");
        alert.setContentText(messaggio);


        alert.show();
    }

    public void inviaEMostraNotifica(String tipoNotifica, String emailUtente) {
        try {
            ControllerTrenical.RisultatoNotifica risultato =
                    controllerTrenical.inviaNotifica(tipoNotifica, emailUtente);

            if (risultato.isSuccesso()) {
                mostraNotifica(risultato.getMessaggio());
            } else {
                mostraErrore("Errore Notifica", risultato.getMessaggio());
            }

        } catch (Exception e) {
            mostraErrore("Errore Sistema",
                    "Errore durante l'invio della notifica: " + e.getMessage());
        }
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
        System.out.println("Avvio GUI Cliente TreniCal");
        launch(args);
    }

    public void setTrenicalService(ControllerTrenical controllerTrenical) {
        this.controllerTrenical = controllerTrenical;
        System.out.println("ControllerTrenical configurato per ClientApp");
    }
    public ControllerTrenical getTrenicalService() {
        return this.controllerTrenical;
    }
}