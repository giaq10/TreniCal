package it.trenical.common.promozioni.factoryMethod;

import it.trenical.common.promozioni.Promozione;
import it.trenical.common.promozioni.PromozioneStandard;

public class PromozioneStandardFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneStandard(nome, percentualeSconto);
    }
}
