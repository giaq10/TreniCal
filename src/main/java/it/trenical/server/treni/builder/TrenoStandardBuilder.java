package it.trenical.server.treni.builder;

import it.trenical.server.treni.ServizioTreno;
import it.trenical.server.treni.TipoTreno;

public class TrenoStandardBuilder extends TrenoBuilder {

    @Override
    public TrenoBuilder buildTipo() {
        this.tipoTreno = TipoTreno.STANDARD;
        return this;
    }

    @Override
    public TrenoBuilder buildPosti() {
        this.postiTotali = TipoTreno.STANDARD.getPostiStandard();
        return this;
    }

    @Override
    public TrenoBuilder buildServizi() {
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
        this.servizi.add(ServizioTreno.WIFI);
        this.servizi.add(ServizioTreno.PRESE_ELETTRICHE);
        return this;
    }
}
