package org.mfg.mdb.compiler.internal.emitters;

import org.mfg.mdb.compiler.*;
import static org.mfg.mya.Printer.*;

import java.io.IOException;

 /**
  * Generator of the session Java file. 
  * emitter.print();author arian
  *
  */
public class SessionClassGenerator implements IExtensionTargets {
	/**
	 * The emitter to use.
	 */
	public OpenPrinter emitter;
	/**
	  * Schema
	  */
	public Schema schema;


	/**
	  * The name of the package 
	  */
	public String pkgname;

	/*
	 * @throws IOException I/O error.
	 */
	private void userText(String openTag, String closeTag, String def) throws IOException {
		emitter.userText(openTag, closeTag, def);
	}

	private void emitExt(String extPoint) {
		emitter.emitExt(extPoint, null);
	}


	/**
	 * @throws IOException If there is any I/O error.
	 */
	public void run() throws IOException {
emitter.print("package " + pkgname + ";\n");
emitter.print("\n");
emitter.print("import java.io.File;\n");
emitter.print("import java.io.IOException;\n");
emitter.print("import org.mfg.mdb.runtime.*;\n");
emitter.print("import java.util.*;\n");
emitter.print("\n");
userText("/* BEGIN USER IMPORTS */", "/* END USER IMPORTS */", "\n/* User can insert his code here */\n");
emitter.print("\n");
emitter.print("\n");
 String schemaName = schema.getName();
emitter.print("public class " + schemaName + "MDBSession\n");
 userText("/* BEGIN USER SESSION HINERITANCE */", "/* END USER SESSION HINERITANCE */", "\n\t\textends MDBSession\n");
emitter.print(" 		{\n");
emitter.print(" 	\n");
emitter.print(" 	private static final Map<String,String> SIGNATURES;\n");
emitter.print(" 	private static final String SCHEMA_JSON = \"" + escape(schema.toJSONString()) + "\";\n");
emitter.print(" 	\n");
emitter.print(" 	static {\n");
emitter.print(" 		SIGNATURES = new HashMap<>();\n");
 for(Table t : schema) {		
emitter.print("		SIGNATURES.put(\"" + t.getUUID().toString() + "\", \"" + t.computeSignature() + "\");\n");
	}	
emitter.print(" 	}\n");
emitter.print(" 	\n");
emitter.print("	public " + schemaName + "MDBSession(String sessionName, File root) throws IOException {\n");
emitter.print("		super(sessionName, root, SIGNATURES, SCHEMA_JSON);\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	public " + schemaName + "MDBSession(String sessionName, File root, SessionMode mode) throws IOException {\n");
emitter.print("		super(sessionName, root, mode, SIGNATURES, SCHEMA_JSON);\n");
emitter.print("	}\n");
emitter.print("\n");
 userText("/* BEGIN USER SESSION CODE */", "/* END USER SESSION CODE */", "\n/* The user can write his code here */\n");	
emitter.print("	\n");
	for(Table table : schema) {
		String tableName = table.getName();
		boolean hasArray = table.hasArray();
emitter.print("\n");
 if (hasArray) {
emitter.print("	private " + tableName + "MDB internal_connectTo_" + tableName + "MDB(File file, File arrayFile, int bufferSize) throws IOException {\n");
emitter.print("	\n");
emitter.print("		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) \n");
emitter.print("			throw new IllegalArgumentException(\"The file is not inside the database folder: \" + file);\n");
emitter.print("		if (!arrayFile.getAbsolutePath().contains(getRoot().getAbsolutePath())) \n");
emitter.print("			throw new IllegalArgumentException(\"The array file is not inside the database folder: \" + arrayFile);\n");
emitter.print("		if (!arrayFile.getParentFile().equals(file.getParentFile())) \n");
emitter.print("			throw new IllegalArgumentException(\"The array file is not in the same folder of the main file: \" + arrayFile);\n");
emitter.print("		if (!arrayFile.getPath().equals(file.getPath() + \".array\"))\n");
emitter.print("			throw new IllegalArgumentException(\"Illegal array file name: \" + arrayFile);\n");
 } else {
emitter.print("	private " + tableName + "MDB internal_connectTo_" + tableName + "MDB(File file, int bufferSize) throws IOException {\n");
emitter.print("	\n");
emitter.print("		if (!file.getAbsolutePath().contains(getRoot().getAbsolutePath())) \n");
emitter.print("			throw new IllegalArgumentException(\"The file is not inside the database folder: \" + file);\n");
	}
emitter.print("	\n");
emitter.print("		_readLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			if (!_open) {\n");
emitter.print("				throw new IOException(\"Trying to access a closed session.\");\n");
emitter.print("			}\n");
emitter.print("				\n");
emitter.print("			if (_cache.containsKey(file)) {\n");
emitter.print("				return (" + tableName + "MDB) _cache.get(file);\n");
emitter.print("			}\n");
 if (hasArray) emitter.print("			" + tableName + "MDB mdb = new " + tableName + "MDB(this, file, arrayFile, bufferSize, getMode()); \n");
 else emitter.print("			" + tableName + "MDB mdb = new " + tableName + "MDB(this, file, bufferSize, getMode()); \n");		
emitter.print("			_cache.put(file, mdb);\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				createFileMetadata(file, \"" + table.getUUID().toString() + "\");\n");
emitter.print("			}\n");
emitter.print("			return mdb;\n");
emitter.print("		} finally {\n");
emitter.print("			_readLock.unlock();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	public " + tableName + "MDB connectTo_" + tableName + "MDB(String filename, int bufferSize) throws IOException {	\n");
		if(hasArray) {	
emitter.print("		return internal_connectTo_" + tableName + "MDB(getFile(filename), getFile(filename + \".array\"), bufferSize);\n");
		} else {
emitter.print("		return internal_connectTo_" + tableName + "MDB(getFile(filename), bufferSize);\n");
		}
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	public " + tableName + "MDB connectTo_" + tableName + "MDB(String filename) throws IOException {\n");
emitter.print("		return connectTo_" + tableName + "MDB(filename, 100);\n");
emitter.print("	}\n");
	}
	emitExt(TARGET_SESSION_CLASS);	
emitter.print("}\n");
emitter.print("\n");
	}
}