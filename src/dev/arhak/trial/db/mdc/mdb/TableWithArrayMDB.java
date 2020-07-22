

package dev.arhak.trial.db.mdc.mdb;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;
import java.io.File;
import java.nio.ByteBuffer;
import org.mfg.mdb.*;
/* BEGIN USER IMPORTS */
/* User can insert his code here */
/* END USER IMPORTS */


@SuppressWarnings("unused")
public class TableWithArrayMDB extends MDB {

	public static class Record implements IRecord {
		public double sum; /* 0 */
		public boolean[] boolArray; /* 1 */
		public byte[] byteArray; /* 2 */
		public double[] doubleArray; /* 3 */
		public float[] floatArray; /* 4 */
		public int[] intArray; /* 5 */
		public long[] longArray; /* 6 */
		public short[] shortArray; /* 7 */
		public String string; /* 8 */

		@Override
		public String toString() {
			return "TableWithArray [ "
				 + "sum=" + sum + " "	
				 + "boolArray=" + java.util.Arrays.toString(boolArray) + " " 	
				 + "byteArray=" + java.util.Arrays.toString(byteArray) + " " 	
				 + "doubleArray=" + java.util.Arrays.toString(doubleArray) + " " 	
				 + "floatArray=" + java.util.Arrays.toString(floatArray) + " " 	
				 + "intArray=" + java.util.Arrays.toString(intArray) + " " 	
				 + "longArray=" + java.util.Arrays.toString(longArray) + " " 	
				 + "shortArray=" + java.util.Arrays.toString(shortArray) + " " 	
				 + "string=" + string + " "	
				 + " ]";
		}

		public Object[] toArray() {
			return new Object[] {
							sum,
							boolArray,
							byteArray,
							doubleArray,
							floatArray,
							intArray,
							longArray,
							shortArray,
							string,
			 			};
		}
		
		public String[] getColumnsName() {
			return COLUMNS_NAME;
		} 
		
		public Class<?>[] getColumnsType() {
			return COLUMNS_TYPE;
		} 
		
		public Record toRecord() {
			Record r = new Record();
			r.sum = this.sum;
			r.boolArray = this.boolArray == null? null : java.util.Arrays.copyOf(this.boolArray, this.boolArray.length); 	
			r.byteArray = this.byteArray == null? null : java.util.Arrays.copyOf(this.byteArray, this.byteArray.length); 	
			r.doubleArray = this.doubleArray == null? null : java.util.Arrays.copyOf(this.doubleArray, this.doubleArray.length); 	
			r.floatArray = this.floatArray == null? null : java.util.Arrays.copyOf(this.floatArray, this.floatArray.length); 	
			r.intArray = this.intArray == null? null : java.util.Arrays.copyOf(this.intArray, this.intArray.length); 	
			r.longArray = this.longArray == null? null : java.util.Arrays.copyOf(this.longArray, this.longArray.length); 	
			r.shortArray = this.shortArray == null? null : java.util.Arrays.copyOf(this.shortArray, this.shortArray.length); 	
			r.string = this.string;
			return r;
		}
		
		public Object get(int columnIndex) {
			switch(columnIndex) {
				case 0: return sum;
				case 1: return boolArray;
				case 2: return byteArray;
				case 3: return doubleArray;
				case 4: return floatArray;
				case 5: return intArray;
				case 6: return longArray;
				case 7: return shortArray;
				case 8: return string;
				default: throw new IndexOutOfBoundsException("Wrong column index " + columnIndex);			 
			}
		}
		
		public void update(Record record) {
			this.sum = record.sum;
			this.boolArray = record.boolArray == null? null : java.util.Arrays.copyOf(record.boolArray, record.boolArray.length);
			this.byteArray = record.byteArray == null? null : java.util.Arrays.copyOf(record.byteArray, record.byteArray.length);
			this.doubleArray = record.doubleArray == null? null : java.util.Arrays.copyOf(record.doubleArray, record.doubleArray.length);
			this.floatArray = record.floatArray == null? null : java.util.Arrays.copyOf(record.floatArray, record.floatArray.length);
			this.intArray = record.intArray == null? null : java.util.Arrays.copyOf(record.intArray, record.intArray.length);
			this.longArray = record.longArray == null? null : java.util.Arrays.copyOf(record.longArray, record.longArray.length);
			this.shortArray = record.shortArray == null? null : java.util.Arrays.copyOf(record.shortArray, record.shortArray.length);
			this.string = record.string;
		}
		
/* BEGIN USER RECORD */
/* User can insert his code here */
/* END USER RECORD */		
	}

