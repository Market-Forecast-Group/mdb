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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mfg.mdb.tests.mdb.Table1MDB;

/**
 * @author arian
 * 
 */
@SuppressWarnings("boxing")
public class Table1MDB_Record_Test {

	/**
	 * Test method for {@link org.mfg.mdb.tests.mdb.Table1MDB.Record#get(int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGet() {
		Table1MDB.Record r = new Table1MDB.Record();
		r.double_0 = 10;
		r.array_int_1 = new int[] { 1, 2 };
		assertEquals(r.double_0, r.get(Table1MDB.COLUMN_DOUBLE_0));
		assertSame(r.array_int_1, r.get(Table1MDB.COLUMN_ARRAY_INT_1));

		for (int i = 0; i < Table1MDB.COLUMNS_NAME.length; i++) {
			Object left = r.get(i);
			Object right = r.toArray()[i];
			out.println(r.getColumnsName()[i] + ": " + left + " = " + right);
			assertEquals(left, right);
		}

		try {
			r.get(-1);
			fail("Expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// nothing
		}

		try {
			r.get(Table1MDB.COLUMNS_NAME.length);
			fail("Expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// nothing
		}
	}
}
