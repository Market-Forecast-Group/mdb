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
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PersonMDB;
import org.mfg.mdb.tests.mdb.PersonMDB.Appender;
import org.mfg.mdb.tests.mdb.PersonMDB.Record;

/**
 * @author arian
 * 
 */
public class PersonMDB_string {
	private JUnitTestsMDBSession session;
	private PersonMDB mdb;
	private Appender app;

	@Before
	public void before() {
		// nothing
	}

	@After
	public void after() {
		// nothing
	}

	@Test
	public void test_name() throws IOException, TimeoutException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		write(root, "Lino");
		write(root, "Arian");
		write(root, "Ramzy");

		Record[] data = mdb.selectAll(mdb.thread_cursor());
		for (Record r : data) {
			out.println(r);
		}
		if (AllTests.TEST_MEMORY) {
			assertEquals(1, data.length);
			assertEquals("Ramzy", data[0].name);
		} else {
			assertEquals("Lino", data[0].name);
			assertEquals("Arian", data[1].name);
			assertEquals("Ramzy", data[2].name);
		}

		MDBSession.delete(root);
		assertFalse(root.exists());
	}

	private void write(File root, String... names) throws IOException,
			TimeoutException {
		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_PersonMDB("test.mdb");
		app = mdb.appender();

		for (String name : names) {
			app.name = name;
			app.append();
		}
		session.close();
	}

}
