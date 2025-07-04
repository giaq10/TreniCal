package it.trenical.server.promozioni;

public class PromozioneFedelta extends Promozione{

    public PromozioneFedelta(String nome, double sconto) {
        super(nome, sconto);
    }
    public PromozioneFedelta(String id, String nome, double sconto) {super(id, nome, sconto);}

    @Override
    public String getTipo() {
        return "Fedelta";
    }
}
