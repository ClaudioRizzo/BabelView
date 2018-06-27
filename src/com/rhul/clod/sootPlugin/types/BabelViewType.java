/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.types;

public abstract class BabelViewType {
	private String type;
	
	
	public final static String ADD_JAVASCRIPT_SIGNATURE = "<android.webkit.WebView: void addJavascriptInterface(java.lang.Object,java.lang.String)>";
	public final static String SET_WEB_CLIENT_SIGNATURE = "<android.webkit.WebView: void setWebViewClient(android.webkit.WebViewClient)>";
	public static final String LOAD_URL_SIGNATURES = "<android.webkit.WebView: void loadUrl(java.lang.String)>";
	public static final String WEBVIEW_INIT = "<android.webkit.WebView: void <init>(android.content.Context)>";
	public static final String STRING_TYPE = "java.lang.String";
	public static final String OBJECT_TYPE = "java.lang.Object";
	public static final String VOID_TYPE = "void";
	public static final String WEBVIEW_CLIENT_TYPE = "android.webkit.WebViewClient";
	public static final String WEBVIEW_MOCKUP_TYPE = "ClaudioWebView";
	public static final String TREE_SET_TYPE = "java.util.TreeSet";
	public static final String CONTEXT = "android.content.Context";
	public static final String LOAD_URL_HEADERS_SIGNATURE = "<android.webkit.WebView: void loadUrl(java.lang.String, java.util.Map<K,V>)>";

	public static final String WEB_VIEW_CLASS = "android.webkit.WebView";
	public static final String ITERATOR_TYPE = "java.util.Iterator";
	public static final String BABEL_VIEW = "BabelView";
	public static final String FIND_VIEW_BY_ID_SIGNATURE = "<android.app.Activity: android.view.View findViewById(int)>";
	public static final String FIND_VIEW_BY_ID_SUB_SIGNATURE = "android.view.View findViewById(int)";
	public static final String FIND_VIEW_BY_ID_COMPACT_SIGNATURE = "<android.support.v7.app.c: android.view.View findViewById(int)>";
	public static final String VIEW_GET_CONTEXT = "<android.view.View: android.content.Context getContext()>";


	public static final String BABEL_LEAK_NAME = "babelLeak";


	public static final String CHAR_TYPE = "java.lang.Character";
	public static final String INTEGER_TYPE = "java.lang.Integer";
	public static final String BOOLEAN_TYPE = "java.lang.Boolean";
	public static final String BYTE_TYPE = "java.lang.Byte";
	public static final String DOUBLE_TYPE = "java.lang.Double";
	public static final String FLOAT_TYPE = "java.lang.Float";
	public static final String LONG_TYPE = "java.lang.Long";


	public static final String SHORT_TYPE = "java.lang.Short";


	public static final Object INTENT_TYPE = "android.content.Intent";


	public static final Object STRING_BUILDER_TYPE = "java.lang.StringBuilder";
	 
	
	public BabelViewType(String type) {
		this.type = type;
		
	}
	
	public String getType() {
		return this.type;
	}
		
}
