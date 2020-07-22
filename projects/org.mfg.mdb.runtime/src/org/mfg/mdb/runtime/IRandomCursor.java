/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdb.runtime;

import java.io.IOException;

/**
 * Common interface for all random cursors.
 * 
 * @author arian
 * @param <T>
 *            The generated cursor class.
 * 
 */
public interface IRandomCursor<T extends IRecord> extends ICursor<T> {

	/**
	 * Move the cursor to the given <code>position</code> and fetch the data.
	 * 
	 * @param position
	 *            The position to move.
	 * @throws IOException
	 *             If there is any problem reading the files.
	 */
	public void seek(long position) throws IOException;

	/**
	 * Move the cursor to the last position and fetch the data.
	 * 
	 * @throws IOException
	 *             If there is any problem reading the files.
	 */
	public void seekLast() throws IOException;

	/**
	 * Move the cursor to the first position and fetch the data.
	 * 
	 * @throws IOException
	 *             If there is any problem reading the files.
	 */
	public void seekFirst() throws IOException;
}
