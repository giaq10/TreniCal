package it.trenical.server.treni;

public enum ServizioTreno {
    ALTA_VELOCITA("Alta Velocità"),
    RISTORAZIONE("Servizio Ristorazione"),
    WIFI("WiFi Gratuito"),
    ARIA_CONDIZIONATA("Aria Condizionata"),
    PRESE_ELETTRICHE("Prese Elettriche"),
    SILENZIOSO("Carrozza Silenzioso"),
    BUSINESS_LOUNGE("Accesso Business Lounge");

    private final String descrizione;

    ServizioTreno(String descrizione) {
        this.descrizione = descrizione;
    }
    public String getDescrizione() { return descrizione; }

    @Override
    public String toString() { return descrizione; }
}