	class TableWithArrayList extends AbstractListMDB<Record> {
		TableWithArrayMDB mdb = TableWithArrayMDB.this;
		private RandomCursor _cursor;
		
		TableWithArrayList(boolean defer) throws IOException {
			_cursor = randomCursor();
			if (defer) {
				if (session == null) {
					throw new IllegalArgumentException("Deferred list are applicable only for MDB attached to a session.");
				}
				session.defer(_cursor);
			}
		}
		
		@Override
		public Record get(int index) {
			try {
				_cursor.seek(index);
				return _cursor.toRecord();
			} catch (IOException e) {
				throw new MDBException(e);
			}
		}

		@Override
		public int size() {
			try {
				return (int) mdb.size();
			} catch (IOException e) {
				throw new MDBException(e);
			}
		}

		@Override
		public boolean add(Record e) {
			Appender app;
			try {
				app = mdb.appender();
				app.update(e);
				app.append();
				return true;
			} catch (IOException e1) {
				throw new MDBException(e1);
			}
		}
		
		@Override
		public TableWithArrayMDB getMDB() {
			return TableWithArrayMDB.this;
		}
		
		public void close() throws IOException {
			_cursor.close();
		}
	}

	@Override
	public AbstractListMDB<Record> asList(boolean defer) throws IOException {
		return new TableWithArrayList(defer);
	}
	
