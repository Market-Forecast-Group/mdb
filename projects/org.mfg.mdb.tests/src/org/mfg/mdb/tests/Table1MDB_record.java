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
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table2MDB;
import org.mfg.mdb.tests.mdb.Table2MDB.RandomCursor;

public class Table1MDB_record {

	private JUnitTestsMDBSession session;
	private Table2MDB mdb;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table2MDB("test.mdb");
	}

	@Test
	public void test_on_empty() throws IOException {
		assertTrue(mdb.size() == 0);
		try (RandomCursor c = mdb.randomCursor()) {
			try {
				c.seek(0);
				fail("Expected index out of bounds.");
			} catch (IndexOutOfBoundsException e) {
				// expected
			}
		}
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}
}
