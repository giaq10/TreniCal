package it.trenical.common.model.viaggi.strategy;

import it.trenical.server.treni.TipoTreno;

public class CalcoloViaggioBusiness implements CalcoloViaggioStrategy {

    private final CalcoloViaggioEconomy economyStrategy = new CalcoloViaggioEconomy();

    @Override
    public int calcolaDurata(int distanzaKm, TipoTreno tipoTreno) {
        // Calcola durata base Economy e poi la riduce significativamente
        int durataEconomy = economyStrategy.calcolaDurata(distanzaKm, TipoTreno.ECONOMY);
        double percentualeRiduzione = 0.70 + (Math.random() * 0.10); // Riduzione tra 70% e 80%
        return (int) Math.round(durataEconomy * (1.0 - percentualeRiduzione));
    }

    @Override
    public double calcolaPrezzo(int distanzaKm, TipoTreno tipoTreno) {
        // Calcola prezzo base Economy e poi lo aumenta significativamente
        double prezzoEconomy = economyStrategy.calcolaPrezzo(distanzaKm, TipoTreno.ECONOMY);
        double percentualeAumento = 2.0 + (Math.random() * 1.0); // Aumento tra 200% e 300%
        return Math.round(prezzoEconomy * (1.0 + percentualeAumento) * 100.0) / 100.0;
    }
}