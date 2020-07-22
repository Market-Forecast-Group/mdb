

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
public class TableWithStringMDB extends MDB {

	public static class Record implements IRecord {
		public String str; /* 0 */
		public int intcol; /* 1 */

		@Override
		public String toString() {
			return "TableWithString [ "
				 + "str=" + str + " "	
				 + "intcol=" + intcol + " "	
				 + " ]";
		}

		public Object[] toArray() {
			return new Object[] {
							str,
							intcol,
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
			r.str = this.str;
			r.intcol = this.intcol;
			return r;
		}
		
		public Object get(int columnIndex) {
			switch(columnIndex) {
				case 0: return str;
				case 1: return intcol;
				default: throw new IndexOutOfBoundsException("Wrong column index " + columnIndex);			 
			}
		}
		
		public void update(Record record) {
			this.str = record.str;
			this.intcol = record.intcol;
		}
		
/* BEGIN USER RECORD */
/* User can insert his code here */
/* END USER RECORD */		
	}

	class TableWithStringList extends AbstractListMDB<Record> {
		TableWithStringMDB mdb = TableWithStringMDB.this;
		private RandomCursor _cursor;
		
		TableWithStringList(boolean defer) throws IOException {
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
		public TableWithStringMDB getMDB() {
			return TableWithStringMDB.this;
		}
		
		public void close() throws IOException {
			_cursor.close();
		}
	}

	@Override
	public AbstractListMDB<Record> asList(boolean defer) throws IOException {
		return new TableWithStringList(defer);
	}
	
