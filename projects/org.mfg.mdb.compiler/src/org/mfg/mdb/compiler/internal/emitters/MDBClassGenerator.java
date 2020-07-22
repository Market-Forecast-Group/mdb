package org.mfg.mdb.compiler.internal.emitters;

import org.mfg.mdb.compiler.*;
import static org.mfg.mya.Printer.*;

import java.io.IOException;

 /**
  * Generator of the session Java file. 
  * emitter.print();author arian
  *
  */
public class MDBClassGenerator implements IExtensionTargets {
	/**
	 * The emitter to use.
	 */
	public OpenPrinter emitter;

	/**
	  * Schema
	  */
	public Schema schema;
	/**
	  * Biffer size
	  */
	public int bufferSize;

	/**
	  * Table
	  */
	public Table table;


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
		emitter.emitExt(extPoint, table);
	}

	/**
	 * <p>
	 * This is not more than other way to replace some texts in a string. The
	 * name "expandExpr" comes from the fact that this is used mainly to expand
	 * a column formula {emitter.print();link Column#getFormula()} into a real Java expression.
	 * </p>
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * expandExpr(&quot;$$.price * $pos$&quot;, &quot;$$&quot;, &quot;this&quot;, &quot;$pos$&quot;, &quot;_row&quot;);
	 * </pre>
	 * 
	 * Is the same of:
	 * 
	 * <pre>
	 * &quot;$$.price * $pos$&quot;.replace(&quot;$$&quot;, &quot;this&quot;).replace(&quot;$pos$&quot;, &quot;_row&quot;);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * emitter.print();param expr
	 *            The expression with the "keywords".
	 * emitter.print();param attrs
	 *            A map of the keywords with the real values.
	 * emitter.print();return The expression after the expansion.
	 */
	private static String expandExpr(final String expr, final String... attrs) {
		String expand = expr;
		for (int i = 0; i < attrs.length / 2; i++) {
			final String key = attrs[i * 2];
			final String val = attrs[i * 2 + 1];
			expand = expand.replace(key, val);
		}
		return expand;
	}

	/**
	 * @throws IOException If there is any I/O error.
	 */
	public void run() throws IOException {
 	String tableName = table.getName();
  String schemaName = table.getSchema().getName();
 	boolean hasArray = table.hasArray();
	boolean hasString = table.hasString();
 	boolean hasVirtualColumns = table.hasVirtualColumns();
	int rowSize = table.getRowSize();
emitter.print("package " + pkgname + ";\n");
emitter.print("\n");
emitter.print("import java.io.IOException;\n");
emitter.print("import java.nio.channels.FileChannel;\n");
emitter.print("import java.io.FileNotFoundException;\n");
emitter.print("import java.io.RandomAccessFile;\n");
emitter.print("import java.io.File;\n");
emitter.print("import java.nio.ByteBuffer;\n");
emitter.print("import java.util.concurrent.atomic.AtomicInteger;\n");
emitter.print("import org.mfg.mdb.runtime.*;\n");
userText("/* BEGIN USER IMPORTS */", "/* END USER IMPORTS */", "\n/* User can insert his code here */\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("/**\n");
emitter.print(" * <p>\n");
emitter.print(" * This class provides the API to manipulate " + tableName + " files. \n");
emitter.print(" * Here you will find the methods to modify and query the " + tableName + " files. \n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * An MDB file does not contain any meta-data, it is just raw data, \n");
emitter.print(" * one record next to the other, every sinlge byte is part of the data,\n");
emitter.print(" * however, this class contains the required information to \"understand\"\n");
emitter.print(" * the files format. Important, do not try to access files created \n");
emitter.print(" * by other classes because you will get an unexpected behavior and corrupted data.\n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * This is the schema this \"driver\" class understands:\n");
emitter.print(" * </p>\n");
emitter.print(" * <h3>" + tableName + " definition</h3>\n");
emitter.print(" * <table border=1>\n");
emitter.print(" *	<caption>" + tableName + "</caption>\n");
emitter.print(" *	<tr>\n");
emitter.print(" *		<td>Column</td>\n");
emitter.print(" *		<td>Type</td>\n");
emitter.print(" *		<td>Order</td>\n");
emitter.print(" *		<td>Virtual</td>\n");
emitter.print(" *		<td>Formula</td>\n");
emitter.print(" *	</tr>\n");
for(Column col : table) {
emitter.print(" * <tr>\n");
emitter.print(" *		<td>" + col.getName() + "</td>\n");
emitter.print(" *		<td>" + col.getType() + "</td>\n");
emitter.print(" *		<td>" + col.getOrder() + "</td>\n");
emitter.print(" *		<td>" + (col.isVirtual()? "Yes" : "No") + "</td>\n");
emitter.print(" *		<td>" + col.getFormula() + "</td>\n");
emitter.print(" *	</tr>\n");
}	
emitter.print(" * </table>\n");
emitter.print(" * <h3>" + tableName + "MDB API</h3>\n");
emitter.print(" * <p>\n");
emitter.print(" * Now let's see the operations you can perform using this class on " + tableName + " files:\n");
emitter.print(" * </p>\n");
emitter.print(" *\n");
emitter.print(" * <h3>Data Insertion</h3>\n");
emitter.print(" *\n");
emitter.print(" * <p>\n");
emitter.print(" * As you know, in MDB you cannot insert a record in the middle of the file, all the data you add to the file is at the end of it, for that reason\n");
emitter.print(" * we named {@link Appender} to the component in charge of this function. So to add data to the file you have to request an appender:\n");
emitter.print(" * </p>\n");
emitter.print(" * <pre>\n");
emitter.print(" * // To connect to an MDB file, the best is to use\n");
emitter.print(" * // the \"connect\" method of the session.\n");
emitter.print(" * // In the next examples, we will asume you know it.\n");
emitter.print(" * \n");
emitter.print(" * " + schemaName + "MDBSession session = ...;\n");
emitter.print(" * " + tableName + "MDB mdb = session.connectTo_" + tableName + "MDB(\"" + tableName.toLowerCase() + ".mdb\");\n");
emitter.print(" * \n");
emitter.print(" * // request the appender.\n");
emitter.print(" * " + tableName + "MDB.Appender app = mdb.appender(); \n");
emitter.print(" *\n");
emitter.print(" * // set the appender values\n");
 for(Column col : table) if (!col.isVirtual()) {
emitter.print(" * app." + col.getName() + " = ...;\n");
 }
 if (table.hasVirtualColumns()) {
emitter.print(" *\n");
emitter.print(" * // ignore the following virtual columns, they are computed automatically:\n");
emitter.print(" * //\n");
 	for(Column col : table) if (col.isVirtual()) {
emitter.print(" * // app." + col.getName() + " == \"" + col.getFormula() + "\"\n");
 	}
 }
emitter.print("\n");
emitter.print(" * // add the record\n");
emitter.print(" * app.append();\n");
emitter.print(" *\n");
emitter.print(" * ...\n");
emitter.print(" *\n");
emitter.print(" * // You can repeat this operation for each item you want to add to the file.\n");
emitter.print(" * // Important, is possible that some of these records are yet in the memory buffer\n");
emitter.print(" * // so to writes them to the file, you have to flush the appender:\n");
emitter.print(" *\n");
emitter.print(" * app.flush();\n");
emitter.print(" *\n");
emitter.print(" * ...\n");
emitter.print(" *\n");
emitter.print(" * // When you are sure you do not want to add new records, close the appender.\n");
emitter.print(" * // The close method also write the pending records to the disk, \n");
emitter.print(" * // so it is not needed to call the flush method.\n");
emitter.print(" *\n");
emitter.print(" * app.close();\n");
emitter.print(" * </pre>\n");
emitter.print(" * <p>\n");
emitter.print(" * So use the method {@link #appender()} to get the appender, and use the \n");
emitter.print(" * methods {@link Appender#append()}, {@link Appender#append(Record)} and \n");
emitter.print(" * {@link Appender#append_ref_unsafe(Record)} to add the records.\n");
emitter.print(" * </p>\n");
emitter.print(" *\n");
emitter.print(" * <h3>Data Query</h3>\n");
emitter.print(" * <p>\n");
emitter.print(" * We provide different APIs to retrieve data: cursors, selection methods and List wrappers. \n");
emitter.print(" * All of them are based on cursors, so it is important you understand how cursors work.\n");
emitter.print(" * </p>\n");
emitter.print(" * <h4>Cursors</h4>\n");
emitter.print(" * <p>\n");
emitter.print(" * There are two type of cursors:\n");
emitter.print(" * </p>\n");
emitter.print(" * <ul>\n");
emitter.print(" * <li>{@link Cursor}: sequential cursor.</li>\n");
emitter.print(" * <li>{@link RandomCursor}: random access cursor.</li>\n");
emitter.print(" * </ul>\n");
emitter.print(" * <p>\n");
emitter.print(" * Both cursors have a particular function and you must use the more appropiate \n");
emitter.print(" * depending on the problem. \n");
emitter.print(" * </p>\n");
emitter.print(" *\n");
emitter.print(" * <h5>Sequential Cursor</h5>\n");
emitter.print(" *\n");
emitter.print(" * <p>\n");
emitter.print(" * This is the common cursor, it retrieves all the records from a start position to an stop position.\n");
emitter.print(" * The API is very simple, in the following example we print the data from \"start\" to \"stop\":\n");
emitter.print(" * </p>\n");
emitter.print(" * <pre>\n");
emitter.print(" * " + tableName + "MDB mdb = ...;\n");
emitter.print(" * long start = ...;\n");
emitter.print(" * long stop = ...;\n");
emitter.print(" *\n");
emitter.print(" * // request a sequential cursor from start to stop\n");
emitter.print(" * " + tableName + ".Cursor cursor = mdb.cursor(start, stop);\n");
emitter.print(" *\n");
emitter.print(" * // iterate the records from start to stop\n");
emitter.print(" * while (cursor.next()) {\n");
emitter.print(" * 	// print the content of the current record\n");
emitter.print(" * 	System.out.println(\"Read \"  \n");
 for(Column col : table) {
emitter.print(" * 			+ cursor." + col.getName() + " + \" \"\n");
 }
emitter.print(" * 		);\n");
emitter.print(" * }\n");
emitter.print(" * // important always close the cursor\n");
emitter.print(" * cursor.close();\n");
emitter.print(" * </pre>\n");
emitter.print(" * <p>\n");
emitter.print(" * As you can see it is very simple, just to highlight that you should close the cursor\n");
emitter.print(" * when you stop using it. A cursor creates certain OS resources that should be released as\n");
emitter.print(" * soon the cursor is not needed anymore, also in certain operating systems like Windows, you \n");
emitter.print(" * cannot delete the underlaying file until it gets released.\n");
emitter.print(" * <p> \n");
emitter.print(" * See the {@link Cursor} javadoc for more details.\n");
emitter.print(" * </p>\n");
emitter.print(" * \n");
emitter.print(" * <h5>Random Access Cursor</h5>\n");
emitter.print(" * <p>\n");
emitter.print(" * This cursor provides random access to the file. It is possible to implement a \n");
emitter.print(" * random access method using sequential cursors (open it, read a record, and close), \n");
emitter.print(" * but a {@link RandomCursor} is the API we provide\n");
emitter.print(" * to perform this task in a more efficient way.\n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * Let's see this API with an exmaple:\n");
emitter.print(" * </p>\n");
emitter.print(" * <pre>\n");
emitter.print(" * " + tableName + "MDB mdb = ...;\n");
emitter.print(" *\n");
emitter.print(" * // request a random cursor\n");
emitter.print(" * " + tableName + "MDB.RandomCursor cursor = mdb.randomCursor();\n");
emitter.print(" *\n");
emitter.print(" * // read record at position 10\n");
emitter.print(" * cursor.seek(10);\n");
emitter.print(" * System.out.println(cursor.toRecord());\n");
emitter.print(" *\n");
emitter.print(" * // read record at position 34\n");
emitter.print(" * cursor.seek(34);\n");
emitter.print(" * System.out.println(cursor.toRecord());\n");
emitter.print(" *\n");
emitter.print(" * // remember always to close the cursor\n");
emitter.print(" * cursor.close();\n");
emitter.print(" * </pre>\n");
emitter.print(" * <p>\n");
emitter.print(" * In our experience, many times you would like to keep alive a random access cursor\n");
emitter.print(" * until the session gets closed. In this case, we recommend to \"defer\" the cursor.\n");
emitter.print(" * A deferred cursor is not more than a cursor that is closed automatically\n");
emitter.print(" * when the session is closed.\n");
emitter.print(" * See the {@link MDBSession#defer(ICursor)} javadoc for more information.\n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * MDB uses random access cursors to implement other APIs with a higher level of abstraction\n");
emitter.print(" * like the {@link MDB#record(IRandomCursor, long)} method and the lists. Now we want to focus\n");
emitter.print(" * on the \"record\" method. It is very easy to use, the previous example can be re-implemented\n");
emitter.print(" * in this way:\n");
emitter.print(" * </p>\n");
emitter.print(" * <pre>\n");
emitter.print(" * " + tableName + "MDB mdb = ...;\n");
emitter.print(" * // read record at position 10\n");
emitter.print(" * System.out.println(mdb.record(10));\n");
emitter.print(" *\n");
emitter.print(" * // read record at position 34\n");
emitter.print(" * System.out.println(mdb.record(34));\n");
emitter.print(" * </pre>\n");
emitter.print(" * <p>\n");
emitter.print(" * Is it simpler right? And it performs very well, yet a random cursor is a bit faster\n");
emitter.print(" * but you can use the \"record\" if you have a deadline, just do not use to retrieve\n");
emitter.print(" * sequential, in that case remember to use a sequential cursor or one of the \"select\"\n");
emitter.print(" * methods (we explain later these methods).\n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * Something to highlight about the \"record\" method implementation, it creates \n");
emitter.print(" * a random-deferred cursor per thread, so it i safe if many threads call it \n");
emitter.print(" * at the same time.\n");
emitter.print(" * </p>\n");
emitter.print(" *\n");
emitter.print(" *\n");
emitter.print(" * <h4>Index Search</h4>\n");
emitter.print(" * <p>\n");
emitter.print(" * TODO: Documentation is coming...\n");
emitter.print(" * </p>\n");
emitter.print(" *\n");
emitter.print(" * <h3>Data Update</h3>\n");
emitter.print(" *\n");
emitter.print(" * <p>\n");
emitter.print(" * MDB provides two type of methods to update the values:\n");
emitter.print(" * </p>\n");
emitter.print(" * <ul>\n");
emitter.print(" * 	<li>\n");
emitter.print(" * 		Update a unqiue row. It re-writes the content of the whole record. \n");
emitter.print(" * 		It is available for tables without array definitions.\n");
emitter.print(" * </li>\n");
emitter.print(" * 	<li>\n");
emitter.print(" *		Update a particular column or a unique row. It re-writes only that field of the record. \n");
emitter.print(" * 		It is available only for non-array columns.\n");
emitter.print(" * 	</li>\n");
emitter.print(" * </ul>\n");
emitter.print(" * <p>\n");
emitter.print(" * You see in both cases it updates only one record at the same time, and the value to replace should be primitive.\n");
emitter.print(" * Also remember virtual columns are not updated because its values are computed automatically, they are not stored physically.\n");
emitter.print(" * </p>\n");
emitter.print(" * <p>\n");
emitter.print(" * The API is simple:\n");
emitter.print(" * </p>\n");
emitter.print(" * <pre>\n");
emitter.print(" * " + tableName + "MDB mdb = ...;\n");
emitter.print(" * // the index of the record you want to update/replace.\n");
emitter.print(" * long index = ...;\n");
emitter.print(" *\n");
 if (table.hasVirtualColumns()) emitter.print(" * // the new values, remember virtual columns are ignored 	\n");
 else emitter.print(" * // the new values 										\n");
 for(Column col : table.realColumns()) {
emitter.print(" * " + col.getJavaTypeName() + " new_val_" + col.getName() + " = ...;\n");
 }
emitter.print(" *\n");
emitter.print(" * mdb.replace(index \n");
 for(Column col : table.realColumns()) {
emitter.print(" * 		, new_val_" + col.getName() + "\n");
 }
emitter.print(" *		);\n");
emitter.print(" * </pre>\n");
emitter.print(" * <p>\n");
emitter.print(" * If you want to update just one column of the record, then you may use the following methods:\n");
emitter.print(" * </p>\n");
emitter.print(" * <ul>\n");
 for(Column col : table) if (!col.isVirtual()) {
emitter.print(" * <li>{@link " + tableName + "MDB#replace_" + col.getName() + "(long, " + col.getType().getJavaType().getSimpleName() + ")}: To replace the " + col.getName() + " value.</li>\n");
 }
emitter.print(" * </ul>\n");
emitter.print(" *\n");
emitter.print(" * <h3>List API</h3>\n");
emitter.print(" * TODO: Documentation is comming\n");
emitter.print(" *\n");
emitter.print(" * @see " + schemaName + "MDBSession#connectTo_" + tableName + "MDB(String)\n");
emitter.print(" */\n");
emitter.print("\n");
emitter.print("public final class " + tableName + "MDB\n");
userText("/* BEGIN MDB EXTENDS */\n", "\n/* END MDB EXTENDS */","\t\textends MDB<" + tableName + "MDB.Record>");
emitter.print("\n");
 if (hasArray) { emitter.print("implements IArrayMDB \n"); }  
emitter.print("{\n");
emitter.print("\n");
 userText("/* BEGIN USER MDB */", "/* END USER MDB */", "\n/* User can insert his code here */\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: column names.\n");
emitter.print("	 */\n");
emitter.print("	public static final String[] COLUMNS_NAME = {\n");
 for (Column col : table) emitter.print("		\"" + col.getName() + "\",\n");
emitter.print("	};\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: column Java types.\n");
emitter.print("	 */\n");
emitter.print("	public static final Class<?>[] COLUMNS_TYPE = {\n");
 for (Column col : table) emitter.print("		" + col.getType().getJavaType().getCanonicalName() + ".class,\n");
emitter.print("	};\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: column Java types size (in bytes).\n");
emitter.print("	 */\n");
emitter.print("	public static final int[] COLUMNS_SIZE = { \n");
 for(Column col : table) emitter.print("		" + col.getType().getSize() + ", \n");
emitter.print("	};\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: virtual column flags.\n");
emitter.print("	 */\n");
emitter.print("	public static final boolean[] COLUMNS_IS_VIRTUAL = { \n");
 for(Column col : table) emitter.print("		" + col.isVirtual() + ", \n");
emitter.print("	};\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: column byte-offset.\n");
emitter.print("	 */\n");
emitter.print("	public static final int[] COLUMN_OFFSET = {  \n");
 for (Column col : table) {
emitter.print("		" + col.getOffset() + ", \n");
}
emitter.print("	};\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: size of the record, in bytes.\n");
emitter.print("	 */\n");
emitter.print("	public static final int RECORD_SIZE = " + table.getRowSize() + ";\n");
emitter.print("	\n");
for(int z = 0; z < table.size(); z++) {
Column col = table.get(z);
emitter.print("	/**\n");
emitter.print("	* " + col.getName() + "'s meta-data: index in a record.\n");
emitter.print("	*/	\n");
emitter.print("	public static final int COLUMN_" + col.getName().toUpperCase() + " = " + z + ";\n");
}	
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: UUID used in schemas.\n");
emitter.print("	 */\n");
emitter.print("	public static final String TABLE_ID = \"" + table.getUUID().toString() + "\";\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * " + tableName + "'s meta-data: signature used to check schema changes.\n");
emitter.print("	 */ \n");
emitter.print("	public static final String TABLE_SIGNATURE = \"" + table.computeSignature() + "\";\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("	private Appender _appender;\n");
 if (!hasArray) emitter.print("	private ByteBuffer _replaceBuffer; \n");
	for(Column col : table.realPrimColumns()) {
emitter.print("	private ByteBuffer _replaceBuffer_" + col.getName() + ";\n");
	}
emitter.print("	int _rbufSize;\n");
emitter.print("	AtomicInteger _openCursorCount;\n");
emitter.print("	long _rbufPos;\n");
emitter.print("	Record[] _rbuf;\n");
emitter.print("	long _size;\n");
emitter.print("	final " + schemaName + "MDBSession _session;\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * The constructor. You can manipulate MDB files with an instance of this class \n");
emitter.print("	 * and you don't need a session, but we recommend to create a session \n");
emitter.print("	 * and connect to files with the session \"connect\" methods, \n");
emitter.print("	 * specially when you have more than one file.\n");
 if (hasArray) {
emitter.print("	 * @param session The session attached to this MDB instance.\n");
emitter.print("	 * @param file The main file.\n");
emitter.print("	 * @param arrayFile The file with the array values.\n");
emitter.print("	 * @param bufferSize The number of records to use in the buffer.\n");
emitter.print("	 * @param mode The session mode.\n");
emitter.print("	 * @throws IOException If there is an I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public " + tableName + "MDB(" + schemaName + "MDBSession session, File file, File arrayFile, int bufferSize, SessionMode mode) throws IOException {\n");
emitter.print("		super(TABLE_ID, TABLE_SIGNATURE, mode, file, arrayFile, bufferSize, COLUMNS_NAME, COLUMNS_TYPE);\n");
emitter.print("		\n");
emitter.print("		if (file == null || arrayFile == null) throw new IllegalArgumentException(\"Null files.\");\n");
emitter.print("		if (file.equals(arrayFile)) throw new IllegalArgumentException(\"Main file is equal to the array file: \" + file);\n");
 } else {
emitter.print("	 * @param session The session attached to this MDB instance.\n");
emitter.print("	 * @param file The main file.\n");
emitter.print("	 * @param bufferSize The number of records to use in the buffer.\n");
emitter.print("	 * @param mode The session mode.\n");
emitter.print("	 * @throws IOException If there is an I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public " + tableName + "MDB(" + schemaName + "MDBSession session, File file, int bufferSize, SessionMode mode) throws IOException {\n");
emitter.print("		super(TABLE_ID, TABLE_SIGNATURE, mode, file, null, bufferSize, COLUMNS_NAME, COLUMNS_TYPE);\n");
emitter.print("		\n");
emitter.print("		if (file == null) throw new IllegalArgumentException(\"Null files.\");\n");
 }
emitter.print("		_session = session;\n");
emitter.print("		_openCursorCount = new AtomicInteger(0);\n");
emitter.print("		_rbufSize = 0;\n");
emitter.print("		_size = fsize();\n");
emitter.print("		\n");
emitter.print("		if (!_basic) {\n");
emitter.print("			_rbuf = new Record[_bufferSize];\n");
emitter.print("			for (int i = 0; i < _bufferSize; i++) {\n");
emitter.print("				_rbuf[i] = new Record();\n");
emitter.print("			}\n");
emitter.print("			_rbufPos = _size;\n");
emitter.print("			\n");
emitter.print("		}\n");
	if (!hasArray || !table.realPrimColumns().isEmpty()) {
emitter.print("		if (!_memory) {		\n");
		if (!hasArray) {
emitter.print("			_replaceBuffer = ByteBuffer.allocate(" + rowSize + ");		\n");
		}
		for(Column col : table.realPrimColumns()) {
emitter.print("			_replaceBuffer_" + col.getName() + " = ByteBuffer.allocate(" + col.getType().getSize() + ");\n");
		}
emitter.print("		}\n");
	}
emitter.print("	}	\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* " + tableName + " record structure.\n");
emitter.print("	*/\n");
emitter.print("	public static class Record \n");
 userText("/* BEGIN RECORD EXTENDS */\n", "\n/* END RECORD EXTENDS */","\t\timplements IRecord");
emitter.print("	{\n");
	for (Column col : table) {
		if (col.isVirtual()) {
emitter.print("		/** Virtual field. Formula: <code>" + col.getFormula() + "</code>*/		\n");
		}
emitter.print("		/**\n");
emitter.print("		* Represents the " + col.getName() + " column.\n");
emitter.print("		* <h2>Definition</h2>\n");
emitter.print("		* <table border=1>\n");
emitter.print("		* <caption>Definition of " + col.getName() + "</caption>\n");
emitter.print("		* <tr><td>Column</td><td>" + col.getName() + "</td></tr>\n");
emitter.print("		* <tr><td>Type</td><td>" + col.getType() + "</td></tr>\n");
emitter.print("		* <tr><td>Order</td><td>" + col.getOrder() + "</td></tr>\n");
emitter.print("		* <tr><td>Virtual</td><td>" + (col.isVirtual()? "Yes" : "No") + "</td></tr>\n");
 if (col.isVirtual()) emitter.print("		* <tr><td>Formula</td><td>" + col.getFormula() + "</td></tr> \n");
emitter.print("		* </table>\n");
emitter.print("		*/\n");
emitter.print("		public " + col.getType().getJavaType().getSimpleName() + " " + col.getName() + "; /* " + col.getIndex() + " */\n");
	}	
emitter.print("\n");
emitter.print("		/**\n");
emitter.print("		* Returns an string representation of the record content.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public String toString() {\n");
emitter.print("			return \"" + tableName + " [ \"\n");
	for(Column col : table) {
		if (col.getType().isArrayButNotString()) {
emitter.print("				 + \"" + col.getName() + "=\" + java.util.Arrays.toString(" + col.getName() + ") + \" \" 	\n");
		} else {
emitter.print("				 + \"" + col.getName() + "=\" + " + col.getName() + " + \" \"	\n");
		}
	}
emitter.print("				 + \" ]\";\n");
emitter.print("		}\n");
emitter.print("\n");
emitter.print("	\n");
emitter.print("		/**\n");
emitter.print("		* An array of the record values.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public Object[] toArray() {\n");
emitter.print("			return new Object[] {\n");
	for(Column col : table) {			
emitter.print("							" + col.getType().boxing(col.getName()) + ",\n");
 }
emitter.print("			 			};\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Record meta-data: the column names.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public String[] getColumnsName() {\n");
emitter.print("			return COLUMNS_NAME;\n");
emitter.print("		} \n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Record meta-data: the column Java types.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public Class<?>[] getColumnsType() {\n");
emitter.print("			return COLUMNS_TYPE;\n");
emitter.print("		} 			\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Clone the record.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public Record clone() {\n");
emitter.print("			try {\n");
 	if (hasArray) {
emitter.print("				Record r = (Record) super.clone();\n");
				for(Column col : table) {
					String name = col.getName();
					if (col.getType().isArrayButNotString()) {
emitter.print("				r." + name + " = this." + name + " == null? null : java.util.Arrays.copyOf(this." + name + ", this." + name + ".length); 	\n");
					} 
				}
emitter.print("				return r;\n");
	} else {				
emitter.print("				return (Record) super.clone();\n");
	}				
emitter.print("			} catch (CloneNotSupportedException e) {\n");
emitter.print("				throw new RuntimeException(e);\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Get the value of the column at the <code>columnIndex</code> index.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		@SuppressWarnings(\"boxing\")\n");
emitter.print("		public Object get(int columnIndex) {\n");
emitter.print("			switch(columnIndex) {\n");
	for(Column col : table) {
emitter.print("				case " + col.getIndex() + ": return " + col.getName() + ";\n");
	}
emitter.print("				default: throw new IndexOutOfBoundsException(\"Wrong column index \" + columnIndex);			 \n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Update the record with the given record's values. In case of arrays the content is copied too. \n");
emitter.print("		* @param record The record to update.\n");
emitter.print("		*/ \n");
emitter.print("		public void update(Record record) {\n");
 	for(Column col : table) {
		String name = col.getName();
		if (col.getType().isArrayButNotString()) {
emitter.print("			this." + name + " = record." + name + " == null? null : java.util.Arrays.copyOf(record." + name + ", record." + name + ".length);\n");
		} else {			
emitter.print("			this." + name + " = record." + name + ";\n");
		}
	}
emitter.print("		}\n");
emitter.print("\n");
	userText("/* BEGIN USER RECORD */", "/* END USER RECORD */", "\n/* User can insert his code here */\n");		
emitter.print("		\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override\n");
emitter.print("	public Record[] makeRecordArray(int size) {\n");
emitter.print("		return new Record[size];\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override \n");
emitter.print("	public Record makeRecord() {\n");
emitter.print("		return new Record();\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("/**\n");
emitter.print("	* <p>\n");
emitter.print("	* This is the class used to append records to an MDB file.\n");
emitter.print("	* </p>\n");
emitter.print("	* The common way to use an appender is:\n");
emitter.print("	* <pre>\n");
emitter.print("	* Appender ap = mdb.appender();\n");
emitter.print("	* while( ... ) {\n");
	for(Column col : table) { if (col.isVirtual()) continue;
emitter.print("	* 	ap." + col.getName() + " = get" + camelCase(col.getName()) + "();	\n");
	}
emitter.print("	* 	ap.append();\n");
emitter.print("	* }\n");
emitter.print("	* ap.close();\n");
emitter.print("	* </pre>\n");
 if (table.hasVirtualColumns()) {	
emitter.print("	* <p>\n");
emitter.print("	* Note the virtual columns was ignored, this is because those values \n");
emitter.print("	* are computed by a formula in the \"append\" method.\n");
emitter.print("	* </p>\n");
 }
emitter.print("	*/\n");
emitter.print("	public final class Appender implements IAppender<Record> {\n");
emitter.print("		protected RandomAccessFile _raf;\n");
emitter.print("		FileChannel _channel;\n");
emitter.print("		protected " + tableName + "MDB _mdb;	\n");
emitter.print("		protected ByteBuffer _buf;	 \n");
 	if (hasArray) {
emitter.print("		protected RandomAccessFile _arrayRaf;\n");
emitter.print("		FileChannel _arrayChannel;\n");
	}		
		for (Column col : table) {
		if (col.isVirtual()) {
emitter.print("		/** \n");
emitter.print("		 * Virtual field. Formula: <code>" + col.getFormula() + "</code>\n");
emitter.print("		 */		\n");
		}
emitter.print("		public " + col.getType().getJavaType().getSimpleName() + " " + col.getName() + "; /* " + col.getIndex() + " */\n");
	}
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* The constructor.\n");
emitter.print("		*/\n");
emitter.print("		Appender() throws IOException {\n");
emitter.print("			_mdb = " + tableName + "MDB.this;\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				_buf = ByteBuffer.allocate(_bufferSize * " + rowSize + ");\n");
emitter.print("				reconnectFile();\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Close the file handlers and free the files. This method is used to \"unlock\" the file and \n");
emitter.print("		* perform other \"write\" operations outside the appender.\n");
emitter.print("		*/\n");
emitter.print("		void disconnectFile() throws IOException {\n");
emitter.print("			if (_memory) return;\n");
emitter.print("			\n");
emitter.print("			_raf.close();\n");
emitter.print("			_channel.close();	\n");
	if (hasArray) {			
emitter.print("			_arrayRaf.close();\n");
emitter.print("			_arrayChannel.close();\n");
	}
emitter.print("			\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Open the file handlers again.\n");
emitter.print("		*/\n");
emitter.print("		void reconnectFile() throws IOException {		\n");
emitter.print("			if (_memory) return;\n");
emitter.print("		\n");
emitter.print("			_raf = new RandomAccessFile(getFile(), \"rw\");\n");
emitter.print("			_channel = _raf.getChannel();\n");
emitter.print("			_channel.position(_raf.length());\n");
emitter.print("			_buf.rewind();\n");
	if (hasArray) {			
emitter.print("			_arrayRaf = new RandomAccessFile(getArrayFile(), \"rw\");\n");
emitter.print("			_arrayChannel = _arrayRaf.getChannel();\n");
emitter.print("			_arrayChannel.position(_arrayChannel.size());\n");
	}
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Append a new record to the file with the appender's values. \n");
emitter.print("		*/\n");
 if (hasString) emitter.print("		@SuppressWarnings(\"null\") \n");
emitter.print("		@Override\n");
emitter.print("		public void append() throws IOException {\n");
emitter.print("			if (_basic) {\n");
emitter.print("				try {\n");
emitter.print("					assert _rbufSize == 0 && _rbuf == null : \"In basic mode the shared buffer is empty\";\n");
emitter.print("				\n");
emitter.print("					/* basic append, do not put the record in memory */\n");
emitter.print("					if (_buf.position() == _buf.capacity()) {\n");
emitter.print("						flush();\n");
emitter.print("					}\n");
emitter.print("\n");
	if (hasVirtualColumns) {
emitter.print("					/* Virtual fields */\n");
 if (table.containsVirtualColumnsBasedOnPosition()) emitter.print("					long idx = _rbufPos + _rbufSize; \n");
		for(Column col : table) {
			if (col.isVirtual()) {
				String expr = expandExpr(col.getFormula(), "$$", "this", "$pos$", "idx").trim();				
emitter.print("					this." + col.getName() + " = " + expr + ";\n");
			}		
		}
	}
emitter.print("				\n");
	for (final Column col : table) {
		if (!col.isVirtual()) {
			boolean isString = col.getType() == Type.STRING;
			String colName = col.getName();				
			String varName = isString ? "__" + colName : "this." + colName;				
			if (col.getType().isArray()) {
				final Type elementType = col.getType().getElementType();
				final int elementSize = elementType.getSize();			
emitter.print("					{\n");
				if (isString) {
emitter.print("						byte[] " + varName + " = this." + col.getName() + " == null? null : this." + col.getName() + ".getBytes();\n");
					}
emitter.print("						int len = " + varName + " == null? 0 : " + varName + ".length;\n");
emitter.print("						_buf.putLong(_arrayChannel.position());\n");
emitter.print("						_buf.putInt(len * " + elementSize + ");\n");
emitter.print("						if (len > 0) {\n");
emitter.print("							ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * " + elementSize + "]);\n");
emitter.print("							for (int i = 0; i < len; i++) {\n");
 if (elementType == Type.BOOLEAN) emitter.print("								arrayBuf.put((byte)(" + varName + "[i]? 1 : 0)); \n");
 else emitter.print("								arrayBuf.put" + elementType.getGetName() + "(" + varName + "[i]); \n");
emitter.print("							}\n");
emitter.print("							arrayBuf.rewind();\n");
emitter.print("							_arrayChannel.write(arrayBuf);\n");
emitter.print("						}\n");
emitter.print("					}\n");
			} else {
				if (col.getType() == Type.BOOLEAN) {
emitter.print("					_buf.put((byte) (" + varName + "? 1 : 0));\n");
				} else {
emitter.print("					_buf.put" + col.getType().getGetName() + "(" + varName + ");\n");
				}
			} /* is array */
		} /* is not virtual */
	} /* for */
emitter.print("\n");
emitter.print("					_size++;\n");
emitter.print("					\n");
emitter.print("					return;\n");
emitter.print("				} catch (Exception e) {\n");
emitter.print("					_size = fsize();\n");
emitter.print("					throw e;\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("\n");
emitter.print("				/* regular append, put the record in the shared buffer */\n");
emitter.print("				_writeLock.lock();\n");
emitter.print("				try {\n");
emitter.print("					if (_rbufSize == _rbuf.length) {\n");
emitter.print("						if (_memory) {\n");
emitter.print("							int newSize = _rbufSize * 2;\n");
emitter.print("							Record[] b = new Record[newSize];\n");
emitter.print("							System.arraycopy(_rbuf, 0, b, 0, _rbufSize);\n");
emitter.print("							for(int i = 0; i < _rbufSize; i++) {\n");
emitter.print("								b[_rbufSize + i] = new Record();\n");
emitter.print("							}\n");
emitter.print("							_rbuf = b;\n");
emitter.print("						} else {\n");
emitter.print("							flush();\n");
emitter.print("						}\n");
emitter.print("					}	\n");
emitter.print("					Record r = _rbuf[_rbufSize];\n");
	for(Column col : table) {
		if (!col.isVirtual()) {
emitter.print("					r." + col.getName() + " = this." + col.getName() + ";\n");
		}		
	}			
	if (hasVirtualColumns) {
emitter.print("					/* Virtual fields */\n");
 if (table.containsVirtualColumnsBasedOnPosition()) emitter.print("					long idx = _rbufPos + _rbufSize; \n");
		for(Column col : table) {
			if (col.isVirtual()) {
				String expr = expandExpr(col.getFormula(), "$$", "r", "$pos$", "idx").trim();				
emitter.print("					r." + col.getName() + " = this." + col.getName() + " = " + expr + ";\n");
			}		
		}
emitter.print("				\n");
	}			
emitter.print("					_rbufSize++;\n");
emitter.print("					_size++;\n");
emitter.print("				} catch (Exception e) {\n");
emitter.print("					_size = fsize() + _rbufSize;\n");
emitter.print("					throw e;\n");
emitter.print("				} finally {\n");
emitter.print("					_writeLock.unlock();\n");
emitter.print("				}\n");
	emitExt(TARGET_MDB_CLASS_APPEND_METHOD);	
emitter.print("		}\n");
emitter.print("\n");
emitter.print("		/**\n");
emitter.print("		 * Append to the file a copy of the given record.\n");
emitter.print("		 */\n");
emitter.print("		@Override\n");
emitter.print("		public void append(Record record) throws IOException {	\n");
emitter.print("			if (_basic) {\n");
 for(Column col : table.realColumns()) {
emitter.print("				this." + col.getName() + " = record." + col.getName() + ";\n");
 }				
emitter.print("				append();\n");
emitter.print("				return;\n");
emitter.print("			}\n");
emitter.print("\n");
emitter.print("			_writeLock.lock();\n");
emitter.print("			try {										\n");
emitter.print("				if (_rbufSize == _rbuf.length) {\n");
emitter.print("					if (_memory) {\n");
emitter.print("						int newSize = _rbufSize * 2;\n");
emitter.print("						Record[] b = new Record[newSize];\n");
emitter.print("						System.arraycopy(_rbuf, 0, b, 0, _rbufSize);\n");
emitter.print("						for(int i = 0; i < _rbufSize; i++) {\n");
emitter.print("							b[_rbufSize + i] = new Record();\n");
emitter.print("						}\n");
emitter.print("						_rbuf = b;\n");
emitter.print("					} else {\n");
emitter.print("						flush();\n");
emitter.print("					}\n");
emitter.print("				}	\n");
emitter.print("				Record r = _rbuf[_rbufSize];\n");
	for(Column col : table) {
		if (!col.isVirtual()) {
emitter.print("				r." + col.getName() + " = record." + col.getName() + ";\n");
		}		
	}			
	if (hasVirtualColumns) {
emitter.print("				/* Virtual fields */\n");
 if (table.containsVirtualColumnsBasedOnPosition()) emitter.print("				long idx = _rbufPos + _rbufSize; \n");
		for(Column col : table) {
			if (col.isVirtual()) {
				String expr = expandExpr(col.getFormula(), "$$", "r", "$pos$", "idx").trim();				
emitter.print("				r." + col.getName() + " = this." + col.getName() + " = " + expr + ";\n");
			}		
		}
emitter.print("				\n");
	}			
emitter.print("				_rbufSize++;\n");
emitter.print("				_size++;\n");
emitter.print("			} catch (Exception e) {\n");
emitter.print("				_size = fsize() + _rbufPos;\n");
emitter.print("				throw e;\n");
emitter.print("			} finally {\n");
emitter.print("				_writeLock.unlock();\n");
emitter.print("			}\n");
 emitExt(TARGET_MDB_CLASS_APPEND_METHOD);
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		 * <p>\n");
emitter.print("		 * Warning! Do not use this method if you don't know what are you doing!\n");
emitter.print("		 * </p>\n");
emitter.print("		 * <p>\n");
emitter.print("		 * Append the record but not copy it. Use this method if you are fully sure that you will not touch that record instance again, \n");
emitter.print("		 * else the data will be corrupted.\n");
emitter.print("		 * </p> \n");
emitter.print("		 */\n");
emitter.print("		@Override\n");
emitter.print("		public void append_ref_unsafe(Record record) throws IOException {\n");
emitter.print("			if (_basic) {\n");
 for(Column col : table.realColumns()) {
emitter.print("				this." + col.getName() + " = record." + col.getName() + ";\n");
 }			
emitter.print("				append();\n");
emitter.print("				return;\n");
emitter.print("			}	\n");
emitter.print("\n");
emitter.print("			_writeLock.lock();	\n");
emitter.print("			try {											\n");
emitter.print("				if (_rbufSize == _rbuf.length) {\n");
emitter.print("					if (_memory) {\n");
emitter.print("						int newSize = _rbufSize * 2;\n");
emitter.print("						Record[] b = new Record[newSize];\n");
emitter.print("						System.arraycopy(_rbuf, 0, b, 0, _rbufSize);\n");
emitter.print("						for(int i = 0; i < _rbufSize; i++) {\n");
emitter.print("							b[_rbufSize + i] = new Record();\n");
emitter.print("						}\n");
emitter.print("						_rbuf = b;\n");
emitter.print("					} else {\n");
emitter.print("						flush();\n");
emitter.print("					}\n");
emitter.print("				}	\n");
emitter.print("				_rbuf[_rbufSize] = record;\n");
	if (hasVirtualColumns) {
emitter.print("				/* Virtual fields */\n");
 if (table.containsVirtualColumnsBasedOnPosition()) emitter.print("				long idx = _rbufPos + _rbufSize; \n");
		for(Column col : table) {
			if (col.isVirtual()) {
				String expr = expandExpr(col.getFormula(), "$$", "record", "$pos$", "idx").trim();				
emitter.print("				record." + col.getName() + " = this." + col.getName() + " = " + expr + ";\n");
			}		
		}
emitter.print("				\n");
	}			
emitter.print("				_rbufSize++;\n");
emitter.print("				_size++;\n");
emitter.print("			} catch (Exception e) {\n");
emitter.print("				_size = fsize() + _rbufSize;\n");
emitter.print("				throw e;\n");
emitter.print("			} finally {\n");
emitter.print("				_writeLock.unlock();\n");
emitter.print("			}\n");
 emitExt(TARGET_MDB_CLASS_APPEND_METHOD);			
emitter.print("		}\n");
emitter.print("\n");
emitter.print("\n");
 if (hasString) emitter.print("		@SuppressWarnings(\"null\") \n");
emitter.print("		/**\n");
emitter.print("		* Write pending records, it clears the buffer.\n");
emitter.print("		* @throws IOException If there is any I/O error.\n");
emitter.print("		*/\n");
emitter.print("		public void flush() throws IOException {	\n");
emitter.print("			if (_basic) {\n");
emitter.print("				_buf.limit(_buf.position());\n");
emitter.print("				_buf.rewind();\n");
emitter.print("				_channel.write(_buf);\n");
emitter.print("				_buf.limit(_buf.capacity());\n");
emitter.print("				_buf.rewind();\n");
emitter.print("				_rbufPos = fsize();\n");
emitter.print("				return;\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			if (_memory) {\n");
emitter.print("				return;\n");
emitter.print("			}\n");
emitter.print("\n");
emitter.print("			for(int j = 0; j < _rbufSize; j++) {						\n");
emitter.print("				Record r = _rbuf[j];\n");
	for (final Column col : table) {
		if (!col.isVirtual()) {
			boolean isString = col.getType() == Type.STRING;
			String colName = col.getName();				
			String varName = isString ? "__" + colName : "r." + colName;				
			if (col.getType().isArray()) {
				final Type elementType = col.getType().getElementType();
				final int elementSize = elementType.getSize();			
emitter.print("				{\n");
				if (isString) {
emitter.print("					byte[] " + varName + " = r." + col.getName() + " == null? null : r." + col.getName() + ".getBytes();\n");
				}
emitter.print("					int len = " + varName + " == null? 0 : " + varName + ".length;\n");
emitter.print("					_buf.putLong(_arrayChannel.position());\n");
emitter.print("					_buf.putInt(len * " + elementSize + ");\n");
emitter.print("					if (len > 0) {\n");
emitter.print("						ByteBuffer arrayBuf = ByteBuffer.wrap(new byte[len * " + elementSize + "]);\n");
emitter.print("						for (int i = 0; i < len; i++) {\n");
 if (elementType == Type.BOOLEAN) emitter.print("							arrayBuf.put((byte)(" + varName + "[i]? 1 : 0)); \n");
 else emitter.print("							arrayBuf.put" + elementType.getGetName() + "(" + varName + "[i]); \n");
emitter.print("						}\n");
emitter.print("						arrayBuf.rewind();\n");
emitter.print("						_arrayChannel.write(arrayBuf);\n");
emitter.print("					}\n");
emitter.print("				}\n");
			} else {
				if (col.getType() == Type.BOOLEAN) {
emitter.print("				_buf.put((byte) (" + varName + "? 1 : 0));\n");
				} else {
emitter.print("				_buf.put" + col.getType().getGetName() + "(" + varName + ");\n");
				}
			} /* is array */
		} /* is not virtual */
	} /* for */
emitter.print("			}\n");
emitter.print("			_buf.rewind();\n");
emitter.print("			_buf.limit(_rbufSize * " + rowSize + ");\n");
emitter.print("			_channel.write(_buf);\n");
emitter.print("			_buf.limit(_buf.capacity());\n");
emitter.print("			_buf.rewind();\n");
emitter.print("			\n");
emitter.print("			_writeLock.lock();		\n");
emitter.print("			try {			\n");
emitter.print("				_rbufSize = 0;\n");
emitter.print("				_rbufPos = fsize();\n");
emitter.print("			} finally {\n");
emitter.print("				_writeLock.unlock();\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Flush the pending records and close the associated files.\n");
emitter.print("		*/	\n");
emitter.print("		@Override\n");
emitter.print("		public void close() throws IOException {\n");
emitter.print("			if (_memory) return;\n");
emitter.print("			\n");
emitter.print("			if (_basic || _rbufSize > 0) {\n");
emitter.print("				flush();\n");
emitter.print("			}\n");
emitter.print("			disconnectFile();	\n");
emitter.print("		}\n");
emitter.print("					\n");
emitter.print("		/**\n");
emitter.print("		* Get the associated MDB instance.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public " + tableName + "MDB getMDB() {\n");
emitter.print("			return _mdb;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Create a record with the appender's values.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public Record toRecord() {\n");
emitter.print("			Record r = new Record();\n");
	for(Column col : table) {
		String name = col.getName();
		if (col.getType().isArrayButNotString()) {
emitter.print("			r." + name + " = this." + name + " == null? null : java.util.Arrays.copyOf(this." + name + ", this." + name + ".length); 	\n");
		} else {
emitter.print("			r." + name + " = this." + name + ";\n");
		}			
	}				
emitter.print("			return r;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Update the appender's values with the values of the given record.\n");
emitter.print("		* @param record The record to update.\n");
emitter.print("		*/\n");
emitter.print("		public void update(Record record) {\n");
	for(Column col : table) {
		String name = col.getName();
		if (col.getType().isArrayButNotString()) {
emitter.print("			this." + name + " = record." + name + " == null? null : java.util.Arrays.copyOf(record." + name + ", record." + name + ".length);\n");
		} else {			
emitter.print("			this." + name + " = record." + name + ";\n");
		}
	}
emitter.print("		}\n");
emitter.print("\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* Return the singleton appender. For more details see the {@link Appender} class.\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	public Appender appender() throws IOException {\n");
emitter.print("		if (!_connectedToFiles) {\n");
emitter.print("			assert !getFile().exists();		\n");
emitter.print("			throw new FileNotFoundException(\n");
emitter.print("					\"This MDB was disconnected from the file \"\n");
emitter.print("							+ getFile()\n");
emitter.print("							+ \", possibly because a backup restore deleted it.\");\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		if (_session != null) {\n");
emitter.print("			_session.appenderRequested(this);\n");
emitter.print("		}\n");
emitter.print("			\n");
emitter.print("		if (_appender == null) {\n");
emitter.print("			_appender = new Appender();\n");
emitter.print("		}\n");
emitter.print("		return _appender;\n");
emitter.print("	}\n");
emitter.print("		\n");
emitter.print("	/**\n");
emitter.print("	* If the appender is created.\n");
emitter.print("	*\n");
emitter.print("	* @return True in case the appender was requested before.\n");
emitter.print("	*/\n");
emitter.print("	public boolean isAppenderCreated() {\n");
emitter.print("		return _appender != null;\n");
emitter.print("	}\n");
emitter.print("		\n");
emitter.print("	/**\n");
emitter.print("	* If the appender was created and is open.\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	public boolean isAppenderOpen() {\n");
emitter.print("		return _appender != null && _appender._channel.isOpen();\n");
emitter.print("	}\n");
emitter.print("		\n");
emitter.print("	/**\n");
emitter.print("	* Close the appender. If no appender was created, do nothing.\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	public void closeAppender() throws IOException {\n");
emitter.print("		if (_appender != null) {\n");
emitter.print("			_appender.close();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Closes the file handlers. This method is used by the session to restore backups.\n");
emitter.print("	* Do not use this method if you don't know what are you doing.\n");
emitter.print("	*/ \n");
emitter.print("	@Override\n");
emitter.print("	protected void disconnectFile() throws IOException {\n");
emitter.print("		_writeLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			_connectedToFiles = false;\n");
emitter.print("			if (_appender != null) {\n");
emitter.print("				_appender.disconnectFile();	\n");
emitter.print("			}\n");
emitter.print("			// do not close the underlaying cursor\n");
emitter.print("			// they are supposed to be closed\n");
emitter.print("			// by the session because they are deferred\n");
emitter.print("			_localRandCursor.remove();\n");
emitter.print("			_localSeqCursor.remove();\n");
emitter.print("		} finally {\n");
emitter.print("			_writeLock.unlock();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Create the file handlers. This is used for the session backup/recovery methods. \n");
emitter.print("	* Do not use this method if you don't know what are you doing.\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	protected void reconnectFile() throws IOException {\n");
emitter.print("		if (_appender != null) {\n");
emitter.print("			_appender.reconnectFile();\n");
emitter.print("			for(int i = 0; i < _bufferSize; i++) {\n");
emitter.print("				_rbuf[i] = new Record();\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		if (!_basic) {\n");
emitter.print("			_rbufSize = 0;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		_size = fsize();\n");
emitter.print("		_rbufPos = _size;\n");
emitter.print("		_connectedToFiles = true;			\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Flush the appender. \n");
emitter.print("	* @see Appender#flush()\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	public void flushAppender() throws IOException {\n");
emitter.print("		if (_appender != null) {\n");
emitter.print("			_appender.flush();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * <p>\n");
emitter.print("	 * This class provides a sequential cursor API.\n");
emitter.print("	 * </p>\n");
emitter.print("	 * <p>\n");
emitter.print("	 * This cursor is the basic, faster and more controlled way to retrieve sequential\n");
emitter.print("	 * data, it is used internally by other elements like the \"select\" methods.\n");
emitter.print("	 * </p>\n");
emitter.print("	 * <p>\n");
emitter.print("	 * The common way to use a sequential cursor is:\n");
emitter.print("	 * </p>\n");
emitter.print("	 * \n");
emitter.print("	 * <pre>\n");
emitter.print("	 * " + tableName + "MDB mdb = ...;\n");
emitter.print("	 * Cursor c = mdb.cursor(...); \n");
emitter.print("	 * while(c.next()) {\n");
 if (!table.isEmpty()) emitter.print("	 *     doSomething(c." + table.get(0).getName() + "); \n");
emitter.print("	 * }\n");
emitter.print("	 * c.close();\n");
emitter.print("	 * </pre>\n");
emitter.print("	 * <p>\n");
emitter.print("	 * A cursor contains the same \"column fields\" of a record, \n");
emitter.print("	 * when the <code>next()</code> method is called, the cursor\n");
emitter.print("	 * \"column fields\" are updated.\n");
emitter.print("	 * </p>\n");
emitter.print("	 * <p>\n");
emitter.print("	 * Warning: remember always to close the cursor, a common mistake\n");
emitter.print("	 * is to try to delete a database when there are opened cursors: \n");
emitter.print("	 * an open cursor blocks a file (at least in Windows).  \n");
emitter.print("	 * </p>\n");
emitter.print("	 */\n");
emitter.print("	public final class Cursor implements ISeqCursor<Record> {\n");
emitter.print("		private long _stop;\n");
emitter.print("		private long _row;\n");
emitter.print("		private ByteBuffer _buffer;\n");
emitter.print("		FileChannel _channel;\n");
emitter.print("		private RandomAccessFile _raf;\n");
emitter.print("		private long _len;\n");
emitter.print("		private boolean _open;\n");
	if (hasArray) {	
emitter.print("		private RandomAccessFile _arrayRaf;\n");
emitter.print("		private FileChannel _arrayChannel;\n");
	}
		for (Column col : table) {
		if (col.isVirtual()) {
emitter.print("		/** Virtual field. Formula: <code>" + col.getFormula() + "</code>*/		\n");
		}
emitter.print("		public " + col.getType().getJavaType().getSimpleName() + " " + col.getName() + "; /* " + col.getIndex() + " */\n");
	}	
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Cursor constructor.\n");
emitter.print("		*/\n");
emitter.print("		Cursor(RandomAccessFile raf, FileChannel channel, long start, long stop, int bufferSize) throws IOException {\n");
emitter.print("			super();\n");
emitter.print("			_openCursorCount.incrementAndGet();\n");
emitter.print("			_open = true;\n");
emitter.print("			_len = _size;\n");
emitter.print("			_stop = Math.min(stop, _len - 1);\n");
emitter.print("			_row = start;\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				_raf = raf;\n");
emitter.print("				_channel = channel;\n");
emitter.print("				_channel.position(start * " + rowSize + ");\n");
emitter.print("				_buffer = ByteBuffer.allocate(bufferSize * " + rowSize + ");\n");
emitter.print("				_buffer.position(_buffer.capacity());\n");
		if (hasArray) {			
emitter.print("				_arrayRaf = new RandomAccessFile(getArrayFile(),\"rw\");\n");
emitter.print("				_arrayChannel = _arrayRaf.getChannel();\n");
		}
emitter.print("			}\n");
emitter.print("			_session.cursorCreated(this);\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		@Override\n");
emitter.print("		public void reset(long start, long stop) throws IOException {\n");
emitter.print("			if (!_open) throw new ClosedCursorException(this);\n");
emitter.print("			\n");
emitter.print("			long start2 = start < 0? 0 : start;\n");
emitter.print("			_len = _size;\n");
emitter.print("			_stop = stop < start2? start2 : Math.min(stop, _len - 1);\n");
emitter.print("			_row = start;\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				synchronized (this) {\n");
emitter.print("					_channel.position(start * " + rowSize + ");\n");
emitter.print("					_buffer.position(_buffer.capacity());\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("			\n");
emitter.print("		/**\n");
emitter.print("		* Fetch the data and move the cursor to the next record.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public synchronized boolean next() throws IOException {\n");
emitter.print("			// if (!_open) throw new ClosedCursorException(this);\n");
emitter.print("			\n");
emitter.print("			if (_row > _stop || _len == 0) return false;\n");
emitter.print("			\n");
emitter.print("			if (!_basic) {\n");
emitter.print("				_readLock.lock();	\n");
emitter.print("				try {\n");
emitter.print("					if (_rbufSize > 0 && _row >= _rbufPos) {\n");
emitter.print("						Record r;\n");
emitter.print("						r = _rbuf[(int) (_row - _rbufPos)];\n");
	for (Column col : table) {
		String name = col.getName();					
emitter.print("						this." + name + " = r." + name + ";\n");
	}					
emitter.print("						_row ++;\n");
emitter.print("						return true;	\n");
emitter.print("					}\n");
emitter.print("				} finally {\n");
emitter.print("					_readLock.unlock();\n");
emitter.print("				} \n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				if (_buffer.position() == _buffer.capacity()) {\n");
emitter.print("					_buffer.rewind();\n");
emitter.print("					_channel.read(_buffer);\n");
emitter.print("					_buffer.rewind();\n");
emitter.print("				}\n");
emitter.print("				\n");
 	for(Column col : table) {
		if (!col.isVirtual()) {
			Type type = col.getType();			
			boolean isString = type == Type.STRING;
			String colName = col.getName();			
			String varName = (isString? "__" : "this.") + colName;
			if (type.isArray()) {
				Type elementType = col.getType().getElementType();
				int elementSize = elementType.getSize();			
emitter.print("				{\n");
emitter.print("					long start = _buffer.getLong();\n");
emitter.print("					int byteLen = _buffer.getInt();\n");
emitter.print("					ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);\n");
emitter.print("					_arrayChannel.read(arrayBuf, start);\n");
emitter.print("					arrayBuf.rewind();\n");
				if (isString) {
emitter.print("					byte[] " + varName + " = new " + elementType.getJavaType().getCanonicalName() + "[byteLen / " + elementSize + "]; \n");
				} else {
emitter.print("					" + varName + " = new " + elementType.getJavaType().getCanonicalName() + "[byteLen / " + elementSize + "]; \n");
				}
emitter.print("					for(int i = 0; i < " + varName + ".length; i++) {\n");
 if (elementType == Type.BOOLEAN) emitter.print("						" + varName + "[i] = arrayBuf.get() == 0? false : true; \n");
 else emitter.print("						" + varName + "[i] = arrayBuf.get" + elementType.getGetName() + "(); \n");
emitter.print("					}\n");
 if (isString) emitter.print("					this." + colName + " = new String(" + varName + "); \n");
emitter.print("				}\n");
			} else { /* not array */
				if (type == Type.BOOLEAN) {
emitter.print("				" + varName + " = _buffer.get() == 0? false : true;\n");
				} else {
emitter.print("				" + varName + " = _buffer.get" + type.getGetName() + "();\n");
				}			
			}			
		} /* not virtual */		
 	} /* for column */
	if (hasVirtualColumns) {
emitter.print("				/* Virtual fields */\n");
		for(Column col : table) {
			if (col.isVirtual()) {
				String expr = expandExpr(col.getFormula(), "$$", "this", "$pos$", "_row").trim();				
emitter.print("					this." + col.getName() + " = " + expr + ";\n");
			}		
		}			
	}			
emitter.print("				_row ++;\n");
emitter.print("			}\n");
emitter.print("			return true;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		\n");
emitter.print("		/*\n");
emitter.print("		* Close the cursor. Do that when the cursor is not needed anymore.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public synchronized void close() throws IOException {\n");
emitter.print("			if (!_open) return;\n");
emitter.print("			_open = false;\n");
emitter.print("			_openCursorCount.decrementAndGet();\n");
emitter.print("			assert _openCursorCount.get() >= 0;\n");
emitter.print("			\n");
emitter.print("			if (!_memory) {\n");
emitter.print("				_raf.close();\n");
 if (hasArray) emitter.print("				_arrayRaf.close(); \n");
emitter.print("			}\n");
emitter.print("			_session.cursorClosed(this);\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		@Override\n");
emitter.print("		protected void finalize() {\n");
emitter.print("			try {\n");
emitter.print("				close();\n");
emitter.print("			} catch (IOException e) {\n");
emitter.print("				e.printStackTrace();\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* To check if the cursor is open.\n");
emitter.print("		*/	\n");
emitter.print("		@Override\n");
emitter.print("		public synchronized boolean isOpen() {\n");
emitter.print("			return _open;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* The current position of the cursor.\n");
emitter.print("		*/	\n");
emitter.print("		@Override\n");
emitter.print("		public long position() {\n");
emitter.print("			return _row;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* Create a record with the cursor data.\n");
emitter.print("		* You can use this method if you need to store the data in a collection.\n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public Record toRecord() {\n");
emitter.print("			Record r = new Record();\n");
	for(Column col : table) {
		String name = col.getName();
		if (col.getType().isArrayButNotString()) {
emitter.print("			r." + name + " = this." + name + " == null? null : java.util.Arrays.copyOf(this." + name + ", this." + name + ".length); 	\n");
		} else {
emitter.print("			r." + name + " = this." + name + ";\n");
		}			
	}				
emitter.print("			return r;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		/**\n");
emitter.print("		* The associated MDB instance. \n");
emitter.print("		*/\n");
emitter.print("		@Override\n");
emitter.print("		public " + tableName + "MDB getMDB() {\n");
emitter.print("			return " + tableName + "MDB.this;\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Create a cursor to iterate from position <code>start</code> to <code>stop</code>.\n");
emitter.print("	*\n");
emitter.print("	* @param start \n");
emitter.print("	* 			Start position.\n");
emitter.print("	* @param stop \n");
emitter.print("	* 			Stop position.\n");
emitter.print("	*/\n");
emitter.print("	@SuppressWarnings(\"resource\")	\n");
emitter.print("	@Override\n");
emitter.print("	public Cursor cursor(long start, long stop) throws IOException {\n");
emitter.print("		if (_memory) {\n");
emitter.print("			return new Cursor(null, null, start, stop, _bufferSize);\n");
emitter.print("		}\n");
emitter.print("		RandomAccessFile raf = new RandomAccessFile(getFile().getAbsolutePath(), \"r\");\n");
emitter.print("		return new Cursor(raf, raf.getChannel(), start, stop, _bufferSize);\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override\n");
emitter.print("	public Cursor cursor(long start) throws IOException {\n");
emitter.print("		return (Cursor) super.cursor(start);\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override\n");
emitter.print("	public Cursor cursor() throws IOException {\n");
emitter.print("		return (Cursor) super.cursor();\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("\n");
emitter.print("		/**\n");
emitter.print("		* <p>\n");
emitter.print("		* This class provides a random-access cursor API. In case you want to\n");
emitter.print("		* retrieve sequential data, the best is to use a sequential cursor (\n");
emitter.print("		* {@link Cursor}).\n");
emitter.print("		* </p>\n");
emitter.print("		* <p>\n");
emitter.print("		* This cursor is the basic, faster and more controlled way to retrieve\n");
emitter.print("		* random data, it is used internally by other elements like the\n");
emitter.print("		* {@link MDBList} class.\n");
emitter.print("		* </p>\n");
emitter.print("		* <p>\n");
emitter.print("		* The common way to use a random cursor is:\n");
emitter.print("		* </p>\n");
emitter.print("		* \n");
emitter.print("		* <pre>\n");
emitter.print("		* " + tableName + "MDB mdb = ...;\n");
emitter.print("		* RandomCursor c = mdb.randomCursor(); \n");
emitter.print("		* ...\n");
emitter.print("		* c.seek(somePosition);\n");
emitter.print("		* ...\n");
 if (!table.isEmpty()) emitter.print("		* doSomething(c." + table.get(0).getName() + "); \n");
emitter.print("		* ...\n");
emitter.print("		* c.close();\n");
emitter.print("		* </pre>\n");
emitter.print("		* <p>\n");
emitter.print("		* A random cursor contains the same \"column fields\" of a record, when the\n");
emitter.print("		* <code>seek()</code> method is called, the cursor \"column fields\" are\n");
emitter.print("		* updated.\n");
emitter.print("		* </p>\n");
emitter.print("		* <p>\n");
emitter.print("		* Warning: remember always to close the cursor, a common mistake is to try\n");
emitter.print("		* to delete a database when there are opened cursors: an open cursor blocks\n");
emitter.print("		* a file (at least in Windows). \n");
emitter.print("		* </p>\n");
emitter.print("		* <p>\n");
emitter.print("		* Usually you need only one random cursor per file, so probably you want to keep this\n");
emitter.print("		* cursor open while the session is alive, then you can use \"defer\" the cursor, this mean, \n");
emitter.print("		* the cursor will be closed automatically before to close the session. See the {@link MDBSession#defer(ICursor)} method. \n");
emitter.print("		* </p>\n");
emitter.print("		* @see MDBSession#defer(ICursor)\n");
emitter.print("		* @see Cursor\n");
emitter.print("		*/\n");
emitter.print("		public final class RandomCursor implements IRandomCursor<Record> {\n");
emitter.print("			private ByteBuffer _buffer;\n");
	for(Column col : table.realPrimColumns()) if(!col.getOrder().isNone()) {
emitter.print("			ByteBuffer _buffer_" + col.getName() + "; // used by index-of-" + col.getName() + " method.\n");
	}
emitter.print("			private RandomAccessFile _raf;\n");
emitter.print("			FileChannel _channel;\n");
emitter.print("			private long _row;\n");
emitter.print("			private boolean _open;\n");
		if (hasArray) {	
emitter.print("			private RandomAccessFile _arrayRaf;\n");
emitter.print("			private FileChannel _arrayChannel;\n");
		}
			for (Column col : table) {
			if (col.isVirtual()) {
emitter.print("			/** Virtual field. Formula: <code>" + col.getFormula() + "</code>*/		\n");
			}
emitter.print("			public " + col.getType().getJavaType().getSimpleName() + " " + col.getName() + "; /* " + col.getIndex() + " */\n");
		}
emitter.print("\n");
emitter.print("			RandomCursor() throws IOException {\n");
emitter.print("				_open = true;\n");
emitter.print("			    _openCursorCount.incrementAndGet();    \n");
emitter.print("				_row = -1;\n");
emitter.print("				if (!_memory) {\n");
emitter.print("					_raf = new RandomAccessFile(getFile(), \"r\");\n");
emitter.print("					_channel = _raf.getChannel();\n");
emitter.print("					_buffer = ByteBuffer.allocate(" + rowSize + ");\n");
	if (hasArray) {			
emitter.print("					_arrayRaf = new RandomAccessFile(getArrayFile(),\"rw\");\n");
emitter.print("					_arrayChannel = _arrayRaf.getChannel();\n");
	}
	for(Column col : table.realPrimColumns()) if (!col.getOrder().isNone()){
emitter.print("					_buffer_" + col.getName() + " = ByteBuffer.allocate(" + col.getType().getSize() + ");\n");
	}
emitter.print("				}\n");
emitter.print("				_session.cursorCreated(this);\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			@Override\n");
emitter.print("			public synchronized void seek(long position) throws IOException {\n");
emitter.print("				// if (!_open) throw new ClosedCursorException(this);\n");
emitter.print("				if (position < 0 || position >= _size) throw new IndexOutOfBoundsException(\"Index: \" + position + \", Size: \" + _size);\n");
emitter.print("				\n");
emitter.print("				if (!_basic) {\n");
emitter.print("					_readLock.lock();\n");
emitter.print("					try {\n");
emitter.print("						if (_rbufSize > 0 && position >= _rbufPos) {\n");
emitter.print("							_row = position;\n");
emitter.print("							Record r;\n");
emitter.print("							r = _rbuf[(int) (position - _rbufPos)];\n");
	for (Column col : table) {
		String name = col.getName();					
emitter.print("							this." + name + " = r." + name + ";\n");
	}
emitter.print("							return;					\n");
emitter.print("						}\n");
emitter.print("					} finally {\n");
emitter.print("						_readLock.unlock();\n");
emitter.print("					}\n");
emitter.print("				}\n");
emitter.print("				\n");
emitter.print("				assert !_memory;\n");
emitter.print("				\n");
emitter.print("				_row = position;\n");
emitter.print("				_buffer.rewind();\n");
emitter.print("				_channel.read(_buffer, position * " + rowSize + ");\n");
emitter.print("				_buffer.rewind();\n");
			 	for(Column col : table) {
				if (!col.isVirtual()) {
					Type type = col.getType();			
					boolean isString = type == Type.STRING;
					String colName = col.getName();			
					String varName = (isString? "__" : "this.") + colName;
					if (type.isArray()) {
						Type elementType = col.getType().getElementType();
						int elementSize = elementType.getSize();			
emitter.print("				{\n");
emitter.print("					long start = _buffer.getLong();\n");
emitter.print("					int byteLen = _buffer.getInt();\n");
emitter.print("					ByteBuffer arrayBuf = ByteBuffer.allocate(byteLen);\n");
emitter.print("					_arrayChannel.read(arrayBuf, start);\n");
emitter.print("					arrayBuf.rewind();\n");
						if (isString) {
emitter.print("					byte[] " + varName + " = new " + elementType.getJavaType().getCanonicalName() + "[byteLen / " + elementSize + "]; \n");
						} else {
emitter.print("					" + varName + " = new " + elementType.getJavaType().getCanonicalName() + "[byteLen / " + elementSize + "]; \n");
						}
emitter.print("					for(int i = 0; i < " + varName + ".length; i++) {\n");
 if (elementType == Type.BOOLEAN) emitter.print("						" + varName + "[i] = arrayBuf.get() == 0? false : true; \n");
 else emitter.print("						" + varName + "[i] = arrayBuf.get" + elementType.getGetName() + "(); \n");
emitter.print("					}\n");
 if (isString) emitter.print("					this." + colName + " = new String(" + varName + "); \n");
emitter.print("				}\n");
					} else { /* not array */
						if (type == Type.BOOLEAN) {
emitter.print("				" + varName + " = _buffer.get() == 0? false : true;\n");
						} else {
emitter.print("				" + varName + " = _buffer.get" + type.getGetName() + "();\n");
						}			
					}			
				} /* not virtual */		
		 	} /* for column */
			if (hasVirtualColumns) {
emitter.print("					/* Virtual fields */\n");
				for(Column col : table) {
					if (col.isVirtual()) {
						String expr = expandExpr(col.getFormula(), "$$", "this", "$pos$", "_row").trim();				
emitter.print("				this." + col.getName() + " = " + expr + ";\n");
					}		
				}			
			}			
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			@Override\n");
emitter.print("			public void seekLast() throws IOException {\n");
emitter.print("				seek(_size - 1);\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			@Override\n");
emitter.print("			public void seekFirst() throws IOException {\n");
emitter.print("				seek(0);\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			\n");
emitter.print("			/**\n");
emitter.print("			* Close the cursor. Do that when the cursor is not needed anymore.\n");
emitter.print("			*/\n");
emitter.print("			@Override\n");
emitter.print("			public synchronized void close() throws IOException {\n");
emitter.print("				if (!_open) return;\n");
emitter.print("				_open = false;\n");
emitter.print("			    _openCursorCount.decrementAndGet();\n");
emitter.print("			    assert _openCursorCount.get() >= 0; \n");
emitter.print("							    \n");
emitter.print("			    if (!_memory) {\n");
emitter.print("					_raf.close();\n");
 if (hasArray) emitter.print("					_arrayRaf.close(); \n");
emitter.print("				}\n");
emitter.print("\n");
emitter.print("				_session.cursorClosed(this);\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			@Override\n");
emitter.print("			protected void finalize() {\n");
emitter.print("				try {\n");
emitter.print("					close();\n");
emitter.print("				} catch (IOException e) {\n");
emitter.print("					e.printStackTrace();\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			/**\n");
emitter.print("			* To check if the cursor is open.\n");
emitter.print("			*/\n");
emitter.print("			@Override\n");
emitter.print("			public synchronized boolean isOpen() {\n");
emitter.print("				return _open;\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			/**\n");
emitter.print("			* The current position of the cursor.\n");
emitter.print("			*/\n");
emitter.print("			@Override\n");
emitter.print("			public long position() {\n");
emitter.print("				return _row;\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			/**\n");
emitter.print("			* Create a record with the cursor data.\n");
emitter.print("			* You can use this method if you need to store the data in a collection.\n");
emitter.print("			*/\n");
emitter.print("			@Override\n");
emitter.print("			public Record toRecord() {\n");
emitter.print("				Record r = new Record();\n");
	for(Column col : table) {
		String name = col.getName();
		if (col.getType().isArrayButNotString()) {
emitter.print("				r." + name + " = this." + name + " == null? null : java.util.Arrays.copyOf(this." + name + ", this." + name + ".length); 	\n");
		} else {
emitter.print("				r." + name + " = this." + name + ";\n");
		}			
	}				
emitter.print("				return r;\n");
emitter.print("			}\n");
emitter.print("			\n");
emitter.print("			/**\n");
emitter.print("			* The associated MDB instance.\n");
emitter.print("			*/\n");
emitter.print("			@Override\n");
emitter.print("			public " + tableName + "MDB getMDB() {\n");
emitter.print("				return " + tableName + "MDB.this;\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("	/**\n");
emitter.print("	* Create a random cursor.\n");
emitter.print("	* See the class {@link RandomCursor} for more details.\n");
emitter.print("	*\n");
emitter.print("	*/\n");
emitter.print("	@Override\n");
emitter.print("	public RandomCursor randomCursor() throws IOException {\n");
emitter.print("		return new RandomCursor();\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("\n");
 if (!table.hasArray()) {
 	for (String clsname : new String[] {"Cursor", "RandomCursor", "Record"})	{
emitter.print("	/**\n");
emitter.print("	 * <p>Write the field's values into the buffer in the same order they was declared.\n");
emitter.print("	 * The virtual fields are ignored.\n");
emitter.print("	 * </p> \n");
emitter.print("	 * @param obj The object to serialize into the buffer.\n");
emitter.print("	 * @param buffer The buffer to fill.\n");
emitter.print("	 */\n");
emitter.print("	public static void writeBuffer(" + clsname + " obj, ByteBuffer buffer) {\n");
			for (Column col : table) {
				if (!col.isVirtual()) {
 if (col.getType() == Type.BOOLEAN) { emitter.print("		buffer.put(obj." + col.getName() + " ? (byte) 1 : (byte) 0); \n"); }
 else { emitter.print("		buffer.put" + col.getType().getGetName() + "(obj." + col.getName() + "); \n"); }
				}
			}	
emitter.print("	}\n");
emitter.print("	\n");
 	}
 }
emitter.print("\n");
emitter.print("\n");

	String replaceMethodSignature = "";
	for(Column col : table.realPrimColumns()) {
		replaceMethodSignature += ", " + col.getJavaTypeName();
	}

 if (!table.hasArray()) {
emitter.print("	/**\n");
emitter.print("	 * Replace the record at the given <code>index</code>.\n");
emitter.print("	 *\n");
emitter.print("	 * @param index The index to update.\n");
 	for(Column col : table.realColumns()) {
emitter.print("	 * @param val_" + col.getName() + " The value for column " + col.getName() + ".\n");
 	}
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public void replace(long index \n");
 for(Column col : table.realColumns()) {
emitter.print("							, " + col.getType().getJavaType().getSimpleName() + " val_" + col.getName() + "\n");
 }		
emitter.print("			) throws IOException {\n");
emitter.print("		if (index < 0 || index >= _size) {\n");
emitter.print("			throw new IndexOutOfBoundsException(\"Index \" + index + \" out of bounds.\");\n");
emitter.print("		}\n");
emitter.print("		if (!_basic) _writeLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			if (!_basic) {\n");
emitter.print("				if (index >= _rbufPos) {\n");
emitter.print("					int pos = (int) (index - _rbufPos);\n");
emitter.print("					if (pos < _rbufSize) {\n");
emitter.print("						Record r = _rbuf[pos];\n");
 for (Column col : table.realColumns()) {
emitter.print("						r." + col.getName() + " = val_" + col.getName() + ";\n");
 }							
 	if (table.hasVirtualColumns()) {
emitter.print("						// virtual fields\n");
 		for (Column col: table.virtualColumns()) {
 			String expr = expandExpr(col.getFormula(), "$$.", "val_", "$pos$", "index"); 
emitter.print("						r." + col.getName() + " = " + expr + "; \n");
		}
	}
emitter.print("					}\n");
emitter.print("					return;\n");
emitter.print("				} 				\n");
emitter.print("			}\n");
emitter.print("		\n");
emitter.print("			_replaceBuffer.rewind();\n");
	for (Column col : table) {
 		if (!col.isVirtual()) {
 			if (col.getType() == Type.BOOLEAN) {
emitter.print("			_replaceBuffer.put((byte) (val_" + col.getName() + " ? 1 : 0));\n");
			} else {
emitter.print("			_replaceBuffer.put" + col.getType().getGetName() + "(val_" + col.getName() + ");\n");
			}
		} /* is not virtual */
 	}
emitter.print("			_replaceBuffer.rewind();\n");
emitter.print("			appender();\n");
emitter.print("			_appender._channel.write(_replaceBuffer, index * " + rowSize + ");\n");
emitter.print("		} finally {\n");
emitter.print("			if (!_basic) _writeLock.unlock();\n");
emitter.print("		}		\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * Update the record at the given <code>index</code>.\n");
emitter.print("	 * Also you can use {@link #replace(long " + replaceMethodSignature + ")}.\n");
emitter.print("	 *\n");
emitter.print("	 * @param index The index to update.\n");
emitter.print("	 * @param record Contains the data to set.\n");
emitter.print("	 * @see #replace(long " + replaceMethodSignature + ")\n");
emitter.print("	 * @throws IOException If there is any I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public void replace(long index, Record record) throws IOException {\n");
emitter.print("		replace(index \n");
 		for(Column col : table.realColumns()) {
emitter.print("					, record." + col.getName() + "		\n");
		}
emitter.print("				);			\n");
emitter.print("	}\n");
	} /* !table.hasArray() */
emitter.print("\n");
	for(Column col : table) {
		if (!col.getType().isArray() && !col.isVirtual()) {
			String colName = col.getName();	
			String argType = col.getType().getJavaType().getName();	
emitter.print("	/**\n");
emitter.print("	 * Update the record at the given <code>index</code>, \n");
emitter.print("	 * but only the column \"" + col.getName() + "\" is updated. If you like to \n");
emitter.print("	 * update many fields of the record then use {@link #replace(long " + replaceMethodSignature + ")},\n");
emitter.print("	 * but if you want to update just one column, use this method because it is faster.\n");
emitter.print("	 * \n");
emitter.print("	 * @param index The index of the record to update.\n");
emitter.print("	 * @param value The new value to set to the column \"" + col.getName() + "\".\n");
emitter.print("	 *\n");
emitter.print("	 * @throws IOException If there is any I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public void replace_" + colName + "(long index, " + argType + " value) throws IOException {\n");
emitter.print("		if (!_basic) _writeLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			if (!_basic) {\n");
emitter.print("				if (index >= _rbufPos) {\n");
emitter.print("					int pos = (int) (index - _rbufPos);\n");
emitter.print("					if (pos < _rbufSize) {\n");
emitter.print("						_rbuf[pos]." + colName + " = value;\n");
emitter.print("					}\n");
emitter.print("					return;\n");
emitter.print("				} 				\n");
emitter.print("			}\n");
emitter.print("		\n");
			String buf = "_replaceBuffer_" + col.getName();			
emitter.print("			" + buf + ".rewind();\n");
 			if (col.getType() == Type.BOOLEAN) {
emitter.print("			" + buf + ".put((byte) (value ? 1 : 0));\n");
			} else {
emitter.print("			" + buf + ".put" + col.getType().getGetName() + "(value);\n");
			}
emitter.print("			" + buf + ".rewind();\n");
emitter.print("			appender();\n");
emitter.print("			_appender._channel.write(" + buf + ", index * " + rowSize + " + " + col.getOffset() + ");\n");
emitter.print("		} finally {\n");
emitter.print("			if (!_basic) _writeLock.unlock();\n");
emitter.print("		}		\n");
emitter.print("	}\n");
		}	
	} /* replace_$col$*/
emitter.print("\n");
emitter.print("\n");
 for(Column col : table) {
		if (col.getOrder().isNone() || col.isVirtual()) 
			continue;
		String colType = col.getType().getJavaType().getSimpleName();
		String colName = col.getName();	
		String colCamelName = camelCase(colName);
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * Like {@link MDB#select(ISeqCursor, long, long)}, but starts at the index of <code>lower</code> \n");
emitter.print("	 * and stops at the index of <code>upper</code>.\n");
emitter.print("	 * @param randCursor \n");
emitter.print("	 *			The random cursor used to find the start and stop positions.\n");
emitter.print("	 * @param cursor\n");
emitter.print("	 *			The sequential cursor used to collect the data.\n");
emitter.print("	 * @param lower\n");
emitter.print("	 *			The lower value of <code>" + colName + "</code>.\n");
emitter.print("	 * @param upper\n");
emitter.print("	 *			The upper value of <code>" + colName + "</code>\n");
emitter.print("	 * @return The data between the lower and upper values.\n");
emitter.print("	 * @throws IOException If there is any I/O error.\n");
emitter.print("	 */	\n");
emitter.print("	public Record[] select__where_" + colCamelName + "_in(RandomCursor randCursor, Cursor cursor, " + colType + " lower, " + colType + " upper) throws IOException {\n");
emitter.print("		if (_size == 0) return NO_DATA;\n");
emitter.print("		\n");
emitter.print("		long start = indexOf" + colCamelName + "(randCursor, lower) - 1;\n");
emitter.print("\n");
emitter.print("		if (start < 0) {\n");
emitter.print("			start = 0;\n");
emitter.print("		}\n");
emitter.print("	\n");
emitter.print("		Record[] data = new Record[10];\n");
emitter.print("		int size = 0;\n");
emitter.print("	\n");
emitter.print("		cursor.reset(start, _size - 1);\n");
emitter.print("		while (cursor.next()) {\n");
emitter.print("			if (size + 2 > data.length) {\n");
emitter.print("				Record[] newData = new Record[(data.length * 3) / 2 + 1];\n");
emitter.print("				System.arraycopy(data, 0, newData, 0, size);\n");
emitter.print("				data = newData;\n");
emitter.print("			}\n");
emitter.print("			data[size] = cursor.toRecord();\n");
emitter.print("			size++;\n");
emitter.print("		\n");
emitter.print("			if (cursor." + colName + " > upper) {\n");
emitter.print("				break;\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("\n");
emitter.print("		if (size < data.length) {\n");
emitter.print("			Record[] newData = new Record[size];\n");
emitter.print("			System.arraycopy(data, 0, newData, 0, size);\n");
emitter.print("			data = newData;\n");
emitter.print("		}\n");
emitter.print("		return data;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #select__where_" + colCamelName + "_in(RandomCursor, Cursor, " + colType + ", " + colType + ")} but it uses a sparse cursor.\n");
emitter.print("	* @param randCursor\n");
emitter.print("	*			The random cursor used to find the indexes and collect sparse data.\n");
emitter.print("	* @param cursor\n");
emitter.print("	*			The sequential cursor used to collect the continuous data. There are cases where the data is not sparse cause the small range of search.\n");
emitter.print("	* @param lower\n");
emitter.print("	*			The lower value to search.\n");
emitter.print("	* @param upper\n");
emitter.print("	*			The upper value to search.\n");
emitter.print("	* @param maxLen\n");
emitter.print("	*			The maximum number of records to collect.\n");
emitter.print("	* @return The array of sparse data.    \n");
emitter.print("	* @see MDB#select_sparse(IRandomCursor, ISeqCursor, long, long, int)\n");
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	*/\n");
emitter.print("	public Record[] select_sparse__where_" + colCamelName + "_in(RandomCursor randCursor, Cursor cursor, " + colType + " lower, " + colType + " upper, int maxLen)\n");
emitter.print("			throws IOException {\n");
emitter.print("		if (_size == 0) return NO_DATA;\n");
emitter.print("		\n");
emitter.print("		long start = indexOf" + colCamelName + "(randCursor, lower) - 1;\n");
emitter.print("		long stop = Math.min(indexOf" + colCamelName + "(randCursor, upper) + 1, _size - 1);\n");
emitter.print("		\n");
emitter.print("		if (start < 0) {\n");
emitter.print("			start = 0;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		long step = (stop - start) / maxLen;\n");
emitter.print("		\n");
emitter.print("		if (step < 2) {\n");
emitter.print("			return select__where_" + colCamelName + "_in(randCursor, cursor, lower, upper);\n");
emitter.print("		}\n");
emitter.print("	\n");
emitter.print("		Record[] data = new Record[10];\n");
emitter.print("		int size = 0;\n");
emitter.print("		long pos = start;\n");
emitter.print("		\n");
emitter.print("		while (pos <= stop) {\n");
emitter.print("			randCursor.seek(pos);\n");
emitter.print("			if (size + 2 > data.length) {\n");
emitter.print("				Record[] newData = new Record[(data.length * 3) / 2 + 1];\n");
emitter.print("				System.arraycopy(data, 0, newData, 0, size);\n");
emitter.print("				data = newData;\n");
emitter.print("			}\n");
emitter.print("			data[size] = randCursor.toRecord();\n");
emitter.print("			size++;\n");
emitter.print("		\n");
emitter.print("			if (randCursor." + colName + " > upper) {\n");
emitter.print("				break;\n");
emitter.print("			}\n");
emitter.print("			pos += step;\n");
emitter.print("		}\n");
emitter.print("	\n");
emitter.print("		if (size < data.length) {\n");
emitter.print("			Record[] newData = new Record[size];\n");
emitter.print("			System.arraycopy(data, 0, newData, 0, size);\n");
emitter.print("			data = newData;\n");
emitter.print("		}\n");
emitter.print("		return data;\n");
emitter.print("	}	\n");
 }
 	for(Column col : table) {
   	Order colOrder = col.getOrder();
		if (colOrder != Order.NONE) {
  		String colName = col.getName();
			String colType = col.getType().getJavaType().getSimpleName();
			boolean asc = colOrder == Order.ASCENDING;
emitter.print("	/**\n");
emitter.print("	* Column <code>" + colName + "</code> order validator.\n");
emitter.print("	*/\n");
emitter.print("	public static final IValidator<Record> " + upperCase(colName) + "_" + colOrder + "_VALIDATOR = new IValidator<Record>() {\n");
emitter.print("		@Override\n");
emitter.print("		public boolean validate(ValidationArgs<Record> args, IValidatorListener<Record> listener) {\n");
emitter.print("			Record prev = args.getPrev();\n");
emitter.print("			Record current = args.getCurrent();\n");
emitter.print("			" + colType + " prevValue = prev." + colName + ";\n");
emitter.print("			" + colType + " curValue = current." + colName + ";\n");
 if (asc) emitter.print("			boolean valid = prevValue <= curValue; \n");
 else emitter.print("			boolean valid = prevValue >= curValue; \n");
emitter.print("			if (!valid) {\n");
emitter.print("				long row1 = args.getRow() - 1;\n");
emitter.print("				long row2 = args.getRow();\n");
emitter.print("				listener.errorReported(new ValidatorError<>(args, 						\n");
 if (asc)
emitter.print("						\"" + colName + "(\" + row1 + \")=\" + prevValue + \" > \" + \"" + colName + "(\" + row2 + \")=\" + curValue + \"\"));\n");
 else
emitter.print("						\"" + colName + "(\" + row1 + \")=\" + prevValue + \" < \" + \"" + colName + "(\" + row2 + \")=\" + curValue + \"\"));\n");
emitter.print("			}\n");
emitter.print("			return valid;\n");
emitter.print("		}\n");
emitter.print("	};\n");
 }
	}
 for(Column col : table) {
 	if(!col.isVirtual() && !col.getOrder().isNone()) {
		String colName = col.getName();
		String colCamelName = camelCase(colName);
		String colType = col.getType().getJavaType().getSimpleName();
		int colOrderBelow = col.getOrder() == Order.ASCENDING ? -1 : 1;
		int colOrderAbove = -colOrderBelow;
		String colGetName = col.getType().getGetName();
		int colOffset = col.getOffset();
emitter.print("	/**\n");
emitter.print("	 * <p>\n");
emitter.print("	 * Record comparator for the column <code>" + colName + "</code>. \n");
emitter.print("	 * This comparator takes in consideration the order of the column definition.\n");
emitter.print("	 * </p>\n");
emitter.print("	 */\n");
emitter.print("	public static class " + colCamelName + "Comparator implements java.util.Comparator<Record> {\n");
emitter.print("		\n");
emitter.print("		@Override\n");
emitter.print("		public int compare(Record o1, Record o2) {\n");
emitter.print("			return o1." + colName + " < o2." + colName + "? -1 : (o1." + colName + " > o2." + colName + "? 1 : 0);\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #indexOf" + colCamelName + "(Record[], " + colType + ", int, int)}, but searches in the whole array.\n");
emitter.print("	*\n");
emitter.print("	* @param data\n");
emitter.print("	*			The array of data.\n");
emitter.print("	* @param key\n");
emitter.print("	*			The value to search.\n");
emitter.print("	* @return The index of the value.	\n");
emitter.print("	*/\n");
emitter.print("	public static int indexOf" + colCamelName + "(Record[] data, " + colType + " key) {\n");
emitter.print("		return indexOf" + colCamelName + "(data, key, 0, data.length);\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* <p>\n");
emitter.print("	* From position <code>low</code> to <code>high</code>, this method finds the index \n");
emitter.print("	* of the record with the " + colName + " value closer to the given <code>key</code>. \n");
emitter.print("	* This method uses a binary search algorithm (like <code>Arrays.binarySearch(...)</code>), so it assumes all the records \n");
emitter.print("	* are ordered by the <code>" + colName + "</code> order specified in the column definition. \n");
emitter.print("	* </p>\n");
emitter.print("	* <p>\n");
emitter.print("	* This method is an utility, it does not search on a file, else in an arbitrary array. \n");
emitter.print("	* </p>\n");
emitter.print("	* @param data\n");
emitter.print("	* 			Array of records.\n");
emitter.print("	* @param key\n");
emitter.print("	* 			The value to find.\n");
emitter.print("	* @param low\n");
emitter.print("	* 			The index to start the search.\n");
emitter.print("	* @param high\n");
emitter.print("	* 			The index to stop the search.\n");
emitter.print("	* @return The index of the value.\n");
emitter.print("	*/\n");
emitter.print("	public static int indexOf" + colCamelName + "(Record[] data, " + colType + " key, int low, int high) {\n");
emitter.print("		if (low < 0) throw new IndexOutOfBoundsException(\"Index: \" + low + \", Size: \" + data.length);\n");
emitter.print("		\n");
emitter.print("    	int low1 = low;\n");
emitter.print("    	int high1 = high;\n");
emitter.print("    	\n");
emitter.print("		while (low1 <= high1) {\n");
emitter.print("		    int mid = (low1 + high1) >>> 1;\n");
emitter.print("		    " + colType + " midVal = data[mid]." + colName + ";\n");
emitter.print("		    int cmp = midVal == key ? 0 : (midVal < key ? " + colOrderBelow + " : " + colOrderAbove + ");\n");
emitter.print("	\n");
emitter.print("		    if (cmp < 0) {\n");
emitter.print("				low1 = mid + 1;\n");
emitter.print("			} else if (cmp > 0) {\n");
emitter.print("				high1 = mid - 1;\n");
emitter.print("			} else {\n");
emitter.print("				return mid; /* key found */\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		return low1 == 0 ? 0 : low1 - 1; /* key not found */\n");
emitter.print("    }\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #indexOf" + colCamelName + "(RandomCursor, " + colType + ", long, long)} but searches on the whole file.\n");
emitter.print("	*\n");
emitter.print("	* @param cursor\n");
emitter.print("	*			Random cursor used to search the value.\n");
emitter.print("	* @param key\n");
emitter.print("	*			The value to search.\n");
emitter.print("	* @return The index of the value.\n");
emitter.print("	*\n");
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	*/\n");
emitter.print("	public long indexOf" + colCamelName + "(RandomCursor cursor, " + colType + " key) throws IOException {\n");
emitter.print("		return indexOf" + colCamelName + "(cursor, key, 0, _size - 1);\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* <p>\n");
emitter.print("	* From position <code>low</code> to <code>high</code>, this method finds the index \n");
emitter.print("	* of the record with the " + colName + " value closer to the given <code>key</code>. \n");
emitter.print("	* This method uses a binary search algorithm (like <code>Arrays.binarySearch(...)</code>), so it assumes all the records \n");
emitter.print("	* are ordered by the <code>" + colName + "</code> order specified in the column definition. \n");
emitter.print("	* </p>\n");
emitter.print("	* <p>\n");
emitter.print("	* In MDB there is not any type of \"indexing\" or \"automatic sorting\" of the data, \n");
emitter.print("	* binary searches is the fast way used to find values.\n");
emitter.print("	* Usually, to retrieve certain range of data, first you get the start and stop positions\n");
emitter.print("	* (with this method), and then you create a cursor.  \n");
emitter.print("	* </p>\n");
emitter.print("	*\n");
emitter.print("	* @param cursor\n");
emitter.print("	*			The random cursor used to find the value.\n");
emitter.print("	* @param key\n");
emitter.print("	* 			The value to find.\n");
emitter.print("	* @param low\n");
emitter.print("	* 			The index to start the search.\n");
emitter.print("	* @param high\n");
emitter.print("	* 			The index to stop the search.\n");
emitter.print("	* @return \n");
emitter.print("	*			The index of the value.\n");
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	*/\n");
emitter.print("	public long indexOf" + colCamelName + "(RandomCursor cursor, " + colType + " key, long low, long high) throws IOException {\n");
emitter.print("		if (!_basic) _readLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			if (low < 0) throw new IndexOutOfBoundsException(\"Index: \" + low + \", Size: \" + _size);\n");
emitter.print("			\n");
emitter.print("			long low1 = low;\n");
emitter.print("			\n");
emitter.print("			if (!_basic) {\n");
emitter.print("				if (low1 >= _rbufPos || high >= _rbufPos) {\n");
emitter.print("					Record r = _rbuf[0];\n");
emitter.print("					if (_memory || (r." + colName + " == key ? 0 \n");
emitter.print("							: (r." + colName + " < key ? " + colOrderBelow + " : " + colOrderAbove + ")) <= 0) {\n");
emitter.print("						/* search in memory */\n");
emitter.print("						return _rbufPos + indexOf" + colCamelName + "(_rbuf, key, 0, (int) Math.min(high - _rbufPos, _rbufSize - 1));\n");
emitter.print("					}\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("		\n");
emitter.print("			assert !_memory;		\n");
emitter.print("				\n");
emitter.print("			/* search in file */\n");
emitter.print("			long high2 = high >= _rbufPos ? _rbufPos - 1 : high;\n");
emitter.print("		\n");
emitter.print("			FileChannel channel = cursor._channel;\n");
emitter.print("			ByteBuffer buffer = cursor._buffer_" + col.getName() + ";\n");
emitter.print("				\n");
emitter.print("			while (low1 <= high2) {\n");
emitter.print("				final long mid = (low1 + high2) >>> 1;\n");
emitter.print("				channel.position(mid * " + rowSize + " + " + colOffset + ");\n");
emitter.print("				buffer.rewind();\n");
emitter.print("				channel.read(buffer);\n");
emitter.print("				buffer.rewind();\n");
emitter.print("				\n");
emitter.print("				final " + colType + " midVal = buffer.get" + colGetName + "();\n");
emitter.print("				final int cmp = midVal == key ? 0 : (midVal < key ? " + colOrderBelow + " : " + colOrderAbove + ");\n");
emitter.print("				\n");
emitter.print("				if (cmp < 0) {\n");
emitter.print("					low1 = mid + 1;\n");
emitter.print("				}\n");
emitter.print("				else if (cmp > 0) {\n");
emitter.print("					high2 = mid - 1;\n");
emitter.print("				}\n");
emitter.print("				else {\n");
emitter.print("					return mid; /* key found */\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("			return low1 == 0 ? 0 : low1 - 1; /* key not found */\n");
emitter.print("		} finally {\n");
emitter.print("			if (!_basic) _readLock.unlock();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #indexOf" + colCamelName + "_exact(Record[], " + colType + ", int, int)}, \n");
emitter.print("	* but searches on the whole array.\n");
emitter.print("	* @param data The array of data.\n");
emitter.print("	* @param key The value to search in the array.\n");
emitter.print("	* @return The index of <code>key</code>, or a number &lt; 0 if the key is not found.\n");
emitter.print("	*/\n");
emitter.print("	public static int indexOf" + colCamelName + "_exact(Record[] data, " + colType + " key) {\n");
emitter.print("		return indexOf" + colCamelName + "_exact(data, key, 0, data.length);\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #indexOf" + colCamelName + "(Record[], " + colType + ", int, int)} \n");
emitter.print("	* but it looks for the exact value, if the value does not exist, returns a number &lt; 0.\n");
emitter.print("	*\n");
emitter.print("	* @param data\n");
emitter.print("	* 			Array or records.\n");
emitter.print("	* @param key\n");
emitter.print("	* 			The value to find the index.\n");
emitter.print("	* @param low\n");
emitter.print("	* 			The start position of the search.\n");
emitter.print("	* @param high\n");
emitter.print("	* 			The stop position of the search.\n");
emitter.print("	*\n");
emitter.print("	* @return The index of <code>key</code>, or a number &lt; 0 if the key is not found.\n");
emitter.print("	*/\n");
emitter.print("	public static int indexOf" + colCamelName + "_exact(Record[] data, " + colType + " key, int low, int high) {\n");
emitter.print("		if (low < 0) throw new IndexOutOfBoundsException(\"Index: \" + low + \", Size: \" + data.length);\n");
emitter.print("		\n");
emitter.print("    	int low1 = low;\n");
emitter.print("    	int high2 = high;\n");
emitter.print("		while (low1 <= high2) {\n");
emitter.print("		    int mid = (low1 + high2) >>> 1;\n");
emitter.print("		    " + colType + " midVal = data[mid]." + colName + ";\n");
emitter.print("		    int cmp = midVal == key ? 0 : (midVal < key ? " + colOrderBelow + " : " + colOrderAbove + ");\n");
emitter.print("	\n");
emitter.print("		    if (cmp < 0) {\n");
emitter.print("		    	low1 = mid + 1;\n");
emitter.print("			} else if (cmp > 0) {\n");
emitter.print("				high2 = mid - 1;\n");
emitter.print("			} else {\n");
emitter.print("				return mid; /* key found */\n");
emitter.print("			}\n");
emitter.print("		}\n");
emitter.print("		return -(low1 + 1); /* key not found */\n");
emitter.print("    }\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	* Like {@link #indexOf" + colCamelName + "(RandomCursor, " + colType + ", long, long)} \n");
emitter.print("	* but it looks for the exact value, if the value does not exist, returns a number &lt; 0.\n");
emitter.print("	*\n");
emitter.print("	* @param cursor\n");
emitter.print("	*			The cursor used to find the value.\n");
emitter.print("	* @param key\n");
emitter.print("	* 			The value to find the index.\n");
emitter.print("	* @param low\n");
emitter.print("	* 			The start position of the search.\n");
emitter.print("	* @param high\n");
emitter.print("	* 			The stop position of the search.\n");
emitter.print("	*\n");
emitter.print("	* @return The index of <code>key</code>, or a value &lt; 0 if the key is not found.\n");
emitter.print("	* @throws IOException If there is any I/O error.	\n");
emitter.print("	*/\n");
emitter.print("	public long indexOf" + colCamelName + "_exact(RandomCursor cursor, " + colType + " key, long low, long high) throws IOException {\n");
emitter.print("		if (low < 0) throw new IndexOutOfBoundsException(\"Index: \" + low + \", Size: \" + _size);\n");
emitter.print("		\n");
emitter.print("		long low1 = low;\n");
emitter.print("		if (!_basic) _readLock.lock();\n");
emitter.print("		try {\n");
emitter.print("			if (!_basic) {\n");
emitter.print("				if (low1 >= _rbufPos || high >= _rbufPos) {\n");
emitter.print("					Record r = _rbuf[0];\n");
emitter.print("					if (_memory || (r." + colName + " == key ? 0 \n");
emitter.print("							: (r." + colName + " < key ? " + colOrderBelow + " : " + colOrderAbove + ")) <= 0) {\n");
emitter.print("						/* search in memory */\n");
emitter.print("						long index = indexOf" + colCamelName + "_exact(_rbuf, key, 0, (int) Math.min(high - _rbufPos, _rbufSize - 1));\n");
emitter.print("						return index < 0? -_rbufPos + index /* key no found */ : _rbufPos + index;\n");
emitter.print("					}\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("		\n");
emitter.print("			assert !_memory;\n");
emitter.print("			\n");
emitter.print("			/* search in file */\n");
emitter.print("	\n");
emitter.print("			long high2 = high >= _rbufPos ? _rbufPos - 1 : high;\n");
emitter.print("		\n");
emitter.print("			FileChannel channel = cursor._channel;\n");
emitter.print("			ByteBuffer buffer = cursor._buffer_" + col.getName() + ";\n");
emitter.print("				\n");
emitter.print("			while (low1 <= high2) {\n");
emitter.print("				final long mid = (low1 + high2) >>> 1;\n");
emitter.print("				\n");
emitter.print("				channel.position(mid * " + rowSize + " + " + colOffset + ");\n");
emitter.print("				buffer.rewind();\n");
emitter.print("				channel.read(buffer);\n");
emitter.print("				buffer.rewind();\n");
emitter.print("				\n");
emitter.print("				final " + colType + " midVal = buffer.get" + colGetName + "();\n");
emitter.print("				final int cmp = midVal == key ? 0 : (midVal < key ? " + colOrderBelow + " : " + colOrderAbove + ");\n");
emitter.print("				\n");
emitter.print("				if (cmp < 0) {\n");
emitter.print("					low1 = mid + 1;\n");
emitter.print("				}\n");
emitter.print("				else if (cmp > 0) {\n");
emitter.print("					high2 = mid - 1;\n");
emitter.print("				}\n");
emitter.print("				else {\n");
emitter.print("					return mid; /* key found */\n");
emitter.print("				}\n");
emitter.print("			}\n");
emitter.print("		\n");
emitter.print("			return -(low1 + 1); /* key not found */\n");
emitter.print("		} finally {\n");
emitter.print("			if (!_basic) _readLock.unlock();\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * Like {@link #indexOf" + colCamelName + "_exact(RandomCursor, " + colType + ", long, long)}, but searches on the whole file.\n");
emitter.print("	 *\n");
emitter.print("	 * @param cursor The random cursor used to find the value.\n");
emitter.print("	 * @param key The value to search.\n");
emitter.print("	 * @return The index of the value. \n");
emitter.print("	 * @throws IOException If there is any I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public long indexOf" + colCamelName + "_exact(RandomCursor cursor, " + colType + " key) throws IOException {\n");
emitter.print("		return indexOf" + colCamelName + "_exact(cursor, key, 0, _size - 1);\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Search the index of the given " + colName + " and then truncate the database in that position.\n");
emitter.print("	*\n");
emitter.print("	* @param randCursor The cursor used to find the position of " + colName + ".\n");
emitter.print("	* @param " + colName + " Truncate the file in the index of this value.\n");
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	*/\n");
emitter.print("	public void truncate" + colCamelName + "(RandomCursor randCursor, " + colType + " " + colName + ") throws IOException {\n");
emitter.print("		if (_size > 0) {\n");
emitter.print("			long len = indexOf" + colCamelName + "(randCursor, " + colName + ");\n");
emitter.print("			if (len > 0) {\n");
emitter.print("				len--;\n");
emitter.print("			}\n");
emitter.print("			while (len < _size) {\n");
emitter.print("				randCursor.seek(len);\n");
emitter.print("				if (randCursor." + colName + " > " + colName + ") {\n");
emitter.print("					break;\n");
emitter.print("				}\n");
emitter.print("				len++;\n");
emitter.print("			}\n");
emitter.print("			truncate(len);\n");
emitter.print("		}\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	* Find the record with <code>" + colName + "</code> equals to the given value.\n");
emitter.print("	* \n");
emitter.print("	* @param cursor The random cursor used to find the indexes.\n");
emitter.print("	* @param " + colName + " The value to find.\n");
emitter.print("	*\n");
emitter.print("	* @return The found record or <code>null</code> if there is not any record with that value.\n");
emitter.print("	* @throws IOException If there is any I/O error.\n");
emitter.print("	*/\n");
emitter.print("	public Record findRecord_where_" + colName + "_is(RandomCursor cursor, " + colType + " " + colName + ") throws IOException {\n");
emitter.print("		if (_size > 0) {\n");
emitter.print("			long i = indexOf" + colCamelName + "_exact(cursor, " + colName + ");\n");
emitter.print("			if (i < 0) {\n");
emitter.print("				return null;\n");
emitter.print("			}\n");
emitter.print("			cursor.seek(i);\n");
emitter.print("			Record r = cursor.toRecord();\n");
emitter.print("			assert r." + colName + " == " + colName + ";\n");
emitter.print("			return r;\n");
emitter.print("		}\n");
emitter.print("		return null;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * Count the number of records between the " + colName + " values <code>keyLower</code> and <code>keyUpper</code>.\n");
emitter.print("	 * If there are not records with those values, then it returns an approximation.\n");
emitter.print("	 *\n");
emitter.print("	 * @param cursor The random cursor used to find the indexes.\n");
emitter.print("	 * @param keyLower The value to start counting.\n");
emitter.print("	 * @param keyUpper The value to stop counting.\n");
emitter.print("	 * @return The number of records between the given values.\n");
emitter.print("	 *\n");
emitter.print("	 * @throws IOException If there is any I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public long count" + colCamelName + "(RandomCursor cursor, " + colType + " keyLower, " + colType + " keyUpper) throws IOException {\n");
emitter.print("		if (_memory) {\n");
emitter.print("			long high = _size - 1;\n");
emitter.print("			long a = indexOf" + colCamelName + "(null, keyLower, 0L, high);\n");
emitter.print("			long b = indexOf" + colCamelName + "(null, keyUpper, 0L, high);\n");
emitter.print("			return b - a;\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		long high = _size - 1;\n");
emitter.print("		long a = indexOf" + colCamelName + "(cursor, keyLower, 0L, high);\n");
emitter.print("		long b = indexOf" + colCamelName + "(cursor, keyUpper, 0L, high);\n");
emitter.print("		return b - a;\n");
emitter.print("	}\n");
	} /* is !order.none */
 } /* foreach col */
emitter.print("\n");
emitter.print("\n");
emitter.print("	@SuppressWarnings(\"unchecked\")\n");
emitter.print("	@Override\n");
emitter.print("	public " + schemaName + "MDBSession getSession() {\n");
emitter.print("		return _session;\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * Get the buffer. Warning: do not use this if you don't know what are you\n");
emitter.print("	 * doing.\n");
emitter.print("	 * \n");
emitter.print("	 * @return The memory buffer.\n");
emitter.print("	 */\n");
emitter.print("	@Override\n");
emitter.print("	public Record[] getRecentRecordsBuffer() {\n");
emitter.print("		return _rbuf;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * The number of opened cursors. You can use this to \"debug\" your programs.\n");
emitter.print("	 * \n");
emitter.print("	 * @return The number of cursors.\n");
emitter.print("	 */\n");
emitter.print("	@Override\n");
emitter.print("	public int getOpenCursorCount() {\n");
emitter.print("		return _openCursorCount.get();\n");
emitter.print("	}\n");
emitter.print("\n");
emitter.print("	/**\n");
emitter.print("	 * Delete the associated files. Remember to close the cursors before to\n");
emitter.print("	 * perform this operation. If this MDB instance was created with a session,\n");
emitter.print("	 * do not call this method, else the {@link MDBSession#closeAndDelete()}\n");
emitter.print("	 * method.\n");
emitter.print("	 * \n");
emitter.print("	 * @return <code>true</code> if all files was deleted.\n");
emitter.print("	 */\n");
emitter.print("	 @Override\n");
emitter.print("	public boolean deleteFiles() {\n");
emitter.print("		if (_memory) {\n");
emitter.print("			_rbufSize = 0;\n");
emitter.print("			return true;\n");
emitter.print("		}\n");
emitter.print("\n");
emitter.print("		boolean result = true;\n");
 if (hasArray) {
emitter.print("			File arrayFile = getArrayFile();\n");
emitter.print("			if (arrayFile.exists() && !arrayFile.delete()) {\n");
emitter.print("				java.lang.System.err\n");
emitter.print("						.println(\"Cannot delete file \" + arrayFile);\n");
emitter.print("				result = false;\n");
emitter.print("			}\n");
 }
emitter.print("		File file = getFile();\n");
emitter.print("		if (file.exists() && !file.delete()) {\n");
emitter.print("			java.lang.System.err.println(\"Cannot delete file \" + file);\n");
emitter.print("			result = false;\n");
emitter.print("		}\n");
emitter.print("		return result;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * The number of records.\n");
emitter.print("	 * \n");
emitter.print("	 * @return The number of rows.\n");
emitter.print("	 * @throws IOException If there is an I/O error.\n");
emitter.print("	 */\n");
emitter.print("	@Override\n");
emitter.print("	public long size() throws IOException {		\n");
emitter.print("		return _size;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * The number of records already persisted in the file system.\n");
emitter.print("	 * \n");
emitter.print("	 * @return The size in rows.\n");
emitter.print("	 * @throws IOException If there is an I/O error.\n");
emitter.print("	 */\n");
emitter.print("	public long fsize() throws IOException {		\n");
emitter.print("		return _memory? 0 : _file.length() / " + rowSize + ";\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * The number of records in the buffer.\n");
emitter.print("	 * \n");
emitter.print("	 * @return Count buffer records.\n");
emitter.print("	 */\n");
emitter.print("	@Override\n");
emitter.print("	public int getRecentRecordsCount() {\n");
emitter.print("		return _rbufSize;\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override\n");
emitter.print("	public RandomCursor thread_randomCursor() throws IOException {\n");
emitter.print("		return (RandomCursor) super.thread_randomCursor();\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	@Override\n");
emitter.print("	public Cursor thread_cursor() throws IOException {\n");
emitter.print("		return (Cursor) super.thread_cursor();\n");
emitter.print("	}\n");
emitter.print("	\n");
emitter.print("	/**\n");
emitter.print("	 * Truncate the file to the number of rows <code>len</code>.\n");
emitter.print("	 * \n");
emitter.print("	 * @param len\n");
emitter.print("	 *            The desired number of rows.\n");
emitter.print("	 */\n");
emitter.print("	@Override\n");
emitter.print("	public void truncate(long len) throws IOException {\n");
emitter.print("		if (len < 0 || len > _size) {\n");
emitter.print("			throw new IllegalArgumentException(\"Cannot truncate to \" + len + \", value out of range.\");\n");
emitter.print("		}\n");
emitter.print("		\n");
emitter.print("		if (!_basic) _writeLock.lock();\n");
emitter.print("		\n");
emitter.print("		try {\n");
emitter.print("			if (_memory) {\n");
emitter.print("				_rbufSize = (int) len;\n");
emitter.print("				return;\n");
emitter.print("			}\n");
emitter.print("\n");
emitter.print("			long newLen = len * " + table.getRowSize() + ";\n");
emitter.print("			appender();\n");
emitter.print("			_appender.flush();\n");
emitter.print("			FileChannel channel = _appender._channel;\n");
emitter.print("			channel.truncate(newLen);\n");
emitter.print("			\n");
 for(Column col : table) {		
 	if (col.getType().isArray() && !col.isVirtual()) {
emitter.print("				\n");
emitter.print("			long newArrLen = 0;\n");
emitter.print("				\n");
emitter.print("			if (newLen > 0) {				\n");
 emitter.print("				// Truncate the array file \n");		
emitter.print("				long startPos = newLen - " + table.getRowSize() + ";   \n");
emitter.print("				ByteBuffer buf = ByteBuffer.wrap(new byte[" + (Integer.SIZE + Long.SIZE) / 8 + "]);				\n");
emitter.print("				channel.read(buf, startPos + " + col.getOffset() + ");\n");
emitter.print("				buf.rewind();\n");
emitter.print("				long arrPos = buf.getLong();\n");
emitter.print("				int arrLen = buf.getInt();			\n");
emitter.print("				newArrLen = arrPos + arrLen;					\n");
emitter.print("			}\n");
emitter.print("				\n");
emitter.print("			_appender._arrayChannel.truncate(newArrLen);\n");
		break;
	}		
}					
emitter.print("			if (!_basic) {\n");
emitter.print("				_rbufPos = fsize();\n");
emitter.print("			}\n");
emitter.print("		} finally {\n");
emitter.print("			_size = fsize() + _rbufSize;\n");
emitter.print("			if (!_basic) _writeLock.unlock();\n");
emitter.print("		}					\n");
emitter.print("	}\n");
emitter.print("\n");
 emitExt(TARGET_MDB_CLASS);
emitter.print("}	\n");
emitter.print("	\n");
emitter.print("\n");
	}
}