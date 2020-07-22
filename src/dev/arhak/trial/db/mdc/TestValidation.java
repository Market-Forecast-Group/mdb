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

import org.mfg.mdb.IValidatorListener;
import org.mfg.mdb.ValidatorError;

import dev.arhak.trial.db.mdc.mdb.TestMDBSession;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB;
import dev.arhak.trial.db.mdc.mdb.TestSparseCursorMDB.Appender;

/**
 * @author arian
 * 
 */
public class TestValidation {

	public static void main(String[] args) throws IOException {
		File root = new File("./test_list_db");
		root.mkdirs();

		TestMDBSession session = new TestMDBSession("", root);

		File file = new File(session.getRoot(), "test_sparse");
		file.delete();

		TestSparseCursorMDB mdb = session.connectTo_TestSparseCursorMDB(file);
		Appender app = mdb.appender();
		for (int i = 0; i < 10; i++) {
			app.value = (int) (Math.random() * 100 / 2);
			out.println(app.value);
			app.append();
		}

		mdb.validate(10, new IValidatorListener() {

			@Override
			public void errorReported(ValidatorError args) {
				out.println(args.getMessage());
			}
		}, TestSparseCursorMDB.VALUE_ASCENDING_VALIDATOR);

		session.closeAndDelete();

		if (session.getRoot().exists()) {
			throw new IOException("Fail removing " + session.getRoot());
		}

	}

}
