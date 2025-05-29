package it.trenical.common.model.treni.builder;

import it.trenical.common.model.treni.ServizioTreno;
import it.trenical.common.model.treni.TipoTreno;

public class BuilderStandard extends Builder {
    public BuilderStandard() {
        this.tipoTreno = TipoTreno.STANDARD;
        this.postiTotali = TipoTreno.STANDARD.getPostiStandard();
        // Servizi intermedi
        this.servizi.add(ServizioTreno.ARIA_CONDIZIONATA);
        this.servizi.add(ServizioTreno.WIFI);
        this.servizi.add(ServizioTreno.PRESE_ELETTRICHE);
    }
}
