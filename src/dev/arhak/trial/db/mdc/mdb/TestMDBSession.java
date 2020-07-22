package dev.arhak.trial.db.mdc.mdb;

import java.io.File;
import java.io.IOException;
import org.mfg.mdb.*;

public class TestMDBSession extends AbstractMDBSession {
	public TestMDBSession(String sessionName, File root) throws IOException {
		super(sessionName, root);
	}
	
	public TestMDBSession(String sessionName, File root, SessionMode mode) throws IOException {
		super(sessionName, root, mode);
	}
	
	public TableWithArrayMDB connectTo_TableWithArrayMDB(File file, File arrayFile, int bufferSize) throws IOException {
		assert _open : "The session must to be open.";
		if (_cache.containsKey(file)) {
			return (TableWithArrayMDB) _cache.get(file);
		}
	    
		TableWithArrayMDB mdb = new TableWithArrayMDB(file, arrayFile, bufferSize);
		mdb.setSession(this);
		_cache.put(file, mdb);
	    
		return mdb;
	}
	
	public TableWithArrayMDB connectTo_TableWithArrayMDB(File file, File arrayFile) throws IOException {
		return connectTo_TableWithArrayMDB(file, arrayFile, 100);
	}

	public TableWithStringMDB connectTo_TableWithStringMDB(File file, File arrayFile, int bufferSize) throws IOException {
		assert _open : "The session must to be open.";
		if (_cache.containsKey(file)) {
			return (TableWithStringMDB) _cache.get(file);
		}
	    
		TableWithStringMDB mdb = new TableWithStringMDB(file, arrayFile, bufferSize);
		mdb.setSession(this);
		_cache.put(file, mdb);
	    
		return mdb;
	}
	
	public TableWithStringMDB connectTo_TableWithStringMDB(File file, File arrayFile) throws IOException {
		return connectTo_TableWithStringMDB(file, arrayFile, 100);
	}

	public TestSparseCursorMDB connectTo_TestSparseCursorMDB(File file, int bufferSize) throws IOException {
		assert _open : "The session must to be open.";
		if (_cache.containsKey(file)) {
			return (TestSparseCursorMDB) _cache.get(file);
		}
	    
		TestSparseCursorMDB mdb = new TestSparseCursorMDB(file, bufferSize);
		mdb.setSession(this);
		_cache.put(file, mdb);
	    
		return mdb;
	}
	
	public TestSparseCursorMDB connectTo_TestSparseCursorMDB(File file) throws IOException {
		return connectTo_TestSparseCursorMDB(file, 100);
	}


}
