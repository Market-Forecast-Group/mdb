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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Cursor;
import org.mfg.mdb.tests.mdb.PriceMDB.RandomCursor;
import org.mfg.mdb.tests.mdb.PriceMDB.Record;

/**
 * @author arian
 * 
 */
public class PriceMDB_virtualColumns {
	private static final int NUM_ROWS = 5732;
	private JUnitTestsMDBSession session;
	private PriceMDB mdb;
	private org.mfg.mdb.tests.mdb.PriceMDB.Appender app;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new TestSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_PriceMDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	// @Test
	public void test_append_cursor() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.real = i % 2 == 0;
			r.rawPrice = i * (r.real ? 1 : -1);
			r.fakeTime = i;
			r.price = i;
			list.add(r);
		}

		for (Record r : list) {
			app.rawPrice = r.rawPrice;
			app.append();
		}
		// app.flush();

		Cursor c = mdb.cursor();
		int i = 0;
		while (c.next()) {
			Record r = list.get(i);
			assertArrayEquals("Record " + i, r.toArray(), c.toRecord()
					.toArray());
			i++;
		}
		c.close();
	}

	// @Test
	public void test_append_cursor_random() throws IOException {
		Random rand = new Random();
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.rawPrice = rand.nextLong();
			r.real = r.rawPrice >= 0;
			r.fakeTime = i;
			r.price = Math.abs(r.rawPrice);
			list.add(r);
		}

		for (Record r : list) {
			app.rawPrice = r.rawPrice;
			app.append();
		}
		app.flush();

		Cursor c = mdb.cursor();
		int i = 0;
		while (c.next()) {
			Record r = list.get(i);
			// out.println(c);
			assertArrayEquals("Record " + i, r.toArray(), c.toRecord()
					.toArray());
			i++;
		}
		c.close();
	}

	// @Test
	public void test_record() throws IOException {
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.real = i % 2 == 0;
			r.rawPrice = i * (r.real ? 1 : -1);
			r.fakeTime = i;
			r.price = i;
			list.add(r);
		}

		for (Record r : list) {
			app.rawPrice = r.rawPrice;
			app.append();
		}

		// when it is using the basic mode
		// it should flush the changes to make them visible
		// to the readers
		if (mdb.getSession().getMode() == SessionMode.BASIC_READ_WRITE) {
			app.flush();
		}
		try (RandomCursor c = mdb.randomCursor()) {
			for (int i = 0; i < list.size(); i++) {
				Record r1 = list.get(i);
				c.seek(i);
				Record r2 = c.toRecord();
				assertArrayEquals("Record " + i, r1.toArray(), r2.toArray());
				i++;
			}
		}
	}

	// @Test
	public void test_record_random() throws IOException {
		Random rand = new Random();
		List<Record> list = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.rawPrice = rand.nextLong();
			r.real = r.rawPrice >= 0;
			r.fakeTime = i;
			r.price = Math.abs(r.rawPrice);
			list.add(r);
		}

		for (Record r : list) {
			app.update(r);
			app.append();
		}
		app.flush();

		try (RandomCursor c = mdb.randomCursor()) {
			for (int i = 0; i < list.size(); i++) {
				Record r1 = list.get(i);
				c.seek(i);
				Record r2 = c.toRecord();
				assertArrayEquals("Record " + i, r1.toArray(), r2.toArray());
			}
		}
	}

	@Test
	public void test_replace() throws IOException {
		test_record_random();

		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.rawPrice = i * 2;
			r.fakeTime = -1;
			r.price = -1;
			r.real = false;
			mdb.replace(i, r);
		}

		Record[] all = mdb.selectAll(mdb.thread_cursor());

		for (int i = 0; i < NUM_ROWS; i++) {
			Record r1 = new Record();
			r1.fakeTime = i;
			r1.price = i * 2;
			r1.real = true;
			r1.rawPrice = i * 2;
			Record r2 = all[i];
			assertArrayEquals("Record " + i, r1.toArray(), r2.toArray());
		}

		// all = mdb.selectAll();
		// for (Record r : all) {
		// out.println(r);
		// }
	}
}
