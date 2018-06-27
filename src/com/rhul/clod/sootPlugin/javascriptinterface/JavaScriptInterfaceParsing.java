/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.javascriptinterface;

import java.util.List;

import soot.SootClass;

/**
 * interface defining strategy for retrieving the js-interfaces of the
 * currently analized apk.
 * 
 * @author clod
 *
 */
public interface JavaScriptInterfaceParsing {

	List<JavaScriptInterface> getJsInterfaces(SootClass jsIfaceClass, String name);
	
}
