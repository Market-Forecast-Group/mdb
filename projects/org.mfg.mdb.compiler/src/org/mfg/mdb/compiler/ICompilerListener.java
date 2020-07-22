package org.mfg.mdb.compiler;

import java.io.File;

/**
 * An interface to listen the files created by the compiler. External tools can
 * use this to show a progress of the compilation.
 * 
 * @author arian
 * 
 */
public interface ICompilerListener {
	/**
	 * Method called when a file is created.
	 * 
	 * @param file
	 *            The file is created.
	 * @param replacing
	 *            If it is replacing an old file.
	 */
	public void compiledFile(File file, boolean replacing);
}