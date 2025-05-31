package it.trenical.common.model.bigliettiEpromozioni.factoryMethod;

import it.trenical.common.model.bigliettiEpromozioni.*;

public class PromozioneStandardFactory extends PromozioneFactory {
    @Override
    public Promozione creaPromozione(String nome, double percentualeSconto) {
        return new PromozioneStandard(nome, percentualeSconto);
    }
}
