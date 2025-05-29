package it.trenical.common.model.treni.builder;

import it.trenical.common.model.treni.ServizioTreno;
import it.trenical.common.model.treni.TipoTreno;

public class BuilderEconomy extends Builder {
    public BuilderEconomy() {
        this.tipoTreno = TipoTreno.ECONOMY;
        this.postiTotali = TipoTreno.ECONOMY.getPostiStandard();
        // Servizi base
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
    }
}