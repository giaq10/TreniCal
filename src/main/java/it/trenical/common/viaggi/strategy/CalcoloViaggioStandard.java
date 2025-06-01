package it.trenical.common.viaggi.strategy;

import it.trenical.server.treni.TipoTreno;

public class CalcoloViaggioStandard implements CalcoloViaggioStrategy {

    private final CalcoloViaggioEconomy economyStrategy = new CalcoloViaggioEconomy();

    @Override
    public int calcolaDurata(int distanzaKm, TipoTreno tipoTreno) {
        // Calcola durata base Economy e poi la riduce
        int durataEconomy = economyStrategy.calcolaDurata(distanzaKm, TipoTreno.ECONOMY);
        double percentualeRiduzione = 0.45 + (Math.random() * 0.15); // Riduzione tra 45% e 60%
        return (int) Math.round(durataEconomy * (1.0 - percentualeRiduzione));
    }

    @Override
    public double calcolaPrezzo(int distanzaKm, TipoTreno tipoTreno) {
        // Calcola prezzo base Economy e poi lo aumenta
        double prezzoEconomy = economyStrategy.calcolaPrezzo(distanzaKm, TipoTreno.ECONOMY);
        double percentualeAumento = 1.0 + (Math.random() * 0.20); // Aumento tra 100% e 120%
        return Math.round(prezzoEconomy * (1.0 + percentualeAumento) * 100.0) / 100.0;
    }
}