/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.loadUrlAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JArrayRef;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class VLoadUrlAnalysis {

	private static final String LOAD_URL = "<android.webkit.WebView: void loadUrl(java.lang.String)>";
	private static final String SB_TO_STRING = "<java.lang.StringBuilder: java.lang.String toString()>";
	private static final String SB_APPEND = "<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>";
	private static final String SB_INIT_PLAIN = "<java.lang.StringBuilder: void <init>()>";
	private static final String SB_INIT_STRING = "<java.lang.StringBuilder: void <init>(java.lang.String)>";

	private final CallGraph callGraph = Scene.v().getCallGraph();
	private List<String> completedMethodSignatures;

	private static final Logger LOGGER = Logger.getLogger(VLoadUrlAnalysis.class.getName());

	public VLoadUrlAnalysis() {
		completedMethodSignatures = new ArrayList<>();
	}

	public String getLoadUrlStrings(SootMethod sMethod, Body body, Unit unit) {

		if (InvokeStmt.class.isAssignableFrom(unit.getClass())) {
			// loadUrl returns void, so it will be in an invokeStm
			InvokeStmt iStmt = (InvokeStmt) unit;

			SootMethod invokedMethod = iStmt.getInvokeExpr().getMethod();
			if (invokedMethod.getSignature().equals(LOAD_URL)) {
				
				//System.out.println("m: "+sMethod);
				// We got a loadUrl invocation
				Value loadUrlArg = iStmt.getInvokeExpr().getArg(0);
				if (StringConstant.class.isAssignableFrom(loadUrlArg.getClass())) {
					// It was easy =)
					return ((StringConstant) loadUrlArg).value;
				} else {
					// The parameter is a local, and we need to perform a back
					// analysis
					Local loadUrlArgLocal = (Local) loadUrlArg;
					UnitGraph cFlowGraph = new ExceptionalUnitGraph(body);
					SimpleLocalDefs sLocalDef = new SimpleLocalDefs(cFlowGraph);

					Unit argDefUnit = getDefinition(loadUrlArgLocal, unit, sLocalDef);
					if(sMethod.getName().contains("run")) {
						//System.out.println("u: "+argDefUnit+" "+argDefUnit.getClass());
					}
					return backTrackAnalysis(sMethod, argDefUnit);
				}

			}
		}

		return "";

	}

	/**
	 * 
	 * @param sMethod:
	 *            current method in which we are analysing the reaching defs
	 * @param argDefUnit:
	 *            unit where the local has been defined
	 * @return
	 */
	private String backTrackAnalysis(SootMethod sMethod, Unit argDefUnit) {

		if (AssignStmt.class.isAssignableFrom(argDefUnit.getClass())) {
			return assgnStmtAnalysis(sMethod, (AssignStmt) argDefUnit);

		} else if (IdentityStmt.class.isAssignableFrom(argDefUnit.getClass())) {

			return identityStmtAnalysis(sMethod, (IdentityStmt) argDefUnit);

		} else {
			// all not supported cases
			LOGGER.log(Level.WARNING, "Unit definition not supported: " + argDefUnit.getClass());
			// System.out.println("NOT IMPLEMENTED: "+argDefUnit.getClass());
			return null;
		}

	}

	private String identityStmtAnalysis(SootMethod sMethod, IdentityStmt identityStmt) {

		ParameterRef pRef = (ParameterRef) identityStmt.getRightOp();

		int index = pRef.getIndex();
		Iterator<Edge> intoEdges = callGraph.edgesInto(sMethod);

		return followParameter(index, intoEdges);

	}

	private String assgnStmtAnalysis(SootMethod sMethod, AssignStmt aStmt) {
		String url = "";

		Value rOpvalue = aStmt.getRightOp();
		if (InvokeExpr.class.isAssignableFrom(rOpvalue.getClass())) {
			InvokeExpr iExpr = (InvokeExpr) rOpvalue;

			SootMethod invokedMethod = iExpr.getMethod();
			if (invokedMethod.getSignature().equals(SB_TO_STRING)) {
				UnitGraph cfg = new ExceptionalUnitGraph(sMethod.retrieveActiveBody());
				List<List<Unit>> sBuilderUnits = getStringBuilderUses(cfg, aStmt);
				
				url = resolveStringBuilder(sMethod, cfg, sBuilderUnits) + url;
				/*
				if(sMethod.getName().contains("run")) {
					for(List<Unit> sbList : sBuilderUnits) {
						for(Unit u : sbList) {
							System.out.println(u);
						}
					}
				}
				*/

			} else if (!this.completedMethodSignatures.contains(invokedMethod.getSignature())) {
				url = checkMethod(invokedMethod) + url;
			}

		} else if (StringConstant.class.isAssignableFrom(rOpvalue.getClass())) {
			url = ((StringConstant) rOpvalue).value + url;

		} else {
			// all not supported case here
			//InstanceFieldRef iFieldRef = (InstanceFieldRef) rOpvalue;
			
			LOGGER.log(Level.WARNING, "Right operator in AssgnStmt not supported: " + rOpvalue.getClass());
			if(JArrayRef.class.isAssignableFrom(rOpvalue.getClass())) {
				Iterator<Edge> iter = callGraph.edgesInto(sMethod);
				while(iter.hasNext()) {
					Edge e = iter.next();
					System.out.println("s: "+e.getSrc().method());
				}
				
				System.out.println("t: "+sMethod);
				
			}
			//return "";

		}
		
		return url;

	}

	private Unit getDefinition(Local l, Unit u, SimpleLocalDefs slocalDef) {
		List<Unit> defList = slocalDef.getDefsOfAt(l, u);

		if (defList.size() > 1) {

			throw new RuntimeException("Multiple definition not supported");
		} else if (defList.size() == 0) {
			throw new RuntimeException("No definition of the local has been found");
		} else {
			return defList.get(0);
		}
	}

	/**
	 * 
	 * @param edges:
	 *            edges pointing to method the parameter comes from
	 * @param index:
	 *            position of parameter to follow
	 * @return The Regexp of the string value that the parameter can take
	 */
	private String followParameter(int index, Iterator<Edge> edges) {
		List<String> results = new ArrayList<>();

		while (edges.hasNext()) {
			String currentResult = "";
			Edge edge = edges.next();
			Unit callingUnit = edge.srcUnit();
			Stmt callingStmt = edge.srcStmt();

			SootMethod callingMethod = edge.getSrc().method();

			Value param = (Value) callingStmt.getInvokeExpr().getArg(index);
			if (StringConstant.class.isAssignableFrom(param.getClass())) {

				currentResult = ((StringConstant) param).value;

			} else {
				// it is a Local

				Local paramLocal = (Local) param;
				UnitGraph cfg = new ExceptionalUnitGraph(callingMethod.retrieveActiveBody());
				SimpleLocalDefs sLocalDef = new SimpleLocalDefs(cfg);
				Unit paramDefUnit = getDefinition(paramLocal, callingUnit, sLocalDef);

				currentResult = backTrackAnalysis(callingMethod, paramDefUnit);

			}
			if (!currentResult.equals(""))
				results.add(currentResult);

		}

		String regExp = "";

		if (results.size() > 1) {
			regExp = createRegExpr(results);
		} else if (!results.isEmpty()) {
			regExp = results.get(0);
		} else {
			regExp = "*NO RESULT*";
		}

		return regExp;

	}

	private String createRegExpr(List<String> strings) {
		StringBuilder sb = new StringBuilder("[");

		if (strings.size() > 1) {
			for (String s : strings) {
				sb.append(s);
				sb.append("|");
			}

			sb.replace(sb.length() - 1, sb.length(), "]");

			return sb.toString();
		} else {
			return "";
		}
	}

	private String checkMethod(SootMethod sMethod) {
		this.completedMethodSignatures.add(sMethod.getSignature());
		String regExprUrl = "";

		for (Unit retStmtUnit : getReturnStmUnit(sMethod.retrieveActiveBody())) {
			String url = "";
			Value retValue = ((ReturnStmt) retStmtUnit).getOp();

			if (StringConstant.class.isAssignableFrom(retValue.getClass())) {
				url = ((StringConstant) retValue).value + url;

			} else {
				// it is a Local
				Local retlocal = (Local) retValue;
				UnitGraph cfg = new ExceptionalUnitGraph(sMethod.retrieveActiveBody());
				SimpleLocalDefs sLocalDef = new SimpleLocalDefs(cfg);
				Unit retDefUnit = getDefinition(retlocal, retStmtUnit, sLocalDef);

				url = backTrackAnalysis(sMethod, retDefUnit) + url;
			}
			
			regExprUrl += "|"+url;
			
		}
		StringBuilder sb = new StringBuilder(regExprUrl);
		sb.replace(0, 1, "");
		return sb.toString();
	}

	private String resolveStringBuilder(SootMethod contextMethod, UnitGraph cfg, List<List<Unit>> sbUnitsLists) {

		String resolved = "";

		for (List<Unit> sbUnitList : sbUnitsLists) {
			String current = "";
			for (Unit sBuilderUnit : sbUnitList) {
				if (InvokeStmt.class.isAssignableFrom(sBuilderUnit.getClass())) {
					InvokeStmt iStmt = (InvokeStmt) sBuilderUnit;
					SootMethod sMethod = iStmt.getInvokeExpr().getMethod();
					if (sMethod.getSignature().equals(SB_INIT_PLAIN)) {
						resolved += current;
					} else if(sMethod.getSignature().equals(SB_INIT_STRING)){
						if(StringConstant.class.isAssignableFrom(iStmt.getInvokeExpr().getArg(0).getClass())) {
							String sbInitString = ( (StringConstant) iStmt.getInvokeExpr().getArg(0) ).value;
							current = sbInitString + current;	
						}  else {
							
							current = followParameter(0, callGraph.edgesInto(sMethod)) + current;
						}
						//TODO: We are ignorig constructor with parameters
						resolved += current;
						
					} else {
						current = computeString(contextMethod, iStmt.getInvokeExpr(), sBuilderUnit, cfg) + current;
					}

				} else if (AssignStmt.class.isAssignableFrom(sBuilderUnit.getClass())) {
					Value rightOp = ((AssignStmt) sBuilderUnit).getRightOp();
					if (InvokeExpr.class.isAssignableFrom(rightOp.getClass())) {
						InvokeExpr iExpr = (InvokeExpr) rightOp;
						SootMethod sMethod = iExpr.getMethod();

						if (sMethod.getSignature().equals(SB_INIT_PLAIN)) {
							resolved += current;
						} else if (sMethod.getSignature().equals(SB_INIT_STRING)){
							if(StringConstant.class.isAssignableFrom(rightOp.getClass())) {
								String sbInitString = ( (StringConstant) rightOp ).value;
								current = sbInitString + current;
							} else {
								current = followParameter(0, callGraph.edgesInto(sMethod)) + current;
							}
							resolved += current;
						} else {
							current = computeString(contextMethod, iExpr, sBuilderUnit, cfg) + current;
						}
					}

				} else {
					// not implemented cases
					LOGGER.log(Level.WARNING, "String builder unit not supported: " + sBuilderUnit.getClass());

				}
			}
			resolved += "|";

		}
		StringBuilder sb = new StringBuilder(resolved);
		sb.replace(resolved.length() - 1, resolved.length(), "");
		return sb.toString();
	}

	private String computeString(SootMethod contextMethod, InvokeExpr iExpr, Unit sBuilderUnit, UnitGraph cfg) {

		SootMethod sMethod = iExpr.getMethod();

		if (sMethod.getSignature().equals(SB_APPEND)) {
			Value appendParam = iExpr.getArg(0);

			if (StringConstant.class.isAssignableFrom(appendParam.getClass())) {

				return ((StringConstant) appendParam).value;

			} else {
				Local paramLocal = (Local) appendParam;
				SimpleLocalDefs sLocalDef = new SimpleLocalDefs(cfg);
				Unit backTrackedUnit = getDefinition(paramLocal, sBuilderUnit, sLocalDef);

				return backTrackAnalysis(contextMethod, backTrackedUnit);

			}

		} else {
			LOGGER.log(Level.WARNING, "StringBuilder method not supported: " + sMethod);
			return "";
		}
	}

	private List<Unit> getReturnStmUnit(Body body) {
		List<Unit> retStmtUnitList = new ArrayList<>();
		for (Unit u : body.getUnits()) {
			if (u.toString().contains("return")) {
				retStmtUnitList.add(u);

			}
		}
		return retStmtUnitList;
	}

	private List<List<Unit>> getStringBuilderUses(UnitGraph cfg, Unit startingUnit) {
		List<Unit> sbUsageList = new ArrayList<>();
		return getStringBuilderUses(cfg, startingUnit, sbUsageList);
	}

	// At the beginning startingUnit is supposed to be the toString() unit
	private List<List<Unit>> getStringBuilderUses(UnitGraph cfg, Unit startingUnit, List<Unit> sbUsageList) {
		// TODO: if a string builder is passed as a parameter this method will
		// fail since the init will never be found!
		StringBuilderModel sbModel = new StringBuilderModel();

		return sbModel.getStringBuilderUnits(cfg, startingUnit);

	}

}
