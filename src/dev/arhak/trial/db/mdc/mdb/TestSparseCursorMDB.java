

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
public class TestSparseCursorMDB extends MDB {

	public static class Record implements IRecord {
		public int value; /* 0 */

		@Override
		public String toString() {
			return "TestSparseCursor [ "
				 + "value=" + value + " "	
				 + " ]";
		}

		public Object[] toArray() {
			return new Object[] {
							value,
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
			r.value = this.value;
			return r;
		}
		
		public Object get(int columnIndex) {
			switch(columnIndex) {
				case 0: return value;
				default: throw new IndexOutOfBoundsException("Wrong column index " + columnIndex);			 
			}
		}
		
		public void update(Record record) {
			this.value = record.value;
		}
		
/* BEGIN USER RECORD */
/* User can insert his code here */
/* END USER RECORD */		
	}

	class TestSparseCursorList extends AbstractListMDB<Record> {
		TestSparseCursorMDB mdb = TestSparseCursorMDB.this;
		private RandomCursor _cursor;
		
		TestSparseCursorList(boolean defer) throws IOException {
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
		public TestSparseCursorMDB getMDB() {
			return TestSparseCursorMDB.this;
		}
		
		public void close() throws IOException {
			_cursor.close();
		}
	}

	@Override
	public AbstractListMDB<Record> asList(boolean defer) throws IOException {
		return new TestSparseCursorList(defer);
	}
	
	@Override
	public AbstractListMDB<Record> asList() throws IOException {
		return new TestSparseCursorList(false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public java.util.List<Record> toList() {
		return new LazyListMDB();
	}


	public void replace(long index, Record r) throws IOException {
		synchronized(_rbuf) {
			if (index >= _rbufPos) {
				int pos = (int) (index - _rbufPos);
				if (pos < _rbufSize) {
					_rbuf[pos] = r;
				}
				return;
			} 				
		}
		RandomAccessFile raf = new RandomAccessFile(_file, "rw");
		FileChannel channel= raf.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(r.value);
		buf.rewind();
		channel.write(buf, index * 4);
		raf.close();		
	}

	public static final IValidator VALUE_ASCENDING_VALIDATOR = new IValidator() {
		@Override
		public boolean validate(ValidationArgs args, IValidatorListener listener) {
			Record prev = (Record) args.getPrev();
			Record current = (Record) args.getCurrent();
			int prevValue = prev.value;
			int curValue = current.value;
			boolean valid = prevValue <= curValue; 
			if (!valid) {
				long row1 = args.getRow() - 1;
				long row2 = args.getRow();
				listener.errorReported(new ValidatorError(args, 						
						"value(" + row1 + ")=" + prevValue + " > " + "value(" + row2 + ")=" + curValue + ""));
			}
			return valid;
		}
	};


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
		long newLen = len * 4; 
		raf.setLength(newLen);				
		raf.close();
		synchronized (_rbuf) {
			_rbufPos = fsize();
		}					
	}

	 
	public static class ValueComparator implements java.util.Comparator<Record> {
		
		@Override
		public int compare(Record o1, Record o2) {
			return o1.value < o2.value? -1 : (o1.value > o2.value? 1 : 0);
		}
	}
	
	public static int indexOfValue(Record[] data, int key) {
		return indexOfValue(data, key, 0, data.length);
	}

    public static int indexOfValue(Record[] data, int key, int low, int high) {
    	low = low < 0? 0 : low; 
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    int midVal = data[mid].value;
		    int cmp = (midVal == key ? 0 : (midVal < key ? -1 : 1));
	
		    if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; /* key found */
			}
		}
		return low == 0 ? 0 : low - 1; /* key not found */
    }


