package org.mfg.mdb.demo;

import java.io.File;
import java.io.IOException;
import org.mfg.mdb.runtime.*;
import java.util.*;

/* BEGIN USER IMPORTS */
/* User can insert his code here */
/* END USER IMPORTS */

public class Demo_02_MDBSession
/* BEGIN USER SESSION HINERITANCE */
extends MDBSession
/* END USER SESSION HINERITANCE */ 		{
 	
 	private static final Map<String,String> SIGNATURES;
 	private static final String SCHEMA_JSON = "{\"name\":\"Demo_02_\",\"source\":\"\",\"packageName\":\"mdb\",\"bufferSize\":100,\"tables\":[{\"uuid\":\"f86bff88-e5d9-4480-ba16-17d4f2bf0256\",\"name\":\"Demo_02_Price\",\"columns\":[{\"name\":\"time\",\"uuid\":\"8b33f637-4a6a-49a3-a8a0-f28c1d387244\",\"type\":\"INTEGER\",\"order\":\"ASCENDING\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"price\",\"uuid\":\"8bb96fd0-fd73-40bb-b7b4-51f36ec3fc88\",\"type\":\"LONG\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"}]}]}";
 	
 	static {
 		SIGNATURES = new HashMap<>();
		SIGNATURES.put("f86bff88-e5d9-4480-ba16-17d4f2bf0256", "8b33f637-4a6a-49a3-a8a0-f28c1d387244 INTEGER; 8bb96fd0-fd73-40bb-b7b4-51f36ec3fc88 LONG; ");
 	}
 	
	public Demo_02_MDBSession(String sessionName, File root) throws IOException {
		super(sessionName, root, SIGNATURES, SCHEMA_JSON);
	}
	
	public Demo_02_MDBSession(String sessionName, File root, SessionMode mode) throws IOException {
		super(sessionName, root, mode, SIGNATURES, SCHEMA_JSON);
	}

/* BEGIN USER SESSION CODE */

	/* END USER SESSION CODE */	

	private Demo_02_PriceMDB internal_connectTo_Demo_02_PriceMDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Demo_02_PriceMDB) _cache.get(file);
			}
			Demo_02_PriceMDB mdb = new Demo_02_PriceMDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "f86bff88-e5d9-4480-ba16-17d4f2bf0256");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Demo_02_PriceMDB connectTo_Demo_02_PriceMDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Demo_02_PriceMDB(getFile(filename), bufferSize);
	}
	
	public Demo_02_PriceMDB connectTo_Demo_02_PriceMDB(String filename) throws IOException {
		return connectTo_Demo_02_PriceMDB(filename, 100);
	}
}

