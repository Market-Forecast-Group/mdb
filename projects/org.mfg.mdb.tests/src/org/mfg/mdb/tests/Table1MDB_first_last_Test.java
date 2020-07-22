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
package org.mfg.mdb.tests;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Appender;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table1MDB.Record;

/**
 * @author arian
 * 
 */
public class Table1MDB_first_last_Test {
	private static final int NUM_ROWS = 201;// 10000;
	private JUnitTestsMDBSession session;
	private Appender app;
	private Table1MDB mdb;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table1MDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_last() throws IOException {
		try (RandomCursor c = mdb.randomCursor()) {
			try {
				c.seekLast();
				fail("Last record of an empty file throws an exception.");
			} catch (IndexOutOfBoundsException e) {
				// expected
			}

			for (int i = 0; i < NUM_ROWS; i++) {
				app.int_0 = i;
				app.append();

				if (mdb.getMode().isBasic()) {
					app.flush();
				}

				c.seekLast();
				int expected = i;
				int actual = c.int_0;
				assertEquals("Compare last value. Expected=" + expected
						+ ", actual=" + actual, expected, actual);
			}
			app.flush();
		}
	}

	@Test
	public void test_first() throws IOException {
		try (RandomCursor c = mdb.randomCursor()) {
			try {
				c.seekFirst();
				fail("First record of an empty file throws exception");
			} catch (IndexOutOfBoundsException e) {
				// expected
			}

			int expected = 1;
			for (int i = expected; i < NUM_ROWS; i++) {
				if (i == 101) {
					out.println(i);
				}
				app.int_0 = i;
				app.append();

				if (mdb.getMode().isBasic()) {
					app.flush();
				}

				c.seekFirst();
				Record first = c.toRecord();
				int actual = first.int_0;
				assertEquals("Compare first value. Expected=" + expected
						+ ", actual=" + actual + ".", expected, actual);
			}
			app.flush();
		}
	}
}
