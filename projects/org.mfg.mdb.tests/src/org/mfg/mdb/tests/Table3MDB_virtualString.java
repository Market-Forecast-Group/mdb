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
import static org.junit.Assert.assertFalse;
import static org.mfg.mdb.tests.mdb.Table3MDB.createVirtualArray;
import static org.mfg.mdb.tests.mdb.Table3MDB.createVirtualString;

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
import org.mfg.mdb.tests.mdb.Table3MDB;
import org.mfg.mdb.tests.mdb.Table3MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table3MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table3MDB.Record;

/**
 * @author arian
 * 
 */
public class Table3MDB_virtualString {
	private static final int NUM_ROWS = 5732;
	private JUnitTestsMDBSession session;
	private Table3MDB mdb;
	private Table3MDB.Appender app;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table3MDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_append_cursor() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.index = i;
			r.virtual_array = createVirtualArray(i);
			r.virtual_string = createVirtualString(i);
			list.add(r);
		}

		for (Record r : list) {
			app.index = r.index;
			app.append();
			app.flush();
		}
		// app.flush();

		try (Cursor c = mdb.cursor()) {
			int i = 0;
			while (c.next()) {
				Record r = list.get(i);
				// out.println(c);
				assertArrayEquals("Record " + i, r.toArray(), c.toRecord()
						.toArray());
				i++;
			}
		}
	}

	@Test
	public void test_record() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.index = i;
			r.virtual_array = createVirtualArray(i);
			r.virtual_string = createVirtualString(i);
			list.add(r);
		}

		for (Record r : list) {
			app.index = r.index;
			app.append();
		}
		app.flush();

		try (RandomCursor c = mdb.randomCursor()) {
			for (int i = 0; i < list.size(); i++) {
				Record r1 = list.get(i);
				c.seek(i);
				Record r2 = c.toRecord();
				// out.println(r2);
				assertArrayEquals("Record " + i, r1.toArray(), r2.toArray());
				i++;
			}
		}
	}
}
