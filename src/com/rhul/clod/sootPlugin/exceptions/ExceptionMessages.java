/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.exceptions;

public final class ExceptionMessages {

	public static final String CLASS_NOT_ADDED = "SootClass class never added!";
	public static final String NO_JSINTERFACE_FOUND = "This app does not have any method annotated with @JavascriptInterface";
	public static final String METHOD_OVERLOAD = "WebView methods in the custom WebView have been Overloaded!";
	public static final String NO_BABELVIEW = "The instrumenter wasn't initialized or no BabelViews has been found. Try to call InitInstrumenter method!";
	public static final String NO_CONSTRUCTOR = "BabelView constructor doesn't have Context as parameter!";
	public static final String NO_BABEL_CONSTR = "We could not find any BabelView constructor.";
}
