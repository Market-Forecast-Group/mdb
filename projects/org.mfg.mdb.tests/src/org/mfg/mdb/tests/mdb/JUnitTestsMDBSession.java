package org.mfg.mdb.tests.mdb;

import java.io.File;
import java.io.IOException;
import org.mfg.mdb.runtime.*;
import java.util.*;

/* BEGIN USER IMPORTS */
/* User can insert his code here */
/* END USER IMPORTS */

public class JUnitTestsMDBSession
/* BEGIN USER SESSION HINERITANCE */
		extends MDBSession
/* END USER SESSION HINERITANCE */ 		{
 	
 	private static final Map<String,String> SIGNATURES;
 	private static final String SCHEMA_JSON = "{\"name\":\"JUnitTests\",\"source\":\"\",\"packageName\":\"mdb\",\"bufferSize\":100,\"tables\":[{\"uuid\":\"8c105b1f-cc0f-477e-90d1-e1515968def2\",\"name\":\"Table1\",\"columns\":[{\"name\":\"double_0\",\"uuid\":\"18e55429-d12a-45b5-aced-cb992faf27a8\",\"type\":\"DOUBLE\",\"order\":\"ASCENDING\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"int_0\",\"uuid\":\"df315e29-4287-4d36-a9fa-bca9397987cc\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"array_int_1\",\"uuid\":\"c9e5a6b0-aff3-40b5-8c86-d541827a23f1\",\"type\":\"ARRAY_INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"}]},{\"uuid\":\"adfc3fff-4690-4120-89c1-f35dc46ee974\",\"name\":\"Table2\",\"columns\":[{\"name\":\"double_0\",\"uuid\":\"db691557-1d86-4a8b-af41-0c83dd6d955a\",\"type\":\"DOUBLE\",\"order\":\"ASCENDING\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"int_0\",\"uuid\":\"df4550f7-ec50-4ed6-b5c7-5ce3f91fa5f1\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"}]},{\"uuid\":\"30ff641d-1bcb-4d52-b53f-a6c885206c58\",\"name\":\"Person\",\"columns\":[{\"name\":\"age\",\"uuid\":\"8263ce11-c736-4227-9324-050f07a20082\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"phone\",\"uuid\":\"0466fd5d-ce61-41bb-8272-fdac107eaca3\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"name\",\"uuid\":\"3eb2417b-1d27-4d76-8fce-cd4167c4f61e\",\"type\":\"STRING\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"}]},{\"uuid\":\"4d969148-98d9-470d-ac1f-ade5050e9474\",\"name\":\"Price\",\"columns\":[{\"name\":\"fakeTime\",\"uuid\":\"2e7671a9-ef40-446d-b9ec-2d8701e0a88f\",\"type\":\"LONG\",\"order\":\"ASCENDING\",\"virtual\":true,\"formula\":\"$pos$\"},{\"name\":\"realTime\",\"uuid\":\"a6fd3e71-0b74-4912-8a9c-b2f6982aac65\",\"type\":\"LONG\",\"order\":\"ASCENDING\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"rawPrice\",\"uuid\":\"00bfa439-06b6-4785-84de-33094815b316\",\"type\":\"LONG\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"price\",\"uuid\":\"93f1b876-2da0-4cf3-96ba-94d0b14287d3\",\"type\":\"LONG\",\"order\":\"NONE\",\"virtual\":true,\"formula\":\"Math.abs($$.rawPrice)\"},{\"name\":\"real\",\"uuid\":\"fae8cd4e-b1dc-449d-997e-cd62568aa1b5\",\"type\":\"BOOLEAN\",\"order\":\"NONE\",\"virtual\":true,\"formula\":\"$$.rawPrice >= 0\"}]},{\"uuid\":\"ea0ef3de-d38e-4690-b130-991bf7ddd932\",\"name\":\"Table3\",\"columns\":[{\"name\":\"index\",\"uuid\":\"3623b288-7e5b-4a73-857d-0e950473ca71\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"virtual_array\",\"uuid\":\"08234fa2-17d9-41ae-9d52-31f5569f900b\",\"type\":\"ARRAY_INTEGER\",\"order\":\"NONE\",\"virtual\":true,\"formula\":\"createVirtualArray($pos$)\"},{\"name\":\"virtual_string\",\"uuid\":\"226c3d4f-64e1-4212-b0f7-e1fff0e68df2\",\"type\":\"STRING\",\"order\":\"NONE\",\"virtual\":true,\"formula\":\"createVirtualString($pos$)\"}]},{\"uuid\":\"198733bc-715a-45f7-aeac-d06a40a31fda\",\"name\":\"Table4\",\"columns\":[{\"name\":\"num\",\"uuid\":\"25237234-4911-47ec-a63f-d28f6dc42125\",\"type\":\"BYTE\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"flag\",\"uuid\":\"13010d24-d152-42cd-b2c8-42d915725a76\",\"type\":\"BOOLEAN\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"}]},{\"uuid\":\"f38e9e96-598e-4d2b-8c29-84e2ee4478c7\",\"name\":\"Table5\",\"columns\":[{\"name\":\"price\",\"uuid\":\"8a5a62e1-e924-4f02-87ae-dd20b4280b93\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":false,\"formula\":\"\"},{\"name\":\"vol\",\"uuid\":\"4108a267-3a20-4070-95e5-f4907b5bdd87\",\"type\":\"INTEGER\",\"order\":\"NONE\",\"virtual\":true,\"formula\":\"10\"}]}]}";
 	
 	static {
 		SIGNATURES = new HashMap<>();
		SIGNATURES.put("8c105b1f-cc0f-477e-90d1-e1515968def2", "18e55429-d12a-45b5-aced-cb992faf27a8 DOUBLE; df315e29-4287-4d36-a9fa-bca9397987cc INTEGER; c9e5a6b0-aff3-40b5-8c86-d541827a23f1 ARRAY_INTEGER; ");
		SIGNATURES.put("adfc3fff-4690-4120-89c1-f35dc46ee974", "db691557-1d86-4a8b-af41-0c83dd6d955a DOUBLE; df4550f7-ec50-4ed6-b5c7-5ce3f91fa5f1 INTEGER; ");
		SIGNATURES.put("30ff641d-1bcb-4d52-b53f-a6c885206c58", "8263ce11-c736-4227-9324-050f07a20082 INTEGER; 0466fd5d-ce61-41bb-8272-fdac107eaca3 INTEGER; 3eb2417b-1d27-4d76-8fce-cd4167c4f61e STRING; ");
		SIGNATURES.put("4d969148-98d9-470d-ac1f-ade5050e9474", "a6fd3e71-0b74-4912-8a9c-b2f6982aac65 LONG; 00bfa439-06b6-4785-84de-33094815b316 LONG; ");
		SIGNATURES.put("ea0ef3de-d38e-4690-b130-991bf7ddd932", "3623b288-7e5b-4a73-857d-0e950473ca71 INTEGER; ");
		SIGNATURES.put("198733bc-715a-45f7-aeac-d06a40a31fda", "25237234-4911-47ec-a63f-d28f6dc42125 BYTE; 13010d24-d152-42cd-b2c8-42d915725a76 BOOLEAN; ");
		SIGNATURES.put("f38e9e96-598e-4d2b-8c29-84e2ee4478c7", "8a5a62e1-e924-4f02-87ae-dd20b4280b93 INTEGER; ");
 	}
 	
	public JUnitTestsMDBSession(String sessionName, File root) throws IOException {
		super(sessionName, root, SIGNATURES, SCHEMA_JSON);
	}
	
	public JUnitTestsMDBSession(String sessionName, File root, SessionMode mode) throws IOException {
		super(sessionName, root, mode, SIGNATURES, SCHEMA_JSON);
	}

/* BEGIN USER SESSION CODE */
/* The user can write his code here */
/* END USER SESSION CODE */	

	private Table1MDB internal_connectTo_Table1MDB(File file, File arrayFile, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
		if (!arrayFile.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The array file is not inside the database folder: " + arrayFile);
		if (!arrayFile.getParentFile().equals(file.getParentFile())) 
			throw new IllegalArgumentException("The array file is not in the same folder of the main file: " + arrayFile);
		if (!arrayFile.getPath().equals(file.getPath() + ".array"))
			throw new IllegalArgumentException("Illegal array file name: " + arrayFile);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Table1MDB) _cache.get(file);
			}
			Table1MDB mdb = new Table1MDB(this, file, arrayFile, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "8c105b1f-cc0f-477e-90d1-e1515968def2");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Table1MDB connectTo_Table1MDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Table1MDB(getFile(filename), getFile(filename + ".array"), bufferSize);
	}
	
	public Table1MDB connectTo_Table1MDB(String filename) throws IOException {
		return connectTo_Table1MDB(filename, 100);
	}

	private Table2MDB internal_connectTo_Table2MDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Table2MDB) _cache.get(file);
			}
			Table2MDB mdb = new Table2MDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "adfc3fff-4690-4120-89c1-f35dc46ee974");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Table2MDB connectTo_Table2MDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Table2MDB(getFile(filename), bufferSize);
	}
	
	public Table2MDB connectTo_Table2MDB(String filename) throws IOException {
		return connectTo_Table2MDB(filename, 100);
	}

	private PersonMDB internal_connectTo_PersonMDB(File file, File arrayFile, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
		if (!arrayFile.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The array file is not inside the database folder: " + arrayFile);
		if (!arrayFile.getParentFile().equals(file.getParentFile())) 
			throw new IllegalArgumentException("The array file is not in the same folder of the main file: " + arrayFile);
		if (!arrayFile.getPath().equals(file.getPath() + ".array"))
			throw new IllegalArgumentException("Illegal array file name: " + arrayFile);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (PersonMDB) _cache.get(file);
			}
			PersonMDB mdb = new PersonMDB(this, file, arrayFile, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "30ff641d-1bcb-4d52-b53f-a6c885206c58");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public PersonMDB connectTo_PersonMDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_PersonMDB(getFile(filename), getFile(filename + ".array"), bufferSize);
	}
	
	public PersonMDB connectTo_PersonMDB(String filename) throws IOException {
		return connectTo_PersonMDB(filename, 100);
	}

	private PriceMDB internal_connectTo_PriceMDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (PriceMDB) _cache.get(file);
			}
			PriceMDB mdb = new PriceMDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "4d969148-98d9-470d-ac1f-ade5050e9474");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public PriceMDB connectTo_PriceMDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_PriceMDB(getFile(filename), bufferSize);
	}
	
	public PriceMDB connectTo_PriceMDB(String filename) throws IOException {
		return connectTo_PriceMDB(filename, 100);
	}

	private Table3MDB internal_connectTo_Table3MDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Table3MDB) _cache.get(file);
			}
			Table3MDB mdb = new Table3MDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "ea0ef3de-d38e-4690-b130-991bf7ddd932");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Table3MDB connectTo_Table3MDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Table3MDB(getFile(filename), bufferSize);
	}
	
	public Table3MDB connectTo_Table3MDB(String filename) throws IOException {
		return connectTo_Table3MDB(filename, 100);
	}

	private Table4MDB internal_connectTo_Table4MDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Table4MDB) _cache.get(file);
			}
			Table4MDB mdb = new Table4MDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "198733bc-715a-45f7-aeac-d06a40a31fda");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Table4MDB connectTo_Table4MDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Table4MDB(getFile(filename), bufferSize);
	}
	
	public Table4MDB connectTo_Table4MDB(String filename) throws IOException {
		return connectTo_Table4MDB(filename, 100);
	}

	private Table5MDB internal_connectTo_Table5MDB(File file, int bufferSize) throws IOException {
	
		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) 
			throw new IllegalArgumentException("The file is not inside the database folder: " + file);
	
		_readLock.lock();
		try {
			if (!_open) {
				throw new IOException("Trying to access a closed session.");
			}
				
			if (_cache.containsKey(file)) {
				return (Table5MDB) _cache.get(file);
			}
			Table5MDB mdb = new Table5MDB(this, file, bufferSize, getMode()); 
			_cache.put(file, mdb);
			if (!_memory) {
				createFileMetadata(file, "f38e9e96-598e-4d2b-8c29-84e2ee4478c7");
			}
			return mdb;
		} finally {
			_readLock.unlock();
		}
	}

	public Table5MDB connectTo_Table5MDB(String filename, int bufferSize) throws IOException {	
		return internal_connectTo_Table5MDB(getFile(filename), bufferSize);
	}
	
	public Table5MDB connectTo_Table5MDB(String filename) throws IOException {
		return connectTo_Table5MDB(filename, 100);
	}
	private long modificationToken = 0;

	public void modified() {
		modificationToken++;
	}

	public long getModificatonToken() {
		return modificationToken;
	}
}

