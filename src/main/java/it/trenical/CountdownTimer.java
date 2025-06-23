package it.trenical;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalTime;

public class CountdownTimer extends Application {
    private final int TIMER_SECONDS = 10; //timer di prova da 10 secondi
    private final int WARNING_THRESHOLD = 2; // quando stampare l'avviso

    private Label timerLabel = new Label();
    private boolean warningPrinted = false; // flag per stampare l'avviso una sola volta

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start 10 Seconds Timer");
        startButton.setOnAction(event -> {
            // reset del flag e avvio il countdown
            warningPrinted = false;
            startCountdown();
        });

        // inizializzo il label con il tempo del timer
        timerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        root.getChildren().addAll(startButton, timerLabel);

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("10 Seconds Countdown Timer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startCountdown() {
        LocalTime end = LocalTime.now().plusSeconds(TIMER_SECONDS);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                Duration remaining = Duration.between(LocalTime.now(), end);
                if (remaining.isPositive()) {
                    timerLabel.setText(format(remaining));

                    // controllo se siamo arrivati a 2 secondi rimanenti
                    long secondsRemaining = remaining.toSeconds();
                    if (secondsRemaining == WARNING_THRESHOLD && !warningPrinted) {
                        System.out.println("2 secondi rimanenti");
                        warningPrinted = true; // evitiamo di stampare più volte
                    }
                } else {
                    //timerLabel.setText("00:00:00");
                    System.out.println("Timer terminato");
                    stop();
                }
            }

            private String format(Duration remaining) {
                return String.format("%02d:%02d:%02d",
                        remaining.toHoursPart(),
                        remaining.toMinutesPart(),
                        remaining.toSecondsPart()
                );
            }
        };

        timer.start();
    }
}