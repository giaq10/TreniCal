package it.trenical.common.cliente;

import it.trenical.server.viaggi.Viaggio;
import it.trenical.common.stazioni.Binario;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.treni.TipoTreno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Biglietto implements Cloneable {

    private String id;
    private final Viaggio viaggio;
    private String nominativo;
    private final LocalDateTime dataAcquisto;

    public Biglietto(Viaggio viaggio) {
        if (viaggio == null)
            throw new IllegalArgumentException("Viaggio obbligatorio");
        if(!viaggio.isDisponibile())
            throw new IllegalArgumentException("Viaggio non disponibile");
        if(!viaggio.prenotaPosto())
            throw new IllegalArgumentException("Posti non disponibili per il viaggio");
        this.viaggio = viaggio;
        this.dataAcquisto = LocalDateTime.now();
        this.id = generaIdBiglietto();
    }

    public Biglietto(Viaggio viaggio, String id, String nominativo, LocalDateTime dataAcquisto) {
        this.viaggio = viaggio;
        this.id = id;
        this.nominativo = nominativo;
        this.dataAcquisto = dataAcquisto;
    }

    private String generaIdBiglietto() {
        if (nominativo == null) {
            return null; // Non genera ID se non c'è nominativo
        }
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String input = String.format("%s_%s_%d",
                nominativo.replaceAll("\\s+", ""),
                viaggio.getId(),
                System.currentTimeMillis() // Timestamp unico!
        );
        int hash = Math.abs(input.hashCode());
        return String.format("BGT_%08d", hash % 100000000);
    }

    @Override
    public Biglietto clone() {
        try {
            Biglietto clonato = (Biglietto) super.clone();
            viaggio.prenotaPosto();
            return clonato;
        } catch (CloneNotSupportedException e) {
            // Non dovrebbe mai succedere dato che implementiamo Cloneable
            throw new AssertionError("Clonazione non supportata", e);
        }
    }

    public void setNominativo(String nominativo) {
        if (nominativo == null || nominativo.trim().isEmpty()) {
            throw new IllegalArgumentException("Nominativo non può essere vuoto");
        }
        this.nominativo = nominativo.trim();
        this.id = generaIdBiglietto(); // Genera ID dopo aver impostato il nominativo
    }

    public boolean isCompleto() {
        return nominativo != null && !nominativo.trim().isEmpty() && id != null;
    }

    public String getId() {
        return id != null ? id : "[ID GENERATO DOPO NOMINATIVO]";
    }
    public String getNominativo() {
        return nominativo != null ? nominativo : "[NOMINATIVO DA INSERIRE]";
    }
    public Viaggio getViaggio() { return viaggio; }
    public LocalDateTime getDataAcquisto() { return dataAcquisto; }

    public String getIdViaggio() { return viaggio.getId(); }
    public String getCodiceTreno() { return viaggio.getTreno().getCodice(); }
    public TipoTreno getTipoTreno() { return viaggio.getTreno().getTipoTreno(); }
    public Stazione getStazionePartenza() { return viaggio.getTratta().getStazionePartenza(); }
    public Stazione getStazioneArrivo() { return viaggio.getTratta().getStazioneArrivo(); }
    public LocalDate getDataViaggio() { return viaggio.getDataViaggio(); }
    public LocalTime getOrarioPartenza() { return viaggio.getOrarioPartenza(); }
    public LocalTime getOrarioArrivo() { return viaggio.getOrarioArrivo(); }
    public LocalTime getOrarioPartenzaEffettivo() { return viaggio.getOrarioPartenzaEffettivo(); }
    public LocalTime getOrarioArrivoEffettivo() { return viaggio.getOrarioArrivoEffettivo(); }
    public Binario getBinarioPartenza() { return viaggio.getBinarioPartenza(); }
    public double getPrezzo() { return viaggio.getPrezzo(); }
    public int getDurataMinuti() { return viaggio.getDurataMinuti(); }
    public String getDurataFormattata() { return viaggio.getDurataFormattata(); }

    public boolean isDisponibile() { return viaggio.isDisponibile(); }
    public boolean isCancellato() { return viaggio.isCancellato(); }
    public boolean haRitardo() { return viaggio.haRitardo(); }
    public int getRitardoMinuti() { return viaggio.getRitardoMinuti(); }

    public String getDataOraPartenzaFormattata() {
        return viaggio.getDataOraPartenzaFormattata();
    }

    public String getDataOraArrivoFormattata() {
        return viaggio.getDataOraArrivoFormattata();
    }

    public String getInfoTratta() {
        return String.format("%s - %s",
                getStazionePartenza().getNome(),
                getStazioneArrivo().getNome());
    }

    public String getDataAcquistoFormattata() {
        return String.format("%02d/%02d/%d alle %02d:%02d",
                dataAcquisto.getDayOfMonth(),
                dataAcquisto.getMonthValue(),
                dataAcquisto.getYear(),
                dataAcquisto.getHour(),
                dataAcquisto.getMinute());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Biglietto biglietto = (Biglietto) obj;
        return Objects.equals(id, biglietto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String nomeDisplay = isCompleto() ? nominativo : "[NOME DA INSERIRE]";
        String idDisplay = id != null ? id : "[ID PENDING]";
        return String.format("Biglietto %s: %s per %s (%s - %s, %s, €%.2f)",
                idDisplay, nomeDisplay, getCodiceTreno(),
                getStazionePartenza().getNome(), getStazioneArrivo().getNome(),
                getDataViaggio(), getPrezzo());
    }
}