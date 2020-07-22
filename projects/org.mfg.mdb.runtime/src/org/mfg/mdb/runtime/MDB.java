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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 
 * Base class for all the MDB generated classes.
 * 
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public abstract class MDB<T extends IRecord> {

	protected final SessionMode _mode;
	/**
	 * If the session mode is in-memory. It is only for internal purpose.
	 */
	public final boolean _memory;

	/**
	 * If the connection mode is basic. Do not use this, it is only for internal
	 * purpose.
	 */
	public final boolean _basic;

	/**
	 * The buffer size (in records). Do not use this, it is only for internal
	 * purpose.
	 */
	public final int _bufferSize;

	/**
	 * Do not touch this, it is only for internal purpose.
	 */
	public final ReadLock _readLock;

	/**
	 * Do not touch this, it is only for internal purpose.
	 */
	public final WriteLock _writeLock;

	/**
	 * An array with 0 records.
	 */
	public final T[] NO_DATA;

	private final String _tableId;
	private final String _tableSignature;
	protected final ThreadLocal<IRandomCursor<T>> _localRandCursor;
	protected final ThreadLocal<ISeqCursor<T>> _localSeqCursor;
	protected boolean _connectedToFiles;
	protected final File _file;
	private final File _arrayFile;
	private final String[] _columnsName;
	private final Class<?>[] _columnsType;

	/**
	 * MDB constructor.
	 * 
	 * @param tableId
	 *            An UUID of the table.
	 * @param tableSignature
	 *            The table signature, is something change in the table
	 *            definition, the signature changes too.
	 * @param mode
	 *            The session mode.
	 * @param file
	 *            The MDB file.
	 * @param arrayFile
	 *            The MDB file with the array values, or <code>null</code> in
	 *            case there is not any column of type array.
	 * @param bufferSize
	 *            The size used for memory buffers and channel buffers.
	 * @param columnsName
	 *            the columns names.
	 * @param columnsType
	 *            The columns type.
	 * @throws IOException
	 *             If there is any problem accessing the file. If there is any
	 *             problem reading the MDB file.
	 */
	public MDB(String tableId, String tableSignature, SessionMode mode,
			File file, File arrayFile, int bufferSize, String[] columnsName,
			Class<?>[] columnsType) throws IOException {
		_tableId = tableId;
		_tableSignature = tableSignature;
		_memory = mode == SessionMode.MEMORY;
		_basic = mode == SessionMode.READ_ONLY
				|| mode == SessionMode.BASIC_READ_WRITE;
		_mode = mode;
		_file = file;
		_arrayFile = arrayFile;
		_bufferSize = bufferSize;
		_columnsName = columnsName;
		_columnsType = columnsType;
		NO_DATA = makeRecordArray(0);

		if (!_basic) {
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
			_readLock = lock.readLock();
			_writeLock = lock.writeLock();
		} else {
			_readLock = null;
			_writeLock = null;
		}

		if (!_memory) {
			/* create it if it doesn't exist already */
			_file.getParentFile().mkdirs();
			_file.createNewFile();
		}

		_connectedToFiles = true;

		_localRandCursor = new ThreadLocal<IRandomCursor<T>>() {
			@Override
			protected IRandomCursor<T> initialValue() {
				try {
					if (!_memory && !getFile().exists()) {
						throw new FileNotFoundException(
								"File "
										+ getFile()
										+ " not found. Probably it was deleted by a backup restore.");
					}
					IRandomCursor<T> cursor = randomCursor();
					getSession().defer(cursor);
					return cursor;
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		};
		_localSeqCursor = new ThreadLocal<ISeqCursor<T>>() {
			@Override
			protected ISeqCursor<T> initialValue() {
				try {
					if (!_memory && !getFile().exists()) {
						throw new FileNotFoundException(
								"File "
										+ getFile()
										+ " not found. Probably it was deleted by a backup restore.");
					}
					ISeqCursor<T> cursor = cursor();
					getSession().defer(cursor);
					return cursor;
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		};
	}

	/**
	 * Get the file associated to this MDB instance.
	 * 
	 * @return The file.
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * Get the file associated to this MDB instance. This file contains the data
	 * of the columns of type array. In case there is not any column with an
	 * array type this method returns <code>null</code>.
	 * 
	 * @return The array file or <code>null</code> if this MDB schema does not
	 *         contain arrays.
	 */
	public File getArrayFile() {
		return _arrayFile;
	}

	/**
	 * <p>
	 * Get the random access cursor attached to the current thread. You can use
	 * this if you want to reuse the same random cursor. Reuse a cursor is a
	 * good idea. Important, this is a deferred cursor, so it is closed
	 * automatically at the end of the session, please do not close this cursor
	 * yourself, if you want to release the resources associated to a thread,
	 * then use the method {@link #thread_forget()}.
	 * </p>
	 * <p>
	 * This method is specially helpful to implement the models of visualization
	 * components like charts, because usually the graphical toolkits like Swing
	 * and SWT use a unique thread to paint, so the models can reuse the same
	 * cursors to query the database.
	 * </p>
	 * 
	 * @see MDBSession#defer(ICursor)
	 * @see #select(ISeqCursor, long, long)
	 * @see #select_sparse(IRandomCursor, ISeqCursor, long, long, int)
	 * @see #record(IRandomCursor, long)
	 * @see #first(IRandomCursor)
	 * @see #last(IRandomCursor)
	 * 
	 * @return Random access cursor
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public IRandomCursor<T> thread_randomCursor() throws IOException {
		IRandomCursor<T> cursor;
		try {
			cursor = _localRandCursor.get();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
		return cursor;
	}

	/**
	 * <p>
	 * Get the sequential cursor attached to the current thread. You can use
	 * this method if you want to reuse the same cursor. Reuse a cursor is a
	 * good idea, just call the {@link ISeqCursor#reset(long, long)} method to
	 * restart the cursor in a new range. Important, this is a deferred cursor,
	 * so it is closed automatically at the end of the session, please do not
	 * close this cursor yourself, if you want to release the resources
	 * associated to a thread, then use the method {@link #thread_forget()}.
	 * Let's see an example of how to use this method:
	 * </p>
	 * 
	 * <pre>
	 * SomeMDB mdb = ...;
	 * long start = ...;
	 * long stop = ...;
	 * 
	 * Cursor cursor = mdb.cursor_thread();
	 * 
	 * cursor.reset(start, stop);
	 * 
	 * while (cursor.next()) {
	 * 	doSomething(cursor);
	 * }
	 * </pre>
	 * <p>
	 * This method is specially helpful to implement the models of visualization
	 * components like charts, because usually the the graphical toolkits like
	 * Swing and SWT use a unique thread to paint, so the models can reuse the
	 * same cursors to query the database.
	 * </p>
	 * 
	 * 
	 * @see MDBSession#defer(ICursor)
	 * @see ISeqCursor#reset(long, long)
	 * @see #thread_forget()
	 * 
	 * @return Sequential cursor
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public ISeqCursor<T> thread_cursor() throws IOException {
		ISeqCursor<T> cursor;
		try {
			cursor = _localSeqCursor.get();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
		return cursor;
	}

	/**
	 * <p>
	 * Close and remove the thread cursors. These cursors are the same of the
	 * methods {@link #thread_cursor()} and {@link #thread_randomCursor()}, see
	 * the javadoc of those methods for more detail.
	 * </p>
	 * <p>
	 * Reuse the same cursor is a good idea, but it is not a fixed rule, if you
	 * are running a session for a long period of time but you need to read the
	 * files from time to time, then it is a better idea to close the cursors
	 * and make them available to the garbage collector, specially if the
	 * application handles a lot of MDB files.
	 * </p>
	 * <p>
	 * Important, this does not mean that you cannot use the thread cursors
	 * again, you can, but the next time a new cursor is created.
	 * </p>
	 * <p>
	 * Take a look to this method: {@link MDBSession#disconnect(MDB)}. It has a
	 * very similar logic, if you are considering to use this method, probably
	 * "disconnect" the file from the session is even a better choice.
	 * </p>
	 * 
	 * @see MDBSession#disconnect(MDB)
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public void thread_forget() throws IOException {
		_writeLock.lock();
		try {
			try (IRandomCursor<T> c1 = _localRandCursor.get();
					ISeqCursor<T> c2 = _localSeqCursor.get()) {
				// just to close them
			}
			_localRandCursor.remove();
			_localSeqCursor.remove();
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Delete the associated files. Remember to close the cursors before to
	 * perform this operation. If this MDB instance was created with a session,
	 * do not call this method, else the {@link MDBSession#closeAndDelete()}
	 * method.
	 * 
	 * @return <code>true</code> if all files was deleted.
	 */
	public abstract boolean deleteFiles();

	/**
	 * The number of records.
	 * 
	 * @return The number of rows.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public abstract long size() throws IOException;

	/**
	 * Get the associated session.
	 * 
	 * @param <S>
	 *            Session type.
	 * @return the session
	 */
	public abstract <S extends MDBSession> S getSession();

	/**
	 * Get the session mode.
	 * 
	 * @return The session mode.
	 */
	public SessionMode getMode() {
		return _mode;
	}

	/**
	 * Request the appender. It is unique for each MDB instance.
	 * 
	 * @return The unique appender.
	 * 
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public abstract IAppender<T> appender() throws IOException;

	/**
	 * Close the appender.
	 * 
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 * 
	 */
	public abstract void closeAppender() throws IOException;

	/**
	 * Close the file handlers. Do not use this method, it is only for internal
	 * purpose.
	 */
	protected abstract void disconnectFile() throws IOException;

	/**
	 * Reopen the file handlers. Do not use this method, it is only for internal
	 * purpose.
	 * 
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	protected abstract void reconnectFile() throws IOException;

	/**
	 * Check if the appender is open.
	 * 
	 * @return <code>true</code> if it is open.
	 * 
	 */
	public abstract boolean isAppenderOpen();

	/**
	 * The names of the columns.
	 * 
	 * @return The names.
	 * 
	 */
	public final String[] getColumnsName() {
		return _columnsName;
	}

	/**
	 * The types of the columns.
	 * 
	 * @return The classes.
	 */
	public final Class<?>[] getColumnsType() {
		return _columnsType;
	}

	/**
	 * Request a sequential cursor.
	 * 
	 * @return The sequential cursor.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 * 
	 */
	public ISeqCursor<T> cursor() throws IOException {
		return cursor(0, size() - 1);
	}

	/**
	 * Request a sequential cursor that starts at the given <code>start</code>
	 * position.
	 * 
	 * @param start
	 *            Start position.
	 * @return The sequential cursor.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public ISeqCursor<T> cursor(long start) throws IOException {
		return cursor(start, size() - 1);
	}

	/**
	 * Create a cursor to iterate from position <code>start</code> to
	 * <code>stop</code>.
	 * 
	 * @param start
	 *            Start position.
	 * @param stop
	 *            Stop position.
	 * @return The sequential cursor.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public abstract ISeqCursor<T> cursor(long start, long stop)
			throws IOException;

	/**
	 * Request a random cursor.
	 * 
	 * @return The random cursor.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 * 
	 */
	public abstract IRandomCursor<T> randomCursor() throws IOException;

	/**
	 * Get the record at position <code>index</code>.
	 * 
	 * @param cursor
	 *            The cursor used to fetch the data.
	 * @param index
	 *            The record position.
	 * @return The record at <tt>index</tt>.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 * @throws IndexOutOfBoundsException
	 *             If the <tt>index</tt> is out of range.
	 * 
	 */
	public final T record(IRandomCursor<T> cursor, long index)
			throws IOException, IndexOutOfBoundsException {
		cursor.seek(index);
		return cursor.toRecord();
	}

	/**
	 * Short-cut for <code>record(cursor, 0)</code>.
	 * 
	 * @param cursor
	 *            The cursor to fetch the data.
	 * @return The first record or <tt>null</tt> if the file is empty..
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public final T first(IRandomCursor<T> cursor) throws IOException {
		return record(cursor, 0);
	}

	/**
	 * Short-cut for <code>record(cursor, size() - 1)</code>.
	 * 
	 * @param cursor
	 *            The cursor to fetch the data.
	 * @return The last record or <tt>null</tt> if the file is empty.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public final T last(IRandomCursor<T> cursor) throws IOException {
		return record(cursor, size() - 1);
	}

	/**
	 * Build an array with all the records.
	 * 
	 * @param cur
	 *            The cursor used to fetch the data.
	 * 
	 * @return The array of records.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public T[] selectAll(ISeqCursor<T> cur) throws IOException {
		return select(cur, 0, size());
	}

	/**
	 * <p>
	 * Build an array with all the records in the range from position
	 * <code>start</code> to <code>stop</code>.
	 * </p>
	 * <p>
	 * Note the retrieved data is stored in an array, it means if you are
	 * selecting a big range, you can get a memory overflow error. If you need
	 * only to read the data and do not store it in memory, then use a
	 * sequential cursor.
	 * </p>
	 * 
	 * @param cur
	 *            The cursor used to fetch the data.
	 * 
	 * @param start
	 *            Range start position.
	 * @param stop
	 *            Range stop position.
	 * @return The array of records.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public T[] select(ISeqCursor<T> cur, long start, long stop)
			throws IOException {
		long dbSize = size();

		if (start > stop || dbSize == 0)
			return NO_DATA;

		long last = dbSize - 1;
		long normalStart = start < 0 ? 0 : start;
		long normalStop = stop > last ? last : stop;

		T[] data = makeRecordArray(10);
		int arraySize = 0;

		cur.reset(normalStart, dbSize);

		long pos = normalStart;
		while (cur.next() && pos <= normalStop) {
			if (arraySize + 2 > data.length) {
				T[] newData = makeRecordArray((data.length * 3) / 2 + 1);
				System.arraycopy(data, 0, newData, 0, arraySize);
				data = newData;
			}
			data[arraySize] = cur.toRecord();
			arraySize++;
			pos++;
		}

		if (arraySize < data.length) {
			T[] newData = makeRecordArray(arraySize);
			System.arraycopy(data, 0, newData, 0, arraySize);
			data = newData;
		}
		return data;
	}

	protected abstract T[] makeRecordArray(int size);

	protected abstract T makeRecord();

	/**
	 * <p>
	 * This is a similar method to {@link #select(ISeqCursor, long, long)}, but
	 * instead of retrieve all the data from <code>start</code> to
	 * <code>stop</code> it retrieves only a fixed number of records (
	 * <code>maxLen</code>). For example, for a file with 10 records,
	 * <code>select_sparse(0, 9, 5)</code> returns 5 records at the positions:
	 * 0, 2, 4, 6, 8.
	 * </p>
	 * <p>
	 * You can use this method to visualize a big amount of data with certain
	 * level of detail. The bigger is <code>maxLen</code>, the bigger is the
	 * level of detail.
	 * </p>
	 * 
	 * @param randCursor
	 *            The random cursor used to fetch the data.
	 * @param seqCur
	 *            The sequential cursor used to fetch the data.
	 * 
	 * @param start
	 *            Range start position
	 * @param stop
	 *            Range stop position
	 * @param maxLen
	 *            Max number of records to include in the result.
	 * @return The array with the fixed number of records.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public T[] select_sparse(IRandomCursor<T> randCursor, ISeqCursor<T> seqCur,
			long start, long stop, int maxLen) throws IOException {
		if (maxLen <= 0) {
			throw new IllegalArgumentException("Invalid maxLen " + maxLen + ".");
		}

		long dbSize = size();

		if (start > stop || dbSize == 0)
			return NO_DATA;

		long last = dbSize - 1;
		long normalStart = start < 0 ? 0 : start;
		long normalStop = stop > last ? last : stop;

		if (normalStop - normalStart <= maxLen) {
			return select(seqCur, start, stop);
		}

		T[] data = makeRecordArray(10);
		int size = 0;

		long pos = normalStart;
		long step = (normalStop - normalStart) / maxLen;
		while (pos <= normalStop) {
			randCursor.seek(pos);
			if (size + 2 > data.length) {
				T[] newData = makeRecordArray((data.length * 3) / 2 + 1);
				System.arraycopy(data, 0, newData, 0, size);
				data = newData;
			}
			data[size] = randCursor.toRecord();
			size++;
			pos += step;
		}

		if (size < data.length) {
			T[] newData = makeRecordArray(size);
			System.arraycopy(data, 0, newData, 0, size);
			data = newData;
		}
		return data;
	}

	/**
	 * The number of opened cursors. You can use this to "debug" your programs.
	 * 
	 * @return The number of cursors.
	 */
	public abstract int getOpenCursorCount();

	/**
	 * Get the buffer. Warning: do not use this if you don't know what are you
	 * doing.
	 * 
	 * @return The memory buffer.
	 */
	public abstract T[] getRecentRecordsBuffer();

	/**
	 * The number of records in the buffer.
	 * 
	 * @return Count buffer records.
	 */
	public abstract int getRecentRecordsCount();

	/**
	 * Flush the associated appender.
	 * 
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public abstract void flushAppender() throws IOException;

	/**
	 * Truncate the file to the given <code>length</code>.
	 * 
	 * @param length
	 *            The desired new length (number of records).
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	// TODO: with the right meta-data, this can be implemented here.
	public abstract void truncate(long length) throws IOException;

	/**
	 * <p>
	 * Create a list using a random cursor and the appender. The data is read by
	 * demand.
	 * <p>
	 * We provide this method because the Java List API is well known by all
	 * Java developers, however we recommend to use the "native" API provided by
	 * MDB: cursors and appenders.
	 * </p>
	 * 
	 * @param cursor
	 *            The cursor to fetch the data.
	 * @return The list.
	 */
	public MDBList<T> list(IRandomCursor<T> cursor) {
		return new MDBList<>(this, cursor);
	}

	/**
	 * Run an array of validators.
	 * 
	 * @param max
	 *            the maximum number of errors.
	 * @param listener
	 *            The validation listener.
	 * @param validators
	 *            The array of validators. Default validators are generated
	 *            together with the MDB classes, see for constants like
	 *            <code>*_VALIDATOR</code>.
	 * @throws IOException
	 *             If there is any problem accessing the file.
	 */
	public void validate(final int max, final IValidatorListener<T> listener,
			@SuppressWarnings("unchecked") final IValidator<T>... validators)
			throws IOException {
		try (final ISeqCursor<T> cur = cursor()) {
			T prev = null;
			long row = 0;
			int err = 0;
			while (cur.next()) {
				final T curRecord = cur.toRecord();
				if (prev != null) {
					final ValidationArgs<T> args = new ValidationArgs<>(this,
							row, prev, curRecord);
					for (final IValidator<T> validator : validators) {
						final boolean valid = validator
								.validate(args, listener);
						if (!valid) {
							err++;
						}
						if (err > max) {
							break;
						}
					}
					if (err > max) {
						break;
					}
				}
				prev = curRecord;
				row++;
			}
		}
	}

	/**
	 * Get the unique table id.
	 * 
	 * @return The identifier.
	 * 
	 */
	public final String getTableId() {
		return _tableId;
	}

	/**
	 * Get the table signature. It changes if the table schema changes.
	 * 
	 * @return The signature.
	 */
	public final String getTableSignature() {
		return _tableSignature;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + _file.getName();
	}
}
