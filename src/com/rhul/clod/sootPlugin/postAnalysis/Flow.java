/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

class Flow {
	private BabelSource source;
	private BabelSink sink;
	
	public Flow(BabelSource source, BabelSink sink) {
		this.source = source;
		this.sink = sink;
	}
	
	public BabelSource getSource() {
		return source;
	}
	public void setSource(BabelSource source) {
		this.source = source;
	}
	public BabelSink getSink() {
		return sink;
	}
	public void setSink(BabelSink sink) {
		this.sink = sink;
	}
	
	
}
