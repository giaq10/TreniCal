package it.trenical.common.model.bigliettiEpromozioni;

public class PromozioneFedelta extends Promozione{

    public PromozioneFedelta(String nome, double sconto) {
        super(nome, sconto);
    }

    @Override
    public String getTipo() {
        return "Fedelta";
    }
}
