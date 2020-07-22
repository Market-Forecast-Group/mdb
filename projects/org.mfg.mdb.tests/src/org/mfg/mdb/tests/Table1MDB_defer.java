package org.mfg.mdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBList;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Appender;
import org.mfg.mdb.tests.mdb.Table1MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table1MDB.Record;

public class Table1MDB_defer {
	private JUnitTestsMDBSession session;
	private Appender app;
	private Table1MDB mdb;
	private boolean _winOS;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root);

		mdb = session.connectTo_Table1MDB("test.mdb");
		app = mdb.appender();

		_winOS = System.getProperty("os.name").contains("indows");
	}

	@After
	public void after() {
		MDBSession.delete(session.getRoot());
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void defer_cursor() throws IOException, TimeoutException {
		for (int i = 0; i < 200; i++) {
			app.append();
		}
		Cursor c = mdb.cursor();
		for (int i = 0; i < 100; i++) {
			c.next();
		}
		assertTrue("Cursor must be open", c.isOpen());
		session.defer(c);
		session.closeAndDelete();
		assertTrue("Cursor must be closed", !c.isOpen());

		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void no_defer_cursor() throws IOException {
		for (int i = 0; i < 200; i++) {
			app.append();
		}
		Cursor c = mdb.cursor();
		for (int i = 0; i < 100; i++) {
			c.next();
		}
		assertTrue("Cursor must be open", c.isOpen());
		try {
			session.closeAndDelete();
			fail("Cannot close when there is an open cursor");
		} catch (TimeoutException e) {
			assertTrue(session.isOpen());
		}

		assertTrue("Cursor must be open", c.isOpen());

		if (_winOS) {
			assertTrue(session.getRoot().exists());
		}

		c.close();

	}

	@Test
	public void defer_list() throws IOException, TimeoutException {
		for (int i = 0; i < 200; i++) {
			app.append();
		}
		MDBList<Record> list = mdb.list(mdb.thread_randomCursor());
		assertTrue(list.getCursor().isOpen());
		int fail = session.closeAndDelete();
		assertTrue(!list.getCursor().isOpen());
		assertTrue(fail == 0);
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void no_defer_list() throws IOException {
		for (int i = 0; i < 200; i++) {
			app.append();
		}
		// create a non deferred cursor
		RandomCursor neverClosedCursor = mdb.randomCursor();
		MDBList<Record> list = mdb.list(neverClosedCursor);

		try {
			session.closeAndDelete();
			fail("Cannot close when there is an open cursor");
		} catch (TimeoutException e) {
			assertTrue(session.isOpen());
		}

		assertTrue(list.getCursor().isOpen());

		list.getCursor().close();
	}
}
