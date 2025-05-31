package it.trenical.server.treni;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class Treno {
    private final String codice;
    private final TipoTreno tipoTreno;
    private final int postiTotali;
    private final Set<ServizioTreno> servizi;

    public Treno(String codice,TipoTreno tipoTreno,int postiTotali,Set<ServizioTreno> servizi) {
        if (codice == null || codice.trim().isEmpty())
            throw new IllegalArgumentException("Codice treno obbligatorio");
        if (tipoTreno == null)
            throw new IllegalArgumentException("Tipo treno obbligatorio");
        if (postiTotali <= 0)
            throw new IllegalArgumentException("Numero posti deve essere positivo");

        this.codice = codice;
        this.tipoTreno = tipoTreno;
        this.postiTotali = postiTotali;
        this.servizi = EnumSet.copyOf(servizi);
    }

    public String getCodice() { return codice; }
    public TipoTreno getTipoTreno() { return tipoTreno; }
    public int getPostiTotali() { return postiTotali; }
    public Set<ServizioTreno> getServizi() { return EnumSet.copyOf(servizi); }

    public boolean hasServizio(ServizioTreno servizio) {
        return servizi.contains(servizio);
    }

    @Override
    public String toString() {
        return String.format("Treno %s (%s, %d posti, %d servizi)",
                codice, tipoTreno, postiTotali, servizi.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Treno treno = (Treno) obj;
        return Objects.equals(codice, treno.codice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codice);
    }
}