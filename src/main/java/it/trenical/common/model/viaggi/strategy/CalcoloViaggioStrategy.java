package it.trenical.common.model.viaggi.strategy;

import it.trenical.server.treni.TipoTreno;
/**
 * Strategy per calcolare durata e prezzo di un viaggio basandosi su distanza in km e tipo di treno
 */
public interface CalcoloViaggioStrategy {

    /**
     * Calcola la durata del viaggio in minuti
     * @param distanzaKm distanza della tratta in kilometri
     * @param tipoTreno tipo di treno che effettua il viaggio
     * @return durata in minuti
     */
    int calcolaDurata(int distanzaKm, TipoTreno tipoTreno);
    /**
     * Calcola il prezzo del viaggio
     * @param distanzaKm distanza della tratta in kilometri
     * @param tipoTreno tipo di treno che effettua il viaggio
     * @return prezzo in euro
     */
    double calcolaPrezzo(int distanzaKm, TipoTreno tipoTreno);
}