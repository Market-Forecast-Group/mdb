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
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.RandomCursor;
import org.mfg.mdb.tests.mdb.PriceMDB.Record;

/**
 * @author arian
 * 
 */
public class PriceMDB_randomCursor {
	private static final int NUM_ROWS = 5732;
	private JUnitTestsMDBSession session;
	private PriceMDB mdb;
	private org.mfg.mdb.tests.mdb.PriceMDB.Appender app;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_PriceMDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_with_seq_positions() throws IOException {
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
				Record r = list.get(i);
				c.seek(i);
				assertEquals(i, c.position());
				assertArrayEquals("Record " + i, r.toArray(), c.toRecord()
						.toArray());
			}
		}
	}

	@Test
	public void test_with_random_positions() throws IOException {
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

		Random rand = new Random();

		try (RandomCursor c = mdb.randomCursor()) {
			for (int i = 0; i < list.size(); i++) {
				int pos = Math.abs(rand.nextInt() % NUM_ROWS);
				Record r = list.get(pos);
				c.seek(pos);
				assertEquals(pos, c.position());
				assertArrayEquals("Record " + pos, r.toArray(), c.toRecord()
						.toArray());
			}
		}
	}
}
