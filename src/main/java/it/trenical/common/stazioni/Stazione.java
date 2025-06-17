package it.trenical.common.stazioni;

import java.util.List;

public enum Stazione {
    REGGIO_CALABRIA("Reggio Calabria", 38.1144, 15.6500),
    COSENZA("Cosenza", 39.2985, 16.2538),
    NAPOLI("Napoli", 40.8518, 14.2681),
    BARI("Bari", 41.1184, 16.8690),
    ROMA("Roma", 41.9028, 12.4964),
    FIRENZE("Firenze", 43.7696, 11.2558),
    BOLOGNA("Bologna", 44.4949, 11.3426),
    VERONA("Verona", 45.4384, 10.9916),
    MILANO("Milano", 45.4642, 9.1900),
    TORINO("Torino", 45.0703, 7.6869),
    GENOVA("Genova", 44.4056, 8.9463),
    VENEZIA("Venezia", 45.4408, 12.3155);

    private final String nome;
    private final double latitudine;
    private final double longitudine;

    Stazione( String nome, double latitudine, double longitudine) {
        this.nome = nome;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    public double getLatitudine() {return latitudine;}

    public double getLongitudine() {return longitudine;}

    public double calcolaDistanzaVerso(Stazione altraStazione) {
        return calcolaDistanzaInLineaAria(this.latitudine, this.longitudine,
                                        altraStazione.latitudine, altraStazione.longitudine);
    }

    double calcolaDistanzaInLineaAria(double lat1, double lon1, double lat2, double lon2) {
        //algoritmo trovato su internet
        int EARTH_RADIUS = 6371;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);

        double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);

        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }

    public String getNome() {return nome;}

    @Override
    public String toString() {return nome;}

    public static Stazione fromNome(String nome) {
        for (Stazione stazione : values()) {
            if (stazione.nome.equalsIgnoreCase(nome)) {
                return stazione;
            }
        }
        throw new IllegalArgumentException("Stazione non trovata: " + nome);
    }

    public static List<Stazione> getTutteLeStazioni() {
        return List.of(REGGIO_CALABRIA, COSENZA, NAPOLI, BARI, ROMA, FIRENZE, BOLOGNA, VERONA, MILANO, TORINO, GENOVA, VENEZIA);
    }
}
