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
 * Common interface for all the appenders.
 * 
 * @author arian
 * @param <T>
 *            The generated record type.
 * 
 */
public interface IAppender<T extends IRecord> {
	/**
	 * Close the associated resources.
	 * 
	 * @throws IOException
	 *             If there is any problem closing the files.
	 */
	public void close() throws IOException;

	/**
	 * Get the associated MDB instance.
	 * 
	 * @return The MDB instance.
	 */
	public MDB<T> getMDB();

	/**
	 * Add a new record (formed by the appender values) to the file.
	 * 
	 * @throws IOException
	 *             If there is any problem writing the files.
	 */
	public void append() throws IOException;

	/**
	 * Add to the file the given <code>record</code>.
	 * 
	 * @param record
	 *            Record to add.
	 * @throws IOException
	 *             If there is any problem writing the files.
	 */
	public void append(T record) throws IOException;

	/**
	 * Add to the file the given record. Warning, the same record instance is
	 * inserted in the memory buffer, do not modify it after this operation.
	 * Just use this method if you know what are you doing.
	 * 
	 * @param record
	 *            The record to append.
	 * @throws IOException
	 *             If there is any problem writing the file.
	 */
	public void append_ref_unsafe(T record) throws IOException;

	/**
	 * Create a record with the appender's values.
	 * 
	 * @return The record.
	 */
	public T toRecord();
}