	/**
	 * Is better use the selectXXX methods
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public Cursor cursorInValueRange(int lower, int upper) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		long high = size() - 1;

		long start = indexOfValue(lower, channel, 0, high);
		long stop = indexOfValue(upper, channel, 0, high);
		return new Cursor(raf, channel, start, stop, _bufferSize);
	}
	
	public long indexOfValue(int key, long low, long high) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		try {
			long i = indexOfValue(key, channel, low, high);
			return i;
		} finally {
			raf.close();
			channel.close();
		}
	}
	
	public long indexOfValue(int key) throws IOException {
		return indexOfValue(key, 0, size() - 1);
	}
	
	private long indexOfValue(int key, FileChannel channel, long low, long high) throws IOException {
		low = low < 0? 0 : low;
		synchronized (_rbuf) {
			if (_rbufSize > 0 && (low >= _rbufPos || high >= _rbufPos)) {
				Record r = _rbuf[0];
				if ((r.value == key ? 0 
						: (r.value < key ? -1 : 1)) <= 0) {
					/* search in memory */
					return _rbufPos + indexOfValue(_rbuf, key, 0, (int) Math.min(high - _rbufPos, _rbufSize - 1));
				}
			}
		}
		
		/* search in file */
	
		high = high >= _rbufPos ? (_rbufPos == 0? 0 : _rbufPos - 1) : high;
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
				
		while (low <= high) {
			final long mid = (low + high) >>> 1;
				
			channel.position(mid * 4 + 0);
			buffer.rewind();
			channel.read(buffer);
			buffer.rewind();
				
			final int midVal = buffer.getInt();
			final int cmp = (midVal == key ? 0 : (midVal < key ? -1 : 1));
				
			if (cmp < 0) {
				low = mid + 1;
			}
			else if (cmp > 0) {
				high = mid - 1;
			}
			else {
				return mid; /* key found */
			}
		}
		return low == 0 ? 0 : low - 1; /* key not found */
	}

	public void truncateValue(int value) throws IOException {
		if (size() > 0) {
			long len = indexOfValue(value);
			if (len > 0) {
				len--;
			}
			Cursor c = cursor(len);
			while (c.next()) {
				if (c.value > value) {
					break;
				}
				len++;
			}
			c.close();
			truncate(len);
		}
	}
	
	public Record findRecord_where_value_is(int value) throws IOException {
		Record r = null;
		if (size() > 0) {
			long i = indexOfValue(value);
			r = record(i);
			if (r.value != value) {
				r = null;
			}
		}
		return r;
	}
	
	public long countValue(int keyLower, int keyUpper) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		try {
			long high = size() - 1;
			long a = indexOfValue(keyLower, channel, 0L, high);
			long b = indexOfValue(keyUpper, channel, 0L, high);
			return b - a;
		} finally {
			raf.close();
			channel.close();
		}
	}
	
	
	public boolean deleteFiles() {
		boolean result = true;
		if (_file.exists() && !_file.delete()) {
			java.lang.System.err.println("Cannot delete file " + _file);
			result = false;
		}		 
		return result;
	}
	
/* BEGIN USER MDB */
/* User can insert his code here */
/* END USER MDB */		

	
	public static final String[] COLUMNS_NAME = { "value" };
	public static final Class<?>[] COLUMNS_TYPE = { int.class };
	public static final int[] COLUMNS_SIZE = { 4 };
	public static final boolean[] COLUMNS_IS_VIRTUAL = { false };
	public static final int COLUMN_VALUE = 0;


	private int _bufferSize;
	private final File _file;
	private Appender _appender;
	private Record _last;
	private Record _first;
	private Record[] _rbuf;	
	private int _rbufSize;
	private long _rbufPos;
	private int _openCursorCount;



