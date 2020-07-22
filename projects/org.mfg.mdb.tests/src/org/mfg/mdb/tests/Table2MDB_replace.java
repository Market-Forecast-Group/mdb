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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table2MDB;
import org.mfg.mdb.tests.mdb.Table2MDB.Appender;
import org.mfg.mdb.tests.mdb.Table2MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table2MDB.Record;

/**
 * @author arian
 * 
 */
public class Table2MDB_replace {
	/**
	 * 
	 */
	private static int COUNT = 5000;
	private JUnitTestsMDBSession session;
	private Appender app;
	private Table2MDB mdb;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table2MDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_replace() throws IOException {
		for (int i = 0; i < COUNT; i++) {
			app.int_0 = i;
			app.double_0 = i * 2;
			app.append();
		}
		app.flush();
		out.println(Arrays.toString(mdb.selectAll(mdb.thread_cursor())));

		Record r = new Table2MDB.Record();
		for (int i = 0; i < COUNT; i++) {
			r.int_0 = i * 2;
			r.double_0 = i;
			mdb.replace(i, r);
		}

		try (Cursor c = mdb.cursor()) {
			int i = 0;
			while (c.next()) {
				assertEquals(i * 2, c.int_0);
				assertEquals(i, c.double_0, 0);
				i++;
			}
		}
		
		out.println(Arrays.toString(mdb.selectAll(mdb.thread_cursor())));
	}
}
