package org.mfg.mdb.compiler;

/**
 * The extension targets are the points of an MDB class where the user can
 * inject custom code via a compiler extension.
 * 
 * @see ICompilerExtension
 * @author arian
 */
public interface IExtensionTargets {
	/**
	 * Refers to the body of an MDB class. You can use this to add custom MDB
	 * classes members like fields, methods, etc...
	 */
	public String TARGET_MDB_CLASS = "mdb";

	/**
	 * Refers to the end of the <code>append()</code> methods of an MDB class.
	 * Use this target to add custom statements at the end of the method. See
	 * the example of the {@link ICompilerExtension} javadoc.
	 */
	public String TARGET_MDB_CLASS_APPEND_METHOD = "mdb.appendMethod";

	/**
	 * Refers to the body of an MDB session class. Use this to add custom
	 * members like methods, fields, etc...
	 */
	public String TARGET_SESSION_CLASS = "session";

}
