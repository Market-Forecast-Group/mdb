package org.mfg.mdb.runtime;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * This class can be used to synchronize the threads that access (to read or
 * write) certain resource with the thread that closes that resource. When the
 * resource is requested to be closed, it is marked as "closing" and therefore
 * the next operations are avoided.
 * </p>
 * <p>
 * The best example is an application that shows many charts of the same
 * database. When the application is closed, it has to destroy the database in
 * the closing-thread, but first it has to stop the chart painting threads.
 * </p>
 * <p>
 * So the user can do the following:
 * </p>
 * 
 * <pre>
 * void paintLoop() {
 * 	while(dbSynchonizer.operation(doPaint));
 * }
 * 
 * ...
 * 
 * void closeDatabase() {
 * 	dbSynchronizer.close(closeOp);
 * }
 * 
 * </pre>
 * 
 * @author Arian
 * 
 */
public class DBSynchronizer {
	// (9:23:56 AM) sergiopeffe:
	//
	// ...you can do a semaphore
	// instead of an atomic boolean
	// you have an atomic integer
	// it starts at zero
	// every reader, before reading increment it
	// after read operation, it decrements it
	// so you have this integer that usually it is
	// zero
	// than can grow to 2,3 ... n readers parallel
	// and when it is zero it means that there is no
	// reader using the db
	// the writer before closing the db
	// waits until the semaphore it is zero
	// and sets it to a negative value, for example
	// -10
	// this is the signal that the reader cannot enter
	// any more in the db
	// because it is shutting down
	// .

	AtomicInteger _mutex;
	private AtomicBoolean _closing;

	/**
	 * The constructor.
	 */
	public DBSynchronizer() {
		_mutex = new AtomicInteger(0);
		_closing = new AtomicBoolean(false);
	}

	/**
	 * Execute <code>op</code> if a close operation was not executed before.
	 * 
	 * @param op
	 *            Operation to perform
	 * @return <code>true</code> if it was executed.
	 */
	public boolean operation(Runnable op) {
		boolean executed = false;
		if (!_closing.get()) {
			executed = true;
			_mutex.incrementAndGet();
			try {
				op.run();
			} finally {
				_mutex.decrementAndGet();
			}
		}
		return executed;
	}

	/**
	 * <p>
	 * Execute the <code>closeOp</code> in a new thread, but first wait for
	 * every executed operation to finish.
	 * </p>
	 * <p>
	 * Usually the "run" code of <code>closeOp</code> is about to close/delete
	 * some MDB databases and related resources.
	 * </p>
	 * 
	 * @param closeOp
	 *            The operation to execute.
	 */
	public void closeAsync(final Runnable closeOp) {
		close1(closeOp);
	}

	/**
	 * Like {@link #closeAsync(Runnable)} but it waits to the thread to finish.
	 * 
	 * @param closeOp
	 *            The operation to execute.
	 */
	public void close(final Runnable closeOp) {
		Thread th = close1(closeOp);
		try {
			th.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Internal method used by the public API.
	 * 
	 */
	private Thread close1(final Runnable closeOp) {
		if (_closing.get()) {
			throw new RuntimeException("The db was already closed");
		}

		_closing.set(true);
		Thread th = new Thread(this + ": closing " + closeOp) {
			@Override
			public void run() {
				while (_mutex.get() > 0) {
					try {
						sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				closeOp.run();
				_mutex.set(-10);
			}
		};
		th.start();
		return th;
	}
}
