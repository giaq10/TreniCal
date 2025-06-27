package it.trenical.client.command;

import it.trenical.client.proxy.ControllerTrenical;
import it.trenical.client.gui.ClientApp;
import it.trenical.grpc.BigliettoDTO;
import it.trenical.grpc.ViaggioDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ModificaOrarioCommand implements Command {
    private static final Logger logger = Logger.getLogger(ModificaOrarioCommand.class.getName());

    private final ControllerTrenical controllerTrenical;
    private final ClientApp clientApp;
    private final BigliettoDTO biglietto;
    private final String emailUtente;

    public ModificaOrarioCommand(ControllerTrenical controllerTrenical, ClientApp clientApp,
                                 BigliettoDTO biglietto, String emailUtente) {
        this.controllerTrenical = controllerTrenical;
        this.clientApp = clientApp;
        this.biglietto = biglietto;
        this.emailUtente = emailUtente;
    }

    @Override
    public void execute() {
        try {
            logger.info("Modifica orario biglietto: " + biglietto.getId());

            LocalDate dataViaggio;
            try {
                dataViaggio = LocalDate.parse(biglietto.getDataViaggio(), DateTimeFormatter.ISO_LOCAL_DATE);
                System.out.println("Data viaggio parsed: " + dataViaggio);
            } catch (Exception e) {
                System.out.println("ERRORE data: " + e.getMessage());
                clientApp.mostraErrore("Errore Data", "Formato data non valido: " + biglietto.getDataViaggio());
                return;
            }

            ControllerTrenical.RisultatoRicerca risultato = controllerTrenical.cercaViaggi(
                    biglietto.getStazionePartenza(),
                    biglietto.getStazioneArrivo(),
                    dataViaggio
            );

            if (risultato.isSuccesso() && risultato.getViaggi() != null && !risultato.getViaggi().isEmpty()) {
                List<ViaggioDTO> viaggiDisponibili = new ArrayList<>();
                for (ViaggioDTO viaggio : risultato.getViaggi()) {
                    if (!viaggio.getId().equals(biglietto.getIdViaggio())) {
                        viaggiDisponibili.add(viaggio);
                    }
                }
                if (!viaggiDisponibili.isEmpty()) {
                    String titolo = "Seleziona Nuova Classe - " + biglietto.getStazionePartenza() + " -> " + biglietto.getStazioneArrivo();
                    clientApp.mostraSelezionaNuovoViaggio(viaggiDisponibili, biglietto, emailUtente, titolo);
                } else {
                    clientApp.mostraErrore("Nessuna Alternativa", "Non ci sono altre classi disponibili per questa tratta e data.");
                }
            } else {
                String dettaglio = risultato.isSuccesso() ? "Nessun viaggio trovato" : risultato.getMessaggio();
                clientApp.mostraErrore("Errore Ricerca", "Impossibile trovare viaggi alternativi: " + dettaglio);
            }

        } catch (Exception e) {
            logger.severe("Errore modifica orario: " + e.getMessage());
            clientApp.mostraErrore("Errore Sistema", "Errore durante la ricerca: " + e.getMessage());
        }
    }
}