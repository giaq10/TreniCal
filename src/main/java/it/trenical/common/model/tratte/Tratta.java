package it.trenical.common.model.tratte;


import it.trenical.common.model.tratte.strategy.CalcoloTrattaBusiness;
import it.trenical.common.model.tratte.strategy.CalcoloTrattaEconomy;
import it.trenical.common.model.tratte.strategy.CalcoloTrattaStandard;
import it.trenical.common.model.tratte.strategy.CalcoloTrattaStrategy;
import it.trenical.common.model.treni.TipoTreno;

public class Tratta {
    private final Stazione stazionePartenza;
    private final Stazione stazioneArrivo;
    private final int distanzaKm;
    private final TipoTreno tipoTreno;
    private final int durataMinuti;
    private final double prezzo;

    public Tratta(Stazione stazionePartenza, Stazione stazioneArrivo, TipoTreno tipoTreno) {
        if (stazionePartenza.equals(stazioneArrivo)) {
            throw new IllegalArgumentException("Stazione di partenza e arrivo non possono essere uguali");
        }

        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
        this.tipoTreno = tipoTreno;
        // Calcolo della distanza in base alla differenza dei valori delle stazioni
        this.distanzaKm = calcolaDistanza();
        // Uso della Strategy per calcolare durata e prezzo
        CalcoloTrattaStrategy strategy = getStrategy(tipoTreno);
        this.durataMinuti = strategy.calcolaDurata(distanzaKm);
        this.prezzo = strategy.calcolaPrezzo(distanzaKm);
    }

    //Calcola la distanza approssimativa tra due stazione
    private int calcolaDistanza() {
        int differenzaValori = Math.abs(stazionePartenza.getValore() - stazioneArrivo.getValore());
        // Base km per ogni unità di differenza (circa 80-120 km per unità)
        int baseKmPerUnita = 100;
        int variazione = (int) (Math.random() * 40) - 20; // ±20 km
        return Math.max(50, (differenzaValori * baseKmPerUnita) + variazione);
    }

    //Restituisce la strategia di calcolo appropriata per il tipo di treno
    private CalcoloTrattaStrategy getStrategy(TipoTreno tipo) {
        switch (tipo) {
            case ECONOMY:
                return new CalcoloTrattaEconomy();
            case STANDARD:
                return new CalcoloTrattaStandard();
            case BUSINESS:
                return new CalcoloTrattaBusiness();
            default:
                throw new IllegalArgumentException("Tipo treno non supportato: " + tipo);
        }
    }

    // Getters
    public Stazione getStazionePartenza() {
        return stazionePartenza;
    }

    public Stazione getStazioneArrivo() {
        return stazioneArrivo;
    }

    public int getDistanzaKm() {
        return distanzaKm;
    }

    public TipoTreno getTipoTreno() {
        return tipoTreno;
    }

    public int getDurataMinuti() {
        return durataMinuti;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public String getDurataFormattata() {
        int ore = durataMinuti / 60;
        int minuti = durataMinuti % 60;
        return String.format("%dh %dm", ore, minuti);
    }

    @Override
    public String toString() {
        return String.format("Tratta: %s → %s (%d km, %s, %.2f€, %s)",
                stazionePartenza.getNome(),
                stazioneArrivo.getNome(),
                distanzaKm,
                tipoTreno,
                prezzo,
                getDurataFormattata());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Tratta tratta = (Tratta) obj;
        return stazionePartenza.equals(tratta.stazionePartenza) &&
                stazioneArrivo.equals(tratta.stazioneArrivo) &&
                tipoTreno.equals(tratta.tipoTreno);
    }

    @Override
    public int hashCode() {
        return stazionePartenza.hashCode() + stazioneArrivo.hashCode() + tipoTreno.hashCode();
    }
}