package it.trenical.server.grpc;

import io.grpc.stub.StreamObserver;
import it.trenical.grpc.*;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.viaggi.Viaggio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;


public class TrenicalServiceImpl extends TrenicalServiceGrpc.TrenicalServiceImplBase {

    private static final Logger logger = Logger.getLogger(TrenicalServiceImpl.class.getName());
    private final ViaggioDAO viaggioDAO;

    public TrenicalServiceImpl() {
        this.viaggioDAO = new ViaggioDAO();
        logger.info("TrenicalServiceImpl inizializzato");
    }


    @Override
    public void cercaViaggi(RicercaViaggioRequest request, StreamObserver<RicercaViaggioResponse> responseObserver) {
        try {
            Stazione stazionePartenza = convertiStringaInStazione(request.getStazionePartenza());
            Stazione stazioneArrivo = convertiStringaInStazione(request.getStazioneArrivo());

            LocalDate dataViaggio= LocalDate.parse(request.getDataViaggio(), DateTimeFormatter.ISO_LOCAL_DATE);

            List<Viaggio> viaggiTrovati = viaggioDAO.findByTrattaEData(stazionePartenza, stazioneArrivo, dataViaggio);

            List<Viaggio> viaggiDisponibili = new ArrayList<>();
            for (Viaggio viaggio : viaggiTrovati) {
                if (viaggio.isDisponibile() && viaggio.getPostiDisponibili() > 0) {
                    viaggiDisponibili.add(viaggio);
                }
            }

            List<ViaggioDTO> viaggiDTO = new ArrayList<>();
            for (Viaggio viaggio : viaggiDisponibili) {
                viaggiDTO.add(convertiViaggioInDTO(viaggio));
            }

            RicercaViaggioResponse.Builder responseBuilder = RicercaViaggioResponse.newBuilder()
                    .setSuccesso(true)
                    .addAllViaggi(viaggiDTO);

            if (viaggiDTO.isEmpty()) {
                responseBuilder.setMessaggio("Nessun viaggio disponibile per la tratta e data richieste");
            } else {
                responseBuilder.setMessaggio("Trovati " + viaggiDTO.size() + " viaggi disponibili");
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            logger.info("Risposta inviata con " + viaggiDTO.size() + " viaggi");

        } catch (Exception e) {
            logger.severe("Errore durante la ricerca viaggi: " + e.getMessage());
            e.printStackTrace();
            inviaRispostaErrore(responseObserver, "Errore interno del server");
        }
    }

    private ViaggioDTO convertiViaggioInDTO(Viaggio viaggio) {
        return ViaggioDTO.newBuilder()
                .setId(viaggio.getId())
                .setTipoTreno(viaggio.getTreno().getTipoTreno().name())
                .setStazionePartenza(viaggio.getTratta().getStazionePartenza().getNome())
                .setStazioneArrivo(viaggio.getTratta().getStazioneArrivo().getNome())
                .setOrarioPartenza(viaggio.getOrarioPartenza().toString())
                .setOrarioArrivo(viaggio.getOrarioArrivoEffettivo().toString())
                .setDataViaggio(viaggio.getDataViaggio().toString())
                .setPrezzo(viaggio.getPrezzo())
                .setServizi(ottieniServiziTreno(viaggio))
                .setDurataFormattata(viaggio.getDurataFormattata())
                .setDistanzaKm(viaggio.getTratta().getDistanzaKm())
                .setStato(viaggio.getStato().name())
                .setPostiDisponibili(viaggio.getPostiDisponibili())
                .setBinario(viaggio.getBinarioPartenza().getDescrizione())
                .build();
    }

    private Stazione convertiStringaInStazione(String nomeStazione) {
        try {
            for (Stazione stazione : Stazione.values()) {
                if (stazione.getNome().equalsIgnoreCase(nomeStazione.trim())) {
                    return stazione;
                }
            }

            return Stazione.valueOf(nomeStazione.trim().toUpperCase().replace(" ", "_"));

        } catch (IllegalArgumentException e) {
            logger.warning("Stazione non trovata: " + nomeStazione);
            return null;
        }
    }

    private String ottieniServiziTreno(Viaggio viaggio) {
        StringBuilder servizi = new StringBuilder();
        boolean primo = true;

        for (Object servizio : viaggio.getTreno().getServizi()) {
            if (!primo)
                servizi.append(", ");

            String nomeServizio = servizio.toString().replace("_", " ").toLowerCase();
            servizi.append(nomeServizio);
            primo = false;
        }

        return servizi.toString();
    }

    private void inviaRispostaErrore(StreamObserver<RicercaViaggioResponse> responseObserver, String messaggio) {
        RicercaViaggioResponse response = RicercaViaggioResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.warning("Risposta errore inviata: " + messaggio);
    }
}