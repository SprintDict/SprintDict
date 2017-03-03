package net.bancer.sparkdict.domain.parsers;

/**
 * ParsingStrategyFactory is a factory that creates parsers for different
 * StarDict data types.
 * 
 * @author Valerij Bancer
 *
 */
public class ParsingStrategyFactory {

	private static ParsingStrategyFactory instance = null;
	
	private IParser xParser;
	private IParser nParser;
	private IParser mParser;

	/**
	 * Returns instance of ParsingStrategyFactory. Ensures only one instance is
	 * allowed.
	 * 
	 * @return instance of ParsingStrategyFactory.
	 */
	public static ParsingStrategyFactory getInstance() {
		if(instance == null) {
			instance = new ParsingStrategyFactory();
		}
		return instance;
	}

	/**
	 * Constructs and returns parsers.
	 * 
	 * @param type 	data type for what the parser is needed.
	 * @return		parser that can convert the byte data into string.
	 */
	public IParser getParser(char type) {
		switch (DataType.valueOf("" + type)) {
			case m:
				return getMParser();
			//TODO: implement LParser, GParser
			//case l:
			//	return getLParser();
			//case g:
			//	return getGParser();
			case t:
				return getMParser();
			case x:
				return getXParser();
			case y:
				return getMParser();
			//TODO: implement KParser, WParser, HParser, RParser
			//case k:
			//	return getKParser();
			//case w:
			//	return getWParser();
			//case h:
			//	return getHParser();
			//case r:
			//	return getRParser();
			//case W:
			//	return getWUppercaseParser();
			//case P:
			//	return getPUppercaseParser();
			case n:
				return getNParser();
			default:
				return getMParser();
		}
	}

	private IParser getXParser() {
		if(xParser == null) {
			xParser = new XParser();
		}
		return xParser;
	}

	private IParser getNParser() {
		if(nParser == null) {
			nParser = new NParser();
		}
		return nParser;
	}

	private IParser getMParser() {
		if(mParser == null) {
			mParser = new MParser();
		}
		return mParser;
	}
}
