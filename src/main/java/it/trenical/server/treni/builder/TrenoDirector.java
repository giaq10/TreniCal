package it.trenical.server.treni.builder;

import it.trenical.server.treni.Treno;
import it.trenical.server.treni.TipoTreno;
import it.trenical.server.treni.builder.*;

public class TrenoDirector {

    public Treno costruisciTreno(TrenoBuilder builder, String codice) {
        return builder
                .setCodice(codice)
                .buildTipo()
                .buildPosti()
                .buildServizi()
                .getResult();
    }

    public Treno costruisciTrenoBusiness(String codice) {
        return costruisciTreno(new TrenoBusinessBuilder(), codice);
    }

    public Treno costruisciTrenoStandard(String codice) {
        return costruisciTreno(new TrenoStandardBuilder(), codice);
    }

    public Treno costruisciTrenoEconomy(String codice) {
        return costruisciTreno(new TrenoEconomyBuilder(), codice);
    }

    public Treno costruisciTrenoPerTipo(TipoTreno tipo, String codice) {
        TrenoBuilder builder;
        switch (tipo) {
            case BUSINESS:
                builder = new TrenoBusinessBuilder();
                break;
            case STANDARD:
                builder = new TrenoStandardBuilder();
                break;
            case ECONOMY:
                builder = new TrenoEconomyBuilder();
                break;
            default:
                throw new IllegalArgumentException("Tipo treno non supportato: " + tipo);
        }
        return costruisciTreno(builder, codice);
    }
}