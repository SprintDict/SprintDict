package net.bancer.sparkdict.domain.parsers;

/**
 * Data parser interface. Converts different StarDict data types into human
 * readable form.
 * 
 * @author Valerij Bancer
 *
 */
public interface IParser {

	/**
	 * Converts byte array of StarDict data block into string.
	 * 
	 * @param data	byte array of data to be converted.
	 * @return		data as string.
	 */
	public String parse(byte[] data);

}
