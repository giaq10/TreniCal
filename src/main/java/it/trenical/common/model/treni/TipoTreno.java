package it.trenical.common.model.treni;

public enum TipoTreno {
    ECONOMY("Economy", 450, 70.0),
    STANDARD("Standard", 350, 90.0),
    BUSINESS("Business", 250, 120.0);

    private final String nome;
    private final int postiStandard;
    private final double velocitaMedia;

    TipoTreno(String nome, int postiStandard, double velocitaMedia) {
        this.nome = nome;
        this.postiStandard = postiStandard;
        this.velocitaMedia = velocitaMedia;
    }

    public String getNome() { return nome; }
    public int getPostiStandard() { return postiStandard; }
    public double getVelocitaMedia() { return velocitaMedia; }

    @Override
    public String toString() { return nome; }
}