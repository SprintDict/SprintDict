package net.bancer.sparkdict.domain.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * BookInfo is an abstraction of <dictionary name>.ifo file.
 * 
 * @author  Valerij Bancer
 */
public class BookInfo {

	private String version;

	/**
	 * required
	 */
	private String bookName;

	/**
	 * wordCount is the count of word entries in .idx file
	 * 
	 * required
	 */
	private int wordCount;

	/**
	 * required if ".syn" file exists
	 */
	private int synWordCount;

	/**
	 * idxFileSize is the size(in bytes) of the .idx file,
	 * even the .idx is compressed to a .idx.gz file,
	 * this entry must record the original .idx file's size,
	 * and it must be right too.
	 * 
	 * required
	 */
	private int idxFileSize;

	/**
	 * idxOffsetBits can be 64 or 32.
	 * If "idxOffsetBits=64", the offset field of the .idx file will be 64 bits.
	 * 
	 * New in 3.0.0
	 */
	private int idxOffsetBits = 32;

	private String author;

	private String email;

	private String website;

	/**
	 * `&lt;br&gt;` can be used for new line.
	 */
	private String description;

	private String date;

	/**
	 * If the sametypesequence option is set, it tells StarDict that each
	 * word's data in the .dict file will have the same sequence of datatypes.
	 * In this case, we expect a .dict file that's been optimized in two
	 * ways: the type identifiers should be omitted, and the size marker for
	 * the last data entry of each word should be omitted.
	 * 
	 * x - http://xdxf.revdanica.com/drafts/visual/latest/XDXF-draft-028.txt, http://kenai.com/projects/xdxf-parser
	 * 
	 * very important
	 */
	private String sameTypeSequence;

	private String dictType;

	private String filePath;

	/**
	 * Path to the folder containing current dictionary files.
	 */
	private String dirPath;

	/**
	 * Constructor.
	 * 
	 * @param path Full path to .ifo file including extension itself.
	 */
	public BookInfo(String path) {
		this(new File(path));
	}

	/**
	 * Constructor.
	 * 
	 * @param infoFile	java.io.File object of <dictionary name>.ifo file.
	 */
	public BookInfo(File infoFile) {
		if(infoFile == null || infoFile.equals("")){
			throw new IllegalArgumentException("infoFile must not be null or empty");
		}
		filePath = infoFile.toString();
		dirPath = infoFile.getParent();
		try {
			Scanner input = new Scanner(infoFile, "UTF-8");
			input.useDelimiter("\n");
			while(input.hasNextLine()) {
				String line = input.nextLine();
				if(line.indexOf("version=") >= 0){
					version = line.substring(8);
				} else if(line.indexOf("bookname=") >= 0){
					bookName = line.substring(9);
				} else if(line.indexOf("synwordcount=") >= 0){
					synWordCount = Integer.parseInt(line.substring(13));
				} else if(line.indexOf("wordcount=") >= 0){
					wordCount = Integer.parseInt(line.substring(10));
				} else if(line.indexOf("idxfilesize=") >= 0){
					idxFileSize = Integer.parseInt(line.substring(12));
				} else if(line.indexOf("idxoffsetbits=") >= 0){
					idxOffsetBits = Integer.parseInt(line.substring(14));
				} else if(line.indexOf("author=") >= 0){
					author = line.substring(7);
				} else if(line.indexOf("email=") >= 0){
					email = line.substring(6);
				} else if(line.indexOf("website=") >= 0){
					website = line.substring(8);
				} else if(line.indexOf("description=") >= 0){
					description = line.substring(12);
				} else if(line.indexOf("date=") >= 0){
					date = line.substring(5);
				} else if(line.indexOf("sametypesequence=") >= 0){
					sameTypeSequence = line.substring(17);
					//System.out.println("Same Type Sequence: " + line.substring(17));
				} else if(line.indexOf("dicttype=") >= 0){
					dictType = line.substring(9);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * String representation of the BookInfo object (for debug  only).
	 */
	@Override
	public String toString() {
		String result = "";
		result += "\nVersion: " + version + "\n";
		result += "Dictionary name: " + bookName + "\n";
		result += "Words: " + wordCount + "\n";
		result += "Synonyms: " + synWordCount + "\n";
		result += "Index file size: " + idxFileSize + "\n";
		result += "Index offset bits: " + idxOffsetBits + "\n";
		result += "Author: " + author + "\n";
		result += "Email: " + email + "\n";
		result += "Website: " + website + "\n";
		result += "Description: " + description + "\n";
		result += "Date: " + date + "\n";
		result += "Same type sequence: " + sameTypeSequence + "\n";
		result += "Dictionary type: " + dictType + "\n";
		result += "Path: " + filePath + "\n";
		return result;
	}

	/**
	 * Version getter.
	 * 
	 * @return dictionary version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Book name getter.
	 * 
	 * @return the title of the dictionary.
	 */
	public String getBookName() {
		return bookName;
	}

	/**
	 * Words quantity getter.
	 * 
	 * @return quantity of dictionary entries.
	 */
	public int getWordCount() {
		return wordCount;
	}

	/**
	 * Index file size getter.
	 * 
	 * @return the size of index file.
	 */
	public int getIdxFileSize() {
		return idxFileSize;
	}

	/**
	 * Index offset bits getter.
	 * 
	 * @return	index offset bits size.
	 */
	public int getIdxOffsetBits() {
		return idxOffsetBits;
	}

	/**
	 * Description getter.
	 * 
	 * @return	description of the dictionary.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Date getter.
	 * 
	 * @return	the date of the dictionary file.
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Same data type sequence getter.
	 * 
	 * @return same data type sequence.
	 */
	public String getSameTypeSequence() {
		return sameTypeSequence;
	}

	/**
	 * File base name getter.
	 * 
	 * @return path to the <dictionary name>.ifo file excluding ".ifo" part.
	 */
	public String getFileBaseName() {
		int end = filePath.length() - 4;
		return filePath.substring(0, end);
	}

	/**
	 * Compressed dictionary file path getter.
	 * 
	 * @return full path to the dictionary data file including ".dict.dz" at the end.
	 */
	public String getPathToDictFile() {
		return getFileBaseName() + ".dict.dz";
	}

	public String getDirPath() {
		return dirPath;
	}

}
