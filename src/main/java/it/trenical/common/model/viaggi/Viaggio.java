package it.trenical.common.model.viaggi;

import it.trenical.common.model.tratte.Tratta;
import it.trenical.common.model.treni.Treno;
import it.trenical.common.model.stazioni.Binario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Viaggio {
    private final String id;
    private final Treno treno;
    private final Tratta tratta;
    private final LocalDate dataViaggio;
    private final LocalTime orarioPartenzaProgrammato;
    private final LocalTime orarioArrivoProgrammato;

    private LocalTime orarioPartenzaEffettivo;
    private LocalTime orarioArrivoEffettivo;

    private Binario binarioPartenza;

    private int postiDisponibili;
    private StatoViaggio stato;

    private int ritardoMinuti;

    private String motivoCancellazione;

    public Viaggio(Treno treno, Tratta tratta, LocalDate dataViaggio, LocalTime orarioPartenza) {
        this(treno, tratta, dataViaggio, orarioPartenza, null, null);
    }

    public Viaggio(Treno treno, Tratta tratta, LocalDate dataViaggio, LocalTime orarioPartenza,
                   Binario binarioPartenza, Binario binarioArrivo) {
        if (treno == null) throw new IllegalArgumentException("Treno obbligatorio");
        if (tratta == null) throw new IllegalArgumentException("Tratta obbligatoria");
        if (dataViaggio == null) throw new IllegalArgumentException("Data viaggio obbligatoria");
        if (orarioPartenza == null) throw new IllegalArgumentException("Orario partenza obbligatorio");

        // Verifica compatibilità treno-tratta
        if (!treno.getTipoTreno().equals(tratta.getTipoTreno())) {
            throw new IllegalArgumentException(
                    String.format("Tipo treno (%s) incompatibile con tratta (%s)",
                            treno.getTipoTreno(), tratta.getTipoTreno()));
        }

        this.id = generateId(treno, tratta, dataViaggio, orarioPartenza);
        this.treno = treno;
        this.tratta = tratta;
        this.dataViaggio = dataViaggio;
        this.orarioPartenzaProgrammato = orarioPartenza;
        this.orarioArrivoProgrammato = calcolaOrarioArrivo(orarioPartenza, tratta.getDurataMinuti());

        // Inizialmente gli orari effettivi sono uguali a quelli programmati
        this.orarioPartenzaEffettivo = orarioPartenza;
        this.orarioArrivoEffettivo = this.orarioArrivoProgrammato;

        // Binario (può essere null se non specificati)
        this.binarioPartenza = binarioPartenza;

        // Stato iniziale
        this.postiDisponibili = treno.getPostiTotali();
        this.stato = StatoViaggio.PROGRAMMATO;
        this.ritardoMinuti = 0;
    }

    private String generateId(Treno treno, Tratta tratta, LocalDate data, LocalTime orario) {
        return String.format("%s_%s_%s_%s_%s",
                treno.getCodice(),
                tratta.getStazionePartenza().name(),
                tratta.getStazioneArrivo().name(),
                data.toString(),
                orario.toString().replace(":", ""));
    }

    private LocalTime calcolaOrarioArrivo(LocalTime partenza, int durataMinuti) {
        return partenza.plusMinutes(durataMinuti);
    }

    public boolean prenotaPosto() {
        if (postiDisponibili > 0 && stato.isAttivo()) {
            postiDisponibili--;
            return true;
        }
        return false;
    }
    public boolean liberaPosto() {
        if (postiDisponibili < treno.getPostiTotali()) {
            postiDisponibili++;
            return true;
        }
        return false;
    }
    public boolean hasPostiDisponibili() {
        return postiDisponibili > 0 && stato.isAttivo();
    }

    public void aggiornaStato(StatoViaggio nuovoStato) {
        this.stato = nuovoStato;
    }

    public void cambioOrarioPartenza(LocalTime nuovoOrario) {
        this.orarioPartenzaEffettivo = nuovoOrario;
        // Ricalcola automaticamente l'orario di arrivo mantenendo la durata della tratta
        this.orarioArrivoEffettivo = nuovoOrario.plusMinutes(tratta.getDurataMinuti());
    }
    public void impostaRitardo(int minuti){//, String motivo) {
        this.ritardoMinuti = minuti;
        //this.motivoRitardo = motivo;
        if (minuti > 0) {
            this.stato = StatoViaggio.RITARDO;
            // Il ritardo si applica all'orario di arrivo, non modifica la partenza
            this.orarioArrivoEffettivo = this.orarioArrivoProgrammato.plusMinutes(minuti);
        } else {
            // Ritardo azzerato
            this.orarioArrivoEffettivo = this.orarioArrivoProgrammato;
        }
    }

    public void cambioBinarioPartenza(Binario nuovoBinario) {
        this.binarioPartenza = nuovoBinario;
    }

    public void cancellaViaggio(String motivo) {
        this.stato = StatoViaggio.CANCELLATO;
        this.motivoCancellazione = motivo;
    }

    public String getId() { return id; }
    public Treno getTreno() { return treno; }
    public Tratta getTratta() { return tratta; }
    public LocalDate getDataViaggio() { return dataViaggio; }
    public LocalTime getOrarioPartenza() { return orarioPartenzaProgrammato; }
    public LocalTime getOrarioArrivo() { return orarioArrivoProgrammato; }

    public LocalTime getOrarioPartenzaEffettivo() { return orarioPartenzaEffettivo; }
    public LocalTime getOrarioArrivoEffettivo() { return orarioArrivoEffettivo; }

    public Binario getBinarioPartenza() { return binarioPartenza; }

    public int getPostiDisponibili() { return postiDisponibili; }
    public int getPostiOccupati() { return treno.getPostiTotali() - postiDisponibili; }
    public StatoViaggio getStato() { return stato; }

    public int getRitardoMinuti() { return ritardoMinuti; }

    public String getMotivoCancellazione() { return motivoCancellazione; }

    public LocalDateTime getDataOraPartenza() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaProgrammato);
    }

    public LocalDateTime getDataOraArrivo() {
        return LocalDateTime.of(dataViaggio, orarioArrivoProgrammato);
    }

    public LocalDateTime getDataOraPartenzaEffettiva() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaEffettivo);
    }

    public LocalDateTime getDataOraArrivoEffettiva() {
        return LocalDateTime.of(dataViaggio, orarioArrivoEffettivo);
    }

    public double getPrezzo() {return tratta.getPrezzo();}

    public boolean isDisponibile() {return hasPostiDisponibili() && stato.isAttivo();}

    public boolean isCancellato() {return stato == StatoViaggio.CANCELLATO;}

    public boolean haRitardo() {return ritardoMinuti > 0;}

    public boolean hasCambioBinario() {return binarioPartenza != null;}

    // Formattazione leggibile per date/ore
    public String getDataOraPartenzaFormattata() {
        return String.format("%02d/%02d/%d alle %02d:%02d",
                dataViaggio.getDayOfMonth(),
                dataViaggio.getMonthValue(),
                dataViaggio.getYear(),
                orarioPartenzaEffettivo.getHour(),
                orarioPartenzaEffettivo.getMinute());
    }
    public String getDataOraArrivoFormattata() {
        return String.format("%02d/%02d/%d alle %02d:%02d",
                dataViaggio.getDayOfMonth(),
                dataViaggio.getMonthValue(),
                dataViaggio.getYear(),
                orarioArrivoEffettivo.getHour(),
                orarioArrivoEffettivo.getMinute());
    }

    public String getInfoBinari() {
        return binarioPartenza == null ?
                "Binario non assegnato" :
                "Binario " + binarioPartenza.getNumero();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Viaggio %s: %s → %s del %s",
                treno.getCodice(),
                tratta.getStazionePartenza().getNome(),
                tratta.getStazioneArrivo().getNome(),
                dataViaggio));
        if (!orarioPartenzaEffettivo.equals(orarioPartenzaProgrammato)) {
            sb.append(String.format(" alle %s (era %s)",
                    orarioPartenzaEffettivo, orarioPartenzaProgrammato));
        } else {
            sb.append(String.format(" alle %s", orarioPartenzaEffettivo));
        }
        if (ritardoMinuti > 0) {
            sb.append(String.format(" (+%d min ritardo)", ritardoMinuti));
        }
        sb.append(String.format(" (€%.2f, %d posti disponibili, %s)",
                getPrezzo(), postiDisponibili, stato));
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Viaggio viaggio = (Viaggio) obj;
        return Objects.equals(id, viaggio.id);
    }

    @Override
    public int hashCode() {return Objects.hash(id);}
}