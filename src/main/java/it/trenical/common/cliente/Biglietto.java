package it.trenical.common.cliente;

import it.trenical.common.viaggi.Viaggio;
import it.trenical.common.stazioni.Binario;
import it.trenical.common.stazioni.Stazione;
import it.trenical.server.treni.TipoTreno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Biglietto implements Cloneable {

    private String id;
    private final Viaggio viaggio;
    private String nominativo;
    private final LocalDateTime dataAcquisto;

    /**
     * Costruttore principale per creare un biglietto
     * @param viaggio Viaggio per cui è acquistato il biglietto
     */
    public Biglietto(Viaggio viaggio) {

        if (viaggio == null) {
            throw new IllegalArgumentException("Viaggio obbligatorio");
        }
        this.viaggio = viaggio;
        this.dataAcquisto = LocalDateTime.now();
        this.id = generaIdBiglietto();
    }

    /**
     * Costruttore privato per la clonazione
     * @param viaggio Viaggio originale
     * @param dataAcquistoOriginale Data acquisto del biglietto originale
     */
    private Biglietto(Viaggio viaggio, LocalDateTime dataAcquistoOriginale) {
        this.viaggio = viaggio;
        this.dataAcquisto = dataAcquistoOriginale; // Mantiene la data originale
        this.id = generaIdBiglietto();
    }

    private String generaIdBiglietto() {
        if (nominativo == null) {
            return null; // Non genera ID se non c'è nominativo
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
        return String.format("%s → %s",
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

    public String getRiepilogoBiglietto() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BIGLIETTO TRENO ===\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Nominativo: ").append(getNominativo()).append("\n");

        if (!isCompleto()) {
            sb.append("⚠️ ATTENZIONE: Inserire nominativo per completare il biglietto\n");
        }

        sb.append("Treno: ").append(getCodiceTreno()).append(" (").append(getTipoTreno()).append(")\n");
        sb.append("Tratta: ").append(getInfoTratta()).append("\n");
        sb.append("Data viaggio: ").append(getDataOraPartenzaFormattata()).append("\n");
        sb.append("Arrivo: ").append(getDataOraArrivoFormattata()).append("\n");
        sb.append("Binario: ").append(getBinarioPartenza().getDescrizione()).append("\n");
        sb.append("Durata: ").append(getDurataFormattata()).append("\n");
        sb.append("Prezzo: €").append(String.format("%.2f", getPrezzo())).append("\n");
        sb.append("Acquistato: ").append(getDataAcquistoFormattata()).append("\n");

        if (haRitardo()) {
            sb.append("⚠️ RITARDO: ").append(getRitardoMinuti()).append(" minuti\n");
        }
        if (isCancellato()) {
            sb.append("❌ VIAGGIO CANCELLATO\n");
        }

        return sb.toString();
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
        return String.format("Biglietto %s: %s per %s (%s → %s, %s, €%.2f)",
                idDisplay, nomeDisplay, getCodiceTreno(),
                getStazionePartenza().getNome(), getStazioneArrivo().getNome(),
                getDataViaggio(), getPrezzo());
    }
}