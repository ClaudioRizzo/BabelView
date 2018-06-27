/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.instrumenters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.exceptions.ExceptionMessages;
import com.rhul.clod.sootPlugin.exceptions.NoBabelViewException;
import com.rhul.clod.sootPlugin.exceptions.NoDefaultConstructorException;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Body;
import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;

public class BabelInstrumenter {

	private static BabelInstrumenter INSTANCE = null;
	private List<BabelView> babelViewsList;

	private boolean CAST = false;

	private Local contextLocal;

	private BabelInstrumenter() {
		babelViewsList = new ArrayList<>();
	}

	public void initInstrumenter(List<BabelView> babelViewsList) {
		if (this.babelViewsList.isEmpty())
			this.babelViewsList = babelViewsList;

	}

	public static BabelInstrumenter getInstance() {

		return INSTANCE == null ? INSTANCE = new BabelInstrumenter() : INSTANCE;
	}

	public void instrument() {
		if (babelViewsList.isEmpty())
			throw new NoBabelViewException(ExceptionMessages.NO_BABELVIEW);

		for (SootClass sClass : Scene.v().getClasses()) {
			if (!isAbabelView(sClass)) {

				for (int j = 0; j < sClass.getMethods().size(); j++) {
					SootMethod sMethod = sClass.getMethods().get(j);
					if (sMethod.isConcrete()) {
						Body body = sMethod.retrieveActiveBody();
						Iterator<Unit> i = body.getUnits().snapshotIterator();

						while (i.hasNext()) {
							Unit u = i.next();

							// replaceNewWithBabelView(body, u);
							replaceNewExprWithBabelView(body, u);

							checkFindViewById(body, u);

							if (CAST) {
								// We can look for a WebView cast

								substituteCastToWebViewExpressions(body, u);

							}

						}

					}
					// We need to re-start the cast process
					CAST = false;
				}

			}

		}

	}

	private void substituteCastToWebViewExpressions(Body body, Unit unitToReplace) {

		if (unitToReplace instanceof AssignStmt) {
			AssignStmt aStm = (AssignStmt) unitToReplace;

			if (aStm.getRightOp() instanceof CastExpr) {

				CastExpr cExpr = (CastExpr) aStm.getRightOp();

				for (BabelView bv : babelViewsList) {
					Type bvSupClassType = bv.getSuperclass().getType();

					if (cExpr.getCastType().toString().equals(bvSupClassType.toString()) && CAST) {
						// Generate a local for our BabelView and create a
						// newExpression to init it

						Local bViewLocal = generateNewLocal(body, bv.getType());
						NewExpr bNewExpr = Jimple.v().newNewExpr(bv.getType());
						AssignStmt newBabelAStm = Jimple.v().newAssignStmt(bViewLocal, bNewExpr);

						// Create a special invoke to the plain BabelView (the
						// one extending WebView)

						// we need to make a call to one of the constructors, we fetch the first one
						// available
						// We could parse the xml where the webview is to actally call the more specific
						// one, but for now we don't
						if (bv.getBabelContructors().isEmpty())
							throw new NoDefaultConstructorException(ExceptionMessages.NO_CONSTRUCTOR);

						SootMethod babelConstructor = bv.getBabelContructors().get(0);
						List<Value> constrParams = new ArrayList<>();

						for (int i = 0; i < babelConstructor.getParameterCount(); i++) {

							if (babelConstructor.getParameterCount() < 1) {
								// We don't have the right constructor
								throw new NoDefaultConstructorException(ExceptionMessages.NO_CONSTRUCTOR);
							} else if (i == 0
									&& !babelConstructor.getParameterType(0).toString().equals(BabelViewType.CONTEXT)) {
								// We don't have the right constructor
								throw new NoDefaultConstructorException(ExceptionMessages.NO_CONSTRUCTOR);
							} else if (i == 0) {
								this.contextLocal = generateNewLocal(body, RefType.v(BabelViewType.CONTEXT));
								constrParams.add(this.contextLocal);

							} else {
								Type param = babelConstructor.getParameterType(i);
								// Local pLocal = LocalFactory.getInstance().genLocal(param);
								if (PrimType.class.isAssignableFrom(param.getClass())) {
									constrParams.add(IntConstant.v(0));
								} else {
									constrParams.add(NullConstant.v());
								}

							}
						}

						SpecialInvokeExpr babelSpecialInvokeExpr = Jimple.v().newSpecialInvokeExpr(bViewLocal,
								babelConstructor.makeRef(), constrParams);

						InvokeStmt initCall = Jimple.v().newInvokeStmt(babelSpecialInvokeExpr);

						// Reassigning WebView
						Local webView = (Local) aStm.getLeftOp(); // Local to
																	// re-assign
						AssignStmt reAssignStm = Jimple.v().newAssignStmt(webView, bViewLocal);

						InvokeExpr iExpr = Jimple.v().newSpecialInvokeExpr(webView,
								Scene.v().getMethod(BabelViewType.VIEW_GET_CONTEXT).makeRef());
						AssignStmt contextAstm = Jimple.v().newAssignStmt(this.contextLocal, iExpr);

						// Inserting generated code

						body.getUnits().insertAfter(reAssignStm, unitToReplace);
						body.getUnits().insertAfter(initCall, unitToReplace);
						body.getUnits().insertAfter(newBabelAStm, unitToReplace);
						body.getUnits().insertAfter(contextAstm, unitToReplace);

						// body.getUnits().remove(unitToReplace);

						// We are done with the cast
						CAST = false;

						break;
					}
				}

			}
		}
	}

