/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.exceptions.NoUnitFoundException;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;

abstract class AbstractSourceSink {
	private String signature;
	private String callerSignature;
	private String stmtString;

	private Unit callingUnit;

	public AbstractSourceSink(String signature, String calleeSignature, String stmtString) {
		this(signature, calleeSignature, stmtString, null);
	}

	public AbstractSourceSink(String signature, String calleeSignature, String stmtString, Unit callingUnit) {
		this.signature = signature;
		this.callerSignature = calleeSignature;
		this.stmtString = stmtString;
		this.callingUnit = callingUnit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callerSignature == null) ? 0 : callerSignature.hashCode());
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + ((stmtString == null) ? 0 : stmtString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractSourceSink other = (AbstractSourceSink) obj;
		if (callerSignature == null) {
			if (other.callerSignature != null)
				return false;
		} else if (!callerSignature.equals(other.callerSignature))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		if (stmtString == null) {
			if (other.stmtString != null)
				return false;
		} else if (!stmtString.equals(other.stmtString))
			return false;
		return true;
	}

	public SootMethod getSootMethod(String signature) {
		return Scene.v().getMethod(signature);
	}

	public SootMethod getCallerSootMethod() {
		return Scene.v().getMethod(this.callerSignature);
	}

	public Unit getCallingUnit() throws NoUnitFoundException {
		if (callingUnit != null) {
			return callingUnit;
		} else {
			return lookForCallingUnit(this.stmtString);
		}
	}

	private Unit lookForCallingUnit(String unitString) throws NoUnitFoundException {
		SootMethod calleeMethod = getCallerSootMethod();
		Body body = calleeMethod.retrieveActiveBody();

		if (calleeMethod.isConcrete()) {
			for (Unit u : body.getUnits()) {
				if (u.toString().equals(unitString)) {
					this.callingUnit = u;
					return u;
				}
			}
		}

		throw new NoUnitFoundException("Calling unit not found. "
				+ "Did you run post analysis out of a transformer or on a different APK version?");
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getCalleeSignature() {
		return callerSignature;
	}

	public void setCalleeSignature(String calleeSignature) {
		this.callerSignature = calleeSignature;
	}

	public String getStmtString() {
		return stmtString;
	}

	public void setStmtString(String stmtString) {
		this.stmtString = stmtString;
	}

	public String getInvolvedIface() {
		if (BabelConfig.getConfigs().isJsinterface()) {

			if (signature.matches("<BabelView[0-9]*:.*babelLeak\\(.*\\)>")
					|| signature.matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
				
				Matcher m = Pattern.compile("\\\"(BabelView)?.*\\\"").matcher(stmtString);
				
				if (m.find()) {
					String babelArg = m.group(0); // it is the all string passed as argument to babelView method
					String iface = babelArg.substring(babelArg.indexOf("<"),babelArg.indexOf(">")+1);
					return iface;
				} else {
					
					throw new RuntimeException("Could not extract the vulnerable interface from: "+stmtString);
				}
				
				//return stmtString.substring(stmtString.indexOf('\"')+1, stmtString.length()-2);

			} else {
				throw new RuntimeException(
						"You can get interface signatures only from babelLeak or inputSource, given: " + signature);
			}
		} else {
			return "";
		}
	}
}
