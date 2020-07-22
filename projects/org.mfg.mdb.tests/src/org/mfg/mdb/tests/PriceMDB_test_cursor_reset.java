package org.mfg.mdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Appender;
import org.mfg.mdb.tests.mdb.PriceMDB.Cursor;
import org.mfg.mdb.tests.mdb.PriceMDB.RandomCursor;

public class PriceMDB_test_cursor_reset {
	private JUnitTestsMDBSession session;
	private PriceMDB mdb;
	private Appender app;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root);

		mdb = session.connectTo_PriceMDB("test.mdb");
		app = mdb.appender();

	}

	@After
	public void after() throws IOException, TimeoutException {
		if (session.isOpen()) {
			session.closeAndDelete();
		}
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_reset() throws IOException, TimeoutException {
		for (int i = 0; i < 200; i++) {
			app.fakeTime = i;
			app.append();
		}

		Cursor cursor = mdb.cursor();
		for (int i = 0; i < 100; i++) {
			cursor.next();
			assertEquals(i, cursor.fakeTime);
		}

		cursor.reset(0, mdb.size());
		for (int i = 0; i < 200; i++) {
			cursor.next();
			assertEquals(i, cursor.fakeTime);
		}
		assertFalse(cursor.next());

		try (RandomCursor randCursor = mdb.randomCursor()) {
			mdb.select__where_RealTime_in(randCursor, cursor, 0, 200);
			mdb.select__where_RealTime_in(randCursor, cursor, 0, 200);

			try {
				session.closeAndDelete();
				fail("The session cannot close because there is an open cursor");
			} catch (TimeoutException e) {
				// expected
			}
			assertTrue(session.isOpen());

			cursor.close();
		}

		session.closeAndDelete();

		assertFalse(session.isOpen());
	}
}