	private boolean isAbabelView(SootClass sClass) {
		for (BabelView bView : babelViewsList) {
			if (sClass.getName().equals(bView.getName()))
				return true;
		}
		return false;
	}

	/**
	 * This method looks for a findViewById, setting CAST to true in case it found
	 * one: now we can check for WebView casts.
	 * 
	 * @param body
	 * @param toRemove
	 */
	private void checkFindViewById(Body body, Unit toRemove) {
		if (toRemove instanceof AssignStmt) {
			AssignStmt aStm = (AssignStmt) toRemove;
			if (aStm.getRightOp() instanceof InvokeExpr) {
				InvokeExpr iExpr = (InvokeExpr) aStm.getRightOp();

				if (iExpr.getMethod().getSubSignature().equals(BabelViewType.FIND_VIEW_BY_ID_SUB_SIGNATURE)) {
					CAST = true;

				}
			}
		}

	}

	private void replaceNewExprWithBabelView(Body body, Unit unit) {

		if (AssignStmt.class.isAssignableFrom(unit.getClass())) {
			AssignStmt aStmt = (AssignStmt) unit;

			if (NewExpr.class.isAssignableFrom(aStmt.getRightOp().getClass())) {
				NewExpr nExpr = (NewExpr) aStmt.getRightOp();
				Local assignedLocal = (Local) aStmt.getLeftOp();
				BabelView bView = getBabelView(nExpr.getType());
				if (bView != null) {

					replaceNewExpWithBabelView(bView, assignedLocal, nExpr, body, unit);

				}
			}
		}
	}

	private void replaceNewExpWithBabelView(BabelView bView, Local toReAssign, NewExpr oldNewExpr, Body body,
			Unit newExprUnit) {

		Unit initCallUnit = body.getUnits().getSuccOf(newExprUnit);
		InvokeStmt initInvokeStmt = null;
		while (initCallUnit != null) {
			if (InvokeStmt.class.isAssignableFrom(initCallUnit.getClass())) {
				initInvokeStmt = (InvokeStmt) initCallUnit;

				if (SpecialInvokeExpr.class.isAssignableFrom(initInvokeStmt.getInvokeExpr().getClass())) {
					SpecialInvokeExpr initSpecialInvExpr = (SpecialInvokeExpr) initInvokeStmt.getInvokeExpr();
					if (initSpecialInvExpr.getBase().getType().equals(oldNewExpr.getBaseType())) {
						break;
					}
				}
			}
			initCallUnit = body.getUnits().getSuccOf(initCallUnit);
		}

		

		// initInvokeStmt CANNOT be null.
		if (initInvokeStmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
			SpecialInvokeExpr invokeExpr = (SpecialInvokeExpr) initInvokeStmt.getInvokeExpr();
			SootMethod constr = fetchBabelViewConstructor(bView, invokeExpr.getMethod().getSubSignature());

			if (constr != null) {
				Local bViewLocal = generateNewLocal(body, bView.getType());

				NewExpr bNewExpr = Jimple.v().newNewExpr(bView.getType());
				AssignStmt newBabelAStm = Jimple.v().newAssignStmt(bViewLocal, bNewExpr);

				SpecialInvokeExpr babelSpecialInvokeExpr = Jimple.v().newSpecialInvokeExpr(bViewLocal, constr.makeRef(),
						invokeExpr.getArgs());
				InvokeStmt initCall = Jimple.v().newInvokeStmt(babelSpecialInvokeExpr);
				AssignStmt assignBabelStm = Jimple.v().newAssignStmt(toReAssign, bViewLocal);

				body.getUnits().insertAfter(assignBabelStm, initCallUnit);
				body.getUnits().insertAfter(initCall, initCallUnit);
				body.getUnits().insertAfter(newBabelAStm, newExprUnit);
				body.getUnits().remove(initCallUnit);
				body.getUnits().remove(newExprUnit);

			}
		}
	}

	private SootMethod fetchBabelViewConstructor(BabelView bView, String constrSubSignature) {

		for (SootMethod bConstr : bView.getBabelContructors()) {
			if (constrSubSignature.equals(bConstr.getSubSignature())) {
				return bConstr;
			}
		}
		return null;
	}

	private BabelView getBabelView(Type type) {
		for (BabelView bView : this.babelViewsList) {
			if (bView.getSuperclass().getType().equals(type)) {
				return bView;
			}
		}
		return null;
	}

	private Local generateNewLocal(Body body, Type type) {
		LocalGenerator lg = new LocalGenerator(body);

		return lg.generateLocal(type);

	}

}
