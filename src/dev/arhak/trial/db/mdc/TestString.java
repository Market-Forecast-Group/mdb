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

import dev.arhak.trial.db.mdc.mdb.TableWithStringMDB;
import dev.arhak.trial.db.mdc.mdb.TestMDBSession;

/**
 * @author arian
 * 
 */
public class TestString {

	public static void main(final String[] args) throws IOException {
		final File root = new File("string_mdb");
		root.mkdirs();
		final File file = new File(root, "string.mdb");
		final File arrayFile = new File(root, "string.amdb");

		file.delete();
		arrayFile.delete();

		if (file.exists() || arrayFile.exists()) {
			throw new IllegalArgumentException("File exixts! Remove it!");
		}

		final TestMDBSession session = new TestMDBSession("test_string", root);

		final TableWithStringMDB mdb = session.connectTo_TableWithStringMDB(
				file, arrayFile);

		final TableWithStringMDB.Appender a = mdb.appender();
		for (int i = 0; i < 10000; i++) {
			a.str = "Hello " + i;
			a.intcol = i;
			a.append();
			a.str = null;
			a.intcol = 0;
			a.append();
		}
		a.flush();

		final TableWithStringMDB.Cursor c = mdb.cursor();
		while (c.next()) {
			out.println(c);
		}
		c.close();

		session.closeAndDelete();

		if (file.exists() || arrayFile.exists()) {
			throw new IllegalArgumentException(
					"File exixts! There is a bug or a problem closing and deleting them!");
		}
	}
}
