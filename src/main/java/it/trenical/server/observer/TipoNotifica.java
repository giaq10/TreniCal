package it.trenical.server.observer;

public enum TipoNotifica {

    //Viaggio
    CAMBIO_ORARIO_PARTENZA("Cambio Orario Partenza"),
    CAMBIO_BINARIO("Cambio Binario"),
    RITARDO_TRENO("Ritardo Treno"),
    CANCELLAZIONE_VIAGGIO("Cancellazione Viaggio"),

    //Biglietto
    SCADENZA_PRENOTAZIONE("Scadenza Prenotazione"),
    CONFERMA_PAGAMENTO("Conferma Pagamento"),
    CONFERMA_MODIFICA_BIGLIETTO("Modifica Biglietto"),

    //Promozione
    PROMOZIONE_FEDELTA("Promozione Fedeltà");

    private final String nome;

    TipoNotifica(String nome) {this.nome = nome;}

    @Override
    public String toString() {
        return nome;
    }
}