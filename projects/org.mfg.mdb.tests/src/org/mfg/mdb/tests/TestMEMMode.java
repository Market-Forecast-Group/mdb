package org.mfg.mdb.tests;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Appender;
import org.mfg.mdb.tests.mdb.PriceMDB.Cursor;
import org.mfg.mdb.tests.mdb.PriceMDB.RandomCursor;
import org.mfg.mdb.tests.mdb.PriceMDB.Record;

public class TestMEMMode {
	private static final int LEN = 1023;

	public static void main(String[] args) throws IOException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession("test", new File(
				"my root"), SessionMode.MEMORY);
		PriceMDB mdb = s.connectTo_PriceMDB("price");

		Appender a = mdb.appender();

		for (int i = 0; i < LEN; i++) {
			a.realTime = i;
			a.append();
		}

		int i = 0;
		try (Cursor seqCur = mdb.cursor();
				RandomCursor randCur = mdb.randomCursor()) {
			while (seqCur.next()) {
				out.println(i + " " + seqCur.realTime);
				i++;
			}
			out.println("index of " + 134 + " = "
					+ mdb.indexOfRealTime_exact(randCur, 134));
			for (i = 0; i < LEN; i++) {
				randCur.seek(i);
				out.println(i + "=" + randCur.realTime);
			}

			Record[] data = mdb.select_sparse(randCur, seqCur, 0, LEN, 10);
			for (Record r : data) {
				out.println(r.realTime);
			}

			assertTrue(!mdb.getFile().exists());
		}
	}
}
