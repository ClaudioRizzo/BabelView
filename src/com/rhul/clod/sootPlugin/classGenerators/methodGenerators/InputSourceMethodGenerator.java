/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;

public class InputSourceMethodGenerator extends MethodGenerator {
	
	
	
	public InputSourceMethodGenerator(BabelView babelViewClass) {
		super(babelViewClass);
	}

	@Override
	public SootMethod generateMethod() {
		
		return generateInputSource();
	}

	private SootMethod generateInputSource() {
		SootMethod sMethod = generateEmptyMethod("inputSource", RefType.v(BabelViewType.OBJECT_TYPE), Modifier.STATIC, RefType.v(BabelViewType.STRING_TYPE));
		Body body = sMethod.getActiveBody();
		
		Local toReturn = localFactory.genLocal(Scene.v().getType(BabelViewType.OBJECT_TYPE));
		body.getLocals().add(toReturn);
		
		NewExpr nExpr = Jimple.v().newNewExpr(RefType.v(BabelViewType.OBJECT_TYPE));
		AssignStmt aStmt = Jimple.v().newAssignStmt(toReturn, nExpr);
		
		SootMethod objInit = Scene.v().getMethod("<java.lang.Object: void <init>()>");
		SpecialInvokeExpr sInvExpr = Jimple.v().newSpecialInvokeExpr(toReturn, objInit.makeRef());
		InvokeStmt iStmt = Jimple.v().newInvokeStmt(sInvExpr);
		body.getUnits().add(aStmt);
		body.getUnits().add(iStmt);
		
		ReturnStmt retStmt = Jimple.v().newReturnStmt(toReturn);
		body.getUnits().add(retStmt);
		
		return sMethod;
	}

	@Override
	public SootMethod generateMethod(String name) {
		return null;
	}

}
