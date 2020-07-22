/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdb.tests.mdb;

import java.io.IOException;

import org.mfg.mdb.compiler.Compiler;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Type;

import com.mfg.mdbplugin.ext.TokenMDBExtension;
import com.mfg.mdbplugin.ext.TokenMDBSessionExtension;

/**
 * @author arian
 * 
 */
public class Build_JUnitTests extends Compiler {

	/**
	 * @param schemaName
	 * @param sourcesDir
	 * @param packageName
	 * @param defaultBufferSize
	 */
	public Build_JUnitTests() {
		schemaName("JUnitTests");
		packageName("org.mfg.mdb.tests.mdb");

		// extension(new HelloExtension());
		extension(new TokenMDBExtension());
		extension(new TokenMDBSessionExtension());

		table("Table1");
		columnAsc("double_0", Type.DOUBLE);
		column("int_0", Type.INTEGER);
		column("array_int_1", Type.ARRAY_INTEGER);

		table("Table2");
		columnAsc("double_0", Type.DOUBLE);
		column("int_0", Type.INTEGER);

		table("Person");
		column("age", Type.INTEGER);
		column("phone", Type.INTEGER);
		column("name", Type.STRING);

		table("Price");
		column("fakeTime", Type.LONG, Order.ASCENDING, true, "$pos$");
		column("realTime", Type.LONG, Order.ASCENDING, false, "");
		column("rawPrice", Type.LONG);
		column("price", Type.LONG, Order.NONE, true, "Math.abs($$.rawPrice)");
		column("real", Type.BOOLEAN, Order.NONE, true, "$$.rawPrice >= 0");

		table("Table3");
		column("index", Type.INTEGER);
		column("virtual_array", Type.ARRAY_INTEGER, Order.NONE, true,
				"createVirtualArray($pos$)");
		column("virtual_string", Type.STRING, Order.NONE, true,
				"createVirtualString($pos$)");
		table("Table4");
		column("num", Type.BYTE);
		column("flag", Type.BOOLEAN);

		table("Table5");
		column("price", Type.INTEGER);
		column("vol", Type.INTEGER, Order.NONE, true, "10");
	}

	public static void main(String[] args) throws IOException {
		Build_JUnitTests script = new Build_JUnitTests();
		script.compile(System.getProperty("user.home")
				+ "/Documents/Source/mdb/projects/org.mfg.mdb.tests/src");
	}

}