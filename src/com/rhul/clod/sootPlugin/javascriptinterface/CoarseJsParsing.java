/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.javascriptinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhul.clod.helpers.HelperDispatcher;
import com.rhul.clod.helpers.SootInstrumenterHelper;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

/**
 * An over approximating analysis which will retrieve jsInterfaces from all the
 * super and subclass of the {@link SootClass} passed as parameter for
 * {@code CoarseJsParsing#getJsInterfaces(SootClass)}
 * 
 * @author clod
 *
 */
public class CoarseJsParsing implements JavaScriptInterfaceParsing {

	private final SootInstrumenterHelper sHelper = (SootInstrumenterHelper) HelperDispatcher.getInstance().getHelper();

	private static List<Type> webViewTypes = new ArrayList<>();

	private Map<String, JavaScriptInterface> jsIfaceMap = new HashMap<>();

	private static CoarseJsParsing instance = null;

	private CoarseJsParsing() {

	}

	/**
	 * It initialize the {@link CoarseJsParsing#webViewTypes} and return an instance
	 * of {@link CoarseJsParsing}. These WebViews will be considered to add all the
	 * interfaces found by this analysis.
	 * 
	 * @param webViewClasses
	 */

	public static CoarseJsParsing getCoarseJsParser(List<SootClass> webViewClasses) {
		if (instance == null)
			instance = new CoarseJsParsing();

		for (SootClass webView : webViewClasses) {
			CoarseJsParsing.webViewTypes.add(webView.getType());
		}

		return instance;
	}

	@Override
	public List<JavaScriptInterface> getJsInterfaces(SootClass jsIfaceClass, String name) {

		// We retrieve every annotated method both for superclass and current class
		SootClass jsIface = jsIfaceClass;

		while (jsIface.hasSuperclass()) {

			getAnnotatedMethod(jsIface, name);
			jsIface = jsIface.getSuperclass();
		}

		// Now we need to look in the subclasses
		Hierarchy h = Scene.v().getActiveHierarchy();

		if (!jsIfaceClass.isInterface()) {
			for (SootClass subClass : h.getSubclassesOf(jsIfaceClass)) {

				getAnnotatedMethod(subClass, name);
			}
		}

		return new ArrayList<JavaScriptInterface>(this.jsIfaceMap.values());
	}

	private void getAnnotatedMethod(SootClass jsInterfaceClass, String ifDevName) {
		JavaScriptInterface jsInterface = jsIfaceMap.get(jsInterfaceClass.getName());

		if (jsInterface == null) {

			jsInterface = new JavaScriptInterface(jsInterfaceClass.getName());
			jsInterface.addName(ifDevName);

			for (Type wType : CoarseJsParsing.webViewTypes) {
				jsInterface.addWebViewBind(wType.toString());
			}

			for (SootMethod jsMethod : jsInterfaceClass.getMethods()) {

				if (this.sHelper.isJavaScriptSigned(jsMethod)) {
					Type returnType = jsMethod.getReturnType();
					List<Type> params = jsMethod.getParameterTypes();

					JavaScriptMethod annMethod = new JavaScriptMethod(jsMethod, jsInterface, returnType,
							params.toArray(new Type[params.size()]));

					jsInterface.addMethod(annMethod);
				}

			}

			// if we found any method annotated for this interface, we add it.
			if (jsInterface != null && jsInterface.iterator().hasNext()) {
				jsIfaceMap.put(jsInterfaceClass.getName(), jsInterface);
			}
		} else {
			jsInterface.addName(ifDevName);

			for (Type wType : CoarseJsParsing.webViewTypes) {
				jsInterface.addWebViewBind(wType.toString());
			}
		}

	}

}
