package it.trenical.server.gui;

import it.trenical.common.cliente.Cliente;
import it.trenical.server.promozioni.Promozione;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.viaggi.Viaggio;
import it.trenical.server.db.dao.*;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;


public class AdminVisualizzaDB {

    private static final Logger logger = Logger.getLogger(AdminVisualizzaDB.class.getName());

    private final ClienteDAO clienteDAO;
    private final ViaggioDAO viaggioDAO;
    private final PromozioneDAO promozioneDAO;

    public AdminVisualizzaDB(ClienteDAO clienteDAO, ViaggioDAO viaggioDAO,
                             PromozioneDAO promozioneDAO, ServerAdminApp gui) {
        this.clienteDAO = clienteDAO;
        this.viaggioDAO = viaggioDAO;
        this.promozioneDAO = promozioneDAO;

        logger.info("AdminVisualizzaDB inizializzato");
    }

    public String getTuttiIViaggi() {
        try {
            List<Viaggio> viaggi = viaggioDAO.findViaggiDisponibili();

            if (viaggi.isEmpty()) {
                return "Nessun viaggio nel database.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Totale: %d viaggi\n\n", viaggi.size()));

            for (int i = 0; i < viaggi.size(); i++) {
                Viaggio v = viaggi.get(i);
                sb.append(String.format("VIAGGIO %d \n", i + 1));
                sb.append(String.format("ID: %s\n", v.getId()));
                sb.append(String.format("CODICE_TRENO: %s\n", v.getTreno().getCodice()));
                sb.append(String.format("TIPO_TRENO: %s\n", v.getTreno().getTipoTreno().name()));
                sb.append(String.format("STAZIONE_PARTENZA: %s\n", v.getTratta().getStazionePartenza().getNome()));
                sb.append(String.format("STAZIONE_ARRIVO: %s\n", v.getTratta().getStazioneArrivo().getNome()));
                sb.append(String.format("DATA_VIAGGIO: %s\n", v.getDataViaggio()));
                sb.append(String.format("ORARIO_PARTENZA: %s\n", v.getOrarioPartenza()));
                sb.append(String.format("ORARIO_ARRIVO: %s\n", v.getOrarioArrivo()));
                sb.append(String.format("DATA_ARRIVO: %s\n", v.getDataArrivo()));
                sb.append(String.format("PREZZO: €%.2f\n", v.getPrezzo()));
                sb.append(String.format("DURATA_MINUTI: %d\n", v.getDurataMinuti()));
                sb.append(String.format("POSTI_TOTALI: %d\n", v.getTreno().getPostiTotali()));
                sb.append(String.format("POSTI_DISPONIBILI: %d\n", v.getPostiDisponibili()));
                sb.append(String.format("STATO: %s\n", v.getStato().name()));
                sb.append(String.format("BINARIO_PARTENZA: %s\n", v.getBinarioPartenza().getDescrizione()));
                sb.append(String.format("RITARDO_MINUTI: %d\n", v.getRitardoMinuti()));
                sb.append(String.format("MOTIVO_CANCELLAZIONE: %s\n",
                        v.getMotivoCancellazione() != null ? v.getMotivoCancellazione() : "N/A"));
                sb.append(String.format("DISTANZA_KM: %d\n", v.getTratta().getDistanzaKm()));
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.severe("Errore recupero viaggi: " + e.getMessage());
            return "Errore nel recupero dei viaggi: " + e.getMessage();
        }
    }

    public String getTuttiIClienti() {
        try {
            List<Cliente> clienti = clienteDAO.findAll();

            if (clienti.isEmpty()) {
                return "Nessun cliente nel database.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Totale: %d clienti\n\n", clienti.size()));

            for (int i = 0; i < clienti.size(); i++) {
                Cliente c = clienti.get(i);
                sb.append(String.format("%d. Email: %s\n", i + 1, c.getEmail()));
                sb.append(String.format("Nome: %s\n", c.getNome()));
                sb.append(String.format("Abbonamento Fedeltà: %s\n",
                        c.hasAbbonamentoFedelta() ? (
                                c.hasNotificheAttive() ? "SI, con notifiche attive": "SI, senza notifiche attive")
                                : "NO"));
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.severe("Errore recupero clienti: " + e.getMessage());
            return "Errore nel recupero dei clienti: " + e.getMessage();
        }
    }

    public String getTutteLePromozioni() {
        try {
            List<Promozione> promozioni = promozioneDAO.findAll();

            if (promozioni.isEmpty()) {
                return "Nessuna promozione nel database.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Totale: %d promozioni\n\n", promozioni.size()));

            for (int i = 0; i < promozioni.size(); i++) {
                Promozione p = promozioni.get(i);
                sb.append(String.format("%d. ID: %s\n", i + 1, p.getId()));
                sb.append(String.format("Nome: %s\n", p.getNome()));
                sb.append(String.format("Tipo: %s\n", p.getTipo()));
                sb.append(String.format("Sconto: %.1f%%\n\n", p.getSconto()));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.severe("Errore recupero promozioni: " + e.getMessage());
            return "Errore nel recupero delle promozioni: " + e.getMessage();
        }
    }

