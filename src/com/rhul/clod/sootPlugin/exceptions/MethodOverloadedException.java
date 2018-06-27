/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.exceptions;

public class MethodOverloadedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MethodOverloadedException(String message) {
		super(message);
	}

}
