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
import java.util.List;

import dev.arhak.trial.db.mdc.mdb.TableWithStringMDB;
import dev.arhak.trial.db.mdc.mdb.TableWithStringMDB.Cursor;
import dev.arhak.trial.db.mdc.mdb.TestMDBSession;

/**
 * @author arian
 * 
 */
public class TestList {
	public static void main(String[] args) throws IOException {
		File root = new File("./test_list_db");
		root.mkdirs();

		TestMDBSession session = new TestMDBSession("", root);

		File mdbFile = new File(session.getRoot(), "f.mdb");
		File arrayFile = new File(session.getRoot(), "f.array");
		mdbFile.delete();
		arrayFile.delete();

		TableWithStringMDB mdb = session.connectTo_TableWithStringMDB(mdbFile,
				arrayFile);

		List<TableWithStringMDB.Record> list = mdb.asList();

		for (int i = 0; i < 20; i++) {
			TableWithStringMDB.Record r = new TableWithStringMDB.Record();
			r.str = "Record " + i;
			r.intcol = i;
			list.add(r);
		}

		session.close();

		session = new TestMDBSession("", root);
		mdb = session.connectTo_TableWithStringMDB(mdbFile, arrayFile);

		out.println(" -- ");

		list = mdb.asList();

		for (TableWithStringMDB.Record r : list) {
			out.println(r);
		}

		out.println(" -- ");

		Cursor c = mdb.cursor(5);
		c.next();
		out.println(c);
		c.close();

		out.println(" -- ");

		list = mdb.toList();

		for (TableWithStringMDB.Record r : list) {
			out.println(r);
		}

		session.closeAndDelete();

		if (session.getRoot().exists()) {
			throw new IOException("Fail removing " + session.getRoot());
		}
	}
}
