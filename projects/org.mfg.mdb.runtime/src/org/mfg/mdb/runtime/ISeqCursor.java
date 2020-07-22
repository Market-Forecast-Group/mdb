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
 * Common interface for all sequential cursors.
 * 
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public interface ISeqCursor<T extends IRecord> extends ICursor<T> {

	/**
	 * Move the cursor to the next position.
	 * 
	 * @return <code>true</code> if there are more records.
	 * @throws IOException
	 *             If there is any problem reading the files.
	 */
	public boolean next() throws IOException;

	/**
	 * <p>
	 * Reset the cursor to retrieve the data from the <code>start</code> to the
	 * <code>stop</code> position.
	 * </p>
	 * <p>
	 * Use this method if you want to reuse the same cursor resources, actually
	 * we recommend to do that when possible. Internally MDB uses this method to
	 * reuse the cursors or the "select" methods.
	 * </p>
	 * <p>
	 * Very important, you cannot reset a closed cursor. Close a cursor when you
	 * will not use it anymore.
	 * </p>
	 * 
	 * @param start
	 *            The cursor statrs here.
	 * @param stop
	 *            The cursor stops here.
	 * @throws IOException
	 *             If there is any problem accessing the files.
	 */
	public void reset(long start, long stop) throws IOException;
}
