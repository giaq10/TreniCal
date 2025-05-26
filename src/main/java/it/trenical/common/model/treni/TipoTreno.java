package it.trenical.common.model.treni;

public enum TipoTreno {
    ECONOMY("Economy"),
    STANDARD("Standard"),
    BUSINESS("Business");

    private final String nome;

    TipoTreno(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
