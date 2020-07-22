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

import static org.junit.Assert.assertArrayEquals;
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
import org.mfg.mdb.tests.mdb.Table1MDB.Appender;
import org.mfg.mdb.tests.mdb.Table1MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table1MDB.Record;

/**
 * @author arian
 * 
 */
public class Table1MDB_writeAll_readAll {
	private static final int NUM_ROWS = 11731;
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
	public void test_writeAll_readAll() throws IOException {
		List<Table1MDB.Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Table1MDB.Record();
			r.array_int_1 = new int[] { i };
			r.double_0 = i;
			r.int_0 = i;
			list.add(r);
		}

		for (Record r : list) {
			app.update(r);
			app.append();
		}
		app.flush();

		try (Cursor c = mdb.cursor(); RandomCursor c2 = mdb.randomCursor()) {
			int i = 0;
			for (Record r : list) {
				c.next();
				assertArrayEquals(r.array_int_1, c.array_int_1);
				assertEquals(r.double_0, c.double_0, 0);
				assertEquals(r.int_0, c.int_0);

				c2.seek(i);

				assertArrayEquals(r.array_int_1, c2.array_int_1);
				assertEquals(r.double_0, c2.double_0, 0);
				assertEquals(r.int_0, c2.int_0);
				i++;
			}
		}
	}
}
