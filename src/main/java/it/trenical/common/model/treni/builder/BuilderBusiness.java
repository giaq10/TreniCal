package it.trenical.common.model.treni.builder;

import it.trenical.common.model.treni.ServizioTreno;
import it.trenical.common.model.treni.TipoTreno;

public class BuilderBusiness extends Builder {
    public BuilderBusiness() {
        this.tipoTreno = TipoTreno.BUSINESS;
        this.postiTotali = TipoTreno.BUSINESS.getPostiStandard();
        // Servizi premium
        this.servizi.add(ServizioTreno.ALTA_VELOCITA);
        this.servizi.add(ServizioTreno.RISTORAZIONE);
        this.servizi.add(ServizioTreno.WIFI);
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
        this.servizi.add(ServizioTreno.PRESE_ELETTRICHE);
        this.servizi.add(ServizioTreno.SILENZIOSO);
        this.servizi.add(ServizioTreno.BUSINESS_LOUNGE);
    }
}
