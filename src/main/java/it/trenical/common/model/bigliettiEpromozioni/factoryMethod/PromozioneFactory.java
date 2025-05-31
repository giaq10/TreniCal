package it.trenical.common.model.bigliettiEpromozioni.factoryMethod;

import it.trenical.common.model.bigliettiEpromozioni.*;

public abstract class PromozioneFactory {
    public abstract Promozione creaPromozione(String nome, double percentualeSconto);

    public static PromozioneFactory getFactory(String tipo) {
        if(tipo==null) throw new IllegalArgumentException("Tipo promozione non può essere null");

        switch (tipo.toLowerCase()) {
            case "standard":
                return new PromozioneStandardFactory();
            case "fedelta":
                return new PromozioneFedeltaFactory();
            default:
                throw new IllegalArgumentException("Tipo promozione non supportato: " + tipo);
        }
    }
}
