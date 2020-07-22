package org.mfg.mdb.demo;

import static java.lang.System.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class contains demos of the code we wrote to prove how fast can be a
 * database like MDB.
 * 
 * @author arian
 *
 */
class Demo_01_HandWrittenProveOfConcept {

	private static final int RECORD_SIZE = Integer.BYTES + Integer.BYTES;
	private static final int BUFFER_SIZE = 100;

	/**
	 * @param args
	 *            Program arguments.
	 * @throws IOException
	 *             IO error.
	 */
	public static void main(String[] args) throws IOException {
		try {
			writeRecords();
			readRecords();
			long i = indexOf(60);
			out.println("index at " + i);
		} finally {
			Files.deleteIfExists(Paths.get("database"));
		}

	}

	private static long indexOf(int time) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile("database", "r");
				FileChannel channel = raf.getChannel()) {
			long low = 0;
			long high = channel.size() / RECORD_SIZE;

			ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE);

			while (low <= high) {
				final long mid = (low + high) >>> 1;

				channel.position(mid * RECORD_SIZE);
				buf.rewind();
				channel.read(buf);
				buf.rewind();

				final long midVal = buf.getInt();

				if (midVal < time) {
					low = mid + 1;
				} else if (midVal > time) {
					high = mid - 1;
				} else {
					return mid; /* key found */
				}
			}
			return -(low + 1); /* key not found */
		}
	}

	private static void readRecords() throws IOException, FileNotFoundException {
		try (RandomAccessFile raf = new RandomAccessFile("database", "r");
				FileChannel channel = raf.getChannel()) {
			ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE * BUFFER_SIZE);
			buf.position(buf.capacity());

			long size = channel.size() / RECORD_SIZE;

			for (int i = 0; i < size; i++) {
				// if the buffer pointer is at the end it means the whole buffer
				// was printed, then load more records from the file into the
				// buffer
				if (buf.position() == buf.capacity()) {
					buf.rewind();
					channel.read(buf);
					buf.rewind();
				}

				// read the record
				int time = buf.getInt();
				int price = buf.getInt();
				out.println("read [" + time + ", " + price + "]");
			}
		}
	}

	private static void writeRecords() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile("database", "rw");
				FileChannel channel = raf.getChannel()) {
			ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE * BUFFER_SIZE);

			for (int i = 0; i < 250; i++) {
				// if the buffer is full write it to the file
				if (buf.position() == buf.capacity()) {
					buf.rewind();
					channel.write(buf);
					buf.rewind();
				}

				// add the record to the buffer
				int time = i;
				int price = i * 2;
				buf.putInt(time);
				buf.putInt(price);
			}
			if (buf.position() > 0) {
				buf.limit(buf.position());
				buf.rewind();
				channel.write(buf);
			}
		}
	}
}
