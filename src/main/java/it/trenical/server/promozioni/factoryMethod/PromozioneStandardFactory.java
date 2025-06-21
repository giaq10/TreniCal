package it.trenical.server.promozioni.factoryMethod;

import it.trenical.server.promozioni.Promozione;
import it.trenical.server.promozioni.PromozioneStandard;

public class PromozioneStandardFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneStandard(nome, percentualeSconto);
    }
}
