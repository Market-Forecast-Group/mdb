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

public class PriceMDB_threading {
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
	public void cursor_thread() throws IOException {
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

		List<Thread> list = new ArrayList<>();

		for (int i = 0; i < 1; i++) {
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Cursor c = mdb.thread_cursor();
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
			});
			list.add(th);
		}
		for (Thread th : list) {
			th.start();
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				fail();
			}
		}
		for (Thread th : list) {
			try {
				th.join();
			} catch (InterruptedException e) {
				fail();
			}
		}
	}
}
