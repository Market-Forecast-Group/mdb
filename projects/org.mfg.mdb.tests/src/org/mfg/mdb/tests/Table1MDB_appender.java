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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table1MDB.Record;

/**
 * @author arian
 * 
 */
public class Table1MDB_appender {
	private static final int NUM_ROWS = 1131;
	private JUnitTestsMDBSession session;
	private Table1MDB mdb;
	private Table1MDB.Appender app;

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
	public void append_size() throws IOException, TimeoutException {
		long size = mdb.size();
		assertEquals(0, size);
		app.append();
		app.flush();

		session.close();

		File root = new File("db-test-" + getClass().getSimpleName());
		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table1MDB("test.mdb");
		app = mdb.appender();
		app.append();
		app.flush();

		long size2 = mdb.size();
		if (AllTests.TEST_MEMORY) {
			assertEquals(size + 1, size2);
		} else {
			assertEquals(size + 2, size2);
		}
	}

	@Test
	public void append() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.int_0 = i;
			list.add(r);
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (Record r : list) {
				app.int_0 = r.int_0;
				app.append();
				if (mdb.getMode().isBasic()) {
					app.flush();
				}
				c.seekLast();
				assertEquals(r.int_0, c.int_0);
				c.seek(mdb.size() - 1);
				assertEquals(r.int_0, c.int_0);
			}
		}

		int i = 0;
		try (Cursor c = mdb.cursor(); RandomCursor c2 = mdb.randomCursor()) {
			for (Record r : list) {
				c.next();
				assertEquals(r.int_0, c.int_0);
				assertEquals(r.int_0, c.toRecord().int_0);
				assertEquals(r.int_0, c.toRecord().clone().int_0);
				c2.seek(i);
				assertEquals(r.int_0, c.int_0);
				assertEquals(r.int_0, i);
				i++;
			}
		}
	}

	@Test
	public void append_record() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.int_0 = i;
			list.add(r);
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (Record r : list) {
				Record r2 = r.clone();
				app.append(r2);
				if (mdb.getMode().isBasic()) {
					app.flush();
				}
				// modify it after append, the data is not corrupted.
				r2.int_0 = Integer.MAX_VALUE;
				c.seekLast();
				assertEquals(r.int_0, c.int_0);
				c.seek(mdb.size() - 1);
				assertEquals(r.int_0, c.int_0);
			}
		}

		int i = 0;
		try (Cursor c = mdb.cursor(); RandomCursor c2 = mdb.randomCursor()) {
			for (Record r : list) {
				c.next();
				assertEquals(r.int_0, c.int_0);
				assertEquals(r.int_0, c.toRecord().int_0);
				assertEquals(r.int_0, c.toRecord().clone().int_0);
				c2.seek(i);
				assertEquals(r.int_0, c2.int_0);
				assertEquals(r.int_0, i);
				i++;
			}
		}
	}

	@Test
	public void append_record_ref_unsafe() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.int_0 = i;
			list.add(r);
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (Record r : list) {
				app.append_ref_unsafe(r);
				if (mdb.getMode().isBasic()) {
					app.flush();
				}
				c.seekLast();
				assertEquals(r.int_0, c.int_0);
				c.seek(mdb.size() - 1);
				assertEquals(r.int_0, c.int_0);
			}
		}

		int i = 0;
		try (Cursor c = mdb.cursor(); RandomCursor c2 = mdb.randomCursor()) {
			for (Record r : list) {
				c.next();
				assertEquals(r.int_0, c.int_0);
				assertEquals(r.int_0, c.toRecord().int_0);
				assertEquals(r.int_0, c.toRecord().clone().int_0);
				c2.seek(i);
				assertEquals(r.int_0, c.int_0);
				assertEquals(r.int_0, i);
				i++;
			}
		}
	}

	@Test
	public void append_record_ref_unsafe_corrupted() throws IOException {
		if (mdb.getMode().isBasic()) {
			return;
		}

		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.int_0 = i;
			list.add(r);
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (Record r : list) {
				Record r2 = r.clone();
				app.append_ref_unsafe(r2);
				// modify it after append and data get's corrupted
				r2.int_0 = Integer.MAX_VALUE;

				c.seekLast();
				assertEquals(Integer.MAX_VALUE, c.int_0);
				c.seek(mdb.size() - 1);
				assertEquals(Integer.MAX_VALUE, c.int_0);
			}
		}

		int i = 0;
		try (Cursor c = mdb.cursor(); RandomCursor c2 = mdb.randomCursor();) {
			for (Record r : list) {
				c.next();
				assertEquals(Integer.MAX_VALUE, c.int_0);
				assertEquals(Integer.MAX_VALUE, c.toRecord().int_0);
				assertEquals(Integer.MAX_VALUE, c.toRecord().clone().int_0);
				c2.seek(i);
				assertEquals(Integer.MAX_VALUE, c2.int_0);
				assertEquals(r.int_0, i);
				i++;
			}
		}
	}

}
