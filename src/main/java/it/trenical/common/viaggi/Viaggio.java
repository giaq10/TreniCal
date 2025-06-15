package it.trenical.common.viaggi;

import it.trenical.common.observer.Notifica;
import it.trenical.common.observer.Observer;
import it.trenical.common.observer.TipoNotifica;
import it.trenical.common.promozioni.Promozione;
import it.trenical.common.observer.Subject;
import it.trenical.server.tratte.Tratta;
import it.trenical.server.treni.Treno;
import it.trenical.common.stazioni.Binario;
import it.trenical.common.viaggi.strategy.CalcoloViaggioStrategy;
import it.trenical.common.viaggi.strategy.StrategyFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Viaggio implements Subject {
    private static final List<LocalTime> ORARI_PARTENZA_DISPONIBILI = Arrays.asList(
            LocalTime.of(4, 0),   // 04:00
            LocalTime.of(6, 0),   // 06:00
            LocalTime.of(8, 0),   // 08:00
            LocalTime.of(10, 0),  // 10:00
            LocalTime.of(12, 0),  // 12:00
            LocalTime.of(14, 0),  // 14:00
            LocalTime.of(16, 0),  // 16:00
            LocalTime.of(18, 0),  // 18:00
            LocalTime.of(20, 0),  // 20:00
            LocalTime.of(22, 0),  // 22:00
            LocalTime.of(7, 30),  // 07:30
            LocalTime.of(15, 30)  // 15:30
    );

    private String id;
    private final Treno treno;
    private final Tratta tratta;
    private final LocalDate dataViaggio;
    private final LocalTime orarioPartenzaProgrammato;
    private final LocalTime orarioArrivoProgrammato;
    private final LocalDate dataArrivoProgrammata;
    private Binario binarioPartenza;

    private double prezzo;
    private final int durataMinuti;

    private LocalTime orarioPartenzaEffettivo;
    private LocalTime orarioArrivoEffettivo;
    private LocalDate dataArrivoEffettiva;
    private int postiDisponibili;
    private StatoViaggio stato;
    private String motivoCancellazione;

    private final List<Observer> observers;

    public Viaggio(Treno treno, Tratta tratta, LocalDate dataViaggio) {
        if (treno == null) throw new IllegalArgumentException("Treno obbligatorio");
        if (tratta == null) throw new IllegalArgumentException("Tratta obbligatoria");
        if (dataViaggio == null) throw new IllegalArgumentException("Data viaggio obbligatoria");
        if(dataViaggio.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Data viaggio non può essere precedente a quella attuale");

        this.treno = treno;
        this.tratta = tratta;
        this.dataViaggio = dataViaggio;

        this.orarioPartenzaProgrammato = generaOrarioRandom();
        this.binarioPartenza = generaBinarioRandom();

        CalcoloViaggioStrategy strategy = StrategyFactory.getStrategy(treno.getTipoTreno());
        this.durataMinuti = strategy.calcolaDurata(tratta.getDistanzaKm(), treno.getTipoTreno());
        this.prezzo = strategy.calcolaPrezzo(tratta.getDistanzaKm(), treno.getTipoTreno());

        LocalDateTime dataOraArrivo = calcolaDataOraArrivo(dataViaggio, orarioPartenzaProgrammato, durataMinuti);
        this.orarioArrivoProgrammato = dataOraArrivo.toLocalTime();
        this.dataArrivoProgrammata = dataOraArrivo.toLocalDate();

        this.id = generaIdUnico();

        this.orarioPartenzaEffettivo = orarioPartenzaProgrammato;
        this.orarioArrivoEffettivo = orarioArrivoProgrammato;
        this.dataArrivoEffettiva = dataArrivoProgrammata;
        this.postiDisponibili = treno.getPostiTotali();
        this.stato = StatoViaggio.PROGRAMMATO;

        this.observers = new ArrayList<>();
    }

    public Viaggio(String id, Treno treno, Tratta tratta, LocalDate dataViaggio,
                   LocalTime orarioPartenza, LocalTime orarioArrivo, LocalDate dataArrivo,
                   double prezzo, int durataMinuti, int postiDisponibili,
                   StatoViaggio stato, Binario binarioPartenza,
                    String motivoCancellazione) {

        if (treno == null) throw new IllegalArgumentException("Treno obbligatorio");
        if (tratta == null) throw new IllegalArgumentException("Tratta obbligatoria");
        if (dataViaggio == null) throw new IllegalArgumentException("Data viaggio obbligatoria");

        this.id = id;
        this.treno = treno;
        this.tratta = tratta;
        this.dataViaggio = dataViaggio;
        this.orarioPartenzaProgrammato = orarioPartenza;
        this.orarioArrivoProgrammato = orarioArrivo;
        this.dataArrivoProgrammata = dataArrivo;
        this.binarioPartenza = binarioPartenza;
        this.prezzo = prezzo;
        this.durataMinuti = durataMinuti;
        this.postiDisponibili = postiDisponibili;
        this.stato = stato;
        this.motivoCancellazione = motivoCancellazione;

        this.orarioPartenzaEffettivo = orarioPartenzaEffettivo != null ? orarioPartenzaEffettivo : orarioPartenza;
        this.orarioArrivoEffettivo = orarioArrivoEffettivo != null ? orarioArrivoEffettivo : orarioArrivo;
        this.dataArrivoEffettiva = dataArrivoEffettiva != null ? dataArrivoEffettiva : dataArrivo;

        this.observers = new ArrayList<>();
    }

    private LocalDateTime calcolaDataOraArrivo(LocalDate dataPartenza, LocalTime orarioPartenza, int durataMinuti) {
        LocalDateTime dataOraPartenza = LocalDateTime.of(dataPartenza, orarioPartenza);
        return dataOraPartenza.plusMinutes(durataMinuti);
    }

    private LocalTime generaOrarioRandom() {
        int index = (int) (Math.random() * ORARI_PARTENZA_DISPONIBILI.size());
        return ORARI_PARTENZA_DISPONIBILI.get(index);
    }

    private Binario generaBinarioRandom() {
        Binario[] binari = Binario.values();
        int index = (int) (Math.random() * binari.length);
        return binari[index];
    }

    private String generaIdUnico() {
        String hashInput = String.format("%s_%s_%s_%s_%s_%s",
                treno.getCodice(),
                tratta.getStazionePartenza().name(),
                tratta.getStazioneArrivo().name(),
                dataViaggio.toString(),
                orarioPartenzaProgrammato.toString().replace(":", ""),
                binarioPartenza.name());
        int hash = Math.abs(hashInput.hashCode());
        return String.format("V%08d", hash % 100000000);
    }

    public static List<LocalTime> getOrariDisponibili() {
        return ORARI_PARTENZA_DISPONIBILI;
    }

    public boolean prenotaPosto() {
        if (postiDisponibili > 0 && stato.isAttivo()) {
            postiDisponibili--;
            return true;
        }
        return false;
    }

    public boolean liberaPosto() {
        if (postiDisponibili < treno.getPostiTotali()) {
            postiDisponibili++;
            return true;
        }
        return false;
    }

    public boolean hasPostiDisponibili() {
        return postiDisponibili > 0 && stato.isAttivo();
    }

    public void aggiornaStato(StatoViaggio nuovoStato) {
        this.stato = nuovoStato;
    }

    public void cambioOrarioPartenza(LocalTime nuovoOrario) {
        this.orarioPartenzaEffettivo = nuovoOrario;
        LocalDateTime nuovaDataOraArrivo = calcolaDataOraArrivo(dataViaggio, nuovoOrario, durataMinuti);
        this.orarioArrivoEffettivo = nuovaDataOraArrivo.toLocalTime();
        this.dataArrivoEffettiva = nuovaDataOraArrivo.toLocalDate();

        notifyObservers(new Notifica(
                TipoNotifica.CAMBIO_ORARIO_PARTENZA,
                "Il tuo treno ha un nuovo orario di partenza: " + nuovoOrario
        ));
    }

    public void cambioBinario(int nuovoBinario) {
        Binario[] binari = Binario.values();
        if (nuovoBinario >= 0 && nuovoBinario < binari.length) {
            this.binarioPartenza = binari[nuovoBinario];

            notifyObservers(new Notifica(
                    TipoNotifica.CAMBIO_BINARIO,
                    "Il tuo treno cambierà binario: " + binarioPartenza.getDescrizione()
            ));
        }
    }

    public void impostaRitardo(int minuti) {
        if (minuti < 0) {
            throw new IllegalArgumentException("Il ritardo non può essere negativo");
        }

        if (minuti > 0) {
            this.orarioPartenzaEffettivo = orarioPartenzaProgrammato.plusMinutes(minuti);

            LocalDateTime dataOraArrivoConRitardo = LocalDateTime.of(dataArrivoProgrammata, orarioArrivoProgrammato).plusMinutes(minuti);
            this.orarioArrivoEffettivo = dataOraArrivoConRitardo.toLocalTime();
            this.dataArrivoEffettiva = dataOraArrivoConRitardo.toLocalDate();

            if (this.stato == StatoViaggio.PROGRAMMATO || this.stato == StatoViaggio.CONFERMATO) {
                this.stato = StatoViaggio.RITARDO;
            }

            notifyObservers(new Notifica(
                    TipoNotifica.RITARDO_TRENO,
                    "Il tuo treno ha accumulato un ritardo di " + minuti + " minuti"
            ));

        } else {
            this.orarioPartenzaEffettivo = orarioPartenzaProgrammato;
            this.orarioArrivoEffettivo = orarioArrivoProgrammato;
            this.dataArrivoEffettiva = dataArrivoProgrammata;

            if (this.stato == StatoViaggio.RITARDO) {
                this.stato = StatoViaggio.CONFERMATO;
            }
        }
    }

    public void cancellaViaggio(String motivo) {
        this.stato = StatoViaggio.CANCELLATO;
        this.motivoCancellazione = motivo;

        notifyObservers(new Notifica(
                TipoNotifica.CANCELLAZIONE_VIAGGIO,
                "Il tuo viaggio è stato cancellato. Motivo: " + motivo
        ));
    }

    public void applicaPromozione(Promozione promozione) {
        if (promozione == null) {
            throw new IllegalArgumentException("Promozione non può essere null");
        }
        this.prezzo = promozione.applicaSconto(this.prezzo);
    }

    public String getId() { return id; }
    public Treno getTreno() { return treno; }
    public Tratta getTratta() { return tratta; }
    public LocalDate getDataViaggio() { return dataViaggio; }
    public LocalTime getOrarioPartenza() { return orarioPartenzaProgrammato; }
    public LocalTime getOrarioArrivo() { return orarioArrivoProgrammato; }
    public LocalDate getDataArrivo() { return dataArrivoProgrammata; }
    public LocalTime getOrarioPartenzaEffettivo() { return orarioPartenzaEffettivo; }
    public LocalTime getOrarioArrivoEffettivo() { return orarioArrivoEffettivo; }
    public LocalDate getDataArrivoEffettiva() { return dataArrivoEffettiva; }
    public Binario getBinarioPartenza() { return binarioPartenza; }
    public double getPrezzo() { return prezzo; }
    public int getDurataMinuti() { return durataMinuti; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public int getPostiOccupati() { return treno.getPostiTotali() - postiDisponibili; }
    public StatoViaggio getStato() { return stato; }
    public String getMotivoCancellazione() { return motivoCancellazione; }


    public int getRitardoMinuti() {
        if (orarioPartenzaEffettivo.equals(orarioPartenzaProgrammato)) {
            return 0;
        }

        LocalDateTime programmato = LocalDateTime.of(dataViaggio, orarioPartenzaProgrammato);
        LocalDateTime effettivo = LocalDateTime.of(dataViaggio, orarioPartenzaEffettivo);

        long minutiDifferenza = java.time.Duration.between(programmato, effettivo).toMinutes();
        return (int) Math.max(0, minutiDifferenza);
    }

    public LocalDateTime getDataOraPartenza() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaProgrammato);
    }

    public LocalDateTime getDataOraArrivo() {
        return LocalDateTime.of(dataArrivoProgrammata, orarioArrivoProgrammato);
    }

    public LocalDateTime getDataOraPartenzaEffettiva() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaEffettivo);
    }

    public LocalDateTime getDataOraArrivoEffettiva() {
        return LocalDateTime.of(dataArrivoEffettiva, orarioArrivoEffettivo);
    }

    public boolean isDisponibile() {
        return hasPostiDisponibili() && stato.isAttivo();
    }

    public boolean isCancellato() {
        return stato == StatoViaggio.CANCELLATO;
    }

    public boolean haRitardo() {
        return getRitardoMinuti() > 0;
    }

    public String getDurataFormattata() {
        int ore = durataMinuti / 60;
        int minuti = durataMinuti % 60;
        return String.format("%dh %dm", ore, minuti);
    }

    public String getDataOraPartenzaFormattata() {
        return String.format("%02d/%02d/%d alle %02d:%02d",
                dataViaggio.getDayOfMonth(),
                dataViaggio.getMonthValue(),
                dataViaggio.getYear(),
                orarioPartenzaEffettivo.getHour(),
                orarioPartenzaEffettivo.getMinute());
    }

    public String getDataOraArrivoFormattata() {
        return String.format("%02d/%02d/%d alle %02d:%02d",
                dataArrivoEffettiva.getDayOfMonth(),
                dataArrivoEffettiva.getMonthValue(),
                dataArrivoEffettiva.getYear(),
                orarioArrivoEffettivo.getHour(),
                orarioArrivoEffettivo.getMinute());
    }

    public String getInfoBinari() {
        return "Binario " + binarioPartenza.getNumero();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Viaggio viaggio = (Viaggio) obj;
        return Objects.equals(id, viaggio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Viaggio %s: %s del %s alle %s",
                id,
                tratta.toString(),
                dataViaggio,
                orarioPartenzaEffettivo));

        int ritardo = getRitardoMinuti();
        if (ritardo > 0) {
            sb.append(String.format(" (+%d min ritardo)", ritardo));
        }

        sb.append(String.format(" (€%.2f; %s; %d posti disponibili, %s, %s)",
                prezzo,
                getDurataFormattata(),
                postiDisponibili,
                getInfoBinari(),
                stato));

        return sb.toString();
    }

    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer " + observer.getObserverId() +
                    " registrato per viaggio " + getId());
        }
    }

    @Override
    public void detach(Observer observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer " + observer.getObserverId() +
                    " rimosso da viaggio " + getId());
        }
    }

    @Override
    public void notifyObservers(Notifica notifica) {
        System.out.println("Viaggio " + getId() + " notifica " +
                observers.size() + " observers: " + notifica.getTipo());

        for (Observer observer : observers) {
            observer.update(notifica);
        }
    }

    @Override
    public int getObserverCount() {
        return observers.size();
    }

    @Override
    public String getSubjectId() {
        return getId();
    }
}