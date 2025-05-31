package it.trenical.common.model.bigliettiEpromozioni.factoryMethod;

import it.trenical.common.model.bigliettiEpromozioni.*;

public class PromozioneFedeltaFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneFedelta(nome, percentualeSconto);
    }
}
