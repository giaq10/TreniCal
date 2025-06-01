package it.trenical.common.promozioni;

public class PromozioneFedelta extends Promozione{

    public PromozioneFedelta(String nome, double sconto) {
        super(nome, sconto);
    }

    @Override
    public String getTipo() {
        return "Fedelta";
    }
}