	@Override
	public AbstractListMDB<Record> asList() throws IOException {
		return new TableWithArrayList(false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public java.util.List<Record> toList() {
		return new LazyListMDB();
	}





	/**
	 * Truncate the file to the number of rows <code>len</code>.
	 * 
	 * @param len
	 *            The desired number of rows.
	 * @throws IOException
	 */
	public void truncate(long len) throws IOException {
		_first = null;
		_last = null;
		flushAppender();
		RandomAccessFile raf = new RandomAccessFile(_file, "rw");
		long newLen = len * 104; 
		raf.setLength(newLen);				
		RandomAccessFile arrRaf = new RandomAccessFile(_arrayFile, "rw");
		long newArrLen = 0;
		if (newLen > 0) {				
			// Truncate the array file 
			long startPos = newLen - 104;   
			FileChannel channel = raf.getChannel();		
			ByteBuffer buf = ByteBuffer.wrap(new byte[12]);				
			channel.read(buf, startPos + 8);
			buf.rewind();
			long arrPos = buf.getLong();
			int arrLen = buf.getInt();			
			newArrLen = arrPos + arrLen;					
		}
		arrRaf.setLength(newArrLen);
		arrRaf.close();
		raf.close();
		synchronized (_rbuf) {
			_rbufPos = fsize();
		}					
	}

	 
	
	public boolean deleteFiles() {
		boolean result = true;
		if (_arrayFile.exists() && !_arrayFile.delete()) {
			java.lang.System.err.println("Cannot delete file " + _arrayFile);
			result = false;
		}
		if (_file.exists() && !_file.delete()) {
			java.lang.System.err.println("Cannot delete file " + _file);
			result = false;
		}		 
		return result;
	}
	
/* BEGIN USER MDB */
/* User can insert his code here */
/* END USER MDB */		

	
	public static final String[] COLUMNS_NAME = { "sum", "boolArray", "byteArray", "doubleArray", "floatArray", "intArray", "longArray", "shortArray", "string" };
	public static final Class<?>[] COLUMNS_TYPE = { double.class, boolean[].class, byte[].class, double[].class, float[].class, int[].class, long[].class, short[].class, java.lang.String.class };
	public static final int[] COLUMNS_SIZE = { 8, 12, 12, 12, 12, 12, 12, 12, 12 };
	public static final boolean[] COLUMNS_IS_VIRTUAL = { false, false, false, false, false, false, false, false, false };
	public static final int COLUMN_SUM = 0;
	public static final int COLUMN_BOOLARRAY = 1;
	public static final int COLUMN_BYTEARRAY = 2;
	public static final int COLUMN_DOUBLEARRAY = 3;
	public static final int COLUMN_FLOATARRAY = 4;
	public static final int COLUMN_INTARRAY = 5;
	public static final int COLUMN_LONGARRAY = 6;
	public static final int COLUMN_SHORTARRAY = 7;
	public static final int COLUMN_STRING = 8;


	private int _bufferSize;
	private final File _file;
	private Appender _appender;
	private Record _last;
	private Record _first;
	private Record[] _rbuf;	
	private int _rbufSize;
	private long _rbufPos;
	private int _openCursorCount;
	private File _arrayFile;


// Common Cursor
	
	public final class Cursor extends Record implements ISeqCursor {
		private final long _stop;
		private long _row;
		private final ByteBuffer _buffer;
		private final RandomAccessFile _raf;
		private long _len;
		private final FileChannel _channel;
		private boolean _open;
		private RandomAccessFile _arrayRaf;
		private FileChannel _arrayChannel;
		private long _start;

		
		private Cursor(RandomAccessFile raf, FileChannel channel, long start, long stop, int bufferSize) throws IOException {
		    _openCursorCount++;
		    _open = true;		    
			_stop = Math.min(stop, size() - 1);
			_row = start;
			_raf = raf;
			_len = raf.length() + _rbufSize;
			_channel = channel;
			_channel.position(start * 104);
			_buffer = ByteBuffer.allocate(bufferSize * 104);
			_channel.read(_buffer);
			_buffer.rewind();
			_arrayRaf = new RandomAccessFile(_arrayFile,"rw");
			_arrayChannel = _arrayRaf.getChannel();
			_start = start;
	
			if (session != null) {
		    	session.cursorCreated(this);
		    }		
		}
		
		public boolean next() throws IOException {
			if (_row > _stop || _len == 0) return false;
			
			synchronized (_rbuf) {
				if (_rbufSize > 0 && _row >= _rbufPos) {
					Record r;
					r = _rbuf[(int) (_row - _rbufPos)];
				this.sum = r.sum;
				this.boolArray = r.boolArray;
				this.byteArray = r.byteArray;
				this.doubleArray = r.doubleArray;
				this.floatArray = r.floatArray;
				this.intArray = r.intArray;
				this.longArray = r.longArray;
				this.shortArray = r.shortArray;
				this.string = r.string;

					_row ++;
					return true;	
				}
			} 
			
			if (_buffer.position() == _buffer.capacity()) {
				_buffer.rewind();
				_channel.read(_buffer);
				_buffer.rewind();
			}
				this.sum = _buffer.getDouble();
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.boolArray = new boolean[byteLen / 1];
				for (int i = 0; i < this.boolArray.length; i++) {
					this.boolArray[i] = arrayBuf.get() == 0 ? false : true;
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.byteArray = new byte[byteLen / 1];
				for (int i = 0; i < this.byteArray.length; i++) {
					this.byteArray[i] = arrayBuf.get();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.doubleArray = new double[byteLen / 8];
				for (int i = 0; i < this.doubleArray.length; i++) {
					this.doubleArray[i] = arrayBuf.getDouble();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.floatArray = new float[byteLen / 4];
				for (int i = 0; i < this.floatArray.length; i++) {
					this.floatArray[i] = arrayBuf.getFloat();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.intArray = new int[byteLen / 4];
				for (int i = 0; i < this.intArray.length; i++) {
					this.intArray[i] = arrayBuf.getInt();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.longArray = new long[byteLen / 8];
				for (int i = 0; i < this.longArray.length; i++) {
					this.longArray[i] = arrayBuf.getLong();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.shortArray = new short[byteLen / 2];
				for (int i = 0; i < this.shortArray.length; i++) {
					this.shortArray[i] = arrayBuf.getShort();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __string = new byte[byteLen / 1];
				for (int i = 0; i < __string.length; i++) {
					__string[i] = arrayBuf.get();
				}
				this.string = new String(__string);
				}

			_row ++;
			return true;
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_channel.close();
			_raf.close();
			_arrayChannel.close();
			_arrayRaf.close();
	
			_open = false;
			if (session != null) {
				session.cursorClosed(this);
			}		
		}
		
		public boolean isOpen() {
			return _open;
		}
		
		public long position() {
			return _row;
		}
	}
	
// Random Cursor
	
	public final class RandomCursor extends Record implements IRandomCursor {
		private final ByteBuffer _buffer;
		private final RandomAccessFile _raf;
		private final FileChannel _channel;
		private long _row;
		private boolean _open;
		private RandomAccessFile _arrayRaf;
		private FileChannel _arrayChannel;
		private long _start;

		
		private RandomCursor() throws IOException {
			_open = true;
		    _openCursorCount++;		    
			_row = -1;
			_raf = new RandomAccessFile(_file, "r");
			_channel = _raf.getChannel();
			_buffer = ByteBuffer.allocate(104);
			if (session != null) {
		    	session.cursorCreated(this);
		    }		
		}
		
		public void seek(long position) throws IOException {
			synchronized (_rbuf) {
				if (_rbufSize > 0 && position >= _rbufPos) {
					_row = position;
					Record r;
					r = _rbuf[(int) (position - _rbufPos)];
				this.sum = r.sum;
				this.boolArray = r.boolArray;
				this.byteArray = r.byteArray;
				this.doubleArray = r.doubleArray;
				this.floatArray = r.floatArray;
				this.intArray = r.intArray;
				this.longArray = r.longArray;
				this.shortArray = r.shortArray;
				this.string = r.string;

					return;					
				}
			} 
			
			_row = position;
			_buffer.rewind();
			_channel.read(_buffer, position * 104);
			_buffer.rewind();
				this.sum = _buffer.getDouble();
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.boolArray = new boolean[byteLen / 1];
				for (int i = 0; i < this.boolArray.length; i++) {
					this.boolArray[i] = arrayBuf.get() == 0 ? false : true;
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.byteArray = new byte[byteLen / 1];
				for (int i = 0; i < this.byteArray.length; i++) {
					this.byteArray[i] = arrayBuf.get();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.doubleArray = new double[byteLen / 8];
				for (int i = 0; i < this.doubleArray.length; i++) {
					this.doubleArray[i] = arrayBuf.getDouble();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.floatArray = new float[byteLen / 4];
				for (int i = 0; i < this.floatArray.length; i++) {
					this.floatArray[i] = arrayBuf.getFloat();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.intArray = new int[byteLen / 4];
				for (int i = 0; i < this.intArray.length; i++) {
					this.intArray[i] = arrayBuf.getInt();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.longArray = new long[byteLen / 8];
				for (int i = 0; i < this.longArray.length; i++) {
					this.longArray[i] = arrayBuf.getLong();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.shortArray = new short[byteLen / 2];
				for (int i = 0; i < this.shortArray.length; i++) {
					this.shortArray[i] = arrayBuf.getShort();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __string = new byte[byteLen / 1];
				for (int i = 0; i < __string.length; i++) {
					__string[i] = arrayBuf.get();
				}
				this.string = new String(__string);
				}
			
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_raf.close();
			_arrayChannel.close();
			_arrayRaf.close();
	
			_open = false;
			if (session != null) {
				session.cursorClosed(this);
			}		
		}
		
		public boolean isOpen() {
			return _open;
		}
		
		public long position() {
			return _row;
		}
	}

// Sparse Cursor

	public final class SparseCursor extends Record implements ISeqCursor {
		private final long _stop;
		private long _row;
		private final ByteBuffer _buffer;
		private final RandomAccessFile _raf;
		private long _len;
		private final FileChannel _channel;
		private long _step;
		private boolean _open;
		private RandomAccessFile _arrayRaf;
		private FileChannel _arrayChannel;
		private long _start;

		
		private SparseCursor(RandomAccessFile raf, FileChannel channel, long start, long stop, int bufferSize, int maxLen) throws IOException {
			_open = true;
		    _openCursorCount++;
		    _len = raf.length() + _rbufSize;
			_stop = Math.min(stop, _len);
			_row = start;
			_raf = raf;			
			_channel = channel;
			_buffer = ByteBuffer.allocate(104);
			
			_step = (stop - start) / maxLen;
			if (_step == 0) {
				_step = 1;
			}
			_arrayRaf = new RandomAccessFile(_arrayFile,"rw");
			_arrayChannel = _arrayRaf.getChannel();
		
			if (session != null) {
				session.cursorCreated(this);
			}
		}
		
		public boolean next() throws IOException {
			if (_row > _stop || _len == 0) return false;
			
			synchronized (_rbuf) {
				if (_rbufSize > 0 && _row >= _rbufPos) {
					Record r;
					r = _rbuf[(int) (_row - _rbufPos)];
				this.sum = r.sum;
				this.boolArray = r.boolArray;
				this.byteArray = r.byteArray;
				this.doubleArray = r.doubleArray;
				this.floatArray = r.floatArray;
				this.intArray = r.intArray;
				this.longArray = r.longArray;
				this.shortArray = r.shortArray;
				this.string = r.string;

					_row += _step;
					return true;
				}
			}
			
			_buffer.rewind();
			_channel.read(_buffer, _row * 104);
			_buffer.rewind();
				this.sum = _buffer.getDouble();
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.boolArray = new boolean[byteLen / 1];
				for (int i = 0; i < this.boolArray.length; i++) {
					this.boolArray[i] = arrayBuf.get() == 0 ? false : true;
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.byteArray = new byte[byteLen / 1];
				for (int i = 0; i < this.byteArray.length; i++) {
					this.byteArray[i] = arrayBuf.get();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.doubleArray = new double[byteLen / 8];
				for (int i = 0; i < this.doubleArray.length; i++) {
					this.doubleArray[i] = arrayBuf.getDouble();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.floatArray = new float[byteLen / 4];
				for (int i = 0; i < this.floatArray.length; i++) {
					this.floatArray[i] = arrayBuf.getFloat();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.intArray = new int[byteLen / 4];
				for (int i = 0; i < this.intArray.length; i++) {
					this.intArray[i] = arrayBuf.getInt();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.longArray = new long[byteLen / 8];
				for (int i = 0; i < this.longArray.length; i++) {
					this.longArray[i] = arrayBuf.getLong();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				this.shortArray = new short[byteLen / 2];
				for (int i = 0; i < this.shortArray.length; i++) {
					this.shortArray[i] = arrayBuf.getShort();
				}
				}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __string = new byte[byteLen / 1];
				for (int i = 0; i < __string.length; i++) {
					__string[i] = arrayBuf.get();
				}
				this.string = new String(__string);
				}
 
			_row += _step;
			return true;
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_channel.close();
			_raf.close();
			_open = false;
			_arrayChannel.close();
			_arrayRaf.close();
		
			session.cursorClosed(this);	
		}
		
		public boolean isOpen() {
			return _open;
		}
		
		public long position() {
			return _row;
		}
	}




	
	public Record[] selectAll() throws IOException {
		Record[] data = new Record[(int) size()];
		int size = 0;
	
		Cursor c = cursor();
		try {
			while (c.next()) {
			    if (size >= data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
		    }
		} finally {
			c.close();
		}
	
		if (size < data.length) {
		    Record[] newData = new Record[size];
		    System.arraycopy(data, 0, newData, 0, size);
		    data = newData;
		}
		return data;
	}

	public Record[] select(long start, long stop) throws IOException {
		long last = size() - 1;
		start = start < 0? 0 : start;
		stop = stop > last? last : stop;
		
		Record[] data = new Record[10];
		int size = 0;
	
		Cursor c = cursor(start);
		try {
		    long pos = start;
			while (c.next() && pos <= stop) {
			    if (size + 2 > data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
			    pos++;
		    }
		} finally {
			c.close();
		}
	
		if (size < data.length) {
		    Record[] newData = new Record[size];
		    System.arraycopy(data, 0, newData, 0, size);
		    data = newData;
		}
		return data;
	}
	
// 	

	public Record[] sselectAll(int maxLen) throws IOException {
		Record[] data = new Record[(int) size()];
		int size = 0;
	
		SparseCursor c = scursor(maxLen);
		try {
			while (c.next()) {
			    if (size >= data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
		    }
		} finally {
			c.close();
		}
	
		if (size < data.length) {
		    Record[] newData = new Record[size];
		    System.arraycopy(data, 0, newData, 0, size);
		    data = newData;
		}
		return data;
	}

	public Record[] sselect(long start, long stop, int maxLen) throws IOException {
		long last = size() - 1;
		start = start < 0? 0 : start;
		stop = stop > last? last : stop;
		
		Record[] data = new Record[10];
		int size = 0;
	
		SparseCursor c = scursor(start, stop, maxLen);
		try {
		    long pos = start;
			while (c.next() && pos <= stop) {
			    if (size + 2 > data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
			    pos++;
		    }
		} finally {
			c.close();
		}
	
		if (size < data.length) {
		    Record[] newData = new Record[size];
		    System.arraycopy(data, 0, newData, 0, size);
		    data = newData;
		}
		return data;
	}	
	
//	

	public Record[] iselectAll(int maxLen) throws IOException {
		if ( size() > maxLen) {
			return sselectAll(maxLen);
		} else {
			return selectAll();
		}
	}

	public Record[] iselect(long start, long stop, int maxLen) throws IOException {
		if (stop - start > maxLen) {
			return sselect(start, stop, maxLen);
		} else {
			return select(start, stop);
		}
	}


	public Cursor cursor(long start, long stop) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		return new Cursor(raf, raf.getChannel(), start, stop, _bufferSize);
	}
	
	public Cursor cursor(long start) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new Cursor(raf, channel, start, raf.length() / 104 + _rbufSize - 1, _bufferSize);
	} 
	
	public Cursor cursor() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new Cursor(raf, channel, 0, raf.length() / 104 + _rbufSize - 1, _bufferSize);
	}
	
	public RandomCursor randomCursor() throws IOException {
		return new RandomCursor();
	}
	
	// --
	
	public SparseCursor scursor(long start, long stop, int maxLen) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		return new SparseCursor(raf, raf.getChannel(), start, stop, _bufferSize, maxLen);
	}
	
	public SparseCursor scursor(long start, int maxLen) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new SparseCursor(raf, channel, start, raf.length() / 104 + _rbufSize - 1, _bufferSize, maxLen);
	} 
	
	public SparseCursor scursor(int maxLen) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new SparseCursor(raf, channel, 0, raf.length() / 104 + _rbufSize - 1, _bufferSize, maxLen);
	}


// Appender

	public final class Appender extends Record implements IAppender {
		private final RandomAccessFile _raf;
		private final FileChannel _channel;
		private final TableWithArrayMDB _mdb;	
		private final ByteBuffer _buf;	 
		private RandomAccessFile _arrayRaf;
		private FileChannel _arrayChannel;
		private long _start;

		 
		private Appender(TableWithArrayMDB mdb, RandomAccessFile raf, FileChannel channel, int bufferSize) throws IOException {
			_mdb = mdb;
			_raf = raf;
			_channel = channel;
			_channel.position(_raf.length());
			_buf = ByteBuffer.allocate(bufferSize * 104);
			_arrayRaf = new RandomAccessFile(_arrayFile,"rw");
			_arrayChannel = _arrayRaf.getChannel();
		
		}
		
		public void append() throws IOException {									
			if (_rbufSize == _rbuf.length) {
				flush();
			}						 		
			synchronized (_rbuf) {									
				_rbuf[_rbufSize] = _last = toRecord();

				_rbufSize++;
			}
		}
		
		public void flush() throws IOException {																
					for(int j = 0; j < _rbufSize; j++) {						
						Record r = _rbuf[j];
						_buf.putDouble(r.sum);
						{
							int len = r.boolArray == null? 0 : r.boolArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 1);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 1]);
								for (int i = 0; i < len; i++) {
									arrayBuf.put((byte)(r.boolArray[i]? 1 : 0));
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.byteArray == null? 0 : r.byteArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 1);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 1]);
								for (int i = 0; i < len; i++) {
									arrayBuf.put(r.byteArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.doubleArray == null? 0 : r.doubleArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 8);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 8]);
								for (int i = 0; i < len; i++) {
									arrayBuf.putDouble(r.doubleArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.floatArray == null? 0 : r.floatArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 4);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 4]);
								for (int i = 0; i < len; i++) {
									arrayBuf.putFloat(r.floatArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.intArray == null? 0 : r.intArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 4);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 4]);
								for (int i = 0; i < len; i++) {
									arrayBuf.putInt(r.intArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.longArray == null? 0 : r.longArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 8);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 8]);
								for (int i = 0; i < len; i++) {
									arrayBuf.putLong(r.longArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							int len = r.shortArray == null? 0 : r.shortArray.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 2);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 2]);
								for (int i = 0; i < len; i++) {
									arrayBuf.putShort(r.shortArray[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						{
							byte[] __string = r.string == null? null : r.string.getBytes();
							int len = __string == null? 0 : __string.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 1);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 1]);
								for (int i = 0; i < len; i++) {
									arrayBuf.put(__string[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
 						 						
					}
					_buf.rewind();
					_buf.limit(_rbufSize * 104);
					_channel.write(_buf);
					_buf.limit(_buf.capacity());
					_buf.rewind();		
				synchronized (_rbuf) {			
					_rbufSize = 0;
					_rbufPos = fsize();								
			
				}
		}
		
		public void close() throws IOException {
			if (_rbufSize > 0) {
				flush();
			}
			_channel.close();
			_raf.close();
			_arrayChannel.close();
			_arrayRaf.close();

		}
		
		public TableWithArrayMDB getMDB() {
			return _mdb;
		}
	}	
	
	public TableWithArrayMDB(File file, File arrayFile, int bufferSize) throws IOException {
		_file = file;
		_bufferSize = bufferSize;
		_openCursorCount = 0;
		// create it if it doesn't exist already
		_file.createNewFile();
		
		_rbuf = new Record[_bufferSize];
		_rbufSize = 0;
		_rbufPos = fsize();
		_arrayFile = arrayFile;
	}
	
	/**
	 * To create a db client is better to use an instance of {@link ChartDBSession} because in this way you will take
	 * advantage of the hybrid memory model.
	 */
	public TableWithArrayMDB(File file, File arrayFile) throws IOException {
		this(file, arrayFile, 100);
	}
	
	public File getFile() {
		return _file;
	}

	public File getArrayFile(){
		return _arrayFile;
	}

	
	public int getOpenCursorCount() {
	    return _openCursorCount;
	}
	
	public Record[] getRecentRecordsBuffer() {
		synchronized (_rbuf) {
			return _rbuf;
		}
	}
	
	public int getRecentRecordsCount() {
		synchronized (_rbuf) {
			return _rbufSize;
		}
	}
	
	public Object[] recordToArray(Object record) {
		return ((Record) record).toArray();
	}
	
	@Override
	public String toString() {
		return "TableWithArray - " + _file.getName();
	} 
	
	/**
	 * The record at index.
	 * 
	 * @return The record at index.
	 * @throws IOException
	 */
	public Record record(long index) throws IOException {
		synchronized (_rbuf) {
			if (index >= _rbufPos) {
				int i = Math.min(_bufferSize - 1, (int) (index - _rbufPos));
				return _rbuf[i];
			}
		}		
		
		index = index < 0? 0 : index;
		
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r"); 
		ByteBuffer buffer = ByteBuffer.allocate(104);
		FileChannel channel = raf.getChannel();
		channel.position(index * 104);
		channel.read(buffer);
		buffer.rewind();
			RandomAccessFile arrayRaf = new RandomAccessFile(_arrayFile,"rw");
			FileChannel arrayChannel = arrayRaf.getChannel();
			
		Record r = new Record();
		r.sum = buffer.getDouble();
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.boolArray = new boolean[byteLen / 1];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.boolArray.length; i++) {
					r.boolArray[i] = arrayBuf.get() == 0? false : true;
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.byteArray = new byte[byteLen / 1];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.byteArray.length; i++) {
					r.byteArray[i] = arrayBuf.get();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.doubleArray = new double[byteLen / 8];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.doubleArray.length; i++) {
					r.doubleArray[i] = arrayBuf.getDouble();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.floatArray = new float[byteLen / 4];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.floatArray.length; i++) {
					r.floatArray[i] = arrayBuf.getFloat();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.intArray = new int[byteLen / 4];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.intArray.length; i++) {
					r.intArray[i] = arrayBuf.getInt();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.longArray = new long[byteLen / 8];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.longArray.length; i++) {
					r.longArray[i] = arrayBuf.getLong();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				r.shortArray = new short[byteLen / 2];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < r.shortArray.length; i++) {
					r.shortArray[i] = arrayBuf.getShort();
				}
		}
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				byte[] __string = new byte[byteLen / 1];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < __string.length; i++) {
					__string[i] = arrayBuf.get();
				}
				r.string = new String(__string);
		}
	
		raf.close();
			arrayChannel.close();
			arrayRaf.close();
			
		return r;
	}
	
	/**
	 * The last record or null if the db is empty.
	 * 
	 * @return The first record or null if the db is empty.
	 * @throws IOException
	 */
	public Record last()  throws IOException {
		if (_last == null) {
			long size = size();
			if (size > 0) {
				_last = record(size - 1);
			}
		}
		return _last;
	}
	
	/**
	 * The first record or null if the db is empty.
	 * 
	 * @return The first record or null if the db is empty.
	 * @throws IOException
	 */
	public Record first()  throws IOException {
		if (_first == null) {
			long size = size();
			if (size > 0) {
				_first = record(0);
			}
		}
		return _first;
	}
	

	/**
	 * The number of records already persisted in the file.
	 * 
	 * @return
	 * @throws IOException
	 */
	public long fsize() throws IOException {
		return _file.length() / 104;
	}
	
	/**
	 * The number of records. It counts the records in the file and the buffer's records.
	 * You can see it as <code>fsize() + getRecent().size</code>
	 * 
	 * @return
	 * @throws IOException
	 */
	public long size() throws IOException {
		synchronized (_rbuf) {
			return _file.length() / 104 + _rbufSize;
		}
	}
	
	/**
	 * Return the singleton appender of this client. Create an appender if it is not created yet.
	 */
	public Appender appender() throws IOException {
		if (session != null) {
			session.appenderRequested(this);
		}
		
		if (_appender == null) {
			RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "rw");
			FileChannel channel = raf.getChannel();
			_appender = new Appender(this, raf, channel, _bufferSize);
		}
		return _appender;
	}
	
	/**
	 * If the appender is created.
	 */
	public boolean isAppenderCreated() {
	    return _appender != null;
	}
	
	/**
	 * If the appender was created and is open.
	 */
	public boolean isAppenderOpen() {
	    return isAppenderCreated() && _appender._channel.isOpen();
	}
	
	/**
	 * Close the appender. If not appender was created, do nothing.
	 */
	public void closeAppender() throws IOException {
	    if (_appender != null) {
			_appender.close();
	    }
	}
	
	public void flushAppender() throws IOException {
	    if (_appender != null) {
		_appender.flush();
	    }
	}

	public String[] getColumnsName() {
		return COLUMNS_NAME;
	} 
		
	public Class<?>[] getColumnsType() {
		return COLUMNS_TYPE;
	}
}
