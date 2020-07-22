package org.mfg.mdb.compiler;

import static java.lang.System.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import org.mfg.mdb.compiler.internal.ExtensionRegistry;
import org.mfg.mdb.compiler.internal.emitters.MDBClassGenerator;
import org.mfg.mdb.compiler.internal.emitters.OpenPrinter;
import org.mfg.mdb.compiler.internal.emitters.SessionClassGenerator;

/**
 * <p>
 * This is the main class of MDB, use this to define a database schema and
 * compile it.
 * <p>
 * You can extend this class and call the schema methods like a script:
 * </p>
 * 
 * <pre>
 * class Demo {
 * 	Demo() {
 * 		schemaName(&quot;Stock&quot;);
 * 		packageName(&quot;com.stock.mdb&quot;);
 * 
 * 		table(&quot;Price&quot;);
 * 		column(&quot;time&quot;, Type.LONG, Order.ASCENDING);
 * 		column(&quot;price&quot;, Type.LONG);
 * 	}
 * 
 * 	public static void main(String[] args) {
 * 		Demo demo = new Demo();
 * 		demo.compile(&quot;/path/to/demo/source&quot;);
 * 	}
 * }
 * </pre>
 * <p>
 * However, if you like to use a visual tool to define and compile the schema
 * you may be interested in the {@link SchemaEditor} class.
 * </p>
 * 
 * @see SchemaEditor
 * @author arian
 * @author Antonio de Rojas
 * 
 */
public class Compiler {

	private final Schema _schema;
	private Table _lastTable;
	private String _sourcesDir;
	private String _packageName;
	private int _bufferSize;
	private int _offset;
	private ExtensionRegistry _registry;

	/**
	 * Creates a database compiler.
	 * 
	 * @param schemaName
	 *            The schema name. It is used for generate the mdb session
	 *            class.
	 * @param sourcesDir
	 *            The source path (usually the 'src' forder) of the target
	 *            project.
	 * @param packageName
	 *            The package of the generated classes, for example:
	 *            <code>com.demo.mdb</code>
	 * @param defaultBufferSize
	 *            The default buffer size used by cursors and appenders.
	 *            TODO:Missing reference.
	 */
	public Compiler(final String schemaName, final String sourcesDir,
			final String packageName, final int defaultBufferSize) {
		_schema = new Schema(schemaName);
		_sourcesDir = sourcesDir;
		_packageName = packageName;
		_bufferSize = defaultBufferSize;
		_offset = 0;
		_registry = new ExtensionRegistry();
	}

	/**
	 * Create a compiler with default values.
	 */
	public Compiler() {
		this("Database1", "../src", "mdb", 100);
	}

	/**
	 * The schema definition.
	 * 
	 * @return the schema.
	 */
	public Schema getSchema() {
		return _schema;
	}

	/**
	 * Default buffer size used in cursors and appenders.
	 * 
	 * @return Buffer size.
	 */
	public int getBufferSize() {
		return _bufferSize;
	}

	/**
	 * Package name used in generated classes.
	 * 
	 * @return Package name.
	 */
	public String getPackageName() {
		return _packageName;
	}

	/**
	 * Get the extension registry. For more information see the
	 * {@link ICompilerExtension}.
	 * 
	 * @see ICompilerExtension
	 * @return The extension registry.
	 */
	public ExtensionRegistry getRegistry() {
		return _registry;
	}

	/**
	 * Register a compiler extension. For more information, see the
	 * {@link ICompilerExtension} javadoc.
	 * 
	 * @see ICompilerExtension
	 * @param extension
	 *            The extension
	 */
	public void extension(ICompilerExtension extension) {
		_registry.register(extension);
	}

	/**
	 * Set the schema name. Use a valid Java identifier name because this is
	 * part of the name of the generated session class. For example, if you set
	 * "Foo" then a class <code>FooMDBSession</code> is generated.
	 * 
	 * @param name
	 *            Schema name.
	 */
	public void schemaName(String name) {
		_schema.setName(name);
	}

	/**
	 * Set the package name for the generated classes.
	 * 
	 * @param name
	 *            Package name.
	 */
	public void packageName(String name) {
		_packageName = name;
	}

	/**
	 * Set the default buffer size will be used in appenders and cursors in the
	 * MDB runtime.
	 * 
	 * @param bufferSize
	 *            The buffer size (number of records of the buffer).
	 */
	public void bufferSize(int bufferSize) {
		_bufferSize = bufferSize;
	}

	/**
	 * Define a table.
	 * 
	 * @param name
	 *            The table's name. Use a valid Java identifier; in the
	 *            generated code this name is used as part of methods and
	 *            classes names.
	 * @param id
	 *            Unique table id.
	 */
	public void table(final String name, UUID id) {
		_lastTable = new Table(name, id);
		_offset = 0; // reset columns' offset
		_schema.add(_lastTable);
	}

