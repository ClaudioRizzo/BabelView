/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.helpers;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.tagkit.Tag;

public class SootInstrumenterHelper extends Helper {

	private static String ADD_JAVASCRIPT_SIGNATURE = "<android.webkit.WebView: void addJavascriptInterface(java.lang.Object,java.lang.String)>";
	private static String ADD_JAVASCRIPT_TAG = "Landroid/webkit/JavascriptInterface";
	
	public static final String STRING_TYPE = "java.lang.String";
	

	private static SootInstrumenterHelper INSTANCE = null;
	

	private SootInstrumenterHelper() {
		
	}
	

	protected static SootInstrumenterHelper getInstance() {
		if (INSTANCE == null)
			INSTANCE = new SootInstrumenterHelper();

		return INSTANCE;
	}

	/**
	 * This method insert the InvokeStatment @iStm after the method with the
	 * given @signature
	 * 
	 * @param Soot
	 *            Unit
	 * @param Soot
	 *            Body
	 * @param Method
	 *            signature: after the method with this signature, the statment
	 *            is inserted
	 * @param InvokeStm
	 *            iStm: the statment to insert
	 */
	public boolean insertAfterSignature(Unit u, Body body, String signature, InvokeStmt iStm) {

		InvokeStmt invoke = (InvokeStmt) u;
		
		
		if (invoke.getInvokeExpr().getMethod().getSignature().equals(signature)) {
			PatchingChain<Unit> units = body.getUnits();
			units.insertAfter(iStm, u);
			body.validate();
			return true;
		}
		return false;
	}

	/**
	 * This method check whether the unit is of the InvokeStmt type, if it is,
	 * it checks if the the InvokeStatment is the Android API
	 * "addJavascirptInterface(Object interface, String name)". In the case the
	 * method is that one, its first argument will be the JavaScript interface
	 * object, containing all the exposed methods. A local representing this
	 * object is then returned. In case nothing is found or the unit was not of
	 * the correct type, null is returned
	 * 
	 * @param u
	 * @return A Local representing the JSInterfaceObject that contains all the
	 *         methods exposed to the JavaScript world.
	 */
	public Local createJavaScriptInterfaceObject(Unit u) {

		if (u instanceof InvokeStmt) {
			InvokeStmt invoke = (InvokeStmt) u;

			if (invoke.getInvokeExpr().getMethod().getSignature().equals(SootInstrumenterHelper.ADD_JAVASCRIPT_SIGNATURE)) {
				// the first argument is the JS interface, which is the base to
				// call
				// its methods
				Local javaScriptInterfaceObject = (Local) invoke.getInvokeExpr().getArg(0);
				return javaScriptInterfaceObject;
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	/**
	 * Generate an InvokeStatment of the method given in the signature starting
	 * from the base variable (base.method()).
	 * 
	 * @param methodSignature:
	 *            the signature of the methods we want to call.
	 * @param base:
	 *            base variable from where to call the method
	 * @return
	 */
	public InvokeStmt generateMethodCall(String methodSignature, Local base) {

		SootMethod sm = Scene.v().getMethod(methodSignature);

		// Creating parameter list that will be populated with all the parameter
		// needed
		List<Value> parameters = new ArrayList<Value>();

		// TODO: extend to other types, not only strings
		for (Type t : sm.getParameterTypes()) {
			Value param = null;

			if (t.toString().equals("java.lang.String")) {
				param = StringConstant.v("SOOT");
			}

			parameters.add(param);
		}

		VirtualInvokeExpr vinvokeExpr = Jimple.v().newVirtualInvokeExpr(base, sm.makeRef(), parameters);
		return Jimple.v().newInvokeStmt(vinvokeExpr);
	}
	
	/**
	 * Generate an InvokeStatment calling the methods from the base with given params
	 * @param methodSignature
	 * @param base
	 * @param params
	 * @return
	 */
	public InvokeStmt generateMethodCall(String methodSignature, Local base, List<Value> params) {
		
		SootMethod sMethod = Scene.v().getMethod(methodSignature);
		
		VirtualInvokeExpr vinvokeExpr = Jimple.v().newVirtualInvokeExpr(base, sMethod.makeRef(), params);
		return Jimple.v().newInvokeStmt(vinvokeExpr);
		
	}
	
	
	public VirtualInvokeExpr generateMethodInvokeExpression(String methodSignature, Local base, List<Value> params) {
		SootMethod sMethod = Scene.v().getMethod(methodSignature);

		VirtualInvokeExpr vinvokeExpr = Jimple.v().newVirtualInvokeExpr(base, sMethod.makeRef(), params);
		return vinvokeExpr;
	}
	
	public Local generateNewLocal(Body body, Type type){
		LocalGenerator lg = new LocalGenerator(body);
		return lg.generateLocal(type);
	}	

	/**
	 * Check if the method passed as parameter has the @JavaScriptIinterface
	 * signature
	 * 
	 * @param m:
	 *            the method to analyse
	 * @return true if it has the signature, false otherwise
	 */
	public boolean isJavaScriptSigned(SootMethod m) {
		for (Tag t : m.getTags()) {
			if (t.toString().contains(SootInstrumenterHelper.ADD_JAVASCRIPT_TAG)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * At the moment only String parameter are supported!
	 * 
	 * @param sMethod
	 * @return List of Values representing the parameters
	 */
	public List<Value> generateParameterList(SootMethod sMethod) {
		
		List<Value> parameters = new ArrayList<Value>();
		for(Type t : sMethod.getParameterTypes()) {
			Value param = null;
			
			if(t.toString().equals(STRING_TYPE)) {
				//TODO: implement the generation strategy according to what value you want to return
				//for now create a plain string
				param = StringConstant.v("CLAUDIO IS THE COLEST!!");
			} //TODO: implement all the other possible parameters type: integers basically 
			
			if(param != null)
				parameters.add(param);
		}
		return parameters;
	}
	
	public List<Value> generateParameterList(SootMethod sMethod, Local oneParam) {
		
		List<Value> parameters = new ArrayList<Value>();
		for(Type t : sMethod.getParameterTypes()) {
			if(t.toString().equals(oneParam.getType().toString())) {
				parameters.add(oneParam);
			} else {
				//TODO: implement all the other scenario, at the moment everything gets and returns strings so it is ok
			}
		}
		
		return parameters;
	}

}
