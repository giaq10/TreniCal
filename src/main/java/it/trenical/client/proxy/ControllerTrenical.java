package it.trenical.client.proxy;

import io.grpc.*;
import it.trenical.client.carrello.CarrelloItem;
import it.trenical.grpc.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class ControllerTrenical {

    private static final Logger logger = Logger.getLogger(ControllerTrenical.class.getName());

    private Channel channel;
    private TrenicalServiceGrpc.TrenicalServiceBlockingStub blockingStub;

    public ControllerTrenical(String serverAddress) {
        inizializzaConnessione(serverAddress);
    }

    private void inizializzaConnessione(String target) {
        try {
            this.channel = Grpc.newChannelBuilder(
                    target,
                    InsecureChannelCredentials.create()
            ).build();

            this.blockingStub = TrenicalServiceGrpc.newBlockingStub(channel);

            logger.info("Connessione gRPC stabilita con " + target);

        } catch (Exception e) {
            logger.severe("Errore nella connessione gRPC: " + e.getMessage());
            throw new RuntimeException("Impossibile connettersi al server", e);
        }
    }

    public RisultatoRicerca cercaViaggi(String stazionePartenza, String stazioneArrivo, LocalDate dataViaggio) {
        logger.info("Ricerca viaggi: " + stazionePartenza + " -> " + stazioneArrivo + " il " + dataViaggio);

        try {
            RicercaViaggioRequest request = RicercaViaggioRequest.newBuilder()
                    .setStazionePartenza(stazionePartenza)
                    .setStazioneArrivo(stazioneArrivo)
                    .setDataViaggio(dataViaggio.toString())
                    .build();

            RicercaViaggioResponse response = blockingStub.cercaViaggi(request);

            RisultatoRicerca risultato = new RisultatoRicerca(
                    response.getSuccesso(),
                    response.getMessaggio(),
                    response.getViaggiList()
            );

            logger.info("Ricerca completata: " + risultato.getMessaggio());
            return risultato;

        } catch (StatusRuntimeException e) {
            logger.severe("Errore nella chiamata gRPC: " + e.getStatus());

            String messaggio = "Errore di connessione al server";
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                messaggio = "Server non disponibile. Riprova più tardi.";
            }

            return new RisultatoRicerca(false, messaggio, null);

        } catch (Exception e) {
            logger.severe("Errore imprevisto: " + e.getMessage());
            return new RisultatoRicerca(false, "Errore imprevisto: " + e.getMessage(), null);
        }
    }

    public RisultatoCarrello aggiungiAlCarrello(String viaggioId, int quantita, String emailUtente) {
        logger.info("Aggiunta carrello: " + viaggioId + " - Quantità: " + quantita + " - Utente: " + emailUtente);
        try {
            AggiungiCarrelloRequest request = AggiungiCarrelloRequest.newBuilder()
                    .setViaggioId(viaggioId)
                    .setQuantita(quantita)
                    .setEmailUtente(emailUtente)
                    .build();
            AggiungiCarrelloResponse response = blockingStub.aggiungiAlCarrello(request);

            List<CarrelloItem> carrelloItems = new ArrayList<>();
            for (BigliettoCarrelloDTO dto : response.getBigliettiCreatiList()) {
                CarrelloItem item = new CarrelloItem(
                        dto.getViaggioId(),
                        dto.getPrezzo(),
                        dto.getQuantita(),
                        dto.getViaggio()
                );
                carrelloItems.add(item);
            }

            RisultatoCarrello risultato = new RisultatoCarrello(
                    response.getSuccesso(),
                    response.getMessaggio(),
                    response.getPostiRimanenti(),
                    carrelloItems
            );
            logger.info("Aggiunta carrello completata: " + risultato.getMessaggio());
            return risultato;
        } catch (StatusRuntimeException e) {
            logger.severe("Errore nella chiamata gRPC carrello: " + e.getStatus());
            String messaggio = "Errore di connessione al server";
            return new RisultatoCarrello(false, messaggio, 0, new ArrayList<>());
        } catch (Exception e) {
            logger.severe("Errore imprevisto carrello: " + e.getMessage());
            return new RisultatoCarrello(false, "Errore imprevisto: " + e.getMessage(), 0, new ArrayList<>());
        }
    }

    public RisultatoAcquisto confermaAcquisto(List<CarrelloItem> carrelloItems,
                                              List<String> nominativi,
                                              String modalitaPagamento,
                                              String emailUtente) {
        logger.info("Conferma acquisto: " + carrelloItems.size() + " items, " + nominativi.size() + " nominativi");
        System.out.println("=== DEBUG CONTROLLER ===");
        System.out.println("Items: " + carrelloItems.size());
        System.out.println("Nominativi: " + nominativi.size());
        try {
            List<CarrelloItemDTO> itemsDTO = new ArrayList<>();
            for (CarrelloItem item : carrelloItems) {
                CarrelloItemDTO dto = CarrelloItemDTO.newBuilder()
                        .setViaggioId(item.getViaggioId())
                        .setQuantita(item.getQuantita())
                        .setPrezzo(item.getPrezzo())
                        .build();
                itemsDTO.add(dto);
            }

            ConfermaAcquistoRequest request = ConfermaAcquistoRequest.newBuilder()
                    .setEmailUtente(emailUtente)
                    .addAllCarrelloItems(itemsDTO)
                    .addAllNominativi(nominativi)
                    .setModalitaPagamento(modalitaPagamento)
                    .build();

            System.out.println("Invio richiesta al server...");
            ConfermaAcquistoResponse response = blockingStub.confermaAcquisto(request);

            System.out.println("Risposta server:");
            System.out.println("- Successo: " + response.getSuccesso());
            System.out.println("- Messaggio: " + response.getMessaggio());

            return new RisultatoAcquisto(
                    response.getSuccesso(),
                    response.getMessaggio(),
                    response.getBigliettiAcquistati(),
                    response.getPrezzoTotale()
            );

        } catch (Exception e) {
            logger.severe("Errore conferma acquisto: " + e.getMessage());
            System.out.println("ERRORE CONTROLLER: " + e.getMessage());
            e.printStackTrace();
            return new RisultatoAcquisto(false, "Errore: " + e.getMessage(), 0, 0.0);
        }
    }

    public static class RisultatoRicerca {
        private final boolean successo;
        private final String messaggio;
        private final List<ViaggioDTO> viaggi;

        public RisultatoRicerca(boolean successo, String messaggio, List<ViaggioDTO> viaggi) {
            this.successo = successo;
            this.messaggio = messaggio;
            this.viaggi = viaggi;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
        public List<ViaggioDTO> getViaggi() { return viaggi; }
    }

    public static class RisultatoCarrello {
        private final boolean successo;
        private final String messaggio;
        private final int postiRimanenti;
        private final List<CarrelloItem> carrelloItems;  // Cambiato da BigliettoCarrelloDTO

        public RisultatoCarrello(boolean successo, String messaggio, int postiRimanenti,
                                 List<CarrelloItem> carrelloItems) {
            this.successo = successo;
            this.messaggio = messaggio;
            this.postiRimanenti = postiRimanenti;
            this.carrelloItems = carrelloItems;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
        public int getPostiRimanenti() { return postiRimanenti; }
        public List<CarrelloItem> getCarrelloItems() { return carrelloItems; }  // Nuovo nome
    }

    public static class RisultatoAcquisto {
        private final boolean successo;
        private final String messaggio;
        private final int bigliettiAcquistati;
        private final double prezzoTotale;

        public RisultatoAcquisto(boolean successo, String messaggio, int biglietti, double prezzo) {
            this.successo = successo;
            this.messaggio = messaggio;
            this.bigliettiAcquistati = biglietti;
            this.prezzoTotale = prezzo;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
        public int getBigliettiAcquistati() { return bigliettiAcquistati; }
        public double getPrezzoTotale() { return prezzoTotale; }
    }
}