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
package dev.arhak.trial.db.mdc;

import java.io.IOException;

import org.mfg.mdc.BuildScript;
import org.mfg.mdc.Order;
import org.mfg.mdc.Type;

/**
 * @author arian
 * 
 */
public class Build extends BuildScript {

	/**
	 * @param schemaName
	 * @param sourcesDir
	 * @param packageName
	 * @param defaultBufferSize
	 */
	public Build() {
		super("Test", "/home/arian/Documents/Source/mfgchart/src",
				"dev.arhak.trial.db.mdc.mdb", 100);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mfg.mdc.BuildScript#build()
	 */
	@Override
	protected void build() {
		table("TableWithArray");
		column("sum", Type.DOUBLE);
		column("boolArray", Type.ARRAY_BOOLEAN);
		column("byteArray", Type.ARRAY_BYTE);
		column("doubleArray", Type.ARRAY_DOUBLE);
		column("floatArray", Type.ARRAY_FLOAT);
		column("intArray", Type.ARRAY_INTEGER);
		column("longArray", Type.ARRAY_LONG);
		column("shortArray", Type.ARRAY_SHORT);
		column("string", Type.STRING);

		table("TableWithString");
		column("str", Type.STRING);
		column("intcol", Type.INTEGER, Order.ASCENDING, false, "");

		table("TestSparseCursor");
		column("value", Type.INTEGER, Order.ASCENDING, false, "");

	}

	public static void main(final String[] args) throws IOException {
		final Build script = new Build();
		script.compile();
	}

}
