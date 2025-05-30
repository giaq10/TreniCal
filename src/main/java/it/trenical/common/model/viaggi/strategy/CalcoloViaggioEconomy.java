package it.trenical.common.model.viaggi.strategy;

import it.trenical.common.model.treni.TipoTreno;

public class CalcoloViaggioEconomy implements CalcoloViaggioStrategy {

    private static final double VELOCITA_BASE_KMH = 70.0;
    private static final double PREZZO_BASE_PER_KM = 0.07;

    @Override
    public int calcolaDurata(int distanzaKm, TipoTreno tipoTreno) {
        // Il tipo treno viene passato ma per Economy usiamo sempre la velocità base
        double durataOre = distanzaKm / VELOCITA_BASE_KMH;
        double variazione = 1.0 + ((Math.random() * 0.2) - 0.1); // ±10% variazione
        return (int) Math.round(durataOre * variazione * 60);
    }

    @Override
    public double calcolaPrezzo(int distanzaKm, TipoTreno tipoTreno) {
        // Prezzo base per Economy
        double prezzoBase = distanzaKm * PREZZO_BASE_PER_KM;
        double variazione = 1.0 + ((Math.random() * 0.3) - 0.15); // ±15% variazione
        return Math.round(prezzoBase * variazione * 100.0) / 100.0;
    }
}