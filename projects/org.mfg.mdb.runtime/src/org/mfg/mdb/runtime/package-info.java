/**
 * 
 * <p>The MDB Runtime provides the API to create, modify and read the MDB files.</p>
 * <p>The runtime is composed by the library and the files (MDB classes) generated by the compiler:</p>
 * <table border=1>
 * 	<caption>MDB Runtime elements</caption>
 * <tr>
 * 	<td colspan="2" align="center" style="padding:1em"><b>MDB Runtime</b></td>
 * </tr>
 * <tr>
 * 	<td style="padding:1em"><p><b>Library</b></p>
 * 		<ul>
 * 			<li>
 * 				<code>com.mfg.mdb.runtime.jar</code><br><i>Contains the base classes which are extended by the generated code.</i>
 * 			</li>
 * 		</ul>
 * 	</td>
 * 	<td style="padding:1em">
 * 		<p><b>Generated classes</b></p>
 * 			<ul>
 * 			<li>MDB Session</li>
 * 			<li>MDB File Handlers</li>
 * 		</ul>
 * 	</td>
 * </tr>
 * </table>
 * 
 * <p>
 * 	<b>Index</b>
 * </p>
 * 
 * <ul>
 * 	<li><a href="#connect-db">Connect to a database (create, access, delete)</a></li>
 * 	<li><a href="#add-data">Add data to a file (appender)</a></li>
 * 	<li><a href="#read-data">Read data from file</a>
 * 		<ul>
 *  		<li><a href="#seq-cursor">Sequential cursors</a></li>
 *  		<li><a href="#ran-cursor">Random cursors</a></li>
 *  	</ul>
 * 	</li>
 * 	<li>
 *  	<a href="#reuse-cursor">Cursor re-usability</a>
 *  	<ul>
 *  		<li><a href="#session-cursor">Session cursors</a></li>
 *  		<li><a href="#thread-cursor">Thread cursors</a></li>
 *  	</ul>
 *  </li>
 *  <li><a href="#indexof-met">Find data: methods <code>indexOf()</code></a></li>  
 *  <li>
 *  	<a href="#select-met">Methods <code>select()</code></a>
 *  	<ul>
 *  		<li><a href="#select-where-met">Methods select_where()</a></li>
 *  	</ul>
 *  </li>
 *  <li><a href="#select-sparse-met">Methods <code>select_sparse()</code></a>
 *  	<ul>
 *  		<li><a href="#select-sparse-where">Methods select_sparse_where()</a></li>
 *  	</ul>
 *  </li>
 *  <li><a href="#count-met">Methods <code>first()</code>, <code>last()</code> and <code>count()</code></a></li>
 *  <li><a href="#virtual-cols">Virtual columns</a></li>
 *  <li><a href="#metadata">Metadata</a></li>
 *  <li><a href="#validators">Validators</a></li>
 *  <li><a href="#hybrid-db">Hybrid database: <code>SessionMode</code></a>
 *  	<ul>
 *  		<li><a href="#hybrid-db-memory">MEMORY</a></li>
 *  		<li><a href="#hybrid-db-read-write">READ_WRITE</a></li>
 *  		<li><a href="#hybrid-db-read-only">READ_ONLY</a></li>
 *  		<li><a href="#hybrid-db-basic-read-write">BASIC_READ_WRITE</a></li>
 *  	</ul>
 *  </li>
 *  <li>
 *  	<a href="#additional-features">Additional features</a>
 *  	<ul>
 *  		<li><a href="#mod-db">Modify a record: methods <code>replace()</code></a></li>
 *  		<li><a href="#truncate-file">Truncate a file</a></li>
 *  		<li><a href="#delete-file">Delete a file</a></li>
 *  		<li><a href="#backup-database">Backup database</a></li>
 *  	</ul>
 *  </li>
 * </ul>
 * <h2 id="connect-db">Connect to a database (create, access, delete)</h2>
 * <p>
 * In a MDB a connection is established through the "MDB session" class. 
 * This class is generated by the compiler and has the name of the database schema plus the "MDBSession" posfix.
 * For example, if you schema name is "Demo", the session class is "DemoMDBSession".
 * </p>
 * <p>
 * In MDB a database is the folder where the MDB files are. Connect to a database is connect
 * to that folder: 
 * </p>
 * <pre>
 * // a name can be used in debug messages 
 * String sessionName = "demo-session";
 * 
 * // folder where the mdb files are 
 * File dbPath = new File("/path/to/db");
 * 
 * // connect to the database 
 * DemoMDBSession session = new DemoMDBSession(sessionName, dbPath);
 * </pre>
 * <p>
 * Now that the session is created, if the folder does not exist, then it is created. To connect to a file there are the "connectTo" methods.
 * These methods are generated by the compiler and there are two methods 
 * per table defined in the schema. If there is a table "Price" and "Pivot", then
 * are generated the methods "connectTo_PriceMDB" and "connectTo_PivotMDB": 
 * </p>
 * <pre>
 * public PriceMDB connectTo_PriceMDB(String filename, int bufferSize);
 * public PriceMDB connectTo_PriceMDB(String filename);
 * public PivotMDB connectTo_PivotMDB(String filename, int bufferSize);
 * </pre>
 * 
 * <p>
 * There are two versions of the same method, the difference is the <code>bufferSize</code> parameter.
 * With it you can change the default buffer size (number of records in memory). In the majority of the cases the default value is fine.
 * The other parameter is "filename", it is the relative path to the MDB file we want to connect to.
 * Remember that file is contained in the database (session) folder we are connected.
 * </p>
 * <p>
 * Note, the "connect" methods return an "MDB" instance, like "PriceMDB". These classes are generated
 * by the compiler and there are one "MDB" class per table. Each MDB file contains the data of one table,
 * so when we connect to a file, we get a file handler, an "MDB" instance, to manipulate that file.  
 * </p>
 * <p>
 * Finally, to close the session, there are the methods "close" and "closeAndDelete". 
 * As you can see, these methods are not generated by the compiler, they are inherited
 * from the in-library "MDBSession" class. As the name says, "closeAndDelete" closes the
 * session and delete it. Important, to close a session all cursors should be closed, else
 * you will get a {@link java.util.concurrent.TimeoutException}, since the session keeps waiting for all cursor to stop reading.
 * </p>
 * <p>
 * MDB has different connection modes, like an in-memory connection. more about this in the section
 * <a href="#hybrid-db">Hybrid database: <code>SessionMode</code></a>.
 * </p>
 * 
 * 
 * <h2 id="add-data">Add data to a file (appender)</h2>
 * 
 * <p>
 * To add data to a file in MDB we use an object we call "appender". As the name says, with this object you can add data (records) to the end of the file (or table).
 * For each table definition is generated an "MDB" class (for the table "Price" is generated a "PriceMDB" class). This class provides methods and inner classes to manipulate the files.
 * In this case, there is an inner class Appender that is the one we use to add the data.  
 * </p>
 * <p>
 * If we have a table definition like:
 * </p>
 * <table border="1" >
 * 	<caption>"Price" table definition</caption>
 * 	<tr>
 * 		<td style="padding:0.5em"><b>COLUMN</b></td>
 * 		<td style="padding:0.5em"><b>TYPE</b></td>
 * 	</tr>
 * <tr>
 * 		<td style="padding:0.5em">time</td>
 * 		<td style="padding:0.5em">LONG</td>
 * 	</tr>
 * <tr>
 * 		<td style="padding:0.5em">price</td>
 * 		<td style="padding:0.5em">LONG</td>
 * 	</tr>
 * </table>
 * <p>
 * The the generated MDB class will have a method "appender" and an inner class "Appender" like this:
 * </p>
 * <pre>
 * class PriceMDB extends MDB {
 * 	...
 * 	public class Appender implements IAppender&lt;Record&gt; {
 * 		public long time;
 * 		public long price;
 * 		
 * 		public void append() {
 * 			...
 * 		}
 * 		...
 * 	}
 * 	public Appender appender() throws IOException {
 * 		...
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Look the "Appender" class contains two fields, "time" and "price", which are the same we used in the table definition. They are used to set the data we want to append.
 * This is a major advantage of MDB in comparison with dynamic APIs like the one used in JDBC. Then, let's say we want to write a method to add an array of data: 
 * </p>
 * <pre>
 * void addData(DemoMDBSession session, long[] times, long[] prices) {
 * 	PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * 	PriceMDB.Appender appender = mdb.appender(); 
 * 	for (int i = 0; i &lt; times.length; i ++) {
 * 		appender.time = times[i];
 * 		appender.price = prices[i];
 * 		appender.append();
 * 	}
 * }
 * </pre>
 * <p>
 * Going by steps:
 * </p>
 * <ul>
 * 	<li>
 * 		<p>
 * 			Write a method "addData" that receives a session -like the one we created in the section 
 * <a href="#connect-db">Connect to a database (create, access, delete)</a>-, and the arrays of data.
 * 		</p>
 * 	</li>
 * 	<li>
 * 		<p>
 * 			Connect to an MDB file "myprices.mdb" with the method "connectTo_PriceMDB". If that file does not exist then it is created. * 			
 * 		</p>
 * 	</li>
 * 	<li>
 * 		<p>
 * 			When we connect to the file we get an MDB instance ("mdb"), to that instance we request an appender with the method "appender()". 
 * This method returns the "PriceMDB.Appender" instance we will use to add the data. This appender is unique for each file and it is not thread safe,
 * you should ensure only one thread will use the appender at the same time. 			
 * 		</p>
 * 	</li>
 * <li>
 * 		<p>
 * 			One time we get the appender, we iterate the data arrays, from a logic point of view, we iterate the rows, then we set the row values ("time" and "price") to the appender,
 * 			using the generated appender fields, and finally, to append the row, we call the "append()" method.  			
 * 		</p>
 * 	</li>
 * <li>
 * 		<p>
 * 			The appender is a resource, is closeable, but you should not close it by yourself, it will be closed automatically by the session at the session shutdown.
 * 		</p>
 * 	</li>
 * </ul>	
 * <p>
 * By default, MDB appends the records to  memory buffer. This buffer is also available to readers so the data will be accessible at the moment, if and only if, you use the same session.
 * Then, when the buffer is full it is written to the file system. However, if you want to force to write the buffer to the file, you can call the method "Appender.flush()".
 * </p>
 * <p>
 * The appender has different type of "append" methods:
 * </p>
 * 
 * <table border="1">
 * 	<caption><code>append(..)</code> methods</caption>
 * 	<tr>
 * 		<td style="padding:0.5em"><b>METHOD SIGNATURE</b></td>
 * 		<td style="padding:0.5em"><b>PURPOSE</b></td>
 * 	</tr>
 * 	<tr>
 * 		<td style="padding:0.5em"><code>append()</code></td>
 * 		<td style="padding:0.5em">This method add to the file the values assigned to the appender fields "time" and "price".</td>
 * 	</tr>
 * 	<tr>
 * 		<td style="padding:0.5em"><code>append(Record)</code></td>
 * 		<td style="padding:0.5em">This method receives a Record instance. The class Record is an inner class of "PriceMDB" generated by the compiler, and like the appender, it contains
 * a field definition per column definition, like "time" and "price". So this append(Record) method adds to the file a copy the record. For example, you can use
 * the Record class to keep an array of data in your internal application model, so we can write the "addData" method in this way: 
 * 			<pre>
 * void addData(DemoMDBSession session, PriceMDB.Record[] rows) {
 * 	PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * 	PriceMDB.Appender appender = mdb.appender(); 
 * 	for (PriceMDB.Record row : rows) {
 * 		appender.append(row);
 * 	}
 * }
 * 			</pre> 
 * 		</td>
 * 	</tr>
 * 	<tr>
 * 		<td style="padding:0.5em">
 * 		<code>append_ref_unsafe(Record r)</code>
 * 		</td>
 * 		<td style="padding:0.5em">
 * This is a very dangerous method and we recommend you don't use it unless you know very well what are you doing. 
 * This method puts the given record directly into the buffer, and this can create problems if there are other readers (cursors)
 * reading the data at that time. This can be use by advanced users to perform faster operations.   
 * 		</td>
 * 	</tr>
 * </table>
 * 
 * <h2 id="read-data">Read data from file</h2>
 * <p>
 * The low level API on MDB to read files are the cursors. There are sequential and random access cursors.
 * Also there are "high level" methods that uses the cursors to provide common tasks, like search a value
 * or retrieve an array of data.  
 * </p>
 * <p>
 * As the appender, the cursors are requested to a file connection, to an "MDB" instance. You can request many cursors
 * for the same file and use them in different threads. A cursor can be temporal or a session cursor. Temporal cursors
 * should be closed by the user, and session cursors are closed by the session at the end of the session. The developer
 * has to ensure all temporal cursors are closed before to end the session.
 * </p>
 * 
 * <h3 id="seq-cursor">Sequential cursors</h3>
 * <p>
 * A sequential cursor is used to retrieve sequential data, for example, all records from position 10 to 1000:
 * </p>
 * <pre>
 * DemoSession session = ...;
 * PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * try(<b>PriceMDB.Cursor cursor = mdb.cursor(10, 1000)</b>) {
 * 	while(<b>cursor.next()</b>) {
 * 		System.out.println(<b>cursor.time</b> + " " + <b>cursor.price</b>);
 * 	}
 * }
 * </pre>
 * <p>
 * Going by steps:
 * </p>
 * <ul>
 * 	<li>Get a session, like the one in the previous examples.</li>
 * 	<li>Connect ("connectTo_PriceMDB") to the file we want to read, in the same way we did with the appender.</li>
 * 	<li>Request a cursor to read from position 10 to 1000. We did it with the "cursor" method of the PriceMDB class.</li>
 * 	<li>We want to use the cursor just for a moment, to print the data, so it is a temporal cursor. 
 * Because the cursor is a Java resource, we enclose it in a "try-with" clause. This will close the cursor
 * automatically.</li>
 * 	<li>A cursor works like an iterator, we iterate the records with the "next" method, until the result is false.
 * Each time we call the "next" method, the next record is fetched, and the cursor gets the row's values. 
 * Note the cursor, like the appender, is an inner class of PriceMDB that was generated by the compiler, and
 * has one field per columns ("time" and "price").
 * 	</li>
 * 	<li>Finally, to print the values, we access the "time" and "price" fields of the cursor. These fields are updated each time
 * the "next" method is called.
 * 	</li>
 * </ul>
 * 
 * <h3 id="ran-cursor">Random cursors</h3>
 * 
 * <p>
 * With random cursors are cursors with random access, for example, to read a record at position 20 and then a record position
 * 1015. We do this distinctions because in hard drives the data is read sequentially, and therefore this make sequential cursors
 * faster, because them read a chunk of data in one file access and put it in a memory buffer. Random access cursors
 * access the file system each time a record is read, for that reason it is slower.   
 * </p>
 * <p>
 * However, there are many cases where a random cursor is more suitable than a sequential cursor, because the data
 * we want to read is spread in the file. This is how we use a random cursor:
 * </p>
 * <pre>
 * DemoSession session = ...;
 * PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * try(PriceMDB.RandomCursor cursor = mdb.randomCursor()) {
 * 	<b>cursor.seek(20);</b>
 * 	System.out.println("The record at position 10 is " + <b>cursor.time</b> + ", " + <b>cursor.price</b>);
 * 
 * 	<b>cursor.seek(1015);</b>
 * 	System.out.println("The record at position 15 is " + <b>cursor.time</b> + ", " + <b>cursor.price</b>);
 * }
 * </pre>
 * <p>To explain it:</p>
 * <ul>
 * 	<li>As in sequential cursors, we get a session and connect to the file.</li>
 * 	<li>We request a random cursor, with the "randomCursor" method.</li>
 * 	<li>We want to use the cursor just to read some data at certain moment, so it is a temporal cursor and we should close it.
 * Since the cursor is a Java resource, we put it in a "try-with" clause, to close it automatically.</li>
 * 	<li>To read the record X, first we place the cursor at that position, with the "seek" method.</li>
 * 	<li>When the "seek" method is executed, the data of the record X is read and set into the cursor "time" and "price" field. The random cursor
 * follows the same philosophy of the appender and the sequential cursor, it is represented by an inner class of PriceMDB, that is generated
 * by the compiler, and for each column of the table definition, is generated a field, with the same name and type.</li>
 * 	<li>Using the "time" and "price" fields of the cursor then we print the row's values.</li>
 * </ul>
 * 
 * 
 * <h2 id="reuse-cursor">Cursor re-usability</h2>
 * 
 * <p>
 * Cursors are reusable. Is possible the majority of the time you only need a temporal cursor, since applications are not reading the database all the time.
 * However there are cases where create temporal cursors is not an optimal solution. For example, in MFG we use MDB to feed the MFG's real-time chart.
 * In a real-time chart, the database is read with a high frequency, and the database access should run as fast as possible. If we create
 * a temporal cursor each time we need to read the data, then it slows the chart painting process, cause all file-system operations are very expensive.
 * Then the solution to this is to reuse the cursors. 
 * </p>
 * <p>
 * To reuse a cursor just keep it open, don't close it, don't call the "close" method, don't put it in a "try-with" clause. In case of a sequential cursor,
 * to reuse it also you have to call the "reset" method ({@linkplain org.mfg.mdb.runtime.ISeqCursor#reset(long, long)}), to reposition the cursor:  
 * </p>
 * 
 * <pre>
 * public class ChartModel {
 * 	private PriceMDB.Cursor reusableCursor;
 * 
 * 	public ChartModel(PriceMDB mdb) {
 * 		<b>this.reusableCursor = mdb.cursor();</b>
 * 	}
 * 
 * 	public Points requestData(long startPosition, long endPosition) {
 * 		Points points = new Points();
 * 		<b>this.reusableCursor.reset(startPosition, endPosition);</b>
 * 		while (<b>this.reusableCursor.next()</b>) {
 * 			points.add(<b>this.reusableCursor.time</b>, <b>this.reusableCursor.price</b>);
 * 		}
 * 		return points;
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * The class above is a simple implementation of a chart model, it gets a connection to a file and keeps a reusable cursor. In the case of a random cursor it is the same,
 * the difference is that you do not have to reset the cursor, cause a random cursor is repositioned in each iteration, with the "seek" method, it is reusable by nature. 
 * </p>
 * <p>
 * Caution: but a reusable cursor should be closed when the owner-component is not used any more in the application, in any case, before to close the session associated
 * to the cursor. Remember all cursors should be closed before to close the session. A practical solution could be to "defer" the cursor by the session. It means, say to the session
 * to close the cursor automatically, in this way, the developer delegates to the session the responsibility of close the cursor. See the next section.      
 * </p>
 * 
 * <h3 id="session-cursor">Session cursors</h3>
 * 
 * <p>
 * As we mentioned in the previous section, we can reuse the same cursor during the live of the database session. To do this, the developer
 * should ensure the cursor is closed before the session, but this can be done automatically if the developer report the cursor as a session
 * cursor, as a deferred cursor, using the session method {@link org.mfg.mdb.runtime.MDBSession#defer(ICursor)}:
 * </p>
 * <p>
 * In the previous session we wrote an example of chart model class, we can change the constructor of that class to defer the cursor: 
 * </p>
 * <pre>
 * public ChartModel(PriceMDB mdb) {
 * 	this.reusableCursor = mdb.cursor();
 * 	DemoSession session = mdb.getSession();
 * 	<b>session.defer(this.reusableCursor);</b>
 * }
 * </pre>
 * 
 * <p>
 * In this way, the cursor will be closed automatically by the session. There are things you have to take in consideration:
 * </p>
 * <ul>
 * 	<li>If the cursor is to be used from time to time, probably the best is to use a temporal cursor, inside a "try-with" clause.</li>
 * 	<li>Don't close a session cursor yourself.</li>
 * </ul>
 * 
 * <h3 id="thread-cursor">Thread cursors</h3>
 * <p>
 * Thread cursors are session cursors that are attached to the current thread. We introduced them because we realized  
 * in lot of cases we access the database from the graphical toolkit thread. The majority of the graphical toolkits, like 
 * swing and SWT, use a single thread to execute UI events. And in many cases, the database is read because an UI event is executed,
 * or in the case of the chart, the chart painting method is also executed in this main UI thread.   
 * </p>
 * <p>
 * Then we created the thread cursors to make easy to the developer reuse the cursors in the same thread. To get a thread cursor you should 
 * use the methods {@link org.mfg.mdb.runtime.MDB#thread_cursor()} to get the sequential cursor attached to the current thread,
 * and {@link org.mfg.mdb.runtime.MDB#thread_randomCursor()} to the get the random cursor attached to the current thread. It means
 * instead of use the methods "cursor" and "randomCurso", to get the thread cursors, you should use "thread_cursor" 
 * and "thread_randomCursor".  
 * </p>
 * <p>
 * In MFG we do an intensive use of thread cursors, specially in the chart, because in this way we simplify our chart model, since we know
 * the chart is used always in the same SWT thread.
 * </p>
 * 
 * <h2 id="indexof-met">Find data: methods <code>indexOf()</code></h2>
 * <p>
 * The structure of MDB files is very simple, you can compare it with an array of records. So, if we want to find data in an array
 * what are the options we have? We have two main options: do a full scan of perform a binary search.    
 * </p>
 * <p>
 * Do a full scan of all the records can be practical in may cases but it can be very slow, in dependence of the size of the database.
 * However, binary searches can run very fast even in big databases. The problem with the binary searches, is that the data should be
 * ordered in the array.
 * </p>
 * <p>
 * In MDB we support binary search algorithms, but the data is never sorted, so these method should be used when the nature of the data
 * is added in order. In MFG, this is the case of the majority of the data, because the majority of the data are time series or indicators
 * added in the time, so this data is orderer by the time. 
 * </p>
 * <p>
 * The MDB compiler generates binary search methods when a column is defined as order:
 * </p>
 * 
 * <pre>
 * Compiler comp = ...;
 * comp.table("Price");
 * comp.column("time", Type.LONG, Order.ASCENDING);
 * comp.column("price", Type.LONG);
 * </pre>
 * 
 * <p>
 * In the above code, we define a "Price" table with two columns "time" and "price", 
 * but we declare "time" as an orderer column, with the "ascending" order. Then, the compiler
 * will generated a set of methods around the "time" column, in special, the method "indexOfTime":
 * </p>
 * 
 * <pre>
 * public long indexOfRealTime(RandomCursor cursor, long key, long low, long high) throws IOException {
 * 	...
 * }
 * </pre>
 * 
 * <p>
 * This "indexOf" method can be used to find the position of the record where the "time" value if the same of "key". This method
 * works pretty much like the {@link java.util.Arrays#binarySearch(Object[], Object)} method, but with the difference that
 * if the record is not found, then it will return the closest position. If you want to get the same behavior of the "binarySearch" method,
 * then you can use the "indexOfRealTime_exact" method, it returns a negative value in case the record is not found.
 * </p>
 * <p>
 * As demonstration, in the next code we write a method to print all the records inside a given time range: 
 * </p>
 * 
 * <pre>
 * public void printInRange(PriceMDB mdb, long lowerTime, long upperTime) {
 * 	try (PriceMDB.RandomCursor rndCursor = mdb.randomCursor()) {
 * 		long start = <b>mdb.indexOfTime(rndCursor, lowerTime)</b>;
 * 		long end = <b>mdb.indexOfTime(rndCursor, upperTime)</b>;
 * 		try(PriceMDB.Cursor cursor = mdb.cursor(start, end)) {
 * 			while(cursor.next()) {
 * 				System.out.println("Record " + cursor.time + ", " + cursor.price);
 * 			}
 * 		} 
 * 	}
 * }
 * </pre>
 * <p>Let's explain it by steps:
 * <ul>
 * 	<li>
 * 		The first you note is that we request a random cursor. This is because the "indexOf" method uses a random cursor to
 * read the records. As you know, in the binary search algorithm the data is read in different position, but not in a sequential order.
 * 	</li>
 * 	<li>
 * 		One time we have the random cursor we use it together with the "indexOfTime" method to get the position of the lower and upper times. This is the position in the database file. 
 * 	</li>
 * 	<li>
 * 		Here we know the data we want to print is between the "start" and "end" positions, so we request a sequential cursor to read from "start" to "end".  
 * 	</li>
 * 	<li>
 * 		Finally, we iterate the data with the sequential cursor and print it.
 * 	</li>
 * </ul>
 * 
 * <h2 id="select-met">Methods <code>select()</code></h2>
 * <p>We saw there are method built on top of cursors, like the "indexOf" method. Following that philosophy we have the "select" methods.
 * With these methods you can retrieve an array of record, without the need of iterate with a cursor:</p>
 * <pre>
 * public <b>PriceMDB.Record[]</b> readData(DemoSession session, long start, long end) {
 * 	PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * 	try (PriceMDB.Cursor cursor : mdb.cursor()) {
 * 		return <b>mdb.select(start, end)</b>;
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * You can use this method when you not only need to read the data else also keep it memory, for example, in a chart, you would like to access
 * the database only when there is a change, in the meantime you can store the shown data in a memory dataset. 
 * </p>
 * <p>
 * Note in the above example we used a temporal cursor that we pass to the "select" method. Never forget to close the cursors or defer them.
 * </p>
 * 
 * <h3 id="select-where-met">Methods <code>select_where()</code></h3>
 * 
 * <p>
 * The previous example can be write a method to get the data between two positions, but imagine we want to get the data between two time values.
 * To do this, we can first find the index of the lower and upper values, and then use the "select" method to retrieve the data:
 * </p>
 * 
 * <pre>
 * public PriceMDB.Record[] readBetween(DemoSession session, long lowerTime, long upperTime) {
 * 	PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * 	try (PriceMDB.RandomCursor rndCursor = mdb.randomCursor()) {
 * 		long start = mdb.indexOfTime(lowerTime);
 * 		long stop = mdb.indexOfTime(upperTime);
 * 		try (PriceMDB.Cursor cursor : mdb.cursor()) {
 * 			return mdb.select(start, end);
 * 		}
 * 	}
 * }
 * </pre>
 * <p>
 * In "readBetween" method is pretty similar to the "readData", the difference is that first we have to find the "start" and "stop" positions
 * using the "indexOf" methods. However, you can use the "select_where" method, it does the same thing for us: 
 * </p>
 * 
 * <pre>
 * public PriceMDB.Record[] readBetween(DemoSession session, long lowerTime, long upperTime) {
 * 	PriceMDB mdb = session.connectTo_PriceMDB("myprices.mdb");
 * 	try (PriceMDB.RandomCursor rndCursor = mdb.randomCursor()) {
 * 		try (PriceMDB.Cursor cursor : mdb.cursor()) {
 * 			return <b>mdb.select_where_Time_in(rndCursor, cursor, start, end)</b>;
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Note the "select_where" method is generated by the compiler for each ordered column definition. If you look the method implementation
 * it uses a random cursor to pass it to the "indexOf" methods, and the sequential cursor to get the sequential data. 
 * </p>
 * 
 * <h3 id="select-sparse-met">Methods <code>select_sparse()</code></h3>
 * <h3 id="select-sparse-where">Methods <code>select_sparse_where()</code></h3>
 * 
 * @author arian
 */
package org.mfg.mdb.runtime;
