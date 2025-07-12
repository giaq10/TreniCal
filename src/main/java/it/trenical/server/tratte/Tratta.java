package it.trenical.server.tratte;


import it.trenical.server.stazioni.Stazione;
import java.util.Objects;

public class Tratta {
    private final Stazione stazionePartenza;
    private final Stazione stazioneArrivo;
    private final int distanzaKm;

    public Tratta(Stazione stazionePartenza, Stazione stazioneArrivo) {
        if (stazionePartenza == null) throw new IllegalArgumentException("Stazione di partenza obbligatoria");
        if (stazioneArrivo == null) throw new IllegalArgumentException("Stazione di arrivo obbligatoria");
        if (stazionePartenza.equals(stazioneArrivo))throw new IllegalArgumentException("Stazione di partenza e arrivo non possono essere uguali");
        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
        // Calcolo della distanza in base alla differenza dei valori delle stazioni
        this.distanzaKm = (int) (stazionePartenza.calcolaDistanzaVerso(stazioneArrivo));
    }

    public Stazione getStazionePartenza() {return stazionePartenza;}
    public Stazione getStazioneArrivo() {return stazioneArrivo;}
    public int getDistanzaKm() {return distanzaKm;}

    @Override
    public String toString() {
        return String.format("%s → %s (%d km)",
                stazionePartenza.getNome(),
                stazioneArrivo.getNome(),
                distanzaKm);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tratta tratta = (Tratta) obj;
        return Objects.equals(stazionePartenza, tratta.stazionePartenza) &&
                Objects.equals(stazioneArrivo, tratta.stazioneArrivo);
    }
    @Override
    public int hashCode() {return Objects.hash(stazionePartenza, stazioneArrivo);}
}