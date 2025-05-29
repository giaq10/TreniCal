package it.trenical.common.model.tratte.strategy;

public class CalcoloTrattaStandard implements CalcoloTrattaStrategy {

    private final CalcoloTrattaEconomy economyStrategy = new CalcoloTrattaEconomy();

    @Override
    public int calcolaDurata(int distanzaKm) {
        int durataEconomy = economyStrategy.calcolaDurata(distanzaKm);
        double percentualeRiduzione = 0.45 + (Math.random() * 0.15);// Riduzione tra 45% e 60%
        return (int) Math.round(durataEconomy * (1.0 - percentualeRiduzione));
    }
    @Override
    public double calcolaPrezzo(int distanzaKm) {
        double prezzoEconomy = economyStrategy.calcolaPrezzo(distanzaKm);
        double percentualeAumento = 1 + (Math.random() * 0.20);// Aumento tra 100% e 120%
        return Math.round(prezzoEconomy * (1.0 + percentualeAumento) * 100.0) / 100.0;
    }
}