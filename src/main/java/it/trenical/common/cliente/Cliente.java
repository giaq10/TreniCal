package it.trenical.common.cliente;

import it.trenical.common.observer.Notifica;
import it.trenical.common.observer.Observer;
import it.trenical.server.gui.AdminViaggi;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cliente implements Observer {
    private final String email;
    private String password;
    private String nome;
    private boolean abbonamentoFedelta;
    private boolean notificheAttive;
    private final List<Biglietto> bigliettiAcquistati;

    private final List<Notifica> notificheRicevute;

    public Cliente(String email, String password ,String nome, boolean abbonamentoFedelta, boolean notificheAttive) {
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
        this.notificheAttive = notificheAttive;
        this.bigliettiAcquistati = new ArrayList<>();

        this.notificheRicevute = new ArrayList<>();
    }

    public Cliente(String email,String password, String nome) {
        this(email, password, nome, false, false);
    }

    @Override
    public void update(Notifica notifica) {
        notificheRicevute.add(notifica);
        String notificaCompleta = this.email + "|" + notifica.getMessaggio();
        AdminViaggi.aggiungiNotificaStatica(notificaCompleta);
        System.out.println("Cliente " + email + " ha ricevuto notifica: " + notifica.getMessaggio());
    }

    @Override
    public String getObserverId() {
        return email;
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
        this.notificheAttive = false;
    }

    public void attivaNotifichePromozioni() {
        if (!abbonamentoFedelta) {
            throw new IllegalStateException("Le notifiche promozioni sono disponibili solo per clienti fedeltà");
        }
        this.notificheAttive = true;
    }

    public void disattivaNotifichePromozioni() {
        this.notificheAttive = false;
    }

    public boolean riceviNotificheFedelta() {
        return abbonamentoFedelta && notificheAttive;
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

    public boolean hasNotificheAttive() {return notificheAttive;}

    public String getStatusAbbonamento() {
        return abbonamentoFedelta ? "Abbonato Fedeltà" : "Cliente Standard";
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