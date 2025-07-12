package it.trenical.server.observer;

public interface Subject {

    void attach(Observer observer);

    void detach(Observer observer);

    void notifyObservers(Notifica notifica);

    int getObserverCount();

    String getSubjectId();
}