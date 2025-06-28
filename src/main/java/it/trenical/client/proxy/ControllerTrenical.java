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

    public RisultatoBiglietti visualizzaBiglietti(String emailUtente) {
        logger.info("Richiesta visualizzazione biglietti per: " + emailUtente);

        try {
            if (emailUtente == null || emailUtente.trim().isEmpty()) {
                return new RisultatoBiglietti(false, "Email utente non specificata", new ArrayList<>());
            }

            VisualizzaBigliettiRequest request = VisualizzaBigliettiRequest.newBuilder()
                    .setEmailUtente(emailUtente)
                    .build();

            VisualizzaBigliettiResponse response = blockingStub.visualizzaBiglietti(request);

            RisultatoBiglietti risultato = new RisultatoBiglietti(
                    response.getSuccesso(),
                    response.getMessaggio(),
                    response.getBigliettiList()
            );

            logger.info("Visualizzazione biglietti completata: " + risultato.getMessaggio());
            return risultato;

        } catch (StatusRuntimeException e) {
            logger.severe("Errore nella chiamata gRPC biglietti: " + e.getStatus());
            return new RisultatoBiglietti(false, "Errore Server", new ArrayList<>());

        } catch (Exception e) {
            logger.severe("Errore imprevisto visualizzazione biglietti: " + e.getMessage());
            return new RisultatoBiglietti(false, "Errore imprevisto: " + e.getMessage(), new ArrayList<>());
        }
    }

    public RisultatoModificaBiglietto modificaBiglietto(String idBiglietto, String nuovoIdViaggio, String emailUtente) {
        logger.info("Modifica biglietto: " + idBiglietto + " -> nuovo viaggio: " + nuovoIdViaggio);

        try {
            ModificaBigliettoRequest request = ModificaBigliettoRequest.newBuilder()
                    .setIdBiglietto(idBiglietto)
                    .setNuovoIdViaggio(nuovoIdViaggio)
                    .setEmailUtente(emailUtente)
                    .build();

            ModificaBigliettoResponse response = blockingStub.modificaBiglietto(request);

            RisultatoModificaBiglietto risultato = new RisultatoModificaBiglietto(
                    response.getSuccesso(),
                    response.getMessaggio(),
                    response.getDifferenzaPrezzo(),
                    response.getPrezzoPrecedente(),
                    response.getPrezzoNuovo(),
                    response.hasBigliettoAggiornato() ? response.getBigliettoAggiornato() : null
            );

            if (risultato.isSuccesso()) {
                logger.info("Modifica biglietto completata: " + risultato.getMessaggio());
            } else {
                logger.warning("Modifica biglietto fallita: " + risultato.getMessaggio());
            }

            return risultato;

        } catch (StatusRuntimeException e) {
            logger.severe("Errore chiamata gRPC modifica biglietto: " + e.getStatus());
            String messaggio = "Errore di connessione al server";
            return new RisultatoModificaBiglietto(false, messaggio, 0.0, 0.0, 0.0, null);

        } catch (Exception e) {
            logger.severe("Errore imprevisto modifica biglietto: " + e.getMessage());
            e.printStackTrace();
            return new RisultatoModificaBiglietto(false,
                    "Errore imprevisto: " + e.getMessage(), 0.0, 0.0, 0.0, null);
        }
    }

    public RisultatoNotifica inviaNotifica(String tipoNotifica, String emailUtente) {
        logger.info("Invio notifica: " + tipoNotifica + " per utente: " + emailUtente);

        try {
            NotificaClienteRequest request = NotificaClienteRequest.newBuilder()
                    .setTipoNotifica(tipoNotifica)
                    .setEmailUtente(emailUtente)
                    .build();

            NotificaClienteResponse response = blockingStub.inviaNotificaCliente(request);

            RisultatoNotifica risultato = new RisultatoNotifica(
                    response.getSuccesso(),
                    response.getMessaggio()
            );

            if (risultato.isSuccesso()) {
                logger.info("Notifica ricevuta: " + risultato.getMessaggio());
            } else {
                logger.warning("Errore notifica: " + risultato.getMessaggio());
            }

            return risultato;

        } catch (StatusRuntimeException e) {
            logger.severe("Errore nella chiamata gRPC notifica: " + e.getStatus());
            String messaggio = "Errore di connessione al server";
            return new RisultatoNotifica(false, messaggio);

        } catch (Exception e) {
            logger.severe("Errore imprevisto notifica: " + e.getMessage());
            return new RisultatoNotifica(false, "Errore imprevisto: " + e.getMessage());
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

    public static class RisultatoBiglietti {
        private final boolean successo;
        private final String messaggio;
        private final List<BigliettoDTO> biglietti;

        public RisultatoBiglietti(boolean successo, String messaggio, List<BigliettoDTO> biglietti) {
            this.successo = successo;
            this.messaggio = messaggio;
            this.biglietti = biglietti;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
        public List<BigliettoDTO> getBiglietti() { return biglietti; }
    }

    public static class RisultatoModificaBiglietto {
        private final boolean successo;
        private final String messaggio;
        private final double differenzaPrezzo;
        private final double prezzoPrecedente;
        private final double prezzoNuovo;
        private final BigliettoDTO bigliettoAggiornato;

        public RisultatoModificaBiglietto(boolean successo, String messaggio,
                                          double differenzaPrezzo, double prezzoPrecedente,
                                          double prezzoNuovo, BigliettoDTO bigliettoAggiornato) {
            this.successo = successo;
            this.messaggio = messaggio;
            this.differenzaPrezzo = differenzaPrezzo;
            this.prezzoPrecedente = prezzoPrecedente;
            this.prezzoNuovo = prezzoNuovo;
            this.bigliettoAggiornato = bigliettoAggiornato;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
        public String getDettaglioPrezzo() {
            return String.format("Prezzo precedente: €%.2f\nPrezzo nuovo: €%.2f",
                    prezzoPrecedente, prezzoNuovo);
        }
    }

    public static class RisultatoNotifica {
        private final boolean successo;
        private final String messaggio;

        public RisultatoNotifica(boolean successo, String messaggio) {
            this.successo = successo;
            this.messaggio = messaggio;
        }

        public boolean isSuccesso() { return successo; }
        public String getMessaggio() { return messaggio; }
    }
}