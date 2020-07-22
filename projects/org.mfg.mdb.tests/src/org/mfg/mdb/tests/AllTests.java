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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.mfg.mdb.runtime.SessionMode;

/**
 * @author arian
 * 
 */
public class AllTests {

	public static final boolean DEBUG = false;
	public static SessionMode TEST_SESSION_MODE = SessionMode.READ_WRITE;
	public static boolean TEST_MEMORY = TEST_SESSION_MODE == SessionMode.MEMORY;

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(new JUnit4TestAdapter(MDB_truncate_Test.class));
		suite.addTest(new JUnit4TestAdapter(MDBSession_Test.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_select.class));
		suite.addTest(new JUnit4TestAdapter(PersonMDB_randomCursor.class));
		// suite.addTest(new JUnit4TestAdapter(PersonMDB_string.class));
		suite.addTest(new JUnit4TestAdapter(
				PriceMDB_append_virtual_column.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_test_cursor_reset.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_threading.class));
		suite.addTest(new JUnit4TestAdapter(PersonMDB_string.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_asList.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_randomCursor.class));
		suite.addTest(new JUnit4TestAdapter(PriceMDB_virtualColumns.class));
		suite.addTest(new JUnit4TestAdapter(Table1MDB_appender.class));
		if (!TEST_MEMORY) {
			suite.addTest(new JUnit4TestAdapter(Table1MDB_backup.class));
			suite.addTest(new JUnit4TestAdapter(Table1MDB_defer.class));
		}
		suite.addTest(new JUnit4TestAdapter(Table1MDB_first_last_Test.class));
		suite.addTest(new JUnit4TestAdapter(Table1MDB_indexOf.class));
		suite.addTest(new JUnit4TestAdapter(Table1MDB_Record_Test.class));
		suite.addTest(new JUnit4TestAdapter(Table1MDB_record.class));
		suite.addTest(new JUnit4TestAdapter(Table1MDB_writeAll_readAll.class));
		suite.addTest(new JUnit4TestAdapter(Table2MDB_replace.class));
		suite.addTest(new JUnit4TestAdapter(Table3MDB_virtualArray.class));
		suite.addTest(new JUnit4TestAdapter(Table3MDB_virtualString.class));

		return suite;
	}
}
