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
 * Used by validators to report errors.
 * 
 * @see IValidator
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public interface IValidatorListener<T extends IRecord> {

	/**
	 * This method is called by a validator if there is any error.
	 * 
	 * @param args
	 *            The error information.
	 */
	public void errorReported(ValidatorError<T> args);
}
