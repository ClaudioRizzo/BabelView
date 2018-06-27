/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.webViewParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;

import soot.SootClass;
import soot.SootMethod;

/**
 * Singleton class wrapping all the information retrieved by the previous
 * parsing. This information will be used to create the BabelView Class. Extend
 * to support different analysis/instrumentation
 * 
 * NB jsObject refers to the object wrapping all the methods exposed to
 * JavaScript js-interface-methods refers to the methods exposed in the jsObject
 * 
 * @author clod
 *
 */
public class BabelViewRecords {

	private static BabelViewRecords instance;
	
	/**
	 * It is true if the apk has been parsed.
	 */
	private boolean parsed = false;



	/**
	 * A list of all the js-interfaces found in the apk.
	 */
	private List<JavaScriptInterface> jsInterfacesList;

	/**
	 * A list of all the custom WebViews implemented by the developer
	 */
	private List<SootClass> customWebViews;

	/**
	 * List wrapping all the loadUrl wrapper methods we found
	 */
	private List<SootMethod> loadUrlWrappers;

	private BabelViewRecords() {
		customWebViews = new ArrayList<SootClass>();
		loadUrlWrappers = new ArrayList<>();
		jsInterfacesList = new ArrayList<>();
	}

	public static BabelViewRecords getInstance() {
		if (instance == null) {
			instance = new BabelViewRecords();
		}
		return instance;
	}

	public void addLoadUrlWrapper(SootMethod wrapper) {
		if (!(loadUrlWrappers.contains(wrapper) || wrapper.getSubSignature().equals("void loadUrl(java.lang.String)"))
				|| wrapper.getSubSignature().equals("void loadUrl(java.lang.String,java.util.Map)"))
			loadUrlWrappers.add(wrapper);
	}

	public List<SootMethod> getLoadUrlWrappers() {
		final List<SootMethod> wrapperList = Collections.unmodifiableList(loadUrlWrappers);
		return wrapperList;
	}

	/**
	 * Add a custom web view to this recorder
	 * 
	 * @param cWebView
	 */
	public void addWebView(SootClass cWebView) {
		if (!customWebViews.contains(cWebView)) {
			customWebViews.add(cWebView);
		} else {
			;
		}
	}

	/**
	 * Get all the custom WebViews implemented in the current apk
	 * 
	 * @return
	 */
	public List<SootClass> getWebViews() {
		final List<SootClass> webViewsList = Collections.unmodifiableList(customWebViews);
		return webViewsList;
	}

	

	/**
	 * Add the provided interface to the list.
	 * 
	 * @param jsInterface
	 */
	public void addInterface(JavaScriptInterface jsInterface) {
		if (!jsInterfacesList.contains(jsInterface)) {
			this.jsInterfacesList.add(jsInterface);
		} 
	}
	
	/**
	 * Get all the jsInterfaces (the object wrappers) that have been found
	 * during parsing
	 * 
	 * @return a list of all the JavaScript interfaces that have been added to
	 *         the records
	 */
	public List<JavaScriptInterface> getAllJsInterfaces() {
		return Collections.unmodifiableList(this.jsInterfacesList);
	}

	/**
	 * Given a WevView type, this method returns a list of all the javascript
	 * interfaces added for the WebView type given.
	 * 
	 * @param webViewType
	 * @return
	 */
	public List<JavaScriptInterface> getAllInterfaceForWebViewType(String webViewType) {
		List<JavaScriptInterface> jsInterfaceByWebView = new ArrayList<>();

		for (JavaScriptInterface jsInterface : getAllJsInterfaces()) {
			if(jsInterface.getWebViewTypesBinded().contains(webViewType)) {
				jsInterfaceByWebView.add(jsInterface);
			}
			
		}

		return jsInterfaceByWebView;
	}
	
	/**
	 * 
	 * @return true if the current APK has been parsed
	 */
	public boolean isParsed() { return parsed; }
	
	/**
	 * Set the value for parsed. It has to be set to true when the parsing phase is over
	 * @param parsed
	 */
	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

}