    public String getViaggiPerTratta(String stazionePartenza, String stazioneArrivo) {
        try {
            Stazione partenza = Stazione.fromNome(stazionePartenza);
            Stazione arrivo = Stazione.fromNome(stazioneArrivo);

            List<Viaggio> viaggi = viaggioDAO.findByTratta(partenza, arrivo);

            if (viaggi.isEmpty()) {
                return String.format("Nessun viaggio trovato per la tratta %s-%s",
                        stazionePartenza, stazioneArrivo);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("VIAGGI PER TRATTA: %s-%s\n",
                    stazionePartenza, stazioneArrivo));
            sb.append(String.format("Totale trovati: %d viaggi\n\n", viaggi.size()));

            for (int i = 0; i < viaggi.size(); i++) {
                Viaggio v = viaggi.get(i);
                sb.append(String.format("VIAGGIO %d \n", i + 1));
                sb.append(String.format("ID: %s\n", v.getId()));
                sb.append(String.format("CODICE_TRENO: %s\n", v.getTreno().getCodice()));
                sb.append(String.format("TIPO_TRENO: %s\n", v.getTreno().getTipoTreno().name()));
                sb.append(String.format("STAZIONE_PARTENZA: %s\n", v.getTratta().getStazionePartenza().getNome()));
                sb.append(String.format("STAZIONE_ARRIVO: %s\n", v.getTratta().getStazioneArrivo().getNome()));
                sb.append(String.format("DATA_VIAGGIO: %s\n", v.getDataViaggio()));
                sb.append(String.format("ORARIO_PARTENZA: %s\n", v.getOrarioPartenza()));
                sb.append(String.format("ORARIO_ARRIVO: %s\n", v.getOrarioArrivo()));
                sb.append(String.format("DATA_ARRIVO: %s\n", v.getDataArrivo()));
                sb.append(String.format("PREZZO: €%.2f\n", v.getPrezzo()));
                sb.append(String.format("DURATA_MINUTI: %d\n", v.getDurataMinuti()));
                sb.append(String.format("POSTI_TOTALI: %d\n", v.getTreno().getPostiTotali()));
                sb.append(String.format("POSTI_DISPONIBILI: %d\n", v.getPostiDisponibili()));
                sb.append(String.format("STATO: %s\n", v.getStato().name()));
                sb.append(String.format("BINARIO_PARTENZA: %s\n", v.getBinarioPartenza().getDescrizione()));
                sb.append(String.format("RITARDO_MINUTI: %d\n", v.getRitardoMinuti()));
                sb.append(String.format("MOTIVO_CANCELLAZIONE: %s\n",
                        v.getMotivoCancellazione() != null ? v.getMotivoCancellazione() : "N/A"));
                sb.append(String.format("DISTANZA_KM: %d\n", v.getTratta().getDistanzaKm()));
                sb.append("\n");
            }

            return sb.toString();

        } catch (IllegalArgumentException e) {
            return "Errore: " + e.getMessage() ;
        } catch (Exception e) {
            logger.severe("Errore nel filtro viaggi per tratta: " + e.getMessage());
            return "Errore nel recupero dei viaggi: " + e.getMessage();
        }
    }

    public String getViaggiPerData(LocalDate data) {
        List<Viaggio> viaggi = viaggioDAO.findByData(data);

        if (viaggi.isEmpty()) {
            return String.format("Nessun viaggio trovato per la data %s",
                    data);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("VIAGGI PER DATA: %s\n",data));
        sb.append(String.format("Totale trovati: %d viaggi\n\n", viaggi.size()));
        for (int i = 0; i < viaggi.size(); i++) {
            Viaggio v = viaggi.get(i);
            sb.append(String.format("VIAGGIO %d \n", i + 1));
            sb.append(String.format("ID: %s\n", v.getId()));
            sb.append(String.format("CODICE_TRENO: %s\n", v.getTreno().getCodice()));
            sb.append(String.format("TIPO_TRENO: %s\n", v.getTreno().getTipoTreno().name()));
            sb.append(String.format("STAZIONE_PARTENZA: %s\n", v.getTratta().getStazionePartenza().getNome()));
            sb.append(String.format("STAZIONE_ARRIVO: %s\n", v.getTratta().getStazioneArrivo().getNome()));
            sb.append(String.format("DATA_VIAGGIO: %s\n", v.getDataViaggio()));
            sb.append(String.format("ORARIO_PARTENZA: %s\n", v.getOrarioPartenza()));
            sb.append(String.format("ORARIO_ARRIVO: %s\n", v.getOrarioArrivo()));
            sb.append(String.format("DATA_ARRIVO: %s\n", v.getDataArrivo()));
            sb.append(String.format("PREZZO: €%.2f\n", v.getPrezzo()));
            sb.append(String.format("DURATA_MINUTI: %d\n", v.getDurataMinuti()));
            sb.append(String.format("POSTI_TOTALI: %d\n", v.getTreno().getPostiTotali()));
            sb.append(String.format("POSTI_DISPONIBILI: %d\n", v.getPostiDisponibili()));
            sb.append(String.format("STATO: %s\n", v.getStato().name()));
            sb.append(String.format("BINARIO_PARTENZA: %s\n", v.getBinarioPartenza().getDescrizione()));
            sb.append(String.format("RITARDO_MINUTI: %d\n", v.getRitardoMinuti()));
            sb.append(String.format("MOTIVO_CANCELLAZIONE: %s\n",
            v.getMotivoCancellazione() != null ? v.getMotivoCancellazione() : "N/A"));
            sb.append(String.format("DISTANZA_KM: %d\n", v.getTratta().getDistanzaKm()));
            sb.append("\n");
        }

        return sb.toString();

    }

