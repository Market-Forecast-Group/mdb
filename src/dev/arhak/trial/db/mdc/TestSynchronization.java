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
package dev.arhak.trial.db.mdc;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;

import dev.arhak.trial.db.mdc.mdb.TestMDBSession;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB.Appender;

/**
 * This test is about to get the index out of bounds error.
 * 
 * <pre>
 * Exception in thread "Thread-1" java.lang.ArrayIndexOutOfBoundsException: -1
 * 	at dev.arhak.trial.db.mdc.mdb.
 * 		TestSparseCursorMDB.record(TestSparseCursorMDB.java:570)
 * 	at dev.arhak.trial.db.mdc.
 * 		TestSynchronization$2.run(TestSynchronization.java:84)
 * 	at java.lang.Thread.run(Unknown Source)
 * </pre>
 * 
 * @author arian
 * 
 */
public final class TestSynchronization {
	/**
	 * Rows count.
	 */
	private static final int COUNT = 10000;

	/**
	 * 
	 */
	private TestSynchronization() {

	}

	/**
	 * @param args
	 *            Programs args
	 * @throws IOException
	 *             Sent by MDB
	 * @throws InterruptedException
	 *             Threading
	 */
	public static void main(final String[] args) throws IOException,
			InterruptedException {
		final File root = new File("./test_list_db");
		root.mkdirs();

		final TestMDBSession session = new TestMDBSession("", root);

		final File file = new File(session.getRoot(), "test_synchronization");
		file.delete();
		file.createNewFile();

		final TestSparseCursorMDB mdb = session.connectTo_TestSparseCursorMDB(
				file, 1);
		final Appender app = mdb.appender();
		final Object mutex = new Object();
		final Thread writeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < COUNT; i++) {
						synchronized (mutex) {
							mutex.notifyAll();
						}
						app.value = i;
						app.append();
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
		writeThread.start();

		final Thread readThread = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mutex) {
					try {
						mutex.wait();
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				for (int i = 0; i < COUNT; i++) {
					try {
						mdb.record(mdb.size() - 1);
						out.println(i);
					} catch (final Exception e) {
						out.println("record(): i = " + i);
						e.printStackTrace();
						break;
					}
				}
			}
		});
		readThread.start();
		writeThread.join();
		readThread.join();

		session.closeAndDelete();

		if (session.getRoot().exists()) {
			throw new IOException("Fail removing " + session.getRoot());
		}

	}
}
