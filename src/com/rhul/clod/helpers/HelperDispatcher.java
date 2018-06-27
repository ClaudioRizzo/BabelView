/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.helpers;

public class HelperDispatcher implements HelperFactory {

	private static HelperDispatcher INSTANCE = null;
	
	private HelperDispatcher() {

	}
	
	
	public static HelperDispatcher getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new HelperDispatcher();
		}
		return INSTANCE;
	}
	
	@Override
	public Helper getHelper() {
		return SootInstrumenterHelper.getInstance();
	}


	@Override
	public Helper getCreatorHelper() {
	
		return SootCreatorHelper.getInstance();
	}

	
	
	
	

}
