package it.trenical.common.model.treni.builder;

import it.trenical.common.model.treni.ServizioTreno;
import it.trenical.common.model.treni.TipoTreno;
import it.trenical.common.model.treni.Treno;

import java.util.EnumSet;
import java.util.Set;

public  class Builder {
    protected String codice;
    protected TipoTreno tipoTreno;
    protected int postiTotali;
    protected Set<ServizioTreno> servizi = EnumSet.noneOf(ServizioTreno.class);

    public Builder codice(String codice) {
        this.codice = codice;
        return this;
    }

    public Builder tipo(TipoTreno tipo) {
        this.tipoTreno = tipo;
        this.postiTotali = tipo.getPostiStandard(); // Default del tipo
        return this;
    }

    public Builder postiTotali(int posti) {
        this.postiTotali = posti;
        return this;
    }

    public Builder aggiungiServizio(ServizioTreno servizio) {
        this.servizi.add(servizio);
        return this;
    }

    public Builder servizi(ServizioTreno... servizi) {
        for (ServizioTreno servizio : servizi) {
            this.servizi.add(servizio);
        }
        return this;
    }

    public Treno build() {
        // Validazioni
        if (codice == null || codice.trim().isEmpty()) {
            throw new IllegalArgumentException("Codice treno obbligatorio");
        }
        if (tipoTreno == null) {
            throw new IllegalArgumentException("Tipo treno obbligatorio");
        }
        if (postiTotali <= 0) {
            throw new IllegalArgumentException("Numero posti deve essere positivo");
        }

        return new Treno(codice,tipoTreno,postiTotali,servizi);
    }
}