	/**
	 * Define a table with a random unique identifier.
	 * 
	 * @param name
	 *            The table's name. Use a valid Java identifier; in the
	 *            generated code this name is used as part of methods and
	 *            classes names.
	 */
	public void table(final String name) {
		table(name, UUID.randomUUID());
	}

	/**
	 * Define a column. In this example we define the a table A with columns a1,
	 * a2, and a table B with columns b1 and b2:
	 * 
	 * <pre>
	 * table("A");
	 * 	column("a1", ...);
	 * 	column("a2", ...);
	 * 
	 * table("B");
	 * 	column("b1");
	 * 	column("b2");
	 * </pre>
	 * 
	 * @param name
	 *            The name. Use a valid Java identifier, this one is used as
	 *            part of methods and fields names in the generated code.
	 * @param type
	 *            The type.
	 * @param order
	 *            The order.
	 * @param virtual
	 *            If the column is virtual.TODO:Missing reference.
	 * @param formula
	 *            The formula to compute the value, or <code>null</code> in case
	 *            the column is not virtual.TODO:Missing reference.
	 * @param uuid
	 *            The unique identifier.
	 */
	public void column(final String name, final Type type, final Order order,
			final boolean virtual, final String formula, UUID uuid) {
		if (_lastTable == null) {
			throw new IllegalArgumentException(
					"There not any stable defined yet");
		}
		Column col = new Column(name, type, order, virtual, formula, _offset,
				uuid);
		col.setIndex(_lastTable.size());
		_lastTable.add(col);
		if (!virtual) {
			_offset += type.getSize();
		}
	}

	/**
	 * Define a column. Like
	 * {@link #column(String, Type, Order, boolean, String, UUID)} but non
	 * virtual and with a random unique identifier.
	 * 
	 * @param name
	 *            The name. Use a valid Java identifier, this one is used as
	 *            part of methods and fields names in the generated code.
	 * @param type
	 *            The type.
	 * @param order
	 *            The order.
	 */
	public void column(final String name, final Type type, final Order order) {
		column(name, type, order, false, "", UUID.randomUUID());
	}

	/**
	 * Define a column. Like
	 * {@link #column(String, Type, Order, boolean, String, UUID)} but with a
	 * random unique identifier.
	 * 
	 * @param name
	 *            The name. Use a valid Java identifier, this one is used as
	 *            part of methods and fields names in the generated code.
	 * @param type
	 *            The type.
	 * @param order
	 *            The order.
	 * @param virtual
	 *            If the column is virtual.TODO:Missing reference.
	 * @param formula
	 *            The formula to compute the value, or <code>null</code> in case
	 *            the column is not virtual.TODO:Missing reference.
	 */
	public void column(final String name, final Type type, final Order order,
			final boolean virtual, final String formula) {
		column(name, type, order, virtual, formula, UUID.randomUUID());
	}

	/**
	 * Define a column. Like
	 * {@link #column(String, Type, Order, boolean, String, UUID)} but without
	 * order, non virtual and with a random unique identifier.
	 * 
	 * @param name
	 *            The name. Use a valid Java identifier, this one is used as
	 *            part of methods and fields names in the generated code.
	 * @param type
	 *            The type.
	 */
	public void column(final String name, final Type type) {
		column(name, type, Order.NONE, false, "", UUID.randomUUID());
	}

	/**
	 * Define an ascending column. Like
	 * {@link #column(String, Type, Order, boolean, String, UUID)} but with
	 * order equals to {@link Order#ASCENDING}, non virtual and with a random
	 * unique identifier.
	 * 
	 * @param name
	 *            The name.
	 * @param type
	 *            the type ({@link Type#INTEGER}, {@link Type#BOOLEAN} ...)
	 */
	public void columnAsc(final String name, final Type type) {
		column(name, type, Order.ASCENDING, false, "", UUID.randomUUID());
	}

	/**
	 * Define a descending column. Like
	 * {@link #column(String, Type, Order, boolean, String, UUID)} but with
	 * order equals to {@link Order#ASCENDING}, non virtual and with a random
	 * unique identifier.
	 * 
	 * @param name
	 *            The name.
	 * @param type
	 *            The type.
	 */
	public void columnDesc(final String name, final Type type) {
		column(name, type, Order.DESCENDING, false, "", UUID.randomUUID());
	}

	/**
	 * Generate the MDB classes. Be careful, if the file exists they will be
	 * overwritten, so do not write custom code on MDB files, unless you respect
	 * the "User Code Sections".TODO:Missing reference.
	 * 
	 * @throws IOException
	 *             If there is any error writing to file.
	 */
	public void compile() throws IOException {
		compile(_sourcesDir);
	}

