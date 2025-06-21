package it.trenical.server.promozioni;

public class PromozioneStandard extends Promozione{
    public PromozioneStandard(String nome, double sconto) {
        super(nome, sconto);
    }

    @Override
    public String getTipo() {
        return "Standard";
    }

}
