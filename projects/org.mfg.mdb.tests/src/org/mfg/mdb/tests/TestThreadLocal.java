package org.mfg.mdb.tests;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

public class TestThreadLocal {

	static class Resource {
		private String _name;

		public Resource(String name) {
			super();
			this._name = name;
		}

		@Override
		protected void finalize() throws Throwable {
			out.println("free " + _name);
		}

	}

	public static void main(String[] args) throws InterruptedException {
		final ThreadLocal<Resource> localName = new ThreadLocal<>();
		List<Thread> list = new ArrayList<>();
		for (int i = 0; i < 11000; i++) {
			Thread th = new Thread("Thread " + i) {
				@Override
				public void run() {
					for (int j = 0; j < 5; j++) {
						localName.set(new Resource(getName() + ": " + j));
						try {
							Thread.sleep((long) (Math.random() * 200));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//out.println(localName.get().name);
					}
				}
			};
			list.add(th);
		}
		for(Thread th : list) {
			th.start();
		}
		
		for(Thread th : list) {
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Thread.sleep(1000);
	}
}
