package it.trenical.common.model.viaggi;

public enum StatoViaggio {
    PROGRAMMATO("Programmato"),
    CONFERMATO("Confermato"),
    IN_VIAGGIO("In Viaggio"),
    RITARDO("In Ritardo"),
    ARRIVATO("Arrivato"),
    CANCELLATO("Cancellato");

    private final String descrizione;

    StatoViaggio(String descrizione) {
        this.descrizione = descrizione;
    }
    public String getDescrizione() { return descrizione; }

    @Override
    public String toString() { return descrizione; }

    public boolean isAttivo() {
        return this == PROGRAMMATO || this == CONFERMATO || this == IN_VIAGGIO || this == RITARDO;
    }

    public boolean isConcluso() {
        return this == ARRIVATO || this == CANCELLATO;
    }
}