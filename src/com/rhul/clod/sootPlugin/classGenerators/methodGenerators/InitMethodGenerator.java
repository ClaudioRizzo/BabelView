/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;

import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.ReturnVoidStmt;

public class InitMethodGenerator extends MethodGenerator {

	private final static String INIT_NAME = "<init>";
	// private final static String WEBVIEW_INIT = "<android.webkit.WebView: void
	// <init>(android.content.Context)>";
	//private final static String TREE_SET_INIT = "<java.util.TreeSet: void <init>()>";
	//private static final String TREE_ADD_METHOD = "<java.util.TreeSet: boolean add(java.lang.Object)>";


	private BabelView babelViewClass;
	
	
	
	private List<SootMethod> generatedConstructors = new ArrayList<>();

	public InitMethodGenerator(BabelView babelViewClass) {
		super(babelViewClass);
		this.babelViewClass = babelViewClass;

	}

	@Override
	public SootMethod generateMethod() {
		
		for (SootMethod constructor : babelViewClass.getAllSuperClassConstructors()) {
			
			SootMethod currentConstr = generateConstructor(constructor);
			
			generatedConstructors.add(currentConstr);
		}
		// TODO: it is source of errors in some 
		// stubble situations...
		return generatedConstructors.get(0); // TODO: for now we return a random
												// constuctor, we prob don't
												// need to be better that.

	}
	
	public List<SootMethod> getAllGeneratedConstructors() {
		return Collections.unmodifiableList(generatedConstructors);
	}

	private SootMethod generateConstructor(SootMethod contructor) {
		Type[] params = contructor.getParameterTypes().toArray(new Type[contructor.getParameterTypes().size()]);
		SootMethod sMethod = generateEmptyMethod(INIT_NAME, VoidType.v(), params);
		addCallToSuper(contructor.getSignature());
		
		
		//Local source = addCallToInputSource(sMethod);
		

		ReturnVoidStmt retStm = Jimple.v().newReturnVoidStmt();
		sMethod.retrieveActiveBody().getUnits().add(retStm);
		return sMethod;

	}

	@Override
	public SootMethod generateMethod(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	


	


}