	@Override
	public AbstractListMDB<Record> asList() throws IOException {
		return new TableWithStringList(false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public java.util.List<Record> toList() {
		return new LazyListMDB();
	}



	public static final IValidator INTCOL_ASCENDING_VALIDATOR = new IValidator() {
		@Override
		public boolean validate(ValidationArgs args, IValidatorListener listener) {
			Record prev = (Record) args.getPrev();
			Record current = (Record) args.getCurrent();
			int prevValue = prev.intcol;
			int curValue = current.intcol;
			boolean valid = prevValue <= curValue; 
			if (!valid) {
				long row1 = args.getRow() - 1;
				long row2 = args.getRow();
				listener.errorReported(new ValidatorError(args, 						
						"intcol(" + row1 + ")=" + prevValue + " > " + "intcol(" + row2 + ")=" + curValue + ""));
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
		long newLen = len * 16; 
		raf.setLength(newLen);				
		RandomAccessFile arrRaf = new RandomAccessFile(_arrayFile, "rw");
		long newArrLen = 0;
		if (newLen > 0) {				
			// Truncate the array file 
			long startPos = newLen - 16;   
			FileChannel channel = raf.getChannel();		
			ByteBuffer buf = ByteBuffer.wrap(new byte[12]);				
			channel.read(buf, startPos + 0);
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

	 
	public static class IntcolComparator implements java.util.Comparator<Record> {
		
		@Override
		public int compare(Record o1, Record o2) {
			return o1.intcol < o2.intcol? -1 : (o1.intcol > o2.intcol? 1 : 0);
		}
	}
	
	public static int indexOfIntcol(Record[] data, int key) {
		return indexOfIntcol(data, key, 0, data.length);
	}

    public static int indexOfIntcol(Record[] data, int key, int low, int high) {
    	low = low < 0? 0 : low; 
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    int midVal = data[mid].intcol;
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
	public Cursor cursorInIntcolRange(int lower, int upper) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		long high = size() - 1;

		long start = indexOfIntcol(lower, channel, 0, high);
		long stop = indexOfIntcol(upper, channel, 0, high);
		return new Cursor(raf, channel, start, stop, _bufferSize);
	}
	
	public long indexOfIntcol(int key, long low, long high) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		try {
			long i = indexOfIntcol(key, channel, low, high);
			return i;
		} finally {
			raf.close();
			channel.close();
		}
	}
	
	public long indexOfIntcol(int key) throws IOException {
		return indexOfIntcol(key, 0, size() - 1);
	}
	
	private long indexOfIntcol(int key, FileChannel channel, long low, long high) throws IOException {
		low = low < 0? 0 : low;
		synchronized (_rbuf) {
			if (_rbufSize > 0 && (low >= _rbufPos || high >= _rbufPos)) {
				Record r = _rbuf[0];
				if ((r.intcol == key ? 0 
						: (r.intcol < key ? -1 : 1)) <= 0) {
					/* search in memory */
					return _rbufPos + indexOfIntcol(_rbuf, key, 0, (int) Math.min(high - _rbufPos, _rbufSize - 1));
				}
			}
		}
		
		/* search in file */
	
		high = high >= _rbufPos ? (_rbufPos == 0? 0 : _rbufPos - 1) : high;
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
				
		while (low <= high) {
			final long mid = (low + high) >>> 1;
				
			channel.position(mid * 16 + 12);
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

	public void truncateIntcol(int intcol) throws IOException {
		if (size() > 0) {
			long len = indexOfIntcol(intcol);
			if (len > 0) {
				len--;
			}
			Cursor c = cursor(len);
			while (c.next()) {
				if (c.intcol > intcol) {
					break;
				}
				len++;
			}
			c.close();
			truncate(len);
		}
	}
	
	public Record findRecord_where_intcol_is(int intcol) throws IOException {
		Record r = null;
		if (size() > 0) {
			long i = indexOfIntcol(intcol);
			r = record(i);
			if (r.intcol != intcol) {
				r = null;
			}
		}
		return r;
	}
	
	public long countIntcol(int keyLower, int keyUpper) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		try {
			long high = size() - 1;
			long a = indexOfIntcol(keyLower, channel, 0L, high);
			long b = indexOfIntcol(keyUpper, channel, 0L, high);
			return b - a;
		} finally {
			raf.close();
			channel.close();
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

	
	public static final String[] COLUMNS_NAME = { "str", "intcol" };
	public static final Class<?>[] COLUMNS_TYPE = { java.lang.String.class, int.class };
	public static final int[] COLUMNS_SIZE = { 12, 4 };
	public static final boolean[] COLUMNS_IS_VIRTUAL = { false, false };
	public static final int COLUMN_STR = 0;
	public static final int COLUMN_INTCOL = 1;


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
			_channel.position(start * 16);
			_buffer = ByteBuffer.allocate(bufferSize * 16);
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
				this.str = r.str;
				this.intcol = r.intcol;

					_row ++;
					return true;	
				}
			} 
			
			if (_buffer.position() == _buffer.capacity()) {
				_buffer.rewind();
				_channel.read(_buffer);
				_buffer.rewind();
			}
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __str = new byte[byteLen / 1];
				for (int i = 0; i < __str.length; i++) {
					__str[i] = arrayBuf.get();
				}
				this.str = new String(__str);
				}
				this.intcol = _buffer.getInt();

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
			_buffer = ByteBuffer.allocate(16);
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
				this.str = r.str;
				this.intcol = r.intcol;

					return;					
				}
			} 
			
			_row = position;
			_buffer.rewind();
			_channel.read(_buffer, position * 16);
			_buffer.rewind();
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __str = new byte[byteLen / 1];
				for (int i = 0; i < __str.length; i++) {
					__str[i] = arrayBuf.get();
				}
				this.str = new String(__str);
				}
				this.intcol = _buffer.getInt();
			
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
			_buffer = ByteBuffer.allocate(16);
			
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
				this.str = r.str;
				this.intcol = r.intcol;

					_row += _step;
					return true;
				}
			}
			
			_buffer.rewind();
			_channel.read(_buffer, _row * 16);
			_buffer.rewind();
				{
				long start = _buffer.getLong();
				int byteLen = _buffer.getInt();
				ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[byteLen]);
				_arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				byte[] __str = new byte[byteLen / 1];
				for (int i = 0; i < __str.length; i++) {
					__str[i] = arrayBuf.get();
				}
				this.str = new String(__str);
				}
				this.intcol = _buffer.getInt();
 
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


