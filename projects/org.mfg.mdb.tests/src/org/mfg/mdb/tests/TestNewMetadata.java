package org.mfg.mdb.tests;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PersonMDB;
import org.mfg.mdb.tests.mdb.PersonMDB.Appender;

public class TestNewMetadata {
	public static void main(String[] args) throws IOException, TimeoutException {
		JUnitTestsMDBSession session = new JUnitTestsMDBSession("test",
				new File("/home/arian/Downloads/test_mdb"));
		try {
			PersonMDB mdb = session.connectTo_PersonMDB("cuca.mdb");
			Appender a = mdb.appender();
			a.name = "juan";
			a.age = 10;
			a.append();
		} finally {
			session.close();
		}
	}
}
