/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.webViewParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rhul.clod.BabelConfig;
import com.rhul.clod.helpers.HelperDispatcher;
import com.rhul.clod.helpers.SootInstrumenterHelper;
import com.rhul.clod.sootPlugin.exceptions.ExceptionMessages;
import com.rhul.clod.sootPlugin.exceptions.NoJavaScriptInterfacesButAPIException;
import com.rhul.clod.sootPlugin.exceptions.NoJavaScriptInterfacesException;
import com.rhul.clod.sootPlugin.forwardAnalysis.StringFoldingAnalysis;
import com.rhul.clod.sootPlugin.javascriptinterface.CoarseJsParsing;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterfaceParsing;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.Hierarchy;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

/**
 * Class parsing the application retrieving the necessary data
 * 
 * @author clod
 *
 */

public class BabelViewParser {

	private final BabelViewRecords babelRecord = BabelViewRecords.getInstance();
	private final SootInstrumenterHelper sHelper = (SootInstrumenterHelper) HelperDispatcher.getInstance().getHelper();

	private static BabelViewParser instance = null;
	private boolean callAddJs = false;

	private BabelViewParser() {

	}

	public static BabelViewParser getBabelViewParser() {
		if (instance == null) {
			instance = new BabelViewParser();
		}
		return instance;
	}

	/**
	 * This method parse the apk looking for all the Javascript interfaces (the
	 * wrapping classes) and returns a list of them
	 * 
	 * @return a list of Javascript interfaces types
	 * @throws NoJavaScriptInterfacesException
	 */
	public List<SootClass> getJsInterfaceTypes() throws NoJavaScriptInterfacesException {
		List<SootClass> jsInterfaces = new ArrayList<>();
		for (SootClass sClass : Scene.v().getClasses()) {
			for (SootMethod sMethod : sClass.getMethods()) {
				if (sHelper.isJavaScriptSigned(sMethod)) {
					jsInterfaces.add(sClass);
					break;
				}
			}
		}

		if (!jsInterfaces.isEmpty()) {
			return jsInterfaces;
		} else {
			throw new NoJavaScriptInterfacesException(ExceptionMessages.NO_JSINTERFACE_FOUND);
		}
	}

	public void deepParse() throws NoJavaScriptInterfacesException, NoJavaScriptInterfacesButAPIException {
		Chain<SootClass> classes = Scene.v().getClasses();

		for (SootClass sClass : classes) {
			
			for (int i = 0; i < sClass.getMethods().size(); i++) {
				SootMethod sMethod = sClass.getMethods().get(i);

				if (sMethod.isConcrete()) {
					
					sMethod.retrieveActiveBody();
					
					// This way we are able to match the jsInterface with its
					// webView
					for (Unit u : sMethod.getActiveBody().getUnits()) {
						if (InvokeStmt.class.isAssignableFrom(u.getClass())) {
							InvokeStmt iStm = (InvokeStmt) u;
							InvokeExpr iExpr = iStm.getInvokeExpr();

							if (iExpr.getMethod().toString().equals(BabelViewType.ADD_JAVASCRIPT_SIGNATURE)) {

								this.callAddJs = true;

								InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) iExpr;
								Local webView = (Local) instanceInvokeExpr.getBase();
								SootClass webViewClass = Scene.v().getSootClass(webView.getType().toString());
								
								Hierarchy h = Scene.v().getActiveHierarchy();
								
								List<SootClass> webViewTypes = new ArrayList<>();
								webViewTypes.add(webViewClass);
								webViewTypes.addAll(h.getSubclassesOf(webViewClass));
								
								checkCustomWebView( webViewTypes );
								

								Local jsInterface = (Local) iExpr.getArg(0);

								String jsInterfaceName = getInterfaceName(sMethod.retrieveActiveBody(), iExpr.getArg(1),
										u);
								
								SootClass jsInterfaceClass = Scene.v().getSootClass(jsInterface.getType().toString());

								JavaScriptInterfaceParsing jsParsing = CoarseJsParsing.getCoarseJsParser(
										webViewTypes);

								for (JavaScriptInterface jsIface : jsParsing.getJsInterfaces(jsInterfaceClass,
										jsInterfaceName)) {

									babelRecord.addInterface(jsIface);

								}
							}
						}
					}

				}
			}
			
		}
		
