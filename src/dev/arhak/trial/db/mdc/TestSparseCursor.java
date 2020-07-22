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
package dev.arhak.trial.db.mdc;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;

import org.mfg.mdb.ISeqCursor;

import dev.arhak.trial.db.mdc.mdb.TestMDBSession;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB.Appender;

/**
 * @author arian
 * 
 */
public class TestSparseCursor {

	public static void main(final String[] args) throws IOException {
		final File root = new File("./test_list_db");
		root.mkdirs();

		final TestMDBSession session = new TestMDBSession("", root);

		final File file = new File(session.getRoot(), "test_sparse.mdb");
		file.delete();

		final TestSparseCursorMDB mdb = session
				.connectTo_TestSparseCursorMDB(file);
		final Appender app = mdb.appender();
		for (int i = 0; i < 20; i++) {
			app.value = i;
			app.append();
		}
		session.flush();

		ISeqCursor c = mdb.scursor(5);
		printCursor(c);
		c.close();

		out.println("---");

		c = mdb.cursor();
		printCursor(c);
		c.close();

		session.closeAndDelete();

		if (session.getRoot().exists()) {
			throw new IOException("Fail removing " + session.getRoot());
		}
	}

	private static void printCursor(final ISeqCursor c) throws IOException {
		while (c.next()) {
			out.println(c);
		}
	}

}
