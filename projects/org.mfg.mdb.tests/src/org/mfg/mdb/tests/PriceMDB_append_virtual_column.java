package org.mfg.mdb.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Appender;
import org.mfg.mdb.tests.mdb.PriceMDB.Cursor;
import org.mfg.mdb.tests.mdb.PriceMDB.Record;

public class PriceMDB_append_virtual_column {
	protected static final int COUNT_RECORDS = 2051;
	private JUnitTestsMDBSession session;
	PriceMDB mdb;
	Appender app;

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
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void check_append_virtual_fields() throws IOException {
		final List<Record> data = new ArrayList<>();
		for (int i = 0; i < COUNT_RECORDS; i++) {
			app.realTime = i;
			app.rawPrice = i;
			try {
				app.append();
				data.add(app.toRecord());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		app.flush();

		Cursor c = mdb.thread_cursor();
		try {
			int j = 0;
			c.reset(0, mdb.size());
			while (c.next()) {
				Record r1 = data.get(j);
				Record r2 = c.toRecord();
				assertArrayEquals(r1.toArray(), r2.toArray());
				j++;
			}
		} catch (IOException e) {
			fail();
		}
	}
}
