package it.trenical.common.model.tratte;

import it.trenical.common.model.stazioni.Stazione;
import it.trenical.common.model.treni.TipoTreno;

import java.util.ArrayList;
import java.util.List;

public class TrattaFactory {

    public static Tratta creaTratta(Stazione partenza, Stazione arrivo, TipoTreno tipo) {
        return new Tratta(partenza, arrivo, tipo);
    }

    public static Tratta creaTratta(String partenza, String arrivo, String tipo) {
        Stazione stazionePartenza = Stazione.fromNome(partenza);
        Stazione stazioneArrivo = Stazione.fromNome(arrivo);
        TipoTreno tipoTreno = TipoTreno.valueOf(tipo.toUpperCase());

        return new Tratta(stazionePartenza, stazioneArrivo, tipoTreno);
    }

    public static List<Tratta> creaTutteLeTratte(Stazione partenza, Stazione arrivo) {
        List<Tratta> tratte = new ArrayList<>();

        for (TipoTreno tipo : TipoTreno.values()) {
            tratte.add(new Tratta(partenza, arrivo, tipo));
        }

        return tratte;
    }

    public static boolean isTrattaValida(Stazione partenza, Stazione arrivo) {
        return partenza != null && arrivo != null && !partenza.equals(arrivo);
    }
}