// Common Cursor
	
	public final class Cursor extends Record implements ISeqCursor {
		private final long _stop;
		private long _row;
		private final ByteBuffer _buffer;
		private final RandomAccessFile _raf;
		private long _len;
		private final FileChannel _channel;
		private boolean _open;

		
		private Cursor(RandomAccessFile raf, FileChannel channel, long start, long stop, int bufferSize) throws IOException {
		    _openCursorCount++;
		    _open = true;		    
			_stop = Math.min(stop, size() - 1);
			_row = start;
			_raf = raf;
			_len = raf.length() + _rbufSize;
			_channel = channel;
			_channel.position(start * 4);
			_buffer = ByteBuffer.allocate(bufferSize * 4);
			_channel.read(_buffer);
			_buffer.rewind();
	
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
				this.value = r.value;

					_row ++;
					return true;	
				}
			} 
			
			if (_buffer.position() == _buffer.capacity()) {
				_buffer.rewind();
				_channel.read(_buffer);
				_buffer.rewind();
			}
				this.value = _buffer.getInt();

			_row ++;
			return true;
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_channel.close();
			_raf.close();
	
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

		
		private RandomCursor() throws IOException {
			_open = true;
		    _openCursorCount++;		    
			_row = -1;
			_raf = new RandomAccessFile(_file, "r");
			_channel = _raf.getChannel();
			_buffer = ByteBuffer.allocate(4);
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
				this.value = r.value;

					return;					
				}
			} 
			
			_row = position;
			_buffer.rewind();
			_channel.read(_buffer, position * 4);
			_buffer.rewind();
				this.value = _buffer.getInt();
			
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_raf.close();
	
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

		
		private SparseCursor(RandomAccessFile raf, FileChannel channel, long start, long stop, int bufferSize, int maxLen) throws IOException {
			_open = true;
		    _openCursorCount++;
		    _len = raf.length() + _rbufSize;
			_stop = Math.min(stop, _len);
			_row = start;
			_raf = raf;			
			_channel = channel;
			_buffer = ByteBuffer.allocate(4);
			
			_step = (stop - start) / maxLen;
			if (_step == 0) {
				_step = 1;
			}
		
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
				this.value = r.value;

					_row += _step;
					return true;
				}
			}
			
			_buffer.rewind();
			_channel.read(_buffer, _row * 4);
			_buffer.rewind();
				this.value = _buffer.getInt();
 
			_row += _step;
			return true;
		}
		
		public void close() throws IOException {
		    _openCursorCount--;
			_channel.close();
			_raf.close();
			_open = false;
		
			session.cursorClosed(this);	
		}
		
		public boolean isOpen() {
			return _open;
		}
		
		public long position() {
			return _row;
		}
	}


	public Record[] select__where_Value_in(int lower, int upper) throws IOException {
		long start = indexOfValue(lower) - 1;

		if (start < 0) {
		    start = 0;
		}
	
		Record[] data = new Record[10];
		int size = 0;
	
		Cursor c = cursor(start);
		try {
			while (c.next()) {
			    if (size + 2 > data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
		
			    if (c.value > upper) {
					break;
			    }
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
	
	public Record[] sselect__where_Value_in(int lower, int upper, int maxLen) throws IOException {
		long start = indexOfValue(lower) - 1;
		long stop = indexOfValue(upper) + 1;
		
		if (start < 0) {
		    start = 0;
		}
	
		Record[] data = new Record[10];
		int size = 0;
	
		SparseCursor c = scursor(start, stop, maxLen);
		try {
			while (c.next()) {
			    if (size + 2 > data.length) {
					Record[] newData = new Record[(data.length * 3) / 2 + 1];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
			    }
			    data[size] = c.toRecord();
			    size++;
		
			    if (c.value > upper) {
					break;
			    }
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
	
	public Record[] iselect__where_Value_in(int lower, int upper, int maxLen) throws IOException {
		
		long start = indexOfValue(lower); 
		long stop = indexOfValue(upper);
		
		if (stop - start > maxLen) {
			return sselect__where_Value_in(lower, upper, maxLen);
		} else {
			return select__where_Value_in(lower, upper);
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
		return new Cursor(raf, channel, start, raf.length() / 4 + _rbufSize - 1, _bufferSize);
	} 
	
	public Cursor cursor() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new Cursor(raf, channel, 0, raf.length() / 4 + _rbufSize - 1, _bufferSize);
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
		return new SparseCursor(raf, channel, start, raf.length() / 4 + _rbufSize - 1, _bufferSize, maxLen);
	} 
	
	public SparseCursor scursor(int maxLen) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new SparseCursor(raf, channel, 0, raf.length() / 4 + _rbufSize - 1, _bufferSize, maxLen);
	}


// Appender

	public final class Appender extends Record implements IAppender {
		private final RandomAccessFile _raf;
		private final FileChannel _channel;
		private final TestSparseCursorMDB _mdb;	
		private final ByteBuffer _buf;	 

		 
		private Appender(TestSparseCursorMDB mdb, RandomAccessFile raf, FileChannel channel, int bufferSize) throws IOException {
			_mdb = mdb;
			_raf = raf;
			_channel = channel;
			_channel.position(_raf.length());
			_buf = ByteBuffer.allocate(bufferSize * 4);
		
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
						_buf.putInt(r.value);
 						 						
					}
					_buf.rewind();
					_buf.limit(_rbufSize * 4);
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

		}
		
		public TestSparseCursorMDB getMDB() {
			return _mdb;
		}
	}	
	
	public TestSparseCursorMDB(File file, int bufferSize) throws IOException {
		_file = file;
		_bufferSize = bufferSize;
		_openCursorCount = 0;
		// create it if it doesn't exist already
		_file.createNewFile();
		
		_rbuf = new Record[_bufferSize];
		_rbufSize = 0;
		_rbufPos = fsize();
		
	}
	
	/**
	 * To create a db client is better to use an instance of {@link ChartDBSession} because in this way you will take
	 * advantage of the hybrid memory model.
	 */
	public TestSparseCursorMDB(File file) throws IOException {
		this(file, 100);
	}
	
	public File getFile() {
		return _file;
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
		return "TestSparseCursor - " + _file.getName();
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
		ByteBuffer buffer = ByteBuffer.allocate(4);
		FileChannel channel = raf.getChannel();
		channel.position(index * 4);
		channel.read(buffer);
		buffer.rewind();
			
		Record r = new Record();
		r.value = buffer.getInt();
	
		raf.close();
			
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
		return _file.length() / 4;
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
			return _file.length() / 4 + _rbufSize;
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
