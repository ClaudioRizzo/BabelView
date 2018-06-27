/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Jimple;

public class BabelLeakMethodGenerator extends MethodGenerator {

	public BabelLeakMethodGenerator(SootClass sClass) {
		super(sClass);

	}

	@Override
	public SootMethod generateMethod() {
		return generateBabelLeak();
	}

	private SootMethod generateBabelLeak() {
		SootMethod sMethod = generateEmptyMethod("babelLeak", VoidType.v(), Modifier.STATIC, RefType.v(BabelViewType.STRING_TYPE), RefType.v(BabelViewType.STRING_TYPE));

		Body body = sMethod.getActiveBody();

		body.getUnits().add(Jimple.v().newReturnVoidStmt());

		return sMethod;
	}

	@Override
	public SootMethod generateMethod(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
