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
	public void registerObserver(IObserver o);

	/**
	 * Removes registered observer.
	 * 
	 * @param o observer to be removed.
	 */
	public void removeObserver(IObserver o);

	/**
	 * Nofifies all observers about some event.
	 */
	public void notifyObservers();

}
