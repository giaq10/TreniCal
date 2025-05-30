package it.trenical.common.model.viaggi;

import it.trenical.common.model.tratte.Tratta;
import it.trenical.common.model.treni.Treno;
import it.trenical.common.model.stazioni.Binario;
import it.trenical.common.model.viaggi.strategy.CalcoloViaggioStrategy;
import it.trenical.common.model.viaggi.strategy.StrategyFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Viaggio {
    private final String id;
    private final Treno treno;
    private final Tratta tratta;
    private final LocalDate dataViaggio;
    private final LocalTime orarioPartenzaProgrammato;
    private final LocalTime orarioArrivoProgrammato;
    private final Binario binarioPartenza;

    private final double prezzo;
    private final int durataMinuti;

    private LocalTime orarioPartenzaEffettivo;
    private LocalTime orarioArrivoEffettivo;
    private int postiDisponibili;
    private StatoViaggio stato;
    private int ritardoMinuti;
    private String motivoCancellazione;

    public Viaggio(Treno treno, Tratta tratta, LocalDate dataViaggio, LocalTime orarioPartenza) {
        if (treno == null) throw new IllegalArgumentException("Treno obbligatorio");
        if (tratta == null) throw new IllegalArgumentException("Tratta obbligatoria");
        if (dataViaggio == null) throw new IllegalArgumentException("Data viaggio obbligatoria");
        if (orarioPartenza == null) throw new IllegalArgumentException("Orario partenza obbligatorio");

        this.treno = treno;
        this.tratta = tratta;
        this.dataViaggio = dataViaggio;
        this.orarioPartenzaProgrammato = orarioPartenza;

        this.binarioPartenza = generaBinarioRandom();

        CalcoloViaggioStrategy strategy = StrategyFactory.getStrategy(treno.getTipoTreno());
        this.durataMinuti = strategy.calcolaDurata(tratta.getDistanzaKm(), treno.getTipoTreno());
        this.prezzo = strategy.calcolaPrezzo(tratta.getDistanzaKm(), treno.getTipoTreno());

        this.orarioArrivoProgrammato = orarioPartenza.plusMinutes(durataMinuti);

        this.id = generaIdUnico();

        this.orarioPartenzaEffettivo = orarioPartenza;
        this.orarioArrivoEffettivo = this.orarioArrivoProgrammato;
        this.postiDisponibili = treno.getPostiTotali();
        this.stato = StatoViaggio.PROGRAMMATO;
        this.ritardoMinuti = 0;
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
        // Ricalcola automaticamente l'orario di arrivo mantenendo la durata
        this.orarioArrivoEffettivo = nuovoOrario.plusMinutes(durataMinuti);
    }

    public void impostaRitardo(int minuti) {
        this.ritardoMinuti = minuti;
        if (minuti > 0) {
            this.stato = StatoViaggio.RITARDO;
            // Il ritardo si applica all'orario di arrivo
            this.orarioArrivoEffettivo = this.orarioArrivoProgrammato.plusMinutes(minuti);
        } else {
            // Ritardo azzerato
            this.orarioArrivoEffettivo = this.orarioArrivoProgrammato;
        }
    }

    public void cancellaViaggio(String motivo) {
        this.stato = StatoViaggio.CANCELLATO;
        this.motivoCancellazione = motivo;
    }

    public String getId() { return id; }
    public Treno getTreno() { return treno; }
    public Tratta getTratta() { return tratta; }
    public LocalDate getDataViaggio() { return dataViaggio; }
    public LocalTime getOrarioPartenza() { return orarioPartenzaProgrammato; }
    public LocalTime getOrarioArrivo() { return orarioArrivoProgrammato; }
    public LocalTime getOrarioPartenzaEffettivo() { return orarioPartenzaEffettivo; }
    public LocalTime getOrarioArrivoEffettivo() { return orarioArrivoEffettivo; }
    public Binario getBinarioPartenza() { return binarioPartenza; }

    public double getPrezzo() { return prezzo; }
    public int getDurataMinuti() { return durataMinuti; }

    public int getPostiDisponibili() { return postiDisponibili; }
    public int getPostiOccupati() { return treno.getPostiTotali() - postiDisponibili; }
    public StatoViaggio getStato() { return stato; }
    public int getRitardoMinuti() { return ritardoMinuti; }
    public String getMotivoCancellazione() { return motivoCancellazione; }

    public LocalDateTime getDataOraPartenza() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaProgrammato);
    }

    public LocalDateTime getDataOraArrivo() {
        return LocalDateTime.of(dataViaggio, orarioArrivoProgrammato);
    }

    public LocalDateTime getDataOraPartenzaEffettiva() {
        return LocalDateTime.of(dataViaggio, orarioPartenzaEffettivo);
    }

    public LocalDateTime getDataOraArrivoEffettiva() {
        return LocalDateTime.of(dataViaggio, orarioArrivoEffettivo);
    }

    public boolean isDisponibile() {
        return hasPostiDisponibili() && stato.isAttivo();
    }

    public boolean isCancellato() {
        return stato == StatoViaggio.CANCELLATO;
    }

    public boolean haRitardo() {
        return ritardoMinuti > 0;
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
                dataViaggio.getDayOfMonth(),
                dataViaggio.getMonthValue(),
                dataViaggio.getYear(),
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

        if (ritardoMinuti > 0)
            sb.append(String.format(" (+%d min ritardo)", ritardoMinuti));

        sb.append(String.format(" (€%.2f; %s; %d posti disponibili, %s, %s)",
                prezzo,
                getDurataFormattata(),
                postiDisponibili,
                getInfoBinari(),
                stato));

        return sb.toString();
    }
}