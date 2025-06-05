package it.trenical.common.cliente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cliente {
    private final String email;
    private String password;
    private String nome;
    private boolean abbonamentoFedelta;
    private final List<Biglietto> bigliettiAcquistati;

    /**
     * Costruttore per creare un nuovo cliente
     * @param email Email del cliente (identificativo unico)
     * @param nome Nome del cliente
     * @param abbonamentoFedelta Se il cliente ha l'abbonamento fedeltà
     */
    public Cliente(String email, String password ,String nome, boolean abbonamentoFedelta) {
        if (!isEmailValida(email))
            throw new IllegalArgumentException("Email non valida");
        if (password == null || password.trim().isEmpty() || password.length()<6)
            throw new IllegalArgumentException("Password obbligatoria o troppo corta");
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome obbligatorio");
        this.email = email.toLowerCase();
        this.password = password;
        this.nome = nome.trim();
        this.abbonamentoFedelta = abbonamentoFedelta;
        this.bigliettiAcquistati = new ArrayList<>();
    }
    /**
     * Costruttore semplificato senza abbonamento fedeltà (default false)
     * @param email Email del cliente
     * @param nome Nome del cliente
     */
    public Cliente(String email,String password, String nome) {
        this(email, password, nome, false);
    }

    private boolean isEmailValida(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+([.-][a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    public void modificaPassword(String password) {this.password = password;}
    public boolean autenticaPassword(String password) {
        if(password==null) return false;
        return password.equals(this.password);
    }

    public void attivaAbbonamentoFedelta() {
        this.abbonamentoFedelta = true;
    }

    public void disattivaAbbonamentoFedelta() {
        this.abbonamentoFedelta = false;
    }

    public void addBiglietto(Biglietto biglietto) {
        if (biglietto == null) {
            throw new IllegalArgumentException("Biglietto non può essere null");
        }
        bigliettiAcquistati.add(biglietto);
    }

    public void addBiglietti(List<Biglietto> biglietti) {
        if (biglietti == null) {
            throw new IllegalArgumentException("Lista biglietti non può essere null");
        }
        for (Biglietto biglietto : biglietti) {
            addBiglietto(biglietto);
        }
    }

    public boolean removeBiglietto(Biglietto biglietto) {
        return bigliettiAcquistati.remove(biglietto);
    }

    public boolean removeBigliettoById(String idBiglietto) {
        return bigliettiAcquistati.removeIf(b -> b.getId().equals(idBiglietto));
    }

    public Biglietto getBigliettoById(String idBiglietto) {
        return bigliettiAcquistati.stream()
                .filter(b -> b.getId().equals(idBiglietto))
                .findFirst()
                .orElse(null);
    }

    public List<Biglietto> getBiglietti() {
        return Collections.unmodifiableList(bigliettiAcquistati);
    }

    public int getNumeroBiglietti() {
        return bigliettiAcquistati.size();
    }

    public boolean hasBiglietti() {
        return !bigliettiAcquistati.isEmpty();
    }

    public double getTotaleSpeso() {
        return bigliettiAcquistati.stream()
                .mapToDouble(Biglietto::getPrezzo)
                .sum();
    }

    public List<Biglietto> getBigliettiValidi() {
        return bigliettiAcquistati.stream()
                .filter(b -> !b.isCancellato())
                .toList();
    }

    public void setNome(String nuovoNome) {
        if (nuovoNome == null || nuovoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome non può essere vuoto");
        }
        this.nome = nuovoNome.trim();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {return password;}

    public String getNome() {
        return nome;
    }

    public boolean hasAbbonamentoFedelta() {
        return abbonamentoFedelta;
    }

    public String getStatusAbbonamento() {
        return abbonamentoFedelta ? "Abbonato Fedeltà" : "Cliente Standard";
    }

    public String getStatisticheCliente() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STATISTICHE CLIENTE ===\n");
        sb.append("Nome: ").append(nome).append("\n");
        sb.append("Email: ").append(email).append("\n");
        sb.append("Status: ").append(getStatusAbbonamento()).append("\n");
        sb.append("Biglietti totali: ").append(getNumeroBiglietti()).append("\n");
        sb.append("Biglietti validi: ").append(getBigliettiValidi().size()).append("\n");
        sb.append("Totale speso: €").append(String.format("%.2f", getTotaleSpeso())).append("\n");

        if (getNumeroBiglietti() > 0) {
            double mediaSpesa = getTotaleSpeso() / getNumeroBiglietti();
            sb.append("Spesa media per biglietto: €").append(String.format("%.2f", mediaSpesa)).append("\n");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cliente cliente = (Cliente) obj;
        return Objects.equals(email, cliente.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("Cliente{email='%s', password='%s', nome='%s', %s}",
                email, password, nome, getStatusAbbonamento());
    }
}