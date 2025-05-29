package it.trenical.common.model.tratte.strategy;

public class CalcoloTrattaBusiness implements CalcoloTrattaStrategy {

    private final CalcoloTrattaEconomy economyStrategy = new CalcoloTrattaEconomy();

    @Override
    public int calcolaDurata(int distanzaKm) {
        int durataEconomy = economyStrategy.calcolaDurata(distanzaKm);
        double percentualeRiduzione = 0.70 + (Math.random() * 0.10);// Riduzione tra 70% e 80%
        return (int) Math.round(durataEconomy * (1.0 - percentualeRiduzione));
    }
    @Override
    public double calcolaPrezzo(int distanzaKm) {
        double prezzoEconomy = economyStrategy.calcolaPrezzo(distanzaKm);
        double percentualeAumento = 2 + (Math.random() * 1);// Aumento tra 200% e 300%
        return Math.round(prezzoEconomy * (1.0 + percentualeAumento) * 100.0) / 100.0;
    }
}