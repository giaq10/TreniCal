package it.trenical.server.promozioni.factoryMethod;

import it.trenical.server.promozioni.Promozione;
import it.trenical.server.promozioni.PromozioneFedelta;

public class PromozioneFedeltaFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneFedelta(nome, percentualeSconto);
    }
}
