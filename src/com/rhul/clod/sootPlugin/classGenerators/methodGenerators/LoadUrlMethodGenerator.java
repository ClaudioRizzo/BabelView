/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.methodGenerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.classGenerators.visitors.CastVisitor;
import com.rhul.clod.sootPlugin.classGenerators.visitors.JsReturnTypeVisitor;
import com.rhul.clod.sootPlugin.classGenerators.visitors.ParameterVisitor;
import com.rhul.clod.sootPlugin.exceptions.NoParametersException;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptMethod;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.BooleanType;
import soot.DoubleType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.TypeSwitch;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.MulExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class LoadUrlMethodGenerator extends MethodGenerator {

	private final String LOAD_URL_METHOD_NAME = "loadUrl";
	
	


	private Local objectLocal;
	private Local randDoubleLocal;
	private BabelView babelView;
	
	
	
	private Unit callToRandom;

	private String superSingature;

	private List<Unit> labels;

	public LoadUrlMethodGenerator(BabelView sClass, String superSignature) {
		super(sClass);
		this.babelView = sClass;
		this.superSingature = superSignature;
		this.labels = new ArrayList<>();

	}

	@Override
	public SootMethod generateMethod() {

		return genLoadUrlMethod(null);

	}
	
	@Override
	public SootMethod generateMethod(String name) {
		return genLoadUrlMethod(name);
	}
	
	
	private SootMethod genLoadUrlMethod(String name) {
		SootMethod superMetod = Scene.v().getMethod(superSingature);
		List<Type> paramsList = superMetod.getParameterTypes();
		Type[] params = paramsList.toArray(new Type[paramsList.size()]);
		SootMethod sMethod;
		
		if(name == null)
			sMethod = generateEmptyMethod(LOAD_URL_METHOD_NAME, VoidType.v(), params);
		else 
			sMethod = generateEmptyMethod(name, VoidType.v(), params);
		
		addCallToSuper(superSingature);
		createLoadUrlBody(sMethod);

		// ReturnVoidStmt retStm = Jimple.v().newReturnVoidStmt();
		// sMethod.retrieveActiveBody().getUnits().add(retStm);
		return sMethod;

	}

	private void createLoadUrlBody(SootMethod sMethod) {
		Body body = sMethod.retrieveActiveBody();
		Chain<Unit> units = body.getUnits();

		generateLocals(body); // Create and add cast locals both in the chain
								// and the map

		Local boolLocal = localFactory.genLocal(BooleanType.v());
		body.getLocals().add(boolLocal);

		units.add(Jimple.v().newAssignStmt(boolLocal, IntConstant.v(0)));

		generate(units, body);
		
		addIfStmtOnRandomValue(body);
		
		
		/*
		EqExpr eExpr = Jimple.v().newEqExpr(boolLocal, IntConstant.v(0));
		IfStmt ifStm = Jimple.v().newIfStmt(eExpr, JMP_TRUE_LABEL);

		if (JUMP)
			units.add(ifStm);
		*/
		units.add(Jimple.v().newReturnVoidStmt());

	}

	private void addIfStmtOnRandomValue(Body body) {
		
		for(int i=0; i < labels.size(); i++) {
			Unit label = labels.get(i);
			
			EqExpr eExpr = Jimple.v().newEqExpr(randDoubleLocal, DoubleConstant.v(i));
			IfStmt ifStmt = Jimple.v().newIfStmt(eExpr, label);
			
			// we insert after the call to random
			body.getUnits().insertAfter(ifStmt, callToRandom);
		}
		
		// finally we fix the right range for the random
		MulExpr mulExpr = Jimple.v().newMulExpr(randDoubleLocal, IntConstant.v(labels.size()-1));
		AssignStmt aStmt = Jimple.v().newAssignStmt(randDoubleLocal, mulExpr);
		body.getUnits().insertAfter(aStmt, callToRandom);
		
	}

	private void generate(Chain<Unit> units, Body body) {

		List<JavaScriptInterface> jsInterfacesList = babelView.getJsInterfaces();
		
		Map<String, Local> castDoneMap = new HashMap<>();
		
		
		Map<Type, Local> ifaceMap = new HashMap<>();
		
		initFields(ifaceMap, body);
		
		addCallToRand(body);
		
		
		for (JavaScriptInterface jsInterface : jsInterfacesList) {

			//Local jsInterfaceLocal = localFactory.genLocal(RefType.v(jsInterface.getType()));
			//body.getLocals().add(jsInterfaceLocal);

			// Assign the js to the respective field
			//assignJsObjectFieldToLocal(units, jsInterface, jsInterfaceLocal);
			
			for (JavaScriptMethod jsMethod : jsInterface) {

				
				// call inputSource method
				callInputSoucre(units, StringConstant.v(jsMethod.getSignature()));
				// Generate return value local
				Local jsReturnValueLocal = localFactory.genLocal(jsMethod.getReturnType());
				body.getLocals().add(jsReturnValueLocal);
				List<Local> paramList = new ArrayList<>();
				try {
					for (Type paramType : jsMethod.getParameters()) {

						CastVisitor castVisitor = new CastVisitor(objectLocal, body);
						paramType.apply(castVisitor);
						Local castLocal;
						
						castLocal = castVisitor.getCastLocal();
						Unit castUnit = castVisitor.getCastUnit();
						body.getLocals().add(castLocal);

						castDoneMap.put(paramType.toString(), castLocal);
						units.add(castUnit);

						

						// The parameter list is passed as reference: so the
						// visitor will populate it.
						TypeSwitch paramVisitor = new ParameterVisitor(body, castLocal, paramList);
						paramType.apply(paramVisitor);

					}

				} catch (NoParametersException e) {

				} finally {

					SootMethod sMethod = Scene.v().getMethod(jsMethod.getSignature());
					AssignStmt jsCallAssStm;
					SootClass declaringClass = sMethod.getDeclaringClass();
					
					if(declaringClass.isInterface()) {
						Local jsInterfaceLocal = ifaceMap.get(RefType.v(jsInterface.getType()));
						InterfaceInvokeExpr ifInvExpr = Jimple.v().newInterfaceInvokeExpr(jsInterfaceLocal,
								sMethod.makeRef(), paramList);
						jsCallAssStm = Jimple.v().newAssignStmt(jsReturnValueLocal, ifInvExpr);
					} else if(sMethod.isStatic()) {
						StaticInvokeExpr jsStaticInvokeExpr = Jimple.v().newStaticInvokeExpr(sMethod.makeRef(), paramList);
						jsCallAssStm = Jimple.v().newAssignStmt(jsReturnValueLocal, jsStaticInvokeExpr);
					} else {
						Local jsInterfaceLocal = ifaceMap.get(RefType.v(jsInterface.getType()));
						VirtualInvokeExpr jsInvokeExpr = Jimple.v().newVirtualInvokeExpr(jsInterfaceLocal,
								sMethod.makeRef(), paramList);
						jsCallAssStm = Jimple.v().newAssignStmt(jsReturnValueLocal, jsInvokeExpr);
					}
					
					body.getUnits().add(jsCallAssStm);

					TypeSwitch retTypeVisitor = new JsReturnTypeVisitor(jsReturnValueLocal, getThisLocal(),
							jsMethod.getSignature(), body);

					// The visitor will deal with primitive type and leak the
					// value returned by the interface if needed
					jsMethod.getReturnType().apply(retTypeVisitor);
					
					// we finally Jump back for the loop
					GotoStmt goToStmt = Jimple.v().newGotoStmt(callToRandom);
					units.add(goToStmt);


				}

			}
		}

	}

	private void initFields(Map<Type, Local> ifaceMap, Body body) {
		Local jsInterfaceLocal = null;
		List<JavaScriptInterface> jsInterfacesList = babelView.getJsInterfaces();
		
		for (JavaScriptInterface jsInterface : jsInterfacesList) {

			jsInterfaceLocal = localFactory.genLocal(RefType.v(jsInterface.getType()));
			body.getLocals().add(jsInterfaceLocal);

			// Assign the js to the respective field
			ifaceMap.put(RefType.v(jsInterface.getType()), jsInterfaceLocal);
			assignJsObjectFieldToLocal(body.getUnits(), jsInterface, jsInterfaceLocal);
		}
		
	}

	private void addCallToRand(Body body) {
		SootMethod mathRand = Scene.v().getMethod("<java.lang.Math: double random()>");
		
		StaticInvokeExpr sInvokeRand = Jimple.v().newStaticInvokeExpr(mathRand.makeRef());
		AssignStmt aStmtRand = Jimple.v().newAssignStmt(randDoubleLocal, sInvokeRand);
		body.getUnits().add(aStmtRand);
		this.callToRandom = aStmtRand;
		
		
	}

	private void callInputSoucre(Chain<Unit> units, Value jsInterface) {
		StaticInvokeExpr vInvExp = Jimple.v().newStaticInvokeExpr(Scene.v()
				.getMethod("<" + babelView.getName() + ": java.lang.Object inputSource(java.lang.String)>").makeRef(),
				jsInterface);

		AssignStmt aStm = Jimple.v().newAssignStmt(objectLocal, vInvExp);
		units.add(aStm);
		labels.add(aStm);

	}

	private void assignJsObjectFieldToLocal(Chain<Unit> units, JavaScriptInterface jsInterface,
			Local jsInterfaceLocal) {

		InstanceFieldRef iFieldRef = Jimple.v().newInstanceFieldRef(getThisLocal(),
				babelView.getBabelField(jsInterface.getType()).makeRef());
		AssignStmt aStm = Jimple.v().newAssignStmt(jsInterfaceLocal, iFieldRef);
		units.add(aStm);
		//this.LABEL_1 = aStm;

	}

	
	private void generateLocals(Body body) {

		this.objectLocal = localFactory.genLocal(Scene.v().getType(BabelViewType.OBJECT_TYPE));
		this.randDoubleLocal = localFactory.genLocal(DoubleType.v());
		body.getLocals().add(objectLocal);
		body.getLocals().add(randDoubleLocal);

	}

}
