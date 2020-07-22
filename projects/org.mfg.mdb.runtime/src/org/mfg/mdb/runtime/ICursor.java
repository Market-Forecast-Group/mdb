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

import java.io.Closeable;

/**
 * Common interface for all cursors.
 * 
 * @author arian
 * @param <T>
 *            The generated cursor type.
 * 
 */
public interface ICursor<T extends IRecord> extends Closeable {

	/**
	 * If the cursor is open.
	 * 
	 * @return <code>true</code> if this is open.
	 * 
	 */
	public boolean isOpen();

	/**
	 * The current cursor position.
	 * 
	 * @return The position.
	 */
	public long position();

	/**
	 * Create a record with the cursor's values.
	 * 
	 * @return The record.
	 * 
	 */
	public T toRecord();

	/**
	 * Get the associated MDB instance.
	 * 
	 * @return The MDB instance.
	 */
	public MDB<T> getMDB();
}
