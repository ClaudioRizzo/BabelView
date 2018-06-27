/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.RefType;
import soot.SootMethod;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.util.Chain;

public class SetWebViewClientMethodGenerator extends MethodGenerator {

	private static final String SET_WEB_VIEW_CLIENT_METHOD_NAME = "setWebViewClient";

	private BabelView babelView;
	private String superSignature;

	public SetWebViewClientMethodGenerator(BabelView sClass, String superSignature) {
		super(sClass);
		this.babelView = sClass;
		this.superSignature = superSignature;

	}

	@Override
	public SootMethod generateMethod() {
		return genSetWebViewClientMethod();
	}

	private SootMethod genSetWebViewClientMethod() {

		SootMethod sMethod = generateEmptyMethod(SET_WEB_VIEW_CLIENT_METHOD_NAME, VoidType.v(),
				RefType.v(BabelViewType.WEBVIEW_CLIENT_TYPE));
		addCallToSuper(superSignature);
		createSetWebViewClientBody(sMethod);
		return sMethod;

	}

	private void createSetWebViewClientBody(SootMethod sMethod) {
		Body body = sMethod.getActiveBody();
		sMethod.setActiveBody(body);
		Chain<Unit> units = body.getUnits();

		units.add(Jimple.v().newAssignStmt(
				Jimple.v().newInstanceFieldRef(getThisLocal(),
						babelView.getBabelField(BabelViewType.WEBVIEW_CLIENT_TYPE).makeRef()),
				body.getParameterLocal(0)));
		units.add(Jimple.v().newReturnVoidStmt());

	}

	@Override
	public SootMethod generateMethod(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
