package org.mfg.mdb.demo;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.mfg.mdb.demo.Demo_02_PriceMDB.Appender;
import org.mfg.mdb.demo.Demo_02_PriceMDB.Cursor;
import org.mfg.mdb.demo.Demo_02_PriceMDB.RandomCursor;
import org.mfg.mdb.demo.Demo_02_PriceMDB.Record;

/**
 * Simple demonstration of the appender and the cursor.
 * 
 * @author arian
 *
 */
class Demo_03_SimpleAppenderCursorUsage {
	public static void main(String[] args) throws IOException, TimeoutException {
		Demo_02_MDBSession session = new Demo_02_MDBSession("demo-session",
				new File("demo-database"));

		Demo_02_PriceMDB mdb = session
				.connectTo_Demo_02_PriceMDB("prices-today.mdb");

		Appender appender = mdb.appender();

		int[] times = { 1, 10, 23, 40 };
		long[] prices = { 10, 15, 5, 20 };

		for (int i = 0; i < 4; i++) {
			appender.time = times[i];
			appender.price = prices[i];
			appender.append();
		}
		appender.flush();
		try (Cursor cursor = mdb.cursor()) {
			while (cursor.next()) {
				int time = cursor.time;
				long price = cursor.price;
				out.println("record [" + time + ", " + price + "]");
			}
		}

		try (RandomCursor randomCursor = mdb.randomCursor()) {
			Record r = mdb.findRecord_where_time_is(randomCursor, 23);
			out.println("The price at time 23 is " + r.price);
		}

		session.closeAndDelete();

	}
}
