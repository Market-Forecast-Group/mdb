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
 * Contains the information of a validation error.
 * 
 * @author arian
 * @param <T>
 *            The generated record class.
 * 
 */
public class ValidatorError<T extends IRecord> {
	private ValidationArgs<T> _args;
	private String _message;

	/**
	 * The constructor.
	 * 
	 * @param args
	 *            The validation arguments.
	 * @param message
	 *            The error message.
	 */
	public ValidatorError(ValidationArgs<T> args, String message) {
		super();
		_args = args;
		_message = message;
	}

	/**
	 * Get the validation arguments.
	 * 
	 * @return The validation arguments.
	 */
	public ValidationArgs<T> getArgs() {
		return _args;
	}

	/**
	 * Set validation arguments.
	 * 
	 * @param args
	 *            The validation arguments.
	 */
	public void setArgs(ValidationArgs<T> args) {
		this._args = args;
	}

	/**
	 * Get the error message.
	 * 
	 * @return The error.
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * Set the error message.
	 * 
	 * @param message
	 *            The error.
	 */
	public void setMessage(String message) {
		this._message = message;
	}

}
