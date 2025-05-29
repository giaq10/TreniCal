package it.trenical.common.model.viaggi;

import it.trenical.common.model.tratte.Tratta;
import it.trenical.common.model.treni.Treno;
import it.trenical.common.model.stazioni.Binario;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ViaggioFactory {

    public static Viaggio creaViaggio(Treno treno, Tratta tratta, LocalDate data, LocalTime orarioPartenza) {
        return new Viaggio(treno, tratta, data, orarioPartenza);
    }

    public static Viaggio creaViaggio(Treno treno, Tratta tratta, LocalDate data, LocalTime orarioPartenza,
                                      Binario binarioPartenza, Binario binarioArrivo) {
        return new Viaggio(treno, tratta, data, orarioPartenza, binarioPartenza, binarioArrivo);
    }

    public static Viaggio creaViaggioConBinarioAutomatico(Treno treno, Tratta tratta,
                                                          LocalDate data, LocalTime orarioPartenza) {
        Binario binarioRandom = Binario.values()[(int)(Math.random() * Binario.values().length)];
        return new Viaggio(treno, tratta, data, orarioPartenza, binarioRandom, binarioRandom);
    }

    public static List<Viaggio> filtraViaggi(List<Viaggio> viaggi,
                                             LocalTime orarioMinimo,
                                             LocalTime orarioMassimo,
                                             Double prezzoMassimo,
                                             boolean soloConPosti) {
        List<Viaggio> risultato = new ArrayList<>();

        for (Viaggio viaggio : viaggi) {
            boolean valido = true;
            // Controllo orario minimo
            if (orarioMinimo != null && viaggio.getOrarioPartenza().isBefore(orarioMinimo)) {
                valido = false;
            }
            // Controllo orario massimo
            if (orarioMassimo != null && viaggio.getOrarioPartenza().isAfter(orarioMassimo)) {
                valido = false;
            }
            // Controllo prezzo massimo
            if (prezzoMassimo != null && viaggio.getPrezzo() > prezzoMassimo) {
                valido = false;
            }
            // Controllo posti disponibili
            if (soloConPosti && !viaggio.hasPostiDisponibili()) {
                valido = false;
            }
            if (valido) {
                risultato.add(viaggio);
            }
        }

        return risultato;
    }

    public static boolean isViaggioValido(Treno treno, Tratta tratta, LocalDate data, LocalTime orario) {
        if (treno == null || tratta == null || data == null || orario == null) {
            return false;
        }
        // Verifica compatibilità tipo treno
        if (!treno.getTipoTreno().equals(tratta.getTipoTreno())) {
            return false;
        }
        // Verifica data non nel passato
        if (data.isBefore(LocalDate.now())) {
            return false;
        }
        return true;
    }
}