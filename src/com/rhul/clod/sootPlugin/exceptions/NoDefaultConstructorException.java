/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.exceptions;

/**
 * This Exception is called anytime a custom WebView is implemented and the
 * developer did not implement a default Constructor taking Context as parameter
 * 
 * @author clod
 *
 */
public class NoDefaultConstructorException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoDefaultConstructorException(String message) {
		super(message);
	}
}
