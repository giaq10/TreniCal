package it.trenical.common.promozioni.factoryMethod;

import it.trenical.common.promozioni.Promozione;
import it.trenical.common.promozioni.PromozioneFedelta;

public class PromozioneFedeltaFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneFedelta(nome, percentualeSconto);
    }
}
