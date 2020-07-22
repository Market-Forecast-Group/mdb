package org.mfg.mdb.demo;

import java.io.IOException;

import org.mfg.mdb.compiler.Compiler;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Type;

/**
 * <p>
 * Define a schema to store prices. It contains one "Price" table with "time"
 * and "price" columns.
 * </p>
 * <p>
 * You can follow this class as a template for you to define your own schemas.
 * Note the different elements:
 * </p>
 * <ol>
 * <li>Your class should extends the {@link Compiler} class.</li>
 * <li>In the constructor you code the schema using the "table" and "column"
 * methods.</li>
 * <li>In the main method you execute the compiler.</li>
 * </ol>
 * <p>
 * <a href=
 * "../../../../src-html/org/mfg/mdb/demo/Demo_02_SchemaDefinition.html#line.33"
 * > See the code</a>
 * </p>
 * 
 * @author arian
 *
 */
public class Demo_02_SchemaDefinition extends Compiler {

	/**
	 * In the constructor you define the schema.
	 */
	{
		schemaName("Demo_02_");
		packageName("org.mfg.mdb.demo");

		// start a table definition
		table("Demo_02_Price");
		// define a "time" column of type "int" with an ascending order
		// the order will be used to generate special methods like binary search
		// methods.
		column("time", Type.INTEGER, Order.ASCENDING);

		// define a "price" column of type "long".
		column("price", Type.LONG);
	}

	/**
	 * In the main method you execute the compiler.
	 * 
	 * @param args
	 *            The program arguments.
	 * @throws IOException
	 *             If there is any I/O error.
	 */
	public static void main(String[] args) throws IOException {
		Demo_02_SchemaDefinition demo = new Demo_02_SchemaDefinition();
		String pathToSrcFolder = "/home/arian/Documents/Source/mdb/projects/org.mfg.mdb.demo/src";
		demo.compile(pathToSrcFolder);
	}
}