    public String getViaggiTrattaData(String stazionePartenza, String stazioneArrivo, LocalDate data) {
        try {
            Stazione partenza = Stazione.fromNome(stazionePartenza);
            Stazione arrivo = Stazione.fromNome(stazioneArrivo);

            List<Viaggio> viaggi = viaggioDAO.findByTrattaEData(partenza, arrivo, data);

            if (viaggi.isEmpty()) {
                return String.format("Nessun viaggio trovato per la tratta %s-%s e data %s",
                        stazionePartenza, stazioneArrivo, data);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("VIAGGI PER TRATTA: %s-%s E DATA %s\n",
                    stazionePartenza, stazioneArrivo, data));
            sb.append(String.format("Totale trovati: %d viaggi\n\n", viaggi.size()));

            for (int i = 0; i < viaggi.size(); i++) {
                Viaggio v = viaggi.get(i);
                sb.append(String.format("VIAGGIO %d \n", i + 1));
                sb.append(String.format("ID: %s\n", v.getId()));
                sb.append(String.format("CODICE_TRENO: %s\n", v.getTreno().getCodice()));
                sb.append(String.format("TIPO_TRENO: %s\n", v.getTreno().getTipoTreno().name()));
                sb.append(String.format("STAZIONE_PARTENZA: %s\n", v.getTratta().getStazionePartenza().getNome()));
                sb.append(String.format("STAZIONE_ARRIVO: %s\n", v.getTratta().getStazioneArrivo().getNome()));
                sb.append(String.format("DATA_VIAGGIO: %s\n", v.getDataViaggio()));
                sb.append(String.format("ORARIO_PARTENZA: %s\n", v.getOrarioPartenza()));
                sb.append(String.format("ORARIO_ARRIVO: %s\n", v.getOrarioArrivo()));
                sb.append(String.format("DATA_ARRIVO: %s\n", v.getDataArrivo()));
                sb.append(String.format("PREZZO: €%.2f\n", v.getPrezzo()));
                sb.append(String.format("DURATA_MINUTI: %d\n", v.getDurataMinuti()));
                sb.append(String.format("POSTI_TOTALI: %d\n", v.getTreno().getPostiTotali()));
                sb.append(String.format("POSTI_DISPONIBILI: %d\n", v.getPostiDisponibili()));
                sb.append(String.format("STATO: %s\n", v.getStato().name()));
                sb.append(String.format("BINARIO_PARTENZA: %s\n", v.getBinarioPartenza().getDescrizione()));
                sb.append(String.format("RITARDO_MINUTI: %d\n", v.getRitardoMinuti()));
                sb.append(String.format("MOTIVO_CANCELLAZIONE: %s\n", v.getMotivoCancellazione() != null ? v.getMotivoCancellazione() : "N/A"));
                sb.append(String.format("DISTANZA_KM: %d\n", v.getTratta().getDistanzaKm()));
                sb.append("\n");
            }

            return sb.toString();

        } catch (IllegalArgumentException e) {
            return "Errore: " + e.getMessage() ;
        } catch (Exception e) {
            logger.severe("Errore nel filtro viaggi per tratta: " + e.getMessage());
            return "Errore nel recupero dei viaggi: " + e.getMessage();
        }    }

    public String getClientiAbbonati(){
        try{
            List<Cliente> abbonati = clienteDAO.findClientiFedelta();

            if(abbonati.isEmpty()){
                return "Nessun cliente abbonato al servizio Fedelta.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Totale: %d clienti\n\n", abbonati.size()));

            for (int i = 0; i < abbonati.size(); i++) {
                Cliente c = abbonati.get(i);
                sb.append(String.format("%d. Email: %s\n", i + 1, c.getEmail()));
                sb.append(String.format("Nome: %s\n", c.getNome()));
                sb.append(String.format("Abbonamento Fedeltà: %s\n",
                        c.hasAbbonamentoFedelta() ? "SÌ" : "NO"));
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.severe("Errore recupero clienti: " + e.getMessage());
            return "Errore nel recupero dei clienti: " + e.getMessage();
        }
    }
}