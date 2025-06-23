package it.trenical.client.carrello;

import it.trenical.grpc.BigliettoCarrelloDTO;
import javafx.scene.control.Label;

import java.util.*;
import java.util.logging.Logger;

public class GestoreCarrello {

    private static final Logger logger = Logger.getLogger(GestoreCarrello.class.getName());
    private static GestoreCarrello instance;

    private final List<BigliettoCarrelloDTO> bigliettiCarrello;
    private TimerCarrello timer;

    private GestoreCarrello() {
        this.bigliettiCarrello = new ArrayList<>();
        logger.info("GestoreCarrello inizializzato");
    }

    public static GestoreCarrello getInstance() {
        if (instance == null) {
            instance = new GestoreCarrello();
        }
        return instance;
    }

    public void aggiungiBiglietti(List<BigliettoCarrelloDTO> bigliettiDTO) {
        logger.info("Aggiunta di " + bigliettiDTO.size() + " biglietti al carrello");

        bigliettiCarrello.addAll(bigliettiDTO);
        avviaTimer();

        logger.info("Carrello aggiornato - Totale biglietti: " + bigliettiCarrello.size());
    }

    private void avviaTimer() {
        if(timer!=null)
            timer.stopTimer();
        timer = new TimerCarrello();
        timer.startTimer();
        logger.info("Timer carrello avviato");
    }

    public void svuotaCarrello() {
        if (!bigliettiCarrello.isEmpty()) {
            int numBiglietti = bigliettiCarrello.size();
            bigliettiCarrello.clear();
            logger.info("Carrello svuotato - " + numBiglietti + " biglietti rimossi");
        }
    }

    public void rimuoviBiglietto(String idTemporaneo) {
        Iterator<BigliettoCarrelloDTO> iterator = bigliettiCarrello.iterator();
        while (iterator.hasNext()) {
            BigliettoCarrelloDTO biglietto = iterator.next();
            if (biglietto.getIdTemporaneo().equals(idTemporaneo)) {
                iterator.remove();
                logger.info("Biglietto rimosso: " + idTemporaneo);
            }
        }
        logger.warning("Tentativo di rimozione biglietto inesistente: " + idTemporaneo);
    }

    public List<BigliettoCarrelloDTO> confermaAcquisto() {
        List<BigliettoCarrelloDTO> bigliettiDaAcquistare = new ArrayList<>(bigliettiCarrello);

        svuotaCarrello();
        logger.info("Acquisto confermato - " + bigliettiDaAcquistare.size() + " biglietti");
        return bigliettiDaAcquistare;
    }

    public Label getTimerLabel() {
        if (timer == null) {
            timer = new TimerCarrello();
        }
        return timer.getTimerLabel();
    }

    public List<BigliettoCarrelloDTO> getBigliettiCarrello() {
        return new ArrayList<>(bigliettiCarrello);
    }

    public boolean isVuoto() {
        return bigliettiCarrello.isEmpty();
    }

    public int size() {
        return bigliettiCarrello.size();
    }

    public double getPrezzoTotale() {
        double prezzoTotale = 0;
        for(BigliettoCarrelloDTO b : bigliettiCarrello) {
            prezzoTotale += b.getPrezzo();
        }
        return prezzoTotale;
    }
}