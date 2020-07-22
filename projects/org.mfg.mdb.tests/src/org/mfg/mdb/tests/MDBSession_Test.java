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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Cursor;

/**
 * @author arian
 * 
 */
public class MDBSession_Test {

	@SuppressWarnings("unused")
	@Test
	public void testSessionMode_READ_ONLY() throws IOException,
			TimeoutException {
		if (AllTests.TEST_MEMORY) {
			return;
		}

		File root = new File("db-test-" + getClass().getSimpleName());

		// READ_ONLY mode fail because an appender is requested
		root.mkdirs();
		assertTrue(root.exists());
		try {
			JUnitTestsMDBSession session = new JUnitTestsMDBSession(
					"test-session", root, SessionMode.READ_ONLY);
			Table1MDB mdb = session.connectTo_Table1MDB("table1.mdb");
			try {
				mdb.appender();
				fail();
			} catch (IOException e) {
				assertEquals(e.getMessage(),
						"Session mode READ_ONLY violation. Appender requested for "
								+ mdb.getFile() + ".");
			}
		} catch (IOException e) {
			fail("Never should not fail create a READ_ONLY session");
		}

		// READ_ONLY mode fail because root does not exist
		assertTrue(MDBSession.delete(root) == 0);
		assertFalse(root.exists());
		try {
			new JUnitTestsMDBSession("test-sesison", root,
					SessionMode.READ_ONLY);
			fail("READ_ONLY mode should fail if the root does not exists");
		} catch (IOException e) {
			assertTrue(true);
		}

		assertFalse(root.exists());

		// READ_ONLY success because it is created and root exists and only
		// cursors are created.

		root.mkdirs();
		assertTrue(root.exists());

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test-session",
				root);
		Table1MDB mdb = session.connectTo_Table1MDB("table1.mdb");
		Cursor c = mdb.cursor();
		c.close();
		assertTrue(session.isOpen());
		session.closeAndDelete();
	}

	@Test
	public void testSessionMode_READ_WRITE() throws TimeoutException,
			IOException {
		if (AllTests.TEST_MEMORY) {
			return;
		}

		File root = new File("db-test-" + getClass().getSimpleName());

		// READ_WRITE success with root does not exists
		if (root.exists()) {
			assertTrue(MDBSession.delete(root) == 0);
		}
		assertFalse(root.exists());
		try {
			JUnitTestsMDBSession session = new JUnitTestsMDBSession(
					"test-session", root, SessionMode.READ_WRITE);
			assertSame(session.getMode(), SessionMode.READ_WRITE);
			assertTrue(session.getRoot().exists());
			Table1MDB mdb = session.connectTo_Table1MDB("table1.mdb");
			org.mfg.mdb.tests.mdb.Table1MDB.Appender app = mdb.appender();
			app.double_0 = 10;
			app.append();

			org.mfg.mdb.tests.mdb.Table1MDB.Record[] data = mdb.selectAll(mdb
					.thread_cursor());
			assertTrue(data.length == 1);

			session.closeAndDelete();
			assertFalse(session.getRoot().exists());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertFalse(root.exists());

		// READ_WRITE success with root exists
		root.mkdirs();
		assertTrue(root.exists());

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test-session",
				root, SessionMode.READ_WRITE);
		assertSame(session.getMode(), SessionMode.READ_WRITE);
		assertTrue(session.getRoot().exists());
		Table1MDB mdb = session.connectTo_Table1MDB("table1.mdb");
		org.mfg.mdb.tests.mdb.Table1MDB.Appender app = mdb.appender();
		app.double_0 = 10;
		app.append();

		org.mfg.mdb.tests.mdb.Table1MDB.Record[] data = mdb.selectAll(mdb
				.thread_cursor());
		assertTrue(data.length == 1);

		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@SuppressWarnings("static-method")
	@Test
	public void testMetadata() throws IOException, TimeoutException {
		if (AllTests.TEST_MEMORY) {
			return;
		}

		File root = new File("test-db");
		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test", root);

		session.connectTo_PriceMDB("prices.mdb");

		session.close();

		assertTrue(".metadata", new File(root, ".metadata").exists());
		assertTrue(".metadata/schema.schema-json",
				new File(root, ".metadata").exists());
		// TODO: check the new meta-data
		// assertTrue(".metadata/prices.mdb.metadata", new File(root,
		// ".metadata/prices.mdb.metadata").exists());

		// create it again.

		session = new JUnitTestsMDBSession("test", root);

		session.connectTo_PriceMDB("prices.mdb");

		session.close();

		assertTrue(".metadata", new File(root, ".metadata").exists());
		assertTrue(".metadata/schema.schema-json",
				new File(root, ".metadata").exists());
		// TODO: check new meta-data
		// assertTrue(".metadata/prices.mdb.metadata", new File(root,
		// ".metadata/prices.mdb.metadata").exists());
		MDBSession.delete(root);
	}

}
