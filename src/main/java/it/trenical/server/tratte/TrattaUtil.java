package it.trenical.server.tratte;

import it.trenical.common.stazioni.Stazione;
import java.util.ArrayList;
import java.util.List;

public class TrattaUtil {

    public static Tratta creaTratta(Stazione partenza, Stazione arrivo) {
        return new Tratta(partenza, arrivo);
    }

    public static Tratta creaTratta(String nomePartenza, String nomeArrivo) {
        Stazione stazionePartenza = Stazione.fromNome(nomePartenza);
        Stazione stazioneArrivo = Stazione.fromNome(nomeArrivo);
        return new Tratta(stazionePartenza, stazioneArrivo);
    }

    public static List<Tratta> creaTutteLeTratte() {
        List<Tratta> tratte = new ArrayList<>();
        Stazione[] stazioni = Stazione.values();
        // Per ogni stazione di partenza
        for (Stazione partenza : stazioni) {
            // Per ogni possibile destinazione (esclusa la partenza stessa)
            for (Stazione arrivo : stazioni) {
                if (!partenza.equals(arrivo)) {
                    tratte.add(new Tratta(partenza, arrivo));
                }
            }
        }

        return tratte;
    }

    public static boolean isTrattaValida(Stazione partenza, Stazione arrivo) {
        return partenza != null && arrivo != null && !partenza.equals(arrivo);
    }

    public static Tratta trovaTratta(List<Tratta> tratte, Stazione partenza, Stazione arrivo) {
        return tratte.stream()
                .filter(t -> t.getStazionePartenza().equals(partenza) &&
                        t.getStazioneArrivo().equals(arrivo))
                .findFirst()
                .orElse(null);
    }

}