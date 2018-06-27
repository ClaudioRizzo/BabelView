/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import soot.Unit;

class BabelSource extends AbstractSourceSink {



	public BabelSource(String signature, String callerSignature, String stmtString) {
		this(signature, callerSignature, stmtString, null);
	}

	public BabelSource(String signature, String callerSignature, String stmtString, Unit callingUnit) {
		super(signature, callerSignature, stmtString, callingUnit);
	}



}