	public Record[] select__where_Intcol_in(int lower, int upper) throws IOException {
		long start = indexOfIntcol(lower) - 1;

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
		
			    if (c.intcol > upper) {
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
	
	public Record[] sselect__where_Intcol_in(int lower, int upper, int maxLen) throws IOException {
		long start = indexOfIntcol(lower) - 1;
		long stop = indexOfIntcol(upper) + 1;
		
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
		
			    if (c.intcol > upper) {
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
	
	public Record[] iselect__where_Intcol_in(int lower, int upper, int maxLen) throws IOException {
		
		long start = indexOfIntcol(lower); 
		long stop = indexOfIntcol(upper);
		
		if (stop - start > maxLen) {
			return sselect__where_Intcol_in(lower, upper, maxLen);
		} else {
			return select__where_Intcol_in(lower, upper);
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
		return new Cursor(raf, channel, start, raf.length() / 16 + _rbufSize - 1, _bufferSize);
	} 
	
	public Cursor cursor() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new Cursor(raf, channel, 0, raf.length() / 16 + _rbufSize - 1, _bufferSize);
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
		return new SparseCursor(raf, channel, start, raf.length() / 16 + _rbufSize - 1, _bufferSize, maxLen);
	} 
	
	public SparseCursor scursor(int maxLen) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(_file.getAbsolutePath(), "r");
		FileChannel channel = raf.getChannel();
		return new SparseCursor(raf, channel, 0, raf.length() / 16 + _rbufSize - 1, _bufferSize, maxLen);
	}


// Appender

	public final class Appender extends Record implements IAppender {
		private final RandomAccessFile _raf;
		private final FileChannel _channel;
		private final TableWithStringMDB _mdb;	
		private final ByteBuffer _buf;	 
		private RandomAccessFile _arrayRaf;
		private FileChannel _arrayChannel;
		private long _start;

		 
		private Appender(TableWithStringMDB mdb, RandomAccessFile raf, FileChannel channel, int bufferSize) throws IOException {
			_mdb = mdb;
			_raf = raf;
			_channel = channel;
			_channel.position(_raf.length());
			_buf = ByteBuffer.allocate(bufferSize * 16);
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
						{
							byte[] __str = r.str == null? null : r.str.getBytes();
							int len = __str == null? 0 : __str.length;
							_buf.putLong(_arrayChannel.position());
							_buf.putInt(len * 1);
							if (len > 0) {
								ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * 1]);
								for (int i = 0; i < len; i++) {
									arrayBuf.put(__str[i]);
								}
								arrayBuf.rewind();
								_arrayChannel.write(arrayBuf);
							}
						}
						_buf.putInt(r.intcol);
 						 						
					}
					_buf.rewind();
					_buf.limit(_rbufSize * 16);
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
		
		public TableWithStringMDB getMDB() {
			return _mdb;
		}
	}	
	
	public TableWithStringMDB(File file, File arrayFile, int bufferSize) throws IOException {
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
	public TableWithStringMDB(File file, File arrayFile) throws IOException {
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
		return "TableWithString - " + _file.getName();
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
		ByteBuffer buffer = ByteBuffer.allocate(16);
		FileChannel channel = raf.getChannel();
		channel.position(index * 16);
		channel.read(buffer);
		buffer.rewind();
			RandomAccessFile arrayRaf = new RandomAccessFile(_arrayFile,"rw");
			FileChannel arrayChannel = arrayRaf.getChannel();
			
		Record r = new Record();
			{
				long start = buffer.getLong();
				arrayChannel.position(start);
				int byteLen = buffer.getInt();
				byte[] __str = new byte[byteLen / 1];
				ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);
				arrayChannel.read(arrayBuf);
				arrayBuf.rewind();
				for (int i = 0; i < __str.length; i++) {
					__str[i] = arrayBuf.get();
				}
				r.str = new String(__str);
		}
		r.intcol = buffer.getInt();
	
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
		return _file.length() / 16;
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
			return _file.length() / 16 + _rbufSize;
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
