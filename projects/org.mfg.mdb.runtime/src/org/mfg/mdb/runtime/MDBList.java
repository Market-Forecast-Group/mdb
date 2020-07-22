package org.mfg.mdb.runtime;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;

/**
 * Base class for all MDB based lists. See implementors for more information.
 * 
 * @author arian
 * @param <T>
 *            The record type.
 * 
 */
public class MDBList<T extends IRecord> extends AbstractList<T> implements
		RandomAccess {
	private MDB<T> _mdb;
	private IRandomCursor<T> _cursor;

	/**
	 * Create the list with the given cursor.
	 * 
	 * @param mdb
	 *            The MDB instance.
	 * 
	 * @param cursor
	 *            Cursor to fetch the data.
	 */
	public MDBList(MDB<T> mdb, IRandomCursor<T> cursor) {
		super();
		_mdb = mdb;
		_cursor = cursor;
	}

	/**
	 * Get the MDB instance associated to this list.
	 * 
	 * @return The MDB instance.
	 */
	public MDB<T> getMDB() {
		return _mdb;
	}

	/**
	 * The underlaying cursor.
	 * 
	 * @return The cursor.
	 */
	public IRandomCursor<T> getCursor() {
		return _cursor;
	}

	@Override
	public int indexOf(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(int index) {
		try {
			_cursor.seek(index);
			return _cursor.toRecord();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public int size() {
		try {
			return (int) _mdb.size();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean add(T e) {
		try {
			_mdb.appender().append(e);
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T e : c) {
			add(e);
		}
		return true;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
}