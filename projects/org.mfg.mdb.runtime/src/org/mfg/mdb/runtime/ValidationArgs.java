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
 * Arguments of a validation method.
 * 
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public class ValidationArgs<T extends IRecord> {
	private MDB<T> _mdb;
	private T _prev;
	private T _current;
	private long _row;

	/**
	 * The constructor.
	 * 
	 * @param mdb
	 *            The MDB instance in question.
	 * @param row
	 *            The index of the current record.
	 * @param prev
	 *            The previous record.
	 * @param current
	 *            The current record.
	 */
	public ValidationArgs(MDB<T> mdb, long row, T prev, T current) {
		super();
		_mdb = mdb;
		_prev = prev;
		_current = current;
		_row = row;
	}

	/**
	 * MDB instance is validating.
	 * 
	 * @return The MDB instance.
	 */
	public MDB<T> getMdb() {
		return _mdb;
	}

	/**
	 * Set the MDB instance is validating.
	 * 
	 * @param mdb
	 *            The MDB instance.
	 */
	public void setMdb(MDB<T> mdb) {
		this._mdb = mdb;
	}

	/**
	 * Get the previous record.
	 * 
	 * @return The generated record class.
	 * 
	 */
	public T getPrev() {
		return _prev;
	}

	/**
	 * Set the previous record.
	 * 
	 * @param prev
	 *            The previous record.
	 */
	public void setPrev(T prev) {
		this._prev = prev;
	}

	/**
	 * Get the current record.
	 * 
	 * @return The generated record class.
	 */
	public T getCurrent() {
		return _current;
	}

	/**
	 * Set the current record.
	 * 
	 * @param current
	 *            The current record.
	 */
	public void setCurrent(T current) {
		this._current = current;
	}

	/**
	 * Get the current record position.
	 * 
	 * @return The index of the current record (or row).
	 */
	public long getRow() {
		return _row;
	}

	/**
	 * Set the current record position.
	 * 
	 * @param row
	 *            The index of the current record (or row).
	 */
	public void setRow(long row) {
		this._row = row;
	}
}
