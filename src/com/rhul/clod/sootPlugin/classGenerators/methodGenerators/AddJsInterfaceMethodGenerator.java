/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import com.rhul.clod.sootPlugin.Types;
import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;

import soot.Body;
import soot.BooleanType;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.CastExpr;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.ReturnVoidStmt;
import soot.util.Chain;

public class AddJsInterfaceMethodGenerator extends MethodGenerator {

	private final static String ADD_JS_INTERFACE_METHOD_NAME = "addJavascriptInterface";
	

	// private SootField field;
	private NopStmt nop;
	private BabelView babelView;
	private String superMethodSignature;

	public AddJsInterfaceMethodGenerator(BabelView babelView, String superMethodSignature) {
		super(babelView);
		this.babelView = babelView;
		this.superMethodSignature = superMethodSignature;

	}

	@Override
	public SootMethod generateMethod() {
		return genAddJavaScriptInterfaceMethod();
	}

	private SootMethod genAddJavaScriptInterfaceMethod() {
		SootMethod sMethod = generateEmptyMethod(ADD_JS_INTERFACE_METHOD_NAME, VoidType.v(),
				RefType.v(Types.OBJECT_TYPE), RefType.v(Types.STRING_TYPE));
		addCallToSuper(superMethodSignature);
		createAddJsBody(sMethod);
		return sMethod;
	}

	private void createAddJsBody(SootMethod sMethod) {
		Body body = sMethod.retrieveActiveBody();
		Chain<Unit> units = body.getUnits();
		ReturnVoidStmt retStm = Jimple.v().newReturnVoidStmt();

		for (JavaScriptInterface jsInterface : babelView.getJsInterfaces()) {
			generateInstanceOfCOndition(RefType.v(jsInterface.getType()), body);
			generateCastExpression(body, RefType.v(jsInterface.getType()));
		}

		units.add(retStm);
	}

	private void generateInstanceOfCOndition(Type instanceType, Body body) {

		Chain<Unit> units = body.getUnits();
		Local condition = localFactory.genLocal(BooleanType.v());
		body.getLocals().add(condition);
		units.add(Jimple.v().newAssignStmt(condition,
				Jimple.v().newInstanceOfExpr(body.getParameterLocal(0), instanceType)));

		// init the if statment
		EqExpr eExpr = Jimple.v().newEqExpr(condition, IntConstant.v(0));
		nop = Jimple.v().newNopStmt();

		// change to the first action if the test succeed
		IfStmt ifStm = Jimple.v().newIfStmt(eExpr, nop);

		units.add(ifStm);

	}

	private void generateCastExpression(Body body, Type castType) {

		Chain<Unit> units = body.getUnits();
		Local cast = localFactory.genLocal(castType);
		body.getLocals().add(cast);
		CastExpr cExpr = Jimple.v().newCastExpr(body.getParameterLocal(0), castType);
		units.add(Jimple.v().newAssignStmt(cast, cExpr));

		// assign the new field with the value
		units.add(Jimple.v().newAssignStmt(
				Jimple.v().newInstanceFieldRef(getThisLocal(), babelView.getBabelField(castType.toString()).makeRef()),
				cast));
		units.add(nop);

	}

	@Override
	public SootMethod generateMethod(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
