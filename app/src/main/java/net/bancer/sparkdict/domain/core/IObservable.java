package net.bancer.sparkdict.domain.core;

/**
 * Observable object interface.
 *
 * @author Valerij Bancer
 *
 */
public interface IObservable {

    /**
     * Registers an observer.
     *
     * @param o observer to be registered.
     */
    void registerObserver(IObserver o);

    /**
     * Removes registered observer.
     *
     * @param o observer to be removed.
     */
    void removeObserver(IObserver o);

    /**
     * Nofifies all observers about some event.
     */
    void notifyObservers();

}
