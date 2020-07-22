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
 * 
 * Common interface for all records.
 * 
 * @author arian
 * 
 */
public interface IRecord extends Cloneable {

	/**
	 * Create an array with the record values.
	 * 
	 * @return The array of values.
	 * 
	 */
	public Object[] toArray();

	/**
	 * The names of the columns.
	 * 
	 * @return The names.
	 * 
	 */
	public String[] getColumnsName();

	/**
	 * The Java type of the columns.
	 * 
	 * @return The column types.
	 */
	public Class<?>[] getColumnsType();

	/**
	 * Get the value of a column. The column is at the position
	 * <code>colIndex</code> of the table definition.
	 * 
	 * @param colIndex
	 *            The column index in the table definition.
	 * @return The value at <code>index</code>.
	 */
	public Object get(int colIndex);

	/**
	 * Create a deep clone of the record.
	 * 
	 * @return The clone.
	 */
	public IRecord clone();
}
