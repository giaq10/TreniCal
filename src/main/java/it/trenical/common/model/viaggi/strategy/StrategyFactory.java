package it.trenical.common.model.viaggi.strategy;

import it.trenical.server.treni.TipoTreno;

public class StrategyFactory {

    public static CalcoloViaggioStrategy getStrategy(TipoTreno tipoTreno) {
        switch (tipoTreno) {
            case ECONOMY:
                return new CalcoloViaggioEconomy();
            case STANDARD:
                return new CalcoloViaggioStandard();
            case BUSINESS:
                return new CalcoloViaggioBusiness();
            default:
                throw new IllegalArgumentException("Tipo treno non supportato: " + tipoTreno);
        }
    }
}