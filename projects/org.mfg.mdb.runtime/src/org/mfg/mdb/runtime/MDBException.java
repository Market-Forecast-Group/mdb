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
 * Exception used by MDB operations.
 * 
 * @author arian
 * 
 */
public class MDBException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * The constructor.
	 * 
	 */
	public MDBException() {
	}

	/**
	 * The constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param cause
	 *            The cause.
	 */
	public MDBException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * The constructor.
	 * 
	 * @param message
	 *            A message.
	 */
	public MDBException(String message) {
		super(message);
	}

	/**
	 * The constructor.
	 * 
	 * @param cause
	 *            A cause.
	 */
	public MDBException(Throwable cause) {
		super(cause);
	}

}
