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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Appender;
import org.mfg.mdb.tests.mdb.Table1MDB.RandomCursor;

/**
 * @author arian
 * 
 */
public class Table1MDB_indexOf {
	private JUnitTestsMDBSession session;
	private Appender app;
	private Table1MDB mdb;

	@Before
	public void before() throws IOException {
		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		session = new JUnitTestsMDBSession("test-first", root,
				AllTests.TEST_SESSION_MODE);

		mdb = session.connectTo_Table1MDB("test.mdb");
		app = mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		session.closeAndDelete();
		assertFalse(session.getRoot().exists());
	}

	@Test
	public void indexOf() throws IOException {
		int len = 100000;
		for (int i = 0; i < len; i++) {
			app.double_0 = i;
			app.append();
		}
		app.flush();
		try (RandomCursor c = mdb.randomCursor()) {
			for (long i = 0; i < len; i++) {
				long index = mdb.indexOfDouble_0(c, i);
				assertEquals(i, index);
			}
		}
	}

	@Test
	public void indexOf_no_flush() throws IOException {
		int len = 100000;
		for (int i = 0; i < len; i++) {
			app.double_0 = i;
			app.append();
		}

		if (mdb.getMode().isBasic()) {
			app.flush();
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (long i = 0; i < len; i++) {
				long index = mdb.indexOfDouble_0(c, i);
				assertEquals(i, index);
			}
		}
	}

	@Test
	public void indexOf_low_high() throws IOException {
		int len = 100000;
		for (int i = 0; i < len; i++) {
			app.double_0 = i;
			app.append();
		}

		if (mdb.getMode().isBasic()) {
			app.flush();
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (long i = 0; i < len; i++) {
				long key = i;
				long index1;
				if (i > 10) {
					index1 = mdb.indexOfDouble_0(c, key, i - 10, len - 1);
					assertEquals(i, index1);

					index1 = mdb.indexOfDouble_0(c, key, i - 10, i + 10);
					assertEquals(i, index1);
				}
			}
		}
	}

	@Test
	public void indexOf_last_record_on_rbuf() throws IOException {
		for (int i = 0; i < 10; i++) {
			app.double_0 = 100 + i;
			app.append();
		}

		if (mdb.getMode().isBasic()) {
			app.flush();
		}

		int key = 107;
		long i = mdb.indexOfDouble_0(mdb.thread_randomCursor(), key);
		assertEquals(key - 100, i);
	}

	@Test
	public void indexOf_exact() throws IOException {
		int n = 1000;
		for (int i = 0; i < n; i++) {
			if (i % 2 == 0) {
				app.double_0 = i;
				app.append();
			}
		}

		if (mdb.getMode().isBasic()) {
			app.flush();
		}

		try (RandomCursor c = mdb.randomCursor()) {
			for (int i = 0; i < n; i++) {
				long index = mdb.indexOfDouble_0_exact(c, i);
				if (i % 2 == 0) {
					int expected = i / 2;
					assertEquals("key found at " + expected, expected, index);
				} else {
					assertTrue("key not found " + index, index < 0);
				}
			}
		}
	}

	// Hello Arian,
	//
	// during some tests I have found a problem in MDB generated code.
	//
	// The bug arises when a database is only in memory and you are searching a
	// key smaller than the first key of the buffer.
	//
	// You should return -1 (I am speaking about the binary search exact, but
	// instead you are returning -2)
	//
	// The problem is that the condition
	//
	// if ((r.timestamp == key ? 0 : (r.timestamp < key ? -1 : 1)
	// * order) <= 0) {
	// /* search in memory */
	//
	// is not sufficient, because it does not catch the special case when the
	// file is empty.
	//
	// So it goes on the other route and it returns a not meaningful number.
	//
	// For now I have resolved putting this:
	//
	// if (((r.timestamp == key ? 0 : (r.timestamp < key ? -1 : 1)
	// * order) <= 0)
	// || _file.length() == 0) {
	//
	// and this seems to work in the method indexOf$field_exact
	@Test
	public void indexOf_exact_empty_file() throws IOException {
		// test with empty file but something in memory
		app.double_0 = 5;
		app.append();

		if (mdb.getMode().isBasic()) {
			app.flush();
		}
		int key = 1;
		try (RandomCursor c = mdb.randomCursor()) {
			long i1 = mdb.indexOfDouble_0_exact(c, key);
			int i2 = indexedBinarySearch(Arrays.asList(new Integer(5)),
					new Integer(key));
			assertEquals(i2, i1);
			// test with empty db
			mdb.truncate(0);
			i1 = mdb.indexOfDouble_0_exact(c, key);
			i2 = indexedBinarySearch(new ArrayList<Integer>(), new Integer(key));
			assertEquals(i2, i1);
		}
	}

	/**
	 * The same code that is used in
	 * {@link Collections#binarySearch(List, Object)}.
	 * 
	 * @param list
	 * @param key
	 * @return
	 */
	private static <T> int indexedBinarySearch(
			List<? extends Comparable<? super T>> list, T key) {
		int low = 0;
		int high = list.size() - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			Comparable<? super T> midVal = list.get(mid);
			int cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}
}
