package org.mfg.mdb.compiler;

import java.io.InputStream;

import org.mfg.mdb.compiler.internal.emitters.OpenPrinter;
import org.mfg.mya.MyA;

/**
 * <p>
 * This is the interface to extend the MDB compiler. A compiler extension is not
 * more than the opportunity a developer has to generate part of the MDB classes
 * to implement certain user-domain patterns. Probably this is something you
 * will never use and really it was created to implement certain MFG-related
 * solutions that we decided keep outside MDB, like a CSV import/export feature.
 * </p>
 * <p>
 * An extension is formed by a target and an emitter. A target refers to a point
 * in the generated code. There are defined only 3:
 * <ul>
 * <li>{@link IExtensionTargets#TARGET_MDB_CLASS}: Use this to inject code in
 * the body of an MDB class.</li>
 * <li>{@link IExtensionTargets#TARGET_MDB_CLASS_APPEND_METHOD}: Use this to
 * inject code at the end of the <code>append()</code> method of an MDB class.</li>
 * <li>{@link IExtensionTargets#TARGET_SESSION_CLASS}: Use this to inject code
 * in the body of the MDBSession class.</li>
 * </ul>
 * <p>
 * The following class is an example of extension that print each record is
 * added to any MDB file:
 * </p>
 * 
 * <pre>
 *  class PrintAddedRecordExtension implements ICompilerExtension {
 *  	public String getTarget() {
 *  		return TARGET_MDB_CLASS_APPEND_METHOD;
 *  	}
 *  
 * TODO:missing real example. 
 *  }
 * </pre>
 * <p>
 * To register a compiler extension use the method
 * {@link Compiler#extension(ICompilerExtension)} in the schema definition
 * "script". To run an extension in an {@link MyA} template, use the
 * {@link OpenPrinter#emitExt(String, Table)} method.
 * </p>
 * 
 * @see OpenPrinter
 * @see MyA
 * @author arian
 * 
 */
public interface ICompilerExtension extends IExtensionTargets {
	/**
	 * Get the target of the extension. Use one of the constants defined in
	 * {@link IExtensionTargets}.
	 * 
	 * @return The extension target.
	 */
	public String getTarget();

	/**
	 * Execute the extension. It returns the text to be injected in the caller.
	 * 
	 * @param replacedFile
	 *            The input stream of the file is replacing. This can be useful
	 *            if the emitter needs to know the content of the file it is
	 *            replacing.
	 * @param table
	 *            The table definition associated with the code it is
	 *            generating, however to get access to all the table definitions
	 *            the user can use the {@link Table#getSchema()} method.
	 * @return The extension emitter.
	 */
	public String executeExtension(InputStream replacedFile, Table table);

}
