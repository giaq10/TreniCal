package it.trenical.server.promozioni;

public class PromozioneStandard extends Promozione{
    public PromozioneStandard(String nome, double sconto) {
        super(nome, sconto);
    }
    public PromozioneStandard(String id, String nome, double sconto) {super(id, nome, sconto);}

    @Override
    public String getTipo() {
        return "Standard";
    }

}
