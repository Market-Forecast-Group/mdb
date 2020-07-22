package org.mfg.mdb.compiler.internal.emitters;

import java.io.File;
import java.io.IOException;

import org.mfg.mya.MyA;

/**
 * @author arian
 *
 */
public class Build_Emitters {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// session.java.mya
		final File root = new File(
				System.getProperty("user.home")
						+ "/Documents/Source/mdb/projects/org.mfg.mdb.compiler/src/org/mfg/mdb/compiler/internal/emitters/");

		MyA mya = new MyA();
		mya.inputResource("file_session.java.mya");
		mya.outputFile(new File(root, "SessionClassGenerator.java"));

		mya.inputResource("file_mdb.java.mya");
		mya.outputFile(new File(root, "MDBClassGenerator.java"));

	}
}
