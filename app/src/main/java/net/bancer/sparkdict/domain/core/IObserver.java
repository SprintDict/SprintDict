package net.bancer.sparkdict.domain.core;

/**
 * Observer object interface.
 * 
 * @author Valerij Bancer
 *
 */
public interface IObserver {
	
	/*public static final int ADDED = 1;
	public static final int REMOVED = 2;
	public static final int CLEARED = 3;*/

	/**
	 * Updates observer
	 * 
	 * @param field changed field.
	 * @param value new value of the changed field.
	 */
	public void update(Object field, int value);

}
