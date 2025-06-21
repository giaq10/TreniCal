package it.trenical.server.promozioni;

import java.util.Objects;

public abstract class Promozione {
    private String id;
    private String nome;
    private double sconto;

    public Promozione(String nome, double sconto) {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Nome promozione obbligatorio");
        if (sconto <= 0 || sconto > 100) throw new IllegalArgumentException("Percentuale sconto deve essere tra 0 e 100");

        this.nome = nome;
        this.sconto = sconto;
        this.id = generaId(nome, sconto);
    }

    private String generaId(String nome, double sconto) {
        String input = nome + "_" + sconto;
        int hash = Math.abs(input.hashCode());
        return String.format("PROMO_%08d", hash % 100000000);
    }

    public double applicaSconto(double prezzo) {
        return prezzo - ((prezzo * sconto) / 100);
    }

    public abstract String getTipo();

    public String getId() { return id; }
    public String getNome() { return nome; }
    public double getSconto() { return sconto; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Promozione that = (Promozione) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Promozione %s: %s (%.1f%% sconto)",
                getTipo(), nome, sconto);
    }
}