	/**
	 * Generate the MDB classes. Be careful, if the file exists they will be
	 * overwritten, so do not write custom code on MDB files, unless you respect
	 * the "User Code Sections".TODO:Missing reference.
	 * 
	 * @param listener
	 *            The compiler listener.
	 * 
	 * @throws IOException
	 *             If there is any error writing to file.
	 */
	public void compile(final ICompilerListener listener) throws IOException {
		compile(_sourcesDir, listener);
	}

	/**
	 * Generate the MDB classes. Be careful, if the file exists they will be
	 * overwritten, so do not write custom code on MDB files, unless you respect
	 * the "User Code Sections".TODO:Missing reference.
	 * 
	 * @param srcDir
	 *            Use this destination instead of the one the provided in the
	 *            constructor.
	 * 
	 * @throws IOException
	 *             If there is any error writing to file.
	 */
	public void compile(String srcDir) throws IOException {
		compile(srcDir, new ICompilerListener() {

			@Override
			public void compiledFile(File file, boolean replacing) {
				out.println((replacing ? "Replacing " : "Creating ")
						+ file.getAbsolutePath() + "...");
			}
		});
	}

	/**
	 * Generate the MDB classes. Be careful, if the file exists they will be
	 * overwritten, so do not write custom code on MDB files, unless you respect
	 * the "User Code Sections".TODO:Missing reference.
	 * 
	 * @param srcDir
	 *            Use this destination instead of the one the provided in the
	 *            constructor.
	 * 
	 * @param listener
	 *            The compiler listener.
	 * 
	 * @throws IOException
	 *             If there is any error writing the files.
	 */
	public void compile(String srcDir, final ICompilerListener listener)
			throws IOException {
		if (srcDir == null) {
			throw new IllegalArgumentException("Null source directory.");
		}
		if (_packageName == null) {
			throw new IllegalArgumentException("Null package name.");
		}

		String srcDir2 = srcDir;
		if (!srcDir2.endsWith("/")) {
			srcDir2 += "/";
		}
		// compute the target dir path
		final String dirPath = srcDir2 + _packageName.replaceAll("\\.", "/");

		for (final Table table : _schema) {
			// creates the target file
			final File file = new File(dirPath, table.getName() + "MDB.java");

			final StringBuilder builder = new StringBuilder();
			// build the code associated to the table
			buildTable(builder, table, file);

			writeFile(listener, builder, file);
		}

		buildSession(listener, dirPath);
	}

	private void buildSession(final ICompilerListener listener,
			final String dirPath) throws IOException, FileNotFoundException {
		File sessionFile = new File(dirPath, _schema.getName()
				+ "MDBSession.java");

		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			final SessionClassGenerator gen = new SessionClassGenerator();

			try (OpenPrinter printer = new OpenPrinter(output,
					sessionFile)) {
				printer.setRegistry(_registry);
				gen.emitter = printer;
				gen.schema = _schema;
				gen.pkgname = _packageName;
				gen.run();
			}

			String str = output.toString();

			StringBuilder builder = new StringBuilder(str);

			writeFile(listener, builder, sessionFile);
		}
	}

	private static void writeFile(final ICompilerListener listener,
			final StringBuilder builder, final File file)
			throws FileNotFoundException, IOException {
		// creates the file parents
		file.getParentFile().mkdirs();

		// print a message
		if (file.exists()) {
			listener.compiledFile(file, true);
			// delete in case of file exists
			file.delete();
		} else {
			listener.compiledFile(file, false);
		}

		// write the code to the file
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(builder.toString().getBytes());
		}
	}

	/**
	 * Build the codes associated to a table. This method fill the templates
	 * 'class.java.template' and 'indexMethods.java.template' and append the
	 * text to the builder.
	 * 
	 * @param builder
	 *            A builder.
	 * @param table
	 *            A table.
	 * @param targetfile
	 * @throws IOException
	 */
	private void buildTable(final StringBuilder builder, final Table table,
			final File targetFile) throws IOException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			try (final OpenPrinter emitter = new OpenPrinter(output, targetFile)) {
				emitter.setRegistry(_registry);
				MDBClassGenerator gen = new MDBClassGenerator();
				gen.emitter = emitter;
				gen.table = table;
				gen.pkgname = _packageName;
				gen.bufferSize = _bufferSize;
				gen.run();
			}
			builder.append(output.toString());
		}
	}

	/**
	 * Just print the definitions with a JSON format.
	 * 
	 * @param stream
	 *            The print stream.
	 * @throws IOException
	 *             If there is any error with the stream.
	 */
	public void print(final PrintStream stream) throws IOException {
		for (final Table table : _schema) {
			final StringBuilder builder = new StringBuilder();
			buildTable(builder, table, null);
			stream.println(builder);
		}
	}

	/**
	 * Just print the definitions to "stdout" with a JSON format.
	 * 
	 * @throws IOException
	 *             If there is any error with stdout.
	 */
	public void print() throws IOException {
		print(out);
	}
}
