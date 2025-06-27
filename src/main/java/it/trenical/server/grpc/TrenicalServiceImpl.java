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

    @Override
    public void visualizzaBiglietti(VisualizzaBigliettiRequest request,
                                    StreamObserver<VisualizzaBigliettiResponse> responseObserver) {
        try {
            String emailUtente = request.getEmailUtente();
            if (emailUtente == null || emailUtente.trim().isEmpty()) {
                inviaRispostaBigliettiErrore(responseObserver, "Email utente non specificata");
                return;
            }

            ClienteDAO clienteDAO = new ClienteDAO();
            boolean clienteEsiste = clienteDAO.exists(emailUtente);
            if (!clienteEsiste) {
                inviaRispostaBigliettiErrore(responseObserver, "Cliente non trovato");
                return;
            }

            BigliettoDAO bigliettoDAO = new BigliettoDAO();
            List<Biglietto> biglietti = bigliettoDAO.findByClienteEmail(emailUtente);

            List<BigliettoDTO> bigliettiDTO = new ArrayList<>();
            for (Biglietto biglietto : biglietti) {
                try {
                    BigliettoDTO dto = convertiBigliettoADTO(biglietto);
                    bigliettiDTO.add(dto);
                } catch (Exception e) {
                    System.err.println("Errore conversione biglietto " + biglietto.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            String messaggio = "Trovati " + bigliettiDTO.size() + " biglietti";
            VisualizzaBigliettiResponse response = VisualizzaBigliettiResponse.newBuilder()
                    .setSuccesso(true)
                    .setMessaggio(messaggio)
                    .addAllBiglietti(bigliettiDTO)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Problema Server" + e.getMessage());
            e.printStackTrace();
            inviaRispostaBigliettiErrore(responseObserver, "Errore interno del server");
        }
    }


    private BigliettoDTO convertiBigliettoADTO(Biglietto biglietto) {
        return BigliettoDTO.newBuilder()
                .setId(biglietto.getId())
                .setNominativo(biglietto.getNominativo())
                .setIdViaggio(biglietto.getIdViaggio())
                .setDataAcquisto(biglietto.getDataAcquistoFormattata())
                .setPrezzo(biglietto.getPrezzo())
                .setDataViaggio(biglietto.getDataViaggio().toString())
                .setOrarioPartenza(biglietto.getOrarioPartenza().toString())
                .setOrarioArrivo(biglietto.getOrarioArrivo().toString())
                .setStazionePartenza(biglietto.getStazionePartenza().getNome())
                .setStazioneArrivo(biglietto.getStazioneArrivo().getNome())
                .setTipoTreno(biglietto.getTipoTreno().toString())
                .setBinario(biglietto.getBinarioPartenza().getDescrizione())
                .setDurataFormattata(biglietto.getDurataFormattata())
                .build();
    }

    private void inviaRispostaBigliettiErrore(StreamObserver<VisualizzaBigliettiResponse> responseObserver, String messaggio) {
        VisualizzaBigliettiResponse errorResponse = VisualizzaBigliettiResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .build();

        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();

        logger.warning("Errore visualizzazione biglietti: " + messaggio);
    }

    @Override
    public void modificaBiglietto(ModificaBigliettoRequest request,
                                  StreamObserver<ModificaBigliettoResponse> responseObserver) {

        logger.info("Richiesta modifica biglietto: " + request.getIdBiglietto() +
                " -> nuovo viaggio: " + request.getNuovoIdViaggio());

        try {
            BigliettoDAO bigliettoDAO = new BigliettoDAO();
            ClienteDAO clienteDAO = new ClienteDAO();
            if (!clienteDAO.exists(request.getEmailUtente())) {
                inviaRispostaModificaErrore(responseObserver, "Cliente non trovato");
                return;
            }

            List<Biglietto> bigliettiCliente = bigliettoDAO.findByClienteEmail(request.getEmailUtente());
            Biglietto bigliettoCorrente = null;

            for (Biglietto biglietto : bigliettiCliente) {
                if (biglietto.getId().equals(request.getIdBiglietto())) {
                    bigliettoCorrente = biglietto;
                    break;
                }
            }

            if (bigliettoCorrente == null) {
                inviaRispostaModificaErrore(responseObserver, "Biglietto non trovato o non appartiene al cliente");
                return;
            }

            Optional<Viaggio> nuovoViaggioOpt = viaggioDAO.findById(request.getNuovoIdViaggio());
            if (!nuovoViaggioOpt.isPresent()) {
                inviaRispostaModificaErrore(responseObserver, "Nuovo viaggio non trovato");
                return;
            }

            Viaggio nuovoViaggio = nuovoViaggioOpt.get();
            if (!nuovoViaggio.isDisponibile() || nuovoViaggio.getPostiDisponibili() <= 0) {
                inviaRispostaModificaErrore(responseObserver,
                        "Il viaggio selezionato non è più disponibile");
                return;
            }

            String vecchioViaggioId = bigliettoCorrente.getIdViaggio();
            Optional<Viaggio> vecchioViaggioOpt = viaggioDAO.findById(vecchioViaggioId);
            if (vecchioViaggioOpt.isPresent()) {
                Viaggio vecchioViaggio = vecchioViaggioOpt.get();
                int nuoviPostiVecchio = vecchioViaggio.getPostiDisponibili() + 1;
                viaggioDAO.updatePostiDisponibili(vecchioViaggioId, nuoviPostiVecchio);
                logger.info("Liberato posto nel viaggio precedente: " + vecchioViaggioId);
            }

            int nuoviPostiNuovo = nuovoViaggio.getPostiDisponibili() - 1;
            boolean postiAggiornati = viaggioDAO.updatePostiDisponibili(request.getNuovoIdViaggio(), nuoviPostiNuovo);

            if (!postiAggiornati) {
                if (vecchioViaggioOpt.isPresent()) {
                    Viaggio vecchioViaggio = vecchioViaggioOpt.get();
                    int ripristinaPostiVecchio = vecchioViaggio.getPostiDisponibili();
                    viaggioDAO.updatePostiDisponibili(vecchioViaggioId, ripristinaPostiVecchio);
                }
                inviaRispostaModificaErrore(responseObserver, "Errore nell'aggiornamento posti disponibili");
                return;
            }

            boolean bigliettoAggiornato = bigliettoDAO.updateViaggioId(request.getIdBiglietto(), request.getNuovoIdViaggio());

            if (!bigliettoAggiornato) {
                viaggioDAO.updatePostiDisponibili(request.getNuovoIdViaggio(), nuovoViaggio.getPostiDisponibili());
                if (vecchioViaggioOpt.isPresent()) {
                    Viaggio vecchioViaggio = vecchioViaggioOpt.get();
                    viaggioDAO.updatePostiDisponibili(vecchioViaggioId, vecchioViaggio.getPostiDisponibili());
                }
                inviaRispostaModificaErrore(responseObserver, "Errore nell'aggiornamento del biglietto");
                return;
            }

            double prezzoPrecedente = bigliettoCorrente.getPrezzo();
            double prezzoNuovo = nuovoViaggio.getPrezzo();
            double differenzaPrezzo = prezzoNuovo - prezzoPrecedente;

            Biglietto bigliettoModificato = new Biglietto(nuovoViaggio,
                    bigliettoCorrente.getId(),
                    bigliettoCorrente.getNominativo(),
                    bigliettoCorrente.getDataAcquisto());

            BigliettoDTO bigliettoDTO = convertiBigliettoADTO(bigliettoModificato);

            ModificaBigliettoResponse response = ModificaBigliettoResponse.newBuilder()
                    .setSuccesso(true)
                    .setMessaggio("Biglietto modificato con successo.")
                    .setDifferenzaPrezzo(differenzaPrezzo)
                    .setPrezzoPrecedente(prezzoPrecedente)
                    .setPrezzoNuovo(prezzoNuovo)
                    .setBigliettoAggiornato(bigliettoDTO)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("Modifica biglietto completata con successo: " + request.getIdBiglietto());

        } catch (Exception e) {
            logger.severe("Errore nella modifica biglietto: " + e.getMessage());
            e.printStackTrace();
            inviaRispostaModificaErrore(responseObserver,
                    "Errore interno del server durante la modifica del biglietto");
        }
    }

    private void inviaRispostaModificaErrore(StreamObserver<ModificaBigliettoResponse> responseObserver,
                                             String messaggio) {
        ModificaBigliettoResponse response = ModificaBigliettoResponse.newBuilder()
                .setSuccesso(false)
                .setMessaggio(messaggio)
                .setDifferenzaPrezzo(0.0)
                .setPrezzoPrecedente(0.0)
                .setPrezzoNuovo(0.0)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.warning("Errore modifica biglietto: " + messaggio);
    }
}