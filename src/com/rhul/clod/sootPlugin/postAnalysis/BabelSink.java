/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import soot.Unit;

class BabelSink extends AbstractSourceSink {

	public BabelSink(String signature, String calleeSignature, String stmtString) {
		this(signature, calleeSignature, stmtString, null);
	}

	public BabelSink(String signature, String calleeSignature, String stmtString, Unit callingUnit) {
		super(signature, calleeSignature, stmtString, callingUnit);

	}

}