		validateParsing();

		// Since parsing was a success, we set the parsed flag to true
		babelRecord.setParsed(true);
	}

	private String getInterfaceName(Body body, Value jsInterfaceValue, Unit jsIfaceUnit) {
		if (!BabelConfig.getConfigs().isJsinterface())
			return StringFoldingAnalysis.TOP;
		

		StringFoldingAnalysis sFolding = new StringFoldingAnalysis(new ExceptionalUnitGraph(body), body.getLocals());
		if (StringConstant.class.isAssignableFrom(jsInterfaceValue.getClass())) {
		
			return ((StringConstant) jsInterfaceValue).value;
		} else {
			Local jsLocal = (Local) jsInterfaceValue;
			return sFolding.getStringtAt(jsLocal, jsIfaceUnit);
		}
	}


	private void checkCustomWebView(List<SootClass> webViewList) {
		
		for(SootClass webView : webViewList) {
			if (BabelConfig.getConfigs().isWrappers()) checkLoadUrlWrapers(webView);
			babelRecord.addWebView(webView);
			
		}
		
	}

	private void checkLoadUrlWrapers(SootClass customWebView) {
		for (SootMethod sMethod : customWebView.getMethods()) {
			if (sMethod.isConcrete()) {
				Body body = sMethod.retrieveActiveBody();
				Iterator<Unit> i = body.getUnits().snapshotIterator();
				while (i.hasNext()) {
					Unit u = i.next();
					if (InvokeStmt.class.isAssignableFrom(u.getClass())) {
						InvokeStmt iStm = (InvokeStmt) u;
						String methodSubSignature = iStm.getInvokeExpr().getMethod().getSubSignature();

						if (methodSubSignature.equals("void loadUrl(java.lang.String)")
								|| methodSubSignature.equals("void loadUrl(java.lang.String,java.util.Map)")
								|| methodSubSignature.equals(
										"void loadDataWithBaseURL(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)")) {
							if (iStm.getInvokeExpr() instanceof SpecialInvokeExpr) {
								// We have a call done with super.loadUrl(...)
								// So the analysis makes sense!
								SpecialInvokeExpr sInvExpr = (SpecialInvokeExpr) iStm.getInvokeExpr();
								if (!customWebView.getType().toString().equals(BabelViewType.WEB_VIEW_CLASS)) {

									List<Value> params = new ArrayList<>();
									for (int j = 0; j < iStm.getInvokeExpr().getMethod().getParameterCount(); j++) {
										params.add(NullConstant.v());
									}

									InvokeExpr iExpr = Jimple.v().newVirtualInvokeExpr((Local) sInvExpr.getBase(),
											iStm.getInvokeExpr().getMethod().makeRef(), params);
									InvokeStmt invokeLoadUrl = Jimple.v().newInvokeStmt(iExpr);
									body.getUnits().insertAfter(invokeLoadUrl, u);
									body.getUnits().remove(u);

									System.out.println("[BABELVIEW_INFO] Wrapper found and instrumented");

								}
							}

						}
					}
				}
			}
		}
	}

	private void validateParsing() throws NoJavaScriptInterfacesException, NoJavaScriptInterfacesButAPIException {
		if (babelRecord.getAllJsInterfaces().isEmpty()) {
			if (Scene.v().getAndroidAPIVersion() < 17 && this.callAddJs) {
				throw new NoJavaScriptInterfacesButAPIException();
			}

			throw new NoJavaScriptInterfacesException(ExceptionMessages.NO_JSINTERFACE_FOUND);
		}
	}

}
