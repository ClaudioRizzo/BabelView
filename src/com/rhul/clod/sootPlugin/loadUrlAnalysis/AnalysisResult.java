/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.loadUrlAnalysis;

import soot.Unit;

public class AnalysisResult {
	
	private String result;
	private Unit loadUrlUnit;

	public AnalysisResult() {}
	
	public AnalysisResult(String result, Unit loadUrlUnit) {
		this.setResult(result);
		this.setLoadUrlUnit(loadUrlUnit);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Unit getLoadUrlUnit() {
		return loadUrlUnit;
	}

	public void setLoadUrlUnit(Unit loadUrlUnit) {
		this.loadUrlUnit = loadUrlUnit;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("url: ");
		sb.append(this.result);
		sb.append("\n");
		sb.append("unit: ");
		sb.append(this.loadUrlUnit);
		return sb.toString();
	}

}
