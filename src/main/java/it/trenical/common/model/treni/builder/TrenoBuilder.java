package it.trenical.common.model.treni.builder;

import it.trenical.common.model.treni.ServizioTreno;
import it.trenical.common.model.treni.TipoTreno;
import it.trenical.common.model.treni.Treno;

import java.util.EnumSet;
import java.util.Set;

public abstract class TrenoBuilder {
    protected String codice;
    protected TipoTreno tipoTreno;
    protected int postiTotali;
    protected Set<ServizioTreno> servizi = EnumSet.noneOf(ServizioTreno.class);

    public abstract TrenoBuilder buildTipo();
    public abstract TrenoBuilder buildServizi();
    public abstract TrenoBuilder buildPosti();

    public TrenoBuilder setCodice(String codice) {
        this.codice = codice;
        return this;
    }

    public Treno getResult() {
        if (codice == null || codice.trim().isEmpty()) {throw new IllegalArgumentException("Codice treno obbligatorio");}
        if (tipoTreno == null) {throw new IllegalArgumentException("Tipo treno obbligatorio");}
        if (postiTotali <= 0) {throw new IllegalArgumentException("Numero posti deve essere positivo");}
        return new Treno(codice, tipoTreno, postiTotali, servizi);
    }
}
