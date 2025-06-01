package it.trenical.common.promozioni;

public class PromozioneStandard extends Promozione{
    public PromozioneStandard(String nome, double sconto) {
        super(nome, sconto);
    }

    @Override
    public String getTipo() {
        return "Standard";
    }

}
