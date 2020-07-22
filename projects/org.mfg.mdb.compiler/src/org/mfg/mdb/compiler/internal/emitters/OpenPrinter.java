package org.mfg.mdb.compiler.internal.emitters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.internal.ExtensionRegistry;
import org.mfg.mya.Printer;
import org.mfg.mya.MyA;

/**
 * <p>
 * This emitter is labeled as "open" because it contains the methods to execute
 * compiler extensions ({@link ICompilerExtension}).
 * </p>
 * <p>
 * Take a look to {@link #emitExt(String, Table)} for more details.
 * </p>
 * 
 * @see ICompilerExtension
 * @see MyA
 * @author arian
 * 
 */
public class OpenPrinter extends Printer {

	private ExtensionRegistry _registry;

	/**
	 * The constructor.
	 * 
	 * @param output
	 *            The output stream of the file is creating.
	 * @param replaced
	 *            The file is replacing.
	 * @throws FileNotFoundException
	 */
	public OpenPrinter(OutputStream output, File replaced)
			throws FileNotFoundException {
		super(output, replaced);
	}

	/**
	 * The constructor.
	 * 
	 * @param output
	 *            The output stream of the file is creating.
	 * @param replaced
	 *            The input stream of the file is replacing.
	 */
	public OpenPrinter(OutputStream output, InputStream replaced) {
		super(output, replaced);
	}

	/**
	 * The constructor.
	 * 
	 * @param output
	 *            The output stream of the file is creating.
	 */
	public OpenPrinter(OutputStream output) {
		super(output);
	}

	/**
	 * Get the registry of the extensions.
	 * 
	 * @return The registry.
	 */
	public ExtensionRegistry getRegistry() {
		return _registry;
	}

	/**
	 * Set the registry of extensions.
	 * 
	 * @param registry
	 *            The registry.
	 */
	public void setRegistry(ExtensionRegistry registry) {
		_registry = registry;
	}

	/**
	 * <p>
	 * Run the emitter associated with the given <code>target</code>.
	 * </p>
	 * <p>
	 * Let's see the case of the <code>append()</code> methods of the
	 * <code>mdb_appender.java.mya</code> template. They have a form like this:
	 * </p>
	 * 
	 * <pre>
	 * public void append() {
	 * 	...
	 * 	...
	 * 	// emitExt(TARGET_MDB_CLASS_APPEND_METHOD);			
	 * }
	 * </pre>
	 * <p>
	 * So, if we register an extension like the one of the
	 * {@link ICompilerExtension} javadoc example, then the template will be
	 * compiled into a code like this one:
	 * </p>
	 * 
	 * <pre>
	 * public void append() {
	 * 	...
	 * ... 
	 * 	System.out.println(this + ": Added record: " + toRecord());			
	 * }
	 * </pre>
	 * 
	 * @param target
	 *            The target of the extension. See
	 *            {@link ICompilerExtension#getTarget()}.
	 * @param table
	 *            The table in context.
	 * @see ICompilerExtension
	 * @see MyA
	 */
	public void emitExt(String target, Table table) {
		try {
			List<ICompilerExtension> list = getRegistry().find(target);
			StringBuilder sb = new StringBuilder();
			for (ICompilerExtension ext : list) {
				String str = ext.executeExtension(new ByteArrayInputStream(
						getReplacedText().getBytes()), table);
				sb.append(str);
			}
			print(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
