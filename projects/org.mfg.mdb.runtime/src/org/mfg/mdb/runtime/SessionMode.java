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

/**
 * The different session modes.
 * 
 * @author arian
 * 
 */
public enum SessionMode {

	/**
	 * Only read operations are permitted. Cursors performs in the same way of
	 * the {@link SessionMode#BASIC_READ_WRITE} mode.
	 */
	READ_ONLY(true),

	/**
	 * Default mode, allow read and write operations. If the database does not
	 * exist, a new one is created. This is the default mode.
	 */
	READ_WRITE(false),

	/**
	 * <p>
	 * The purpose of this mode is to improve performance with simpler readers
	 * and writers. Here, the last records are not shared between writers and
	 * readers unless they are written to disk (via the <code>close()</code> and
	 * <code>flush()</code> appender's methods), therefore, there is not an
	 * extra effort to synchronize threads and keep a memory collection.
	 * <p>
	 * The advantage of this method is that cursors and appenders do not spent
	 * time and memory synchronizing threads and keeping a memory collection
	 * with the last records. Common scenarios are write-only or read-only
	 * databases. Actually, case of the read-only database (
	 * {@link SessionMode#READ_ONLY}), the readers will perform in the same way
	 * of this mode.
	 * </p>
	 */
	BASIC_READ_WRITE(true),

	/**
	 * The whole data is saved in RAM memory, so, when the Java process of your
	 * program stops, the data is lost.
	 */
	MEMORY(false);

	private boolean _basic;

	private SessionMode(boolean basic) {
		_basic = basic;
	}

	/**
	 * If the mode is basic ({@link #READ_ONLY} or {@link #BASIC_READ_WRITE}).
	 * 
	 * @return <code>true</code> if this is basic.
	 */
	public boolean isBasic() {
		return _basic;
	}
}
