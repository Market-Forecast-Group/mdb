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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;
import org.mfg.mdb.tests.mdb.Table2MDB;
import org.mfg.mdb.tests.mdb.Table2MDB.Appender;

/**
 * @author arian
 * 
 */
public class MDB_truncate_Test {

	@Test
	public void test_truncate_nonArrayFile() throws IOException,
			TimeoutException {
		if (AllTests.TEST_MEMORY) {
			return;
		}

		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test-first",
				root, AllTests.TEST_SESSION_MODE);

		Table2MDB mdb = session.connectTo_Table2MDB("test.mdb");
		Appender app = mdb.appender();

		for (int i = 100; i < 200; i++) {
			app.int_0 = i;
			app.append();
		}
		app.flush();

		long len1 = mdb.getFile().length();
		mdb.truncate(1);
		long len2 = mdb.getFile().length();

		assertTrue("The file was not truncated.", len2 < len1);
		assertTrue("Truncated to 1 record", mdb.size() == 1);

		try (org.mfg.mdb.tests.mdb.Table2MDB.RandomCursor c = mdb
				.randomCursor()) {
			c.seekFirst();
			assertTrue(100 + " != " + c.int_0, c.int_0 == 100);
		}
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_truncate_ArrayedEmptyFile() throws IOException,
			TimeoutException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test-first",
				root);

		try {
			Table1MDB mdb = session.connectTo_Table1MDB("test.mdb");
			mdb.truncate(0);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void test_truncate_ArrayedFile() throws IOException,
			TimeoutException {
		if (AllTests.TEST_MEMORY) {
			return;
		}

		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test-first",
				root);

		try {
			Table1MDB mdb = session.connectTo_Table1MDB("test.mdb");
			Table1MDB.Appender app = mdb.appender();
			for (int i = 0; i < 100; i++) {
				app.array_int_1 = new int[i];
				Arrays.fill(app.array_int_1, i);
				app.append();
			}
			app.flush();

			out.println("dump all");
			dumpFile(mdb.getArrayFile());

			// truncate 50 test
			int newLen = 50;
			long oldArrayFileLen = mdb.getArrayFile().length();
			mdb.truncate(newLen);
			out.println("dump 50");
			dumpFile(mdb.getArrayFile());

			long newArrayFileLen = mdb.getArrayFile().length();
			assertTrue("The mdb file was not truncated to the right size",
					mdb.size() == newLen);
			assertTrue("The array file was not truncated",
					newArrayFileLen < oldArrayFileLen);
			try (RandomCursor c = mdb.randomCursor()) {
				c.seekLast();
				out.println(Arrays.toString(c.array_int_1));
				int lastArrLen = c.array_int_1.length;
				int expectedLastArrLen = newLen - 1;
				assertTrue("Expected last array len " + expectedLastArrLen
						+ " but it is " + lastArrLen,
						lastArrLen == expectedLastArrLen);
				for (int i = 0; i < lastArrLen; i++) {
					assertEquals(newLen - 1, c.array_int_1[i]);
				}
			}

			// truncate 0 test
			mdb.truncate(0);

			long newSize = mdb.size();
			assertTrue("After truncate(0) the expected size is 0 but it is "
					+ newSize, newSize == 0);
			newSize = mdb.getArrayFile().length();
			assertEquals("After truncate(0) check array size" + newSize, 0,
					newSize);

		} catch (IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	/**
	 * @param mdb
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void dumpFile(File file) throws FileNotFoundException,
			IOException {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			ByteBuffer buf = ByteBuffer.wrap(new byte[(int) raf.length()]);
			raf.getChannel().read(buf);
			out.println(Arrays.toString(buf.array()));
		}
	}
}
