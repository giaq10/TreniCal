package it.trenical.common.model.bigliettiEpromozioni;

public class PromozioneStandard extends Promozione{
    public PromozioneStandard(String nome, double sconto) {
        super(nome, sconto);
    }

    @Override
    public String getTipo() {
        return "Standard";
    }

}
