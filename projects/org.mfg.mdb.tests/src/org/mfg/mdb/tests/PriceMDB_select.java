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
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Cursor;
import org.mfg.mdb.tests.mdb.PriceMDB.Record;

/**
 * @author arian
 * 
 */
public class PriceMDB_select {
	private static final int NUM_ROWS = 10;
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
	public void test_select_wrong_bounds() throws IOException {
		for (int i = 0; i < NUM_ROWS; i++) {
			app.append();
		}

		try (Cursor c = mdb.cursor()) {
			Record[] data = mdb.select(c, 1_000, 1_00);
			assertEquals(0, data.length);
		}
	}

}
