/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators;

import java.util.ArrayList;
import java.util.List;

import com.rhul.clod.helpers.HelperDispatcher;
import com.rhul.clod.helpers.SootCreatorHelper;
import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.MethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.SimpleGenerator;
import com.rhul.clod.sootPlugin.types.BabelViewType;
import com.rhul.clod.sootPlugin.webViewParser.BabelViewRecords;

import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

public class BabelViewGenerator {

	private final SootCreatorHelper sCreatorHelper = (SootCreatorHelper) HelperDispatcher.getInstance()
			.getCreatorHelper();

	private final BabelViewRecords bViewRecords = BabelViewRecords.getInstance();


	private static final String SET_WEB_CLIENT_SUBSIGNATURE = "void setWebViewClient(android.webkit.WebViewClient)";
	private final static String ADD_JS_INTERFACE_SUBSIGNATURE = "void addJavascriptInterface(java.lang.Object,java.lang.String)";
	private static final String LOAD_URL_SUBSIGNATURE = "void loadUrl(java.lang.String)";
	private static final String LOAD_URL_HEADERS_SUBSIGNATURE = "void loadUrl(java.lang.String,java.util.Map)";
	
	private static final String POST_URL_SUBSINGNATURE = "void postUrl(java.lang.String,byte[])";
	private static final String LOAD_DATA_SUBSIGNATURE = "void loadData(java.lang.String,java.lang.String,java.lang.String)";
	private static final String LOAD_DATA_BASE_URL_SUBSIGNATURE = "void loadDataWithBaseURL(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)";
	


	public BabelViewGenerator() {
		
	}

	/**
	 * Generate a list of babelviews, one for each custom webview implemented
	 * plus a plain one in case it is required.
	 * 
	 * An exception is thrown if a WebView method has been overloaded
	 * 
	 * @return a list of babelviews
	 */
	public List<BabelView> generate() {
		List<BabelView> babelViews = new ArrayList<>();

		// generate a babelview for each custom webview implemented
		int i = 0;
		for (SootClass webView : bViewRecords.getWebViews()) {
			
			babelViews.add(genBabelView(webView, BabelView.BABEL_VIEW + i));
			i++;
		
		}
		
		return babelViews;

	}
	
	private BabelView genBabelView(SootClass webView, String name) {
		if (webView.isFinal()) webView.setModifiers(Modifier.PUBLIC);
		
		BabelView babelView = new BabelView(webView, name);
		
		if(!webView.getType().equals(RefType.v(BabelViewType.WEB_VIEW_CLASS))) instrumentSuperClass(webView);
		
		for (SootMethod sm : webView.getMethods()) {
			if (sm.getName().startsWith("<init>")) {
				babelView.addSuperClassConstructor(sm.getSignature(), sm);
				
			}
			// TODO: here we may check if the WebView wraps loadUrl method
		}
		
		babelView.createMethodsBodies(
				webView.getMethod(SET_WEB_CLIENT_SUBSIGNATURE).getSignature(),
				webView.getMethod(ADD_JS_INTERFACE_SUBSIGNATURE).getSignature(),
				webView.getMethod(LOAD_URL_SUBSIGNATURE).getSignature(), 
				webView.getMethod(LOAD_URL_HEADERS_SUBSIGNATURE).getSignature(), 
				webView.getMethod(POST_URL_SUBSINGNATURE).getSignature(), 
				webView.getMethod(LOAD_DATA_SUBSIGNATURE).getSignature(), 
				webView.getMethod(LOAD_DATA_BASE_URL_SUBSIGNATURE).getSignature());
			
		sCreatorHelper.printJimpleToFile(babelView.getName());
		//sCreatorHelper.printClassToFile(babelView.getName());
		return babelView;
		
	}
	
	
	
	private void instrumentSuperClass(SootClass webView) {
		/**
		 * Classic signatures we want to have
		 */
		String[] subsignatures = { 
				SET_WEB_CLIENT_SUBSIGNATURE, 
				ADD_JS_INTERFACE_SUBSIGNATURE, 
				LOAD_URL_SUBSIGNATURE,
				LOAD_URL_HEADERS_SUBSIGNATURE,
				POST_URL_SUBSINGNATURE,
				LOAD_DATA_SUBSIGNATURE,
				LOAD_DATA_BASE_URL_SUBSIGNATURE,
				};
		
		for(String subsignature : subsignatures) {	
			
			instrumentSuperClass(webView, subsignature);
		}
		
					
	}
	
	public static void instrumentSuperClass(SootClass sClass, String subsignature) {
		
		if(sClass.declaresMethod(subsignature)) {
			// We are done since the super class has already implemented the method
			
		} else if(sClass.getSuperclass().declaresMethod(subsignature)) {
			// we only need to instrument the current class
			addMethodToClass(sClass, subsignature);
			
		} else {
			instrumentSuperClass(sClass.getSuperclass(), subsignature);
			addMethodToClass(sClass, subsignature);
		}
	}

	
	// TODO: throw method overload exception ? 
	public static void addMethodToClass(SootClass sClass, String methodSignature) {
		SootClass superClass = sClass.getSuperclass();
		SootMethod sMethod = superClass.getMethod(methodSignature);


		MethodGenerator simpleGenerator = new SimpleGenerator(sClass, sMethod.getSignature(), sMethod.getName(),
				sMethod.getReturnType(),
				sMethod.getParameterTypes().toArray(new Type[sMethod.getParameterTypes().size()]));
		simpleGenerator.generateMethod();
	}
	
	

}
