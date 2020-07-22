package org.mfg.mdb.tests;

import static java.lang.System.out;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMultithread {
	static AtomicInteger _mutex = new AtomicInteger(0);
	static AtomicBoolean _open = new AtomicBoolean(true);
	static AtomicBoolean _closing = new AtomicBoolean(false);

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
				while (!_closing.get()) { // if not open, does not read

					delay(10); // read every 10ms

					_mutex.incrementAndGet();

					read(); // read

					_mutex.decrementAndGet();
				}
			}

			private void read() {
				// start read
				long s = System.currentTimeMillis();
				for (int i = 0; i < 100; i++) {
					if (!_open.get()) { // it is reading, so, if it is not open,
										// it crashes
						throw new RuntimeException("Resource closed");
					}

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
				long s = System.currentTimeMillis();
				_closing.set(true);

				while (_mutex.get() > 0) { // wait until all the readers stops
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				_open.set(false);
				out.println("***** Closed ***** ("
						+ (System.currentTimeMillis() - s) + "ms)");
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