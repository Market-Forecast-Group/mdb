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
package org.mfg.mdb.runtime;

/**
 * Common interface for all the validators.
 * 
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public interface IValidator<T extends IRecord> {

	/**
	 * Validates the current state.
	 * 
	 * @param args
	 *            Validation arguments.
	 * @param listener
	 *            Validation listener.
	 * @return <code>true</code> if the it is a valid state.
	 */
	public boolean validate(ValidationArgs<T> args,
			IValidatorListener<T> listener);
}
