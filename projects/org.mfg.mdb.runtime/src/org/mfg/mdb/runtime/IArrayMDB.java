package org.mfg.mdb.runtime;

import java.io.File;

/**
 * Interface for an MDB with some column of type array.
 * 
 * @author Arian
 * 
 */
public interface IArrayMDB {

	/**
	 * The file where the array values are saved.
	 * 
	 * @return The file where the array data is stored.
	 */
	public File getArrayFile();
}
