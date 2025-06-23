package it.trenical.client.carrello;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import java.time.Duration;
import java.time.LocalTime;

public class TimerCarrello {

    private final int TEMPO = 60;
    private final int AVVISO = 10;

    private Label timerLabel = new Label();
    private boolean avvisoStampato = false;
    private AnimationTimer timer;

    public TimerCarrello() {
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        timerLabel.setText("00:10");
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
                    }
                } else {
                    System.out.println("Timer carrello terminato");
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

}