package it.trenical.server.treni.builder;

import it.trenical.server.treni.ServizioTreno;
import it.trenical.server.treni.TipoTreno;

public class TrenoBusinessBuilder extends TrenoBuilder {

    @Override
    public TrenoBuilder buildTipo() {
        this.tipoTreno = TipoTreno.BUSINESS;
        return this;
    }

    @Override
    public TrenoBuilder buildPosti() {
        this.postiTotali = TipoTreno.BUSINESS.getPostiStandard();
        return this;
    }

    @Override
    public TrenoBuilder buildServizi() {
        this.servizi.add(ServizioTreno.ALTA_VELOCITA);
        this.servizi.add(ServizioTreno.RISTORAZIONE);
        this.servizi.add(ServizioTreno.WIFI);
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
        this.servizi.add(ServizioTreno.PRESE_ELETTRICHE);
        this.servizi.add(ServizioTreno.SILENZIOSO);
        this.servizi.add(ServizioTreno.BUSINESS_LOUNGE);
        return this;
    }
}