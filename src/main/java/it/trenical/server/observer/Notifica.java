package it.trenical.server.observer;

public class Notifica {

    private final TipoNotifica tipo;
    private final String messaggio;

    public Notifica(TipoNotifica tipo, String message) {
        this.tipo = tipo;
        this.messaggio = message;
    }

    public TipoNotifica getTipo() { return tipo; }
    public String getMessaggio() { return messaggio; }

    @Override
    public String toString() {
        return String.format("Notifica: tipo=%s, messaggio='%s'}",
                 tipo, messaggio);
    }
}