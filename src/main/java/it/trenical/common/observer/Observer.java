package it.trenical.common.observer;

public interface Observer {

    void update(Notifica notifica);

    String getObserverId();
}