package it.trenical.client.proxy;

import io.grpc.*;
import it.trenical.grpc.*;

import java.time.LocalDate;
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
}