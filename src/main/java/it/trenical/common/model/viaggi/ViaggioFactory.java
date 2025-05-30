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

    @Deprecated
    public static Viaggio creaViaggio(Treno treno, Tratta tratta, LocalDate data, LocalTime orarioPartenza,
                                      Binario binarioPartenza, Binario binarioArrivo) {
        return new Viaggio(treno, tratta, data, orarioPartenza);
    }

    public static Viaggio creaViaggioConBinarioAutomatico(Treno treno, Tratta tratta,
                                                          LocalDate data, LocalTime orarioPartenza) {
        return new Viaggio(treno, tratta, data, orarioPartenza);
    }

    public static List<Viaggio> filtraViaggi(List<Viaggio> viaggi,LocalTime orarioMinimo,LocalTime orarioMassimo,
                                             Double prezzoMassimo,
                                             boolean soloConPosti) {
        List<Viaggio> risultato = new ArrayList<>();

        for (Viaggio viaggio : viaggi) {
            boolean valido = true;

            if (orarioMinimo != null && viaggio.getOrarioPartenza().isBefore(orarioMinimo)) valido = false;
            if (orarioMassimo != null && viaggio.getOrarioPartenza().isAfter(orarioMassimo)) valido = false;
            if (prezzoMassimo != null && viaggio.getPrezzo() > prezzoMassimo) valido = false;
            if (soloConPosti && !viaggio.hasPostiDisponibili()) valido = false;
            if (valido) risultato.add(viaggio);
        }

        return risultato;
    }

    public static boolean isViaggioValido(Treno treno, Tratta tratta, LocalDate data, LocalTime orario) {
        if (treno == null || tratta == null || data == null || orario == null)
            return false;
        if (data.isBefore(LocalDate.now()))
            return false;
        return true;
    }

    public static List<Viaggio> filtraPerTratta(List<Viaggio> viaggi, Tratta tratta) {
        List<Viaggio> risultato = new ArrayList<>();

        for (Viaggio viaggio : viaggi) {
            if (viaggio.getTratta().equals(tratta)) {
                risultato.add(viaggio);
            }
        }

        return risultato;
    }

    public static void stampaStatistiche(List<Viaggio> viaggi) {
        if (viaggi.isEmpty()) {
            System.out.println("Nessun viaggio da analizzare");
            return;
        }

        System.out.println("=== STATISTICHE VIAGGI ===");
        System.out.println("Totale viaggi: " + viaggi.size());

        long viaggiDisponibili = viaggi.stream().filter(Viaggio::isDisponibile).count();
        System.out.println("Viaggi disponibili: " + viaggiDisponibili);

        double prezzoMedio = viaggi.stream().mapToDouble(Viaggio::getPrezzo).average().orElse(0.0);
        System.out.println("Prezzo medio: €" + String.format("%.2f", prezzoMedio));

        double prezzoMin = viaggi.stream().mapToDouble(Viaggio::getPrezzo).min().orElse(0.0);
        double prezzoMax = viaggi.stream().mapToDouble(Viaggio::getPrezzo).max().orElse(0.0);
        System.out.println("Range prezzi: €" + String.format("%.2f", prezzoMin) + " - €" + String.format("%.2f", prezzoMax));

        double durataMedia = viaggi.stream().mapToInt(Viaggio::getDurataMinuti).average().orElse(0.0);
        System.out.println("Durata media: " + String.format("%.0f", durataMedia) + " minuti");
    }
}