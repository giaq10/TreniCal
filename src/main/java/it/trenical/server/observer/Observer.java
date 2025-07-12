package it.trenical.server.observer;

public interface Observer {

    void update(Notifica notifica);

    String getObserverId();
}