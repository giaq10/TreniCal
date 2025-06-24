package it.trenical.server.grpc;

import io.grpc.stub.StreamObserver;
import it.trenical.common.cliente.Biglietto;
import it.trenical.grpc.*;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.db.dao.BigliettoDAO;
import it.trenical.server.db.dao.ClienteDAO;
import it.trenical.server.db.dao.ViaggioDAO;
import it.trenical.server.viaggi.Viaggio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
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
            inviaRispostaRicercaErrore(responseObserver, "Errore interno del server");
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
                .setDataPartenza(viaggio.getDataViaggio().toString())
                .setDataArrivo(viaggio.getDataArrivo().toString())
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

    private void inviaRispostaRicercaErrore(StreamObserver<RicercaViaggioResponse> responseObserver, String messaggio) {
        RicercaViaggioResponse response = RicercaViaggioResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.warning("Risposta errore inviata: " + messaggio);
    }

    @Override
    public void aggiungiAlCarrello(AggiungiCarrelloRequest request,
                                   StreamObserver<AggiungiCarrelloResponse> responseObserver) {
        try {
            String viaggioId = request.getViaggioId();
            int quantita = request.getQuantita();
            String emailUtente = request.getEmailUtente();

            logger.info("Richiesta aggiunta carrello - Viaggio: " + viaggioId +
                    ", Quantità: " + quantita + ", Utente: " + emailUtente);

            Optional<Viaggio> viaggioOpt = viaggioDAO.findById(viaggioId);
            if (viaggioOpt.isEmpty()) {
                inviaRispostaCarrelloErrore(responseObserver, "Viaggio non trovato");
                return;
            }

            Viaggio viaggio = viaggioOpt.get();
            if (!viaggio.isDisponibile()) {
                inviaRispostaCarrelloErrore(responseObserver, "Viaggio non disponibile");
                return;
            }
            if (viaggio.getPostiDisponibili() < quantita) {
                inviaRispostaCarrelloErrore(responseObserver,
                        "Posti insufficienti. Disponibili: " + viaggio.getPostiDisponibili() +
                                ", Richiesti: " + quantita);
                return;
            }

            int nuoviPostiDisponibili = viaggio.getPostiDisponibili() - quantita;
            boolean postiAggiornati = viaggioDAO.updatePostiDisponibili(viaggioId, nuoviPostiDisponibili);
            if (!postiAggiornati) {
                inviaRispostaCarrelloErrore(responseObserver, "Errore nell'aggiornamento posti");
                return;
            }

            ViaggioDTO viaggioDTO = convertiViaggioInDTO(viaggio);

            viaggioDTO = viaggioDTO.toBuilder()
                    .setPostiDisponibili(nuoviPostiDisponibili)
                    .build();

            List<BigliettoCarrelloDTO> bigliettiCarrello = new ArrayList<>();
            String idTemporaneo = "PRENOTATO_" + System.currentTimeMillis();

            BigliettoCarrelloDTO dto = BigliettoCarrelloDTO.newBuilder()
                    .setIdTemporaneo(idTemporaneo)
                    .setViaggioId(viaggioId)
                    .setPrezzo(viaggio.getPrezzo())
                    .setInfoViaggio(viaggio.getTratta().getStazionePartenza().getNome() +
                            " - " + viaggio.getTratta().getStazioneArrivo().getNome() +
                            " del " + viaggio.getDataViaggio())
                    .setViaggio(viaggioDTO)
                    .setQuantita(quantita)
                    .build();

            bigliettiCarrello.add(dto);


            AggiungiCarrelloResponse response = AggiungiCarrelloResponse.newBuilder()
                    .setSuccesso(true)
                    .setMessaggio("Aggiunti " + quantita + " biglietto/i al carrello")
                    .setPostiRimanenti(nuoviPostiDisponibili)
                    .addAllBigliettiCreati(bigliettiCarrello)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("Successo aggiunta carrello - Viaggio: " + viaggioId +
                    ", Posti rimanenti: " + nuoviPostiDisponibili);

        } catch (Exception e) {
            logger.severe("Errore durante aggiunta al carrello: " + e.getMessage());
            e.printStackTrace();
            inviaRispostaCarrelloErrore(responseObserver, "Errore interno del server");
        }
    }

    private void inviaRispostaCarrelloErrore(StreamObserver<AggiungiCarrelloResponse> responseObserver, String messaggio) {
        AggiungiCarrelloResponse errorResponse = AggiungiCarrelloResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .setPostiRimanenti(0)
                .build();

        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();

        logger.warning("Errore carrello: " + messaggio);
    }

    @Override
    public void confermaAcquisto(ConfermaAcquistoRequest request,
                                 StreamObserver<ConfermaAcquistoResponse> responseObserver) {
        System.out.println("=== DEBUG SERVER ===");
        System.out.println("Email: " + request.getEmailUtente());
        System.out.println("Items ricevuti: " + request.getCarrelloItemsCount());
        System.out.println("Nominativi ricevuti: " + request.getNominativiCount());
        System.out.println("=== METODO confermaAcquisto CHIAMATO ===");
        try {
            String emailUtente = request.getEmailUtente();
            List<CarrelloItemDTO> carrelloItems = request.getCarrelloItemsList();
            List<String> nominativi = request.getNominativiList();
            String modalitaPagamento = request.getModalitaPagamento();

            logger.info("Richiesta conferma acquisto - Utente: " + emailUtente +
                    ", Items: " + carrelloItems.size() +
                    ", Nominativi: " + nominativi.size());

            List<Biglietto> bigliettiCreati = new ArrayList<>();
            double prezzoTotale = 0;
            int indiceNominativo = 0;

            for (CarrelloItemDTO item : carrelloItems) {
                Optional<Viaggio> viaggioOpt = viaggioDAO.findById(item.getViaggioId());
                if (viaggioOpt.isEmpty()) {
                    inviaRispostaAcquistoErrore(responseObserver, "Viaggio non trovato: " + item.getViaggioId());
                    return;
                }

                Viaggio viaggio = viaggioOpt.get();

                Biglietto bigliettoBase = new Biglietto(viaggio);
                bigliettoBase.setNominativo(nominativi.get(indiceNominativo++));
                bigliettiCreati.add(bigliettoBase);
                prezzoTotale += viaggio.getPrezzo();

                for (int i = 1; i < item.getQuantita(); i++) { //prototype
                    Biglietto clone = bigliettoBase.clone();
                    clone.setNominativo(nominativi.get(indiceNominativo++));
                    bigliettiCreati.add(clone);
                    prezzoTotale += viaggio.getPrezzo();
                }
            }

            System.out.println("=== DEBUG SALVATAGGIO ===");
            System.out.println("Biglietti creati: " + bigliettiCreati.size());
            for (int i = 0; i < bigliettiCreati.size(); i++) {
                Biglietto b = bigliettiCreati.get(i);
                System.out.println("Biglietto " + i + ": " + b.getNominativo() + " - Completo: " + b.isCompleto());
            }
            // Debug Foreign Key
            System.out.println("=== DEBUG FOREIGN KEY ===");
            System.out.println("Email cliente: " + emailUtente);

            // Verifica se cliente esiste
            ClienteDAO clienteDAO = new ClienteDAO();
            boolean clienteEsiste = clienteDAO.exists(emailUtente);
            System.out.println("Cliente esiste: " + clienteEsiste);

            // Verifica viaggi
            for (Biglietto b : bigliettiCreati) {
                System.out.println("Viaggio ID: " + b.getViaggio().getId());
                boolean viaggioEsiste = viaggioDAO.findById(b.getViaggio().getId()).isPresent();
                System.out.println("Viaggio esiste: " + viaggioEsiste);
            }

            BigliettoDAO bigliettoDAO = new BigliettoDAO();
            int bigliettSalvati = bigliettoDAO.saveAll(bigliettiCreati, emailUtente);

            System.out.println("Biglietti salvati: " + bigliettSalvati + "/" + bigliettiCreati.size());

            if (bigliettSalvati != bigliettiCreati.size()) {
                inviaRispostaAcquistoErrore(responseObserver, "Errore nel salvataggio biglietti");
                return;
            }

            ConfermaAcquistoResponse response = ConfermaAcquistoResponse.newBuilder()
                    .setSuccesso(true)
                    .setMessaggio("Acquisto completato " + bigliettSalvati + " biglietti acquistati.")
                    .setBigliettiAcquistati(bigliettSalvati)
                    .setPrezzoTotale(prezzoTotale)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("Acquisto completato " + bigliettSalvati + " biglietti per €" + prezzoTotale);

        } catch (Exception e) {
            logger.severe("Errore durante conferma acquisto: " + e.getMessage());
            System.out.println("CRASH nel confermaAcquisto: " + e.getMessage());
            e.printStackTrace();
            inviaRispostaAcquistoErrore(responseObserver, "Errore interno del server");
        }
    }

    private void inviaRispostaAcquistoErrore(StreamObserver<ConfermaAcquistoResponse> responseObserver, String messaggio) {
        ConfermaAcquistoResponse errorResponse = ConfermaAcquistoResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .setBigliettiAcquistati(0)
                .setPrezzoTotale(0.0)
                .build();

        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();

        logger.warning("Errore acquisto: " + messaggio);
    }

}