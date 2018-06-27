/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;

public class SimpleGenerator extends MethodGenerator {

	private String name;
	private Type returnType;
	private Type[] params;
	private String superMethodSignature;

	/**
	 * The method will be added to sClass
	 * 
	 * @param sClass
	 *            class you want the method to be added to
	 * @param name
	 *            name of the method
	 * @param returnType
	 *            returned type
	 * @param params
	 *            parameters list
	 */
	public SimpleGenerator(SootClass sClass, String superMethodSignature, String name, Type returnType,
			Type... params) {
		super(sClass);
		this.name = name;
		this.returnType = returnType;
		this.params = params;
		this.superMethodSignature = superMethodSignature;
	}

	@Override
	public SootMethod generateMethod() {
		SootMethod sMethod = generateEmptyMethod(name, returnType, params);
		addCallToSuper(superMethodSignature);

		if (returnType.toString().equals(BabelViewType.VOID_TYPE)) {
			sMethod.retrieveActiveBody().getUnits().add(Jimple.v().newReturnVoidStmt());
		} else {
			// TODO: to implement

		}

		return sMethod;
	}

	@Override
	public SootMethod generateMethod(String name) {

		return null;
	}

}
