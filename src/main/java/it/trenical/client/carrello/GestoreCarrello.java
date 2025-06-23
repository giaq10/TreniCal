package it.trenical.client.carrello;

import it.trenical.grpc.ViaggioDTO;
import javafx.scene.control.Label;

import java.util.*;
import java.util.logging.Logger;

public class GestoreCarrello {

    private static final Logger logger = Logger.getLogger(GestoreCarrello.class.getName());
    private static GestoreCarrello instance;

    private final List<CarrelloItem> carrielloItems;
    private TimerCarrello timer;

    private GestoreCarrello() {
        this.carrielloItems = new ArrayList<>();
        logger.info("GestoreCarrello inizializzato");
    }

    public static GestoreCarrello getInstance() {
        if (instance == null) {
            instance = new GestoreCarrello();
        }
        return instance;
    }

    public void aggiungiItem(CarrelloItem nuovoItem) {
        System.out.println("=== DEBUG AGGIUNGI ITEM ===");
        System.out.println("Nuovo item: " + nuovoItem.getViaggioId() );
        System.out.println("Carrello attuale size: " + carrielloItems.size());

        for (int i = 0; i < carrielloItems.size(); i++) {
            CarrelloItem existing = carrielloItems.get(i);
            System.out.println("Item " + i + ": " + existing.getViaggioId() );
        }
        logger.info("Aggiunta item carrello: " + nuovoItem.toString());

        CarrelloItem esistente = null;
        for (CarrelloItem item : carrielloItems) {
            if (item.getViaggioId().equals(nuovoItem.getViaggioId())) {
                esistente = item;
                break;
            }
        }

        if (esistente != null) {
            esistente.incrementaQuantita(nuovoItem.getQuantita());
            logger.info("Viaggio già presente - Quantità aggiornata a: " + esistente.getQuantita());
        } else {
            carrielloItems.add(nuovoItem);
            logger.info("Nuovo viaggio aggiunto al carrello");
        }

        avviaTimer();
        logger.info("Carrello aggiornato - Totale viaggi: " + carrielloItems.size() +
                ", Totale biglietti: " + getTotaleBiglietti());
    }

    private void avviaTimer() {
        if(timer != null)
            timer.stopTimer();
        timer = new TimerCarrello();
        timer.startTimer();
        logger.info("Timer carrello avviato");
    }

    public void svuotaCarrello() {
        if (!carrielloItems.isEmpty()) {
            int numItems = carrielloItems.size();
            int totaleBiglietti = getTotaleBiglietti();
            carrielloItems.clear();
            logger.info("Carrello svuotato - " + numItems + " items (" + totaleBiglietti + " biglietti) rimossi");
        }
    }

    public void rimuoviViaggio(String viaggioId) {
        Iterator<CarrelloItem> iterator = carrielloItems.iterator();
        while (iterator.hasNext()) {
            CarrelloItem item = iterator.next();
            if (item.getViaggioId().equals(viaggioId)) {
                iterator.remove();
                logger.info("Viaggio rimosso dal carrello: " + viaggioId + " (" + item.getQuantita() + " biglietti)");
                return;
            }
        }
        logger.warning("Tentativo di rimozione viaggio inesistente: " + viaggioId);
    }

    public List<CarrelloItem> confermaAcquisto() {
        List<CarrelloItem> itemsDaAcquistare = new ArrayList<>(carrielloItems);

        int totaleBiglietti = getTotaleBiglietti();
        svuotaCarrello();
        logger.info("Acquisto confermato - " + itemsDaAcquistare.size() + " viaggi (" +
                totaleBiglietti + " biglietti totali)");
        return itemsDaAcquistare;
    }

    public Label getTimerLabel() {
        if (timer == null) {
            timer = new TimerCarrello();
        }
        return timer.getTimerLabel();
    }

    public List<CarrelloItem> getCarrelloItems() {
        return new ArrayList<>(carrielloItems);
    }

    public boolean isVuoto() {
        return carrielloItems.isEmpty();
    }

    public int size() {
        return carrielloItems.size();
    }

    public int getTotaleBiglietti() {
        return carrielloItems.stream()
                .mapToInt(CarrelloItem::getQuantita)
                .sum();
    }

    public double getPrezzoTotale() {
        return carrielloItems.stream()
                .mapToDouble(CarrelloItem::getPrezzoTotale)
                .sum();
    }
}