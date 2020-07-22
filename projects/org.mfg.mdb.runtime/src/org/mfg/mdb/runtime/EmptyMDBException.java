package org.mfg.mdb.runtime;

import java.io.IOException;

/**
 * Thrown when attempt to read data from an empty MDB file.
 * 
 * @author arian
 * 
 */
public class EmptyMDBException extends IOException {

	private static final long serialVersionUID = 1L;

	/**
	 * The constructor.
	 * 
	 * @param message
	 *            Error message.
	 */
	public EmptyMDBException(String message) {
		super(message);
	}

}
