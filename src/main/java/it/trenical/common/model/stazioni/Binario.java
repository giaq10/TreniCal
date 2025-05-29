package it.trenical.common.model.stazioni;

public enum Binario {
    BINARIO_1(1, "Binario 1"),
    BINARIO_2(2, "Binario 2"),
    BINARIO_3(3, "Binario 3");

    private final int numero;
    private final String descrizione;

    Binario(int numero, String descrizione) {
        this.numero = numero;
        this.descrizione = descrizione;
    }
    public int getNumero() {
        return numero;
    }
    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return descrizione;
    }
}