package org.mfg.mdb.tests;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.PriceMDB;
import org.mfg.mdb.tests.mdb.PriceMDB.Appender;

public class TestMultithreadCursors {
	public static void main(String[] args) throws IOException {

		/**
		 * Right now MDB uses mutual exclusion operations, it means, there are
		 * parts of the code where one cursor has to wait for the other.
		 * 
		 * This should be improved:
		 * 
		 * http://www.ibm.com/developerworks/java/library/j-jtp10264/
		 * 
		 */

		JUnitTestsMDBSession session = new JUnitTestsMDBSession("", new File(
				"pepe"), SessionMode.MEMORY);

		final PriceMDB mdb = session.connectTo_PriceMDB("test.mdb");
		Appender app = mdb.appender();
		for (int i = 0; i < 20; i++) {
			app.append();
		}
		app.close();

		Runnable run = new Runnable() {

			@Override
			public void run() {
				try {
					mdb.selectAll(mdb.thread_cursor());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		long t = currentTimeMillis();

		List<Thread> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(run, "Thread-" + i);
			thread.start();
			list.add(thread);
		}
		for (Thread th : list) {
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		out.println("Total: " + (currentTimeMillis() - t) + "ms");
	}
}
