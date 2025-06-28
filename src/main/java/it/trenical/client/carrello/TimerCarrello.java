package it.trenical.client.carrello;

import it.trenical.client.gui.ClientApp;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import java.time.Duration;
import java.time.LocalTime;

public class TimerCarrello {

    private final int TEMPO = 120;
    private final int AVVISO = 90;

    private Label timerLabel = new Label();
    private boolean avvisoStampato = false;
    private boolean terminato = false;
    private AnimationTimer timer;

    private ClientApp clientApp;
    private String emailUtente;

    public TimerCarrello(ClientApp clientApp, String emailUtente) {
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        timerLabel.setText("00:10");
        this.clientApp=clientApp;
        this.emailUtente=emailUtente;
    }

    public Label getTimerLabel() {
        return timerLabel;
    }

    public void startTimer() {
        if (timer != null) {
            timer.stop();
        }

        avvisoStampato = false;
        LocalTime end = LocalTime.now().plusSeconds(TEMPO);
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                Duration remaining = Duration.between(LocalTime.now(), end);

                if (remaining.isPositive()) {
                    timerLabel.setText(format(remaining));

                    long secondsRemaining = remaining.toSeconds();
                    if (secondsRemaining == AVVISO && !avvisoStampato) {
                        System.out.println(AVVISO+" secondi rimanenti nel carrello");
                        avvisoStampato = true;
                        clientApp.inviaEMostraNotifica("SCADENZA_PRENOTAZIONE", emailUtente);
                    }
                } else {
                    System.out.println("Timer carrello terminato");
                    terminato = true;
                    stop();
                    GestoreCarrello.getInstance().svuotaCarrello();
                }
            }
            private String format(Duration remaining) {
                return String.format("%02d:%02d",
                        remaining.toMinutesPart(),
                        remaining.toSecondsPart()
                );
            }
        };
        timer.start();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public boolean isTerminato() {return terminato;}

}