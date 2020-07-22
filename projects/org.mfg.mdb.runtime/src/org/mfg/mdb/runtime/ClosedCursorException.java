package org.mfg.mdb.runtime;

import java.io.IOException;

/**
 * Checked exception thrown when an attempt to move a closed cursor.
 * 
 * @author arian
 * 
 */
public class ClosedCursorException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor
	 * 
	 * @param cursor
	 *            The cursor.
	 */
	public ClosedCursorException(ICursor<?> cursor) {
		super("Attempt to move a closed cursor of " + cursor.getMDB().getFile());
	}
}
