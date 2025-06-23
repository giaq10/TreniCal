package it.trenical.client.carrello;

import it.trenical.grpc.ViaggioDTO;

public class CarrelloItem {

    private String viaggioId;
    private double prezzo;
    private int quantita;
    private ViaggioDTO viaggio;

    public CarrelloItem(String viaggioId,  double prezzo, int quantita, ViaggioDTO viaggio) {
        this.viaggioId = viaggioId;
        this.prezzo = prezzo;
        this.quantita = quantita;
        this.viaggio = viaggio;
    }

    public String getViaggioId() {return viaggioId;}

    public double getPrezzo() {return prezzo;}

    public int getQuantita() {return quantita;}

    public ViaggioDTO getViaggio() {return viaggio;}

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public double getPrezzoTotale() {
        return prezzo * quantita;
    }

    public void incrementaQuantita(int incremento) {
        this.quantita += incremento;
    }

    @Override
    public String toString() {
        return String.format("Item: viaggioId='%s', quantita=%d, prezzo=%.2f",
                viaggioId, quantita, prezzo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CarrelloItem that = (CarrelloItem) obj;
        return viaggioId != null ? viaggioId.equals(that.viaggioId) : that.viaggioId == null;
    }

    @Override
    public int hashCode() {
        return viaggioId != null ? viaggioId.hashCode() : 0;
    }
}