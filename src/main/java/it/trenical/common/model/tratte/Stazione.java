package it.trenical.common.model.tratte;

public enum Stazione {
    REGGIO_CALABRIA(0, "Reggio Calabria"),
    COSENZA(1, "Cosenza"),
    NAPOLI(2, "Napoli"),
    CASERTA(3, "Caserta"),
    ROMA(4, "Roma"),
    FIRENZE(5, "Firenze"),
    BOLOGNA(6, "Bologna"),
    VERONA(7, "Verona"),
    MILANO(8, "Milano"),
    TORINO(9, "Torino"),
    GENOVA(10, "Genova"),
    VENEZIA(11, "Venezia");

    private final int valore;
    private final String nome;

    Stazione(int valore, String nome) {
        this.valore = valore;
        this.nome = nome;
    }

    public int getValore() {return valore;}

    public String getNome() {return nome;}

    @Override
    public String toString() {return nome;}

    //Trova stazione
    public static Stazione fromNome(String nome) {
        for (Stazione stazione : values()) {
            if (stazione.nome.equalsIgnoreCase(nome)) {
                return stazione;
            }
        }
        throw new IllegalArgumentException("Stazione non trovata: " + nome);
    }
}
