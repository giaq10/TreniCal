package it.trenical.common.model.tratte.strategy;

public class CalcoloTrattaEconomy implements CalcoloTrattaStrategy {

    private static final double VELOCITA_BASE_KMH = 70.0;
    private static final double PREZZO_BASE_PER_KM = 0.07;

    @Override
    public int calcolaDurata(int distanzaKm) {
        // Calcolo base della durata
        double durataOre = distanzaKm / VELOCITA_BASE_KMH;
        double variazione = 1.0 + ((Math.random() * 0.2) - 0.1);
        return (int) Math.round(durataOre * variazione * 60); // conversione in minuti
    }

    @Override
    public double calcolaPrezzo(int distanzaKm) {
        // Prezzo base
        double prezzoBase = distanzaKm * PREZZO_BASE_PER_KM;
        double variazione = 1.0 + ((Math.random() * 0.3) - 0.15);
        return Math.round(prezzoBase * variazione * 100.0) / 100.0; // arrotondamento a 2 decimali
    }
}