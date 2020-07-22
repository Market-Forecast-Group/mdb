package org.mfg.mdb.tests;

import static java.lang.System.out;

import org.mfg.mdb.runtime.DBSynchronizer;

public class TestMultithread2 {
	static DBSynchronizer _synchronizer = new DBSynchronizer();

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			startReader(i);
		}

		startClosing();
	}

	private static void startReader(int id) {
		new Thread("Reader " + id) {
			@Override
			public void run() {
				Runnable read = new Runnable() {

					@Override
					public void run() {
						read(); // read
					}
				};
				while (_synchronizer.operation(read)) {
					delay(10); // read every 10ms
				}
			}

			void read() {
				// start read
				long s = System.currentTimeMillis();
				for (int i = 0; i < 100; i++) {
					delay(1); // read a portion every 10ms
				}

				out.println(getName() + " stop read ("
						+ (System.currentTimeMillis() - s) + "ms)."); // stop
				// reading
			}
		}.start();
	}

	private static void startClosing() {
		new Thread() {
			@Override
			public void run() {

				delay(1000); // wait a second before to close

				out.println("*****  Closing... ***** ");
				final long s = System.currentTimeMillis();
				_synchronizer.close(new Runnable() {

					@Override
					public void run() {
						out.println("***** Closed ***** ("
								+ (System.currentTimeMillis() - s) + "ms)");
					}
				});
			}
		}.start();
	}

	static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}