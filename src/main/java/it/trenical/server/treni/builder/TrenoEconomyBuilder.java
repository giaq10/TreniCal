package it.trenical.server.treni.builder;

import it.trenical.server.treni.ServizioTreno;
import it.trenical.server.treni.TipoTreno;

public class TrenoEconomyBuilder extends TrenoBuilder {

    @Override
    public TrenoBuilder buildTipo() {
        this.tipoTreno = TipoTreno.ECONOMY;
        return this;
    }

    @Override
    public TrenoBuilder buildPosti() {
        this.postiTotali = TipoTreno.ECONOMY.getPostiStandard();
        return this;
    }

    @Override
    public TrenoBuilder buildServizi() {
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
        return this;
    }
}