/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

class IntentAnalysis {

	private Unit startActivityUnit;
	private SootMethod sActivityCaller;
	private String jsIntSignature;

	private static final String STAR = "*";
	private static final String SET_ACTION = "<android.content.Intent: android.content.Intent setAction(java.lang.String)>";
	
	public IntentAnalysis(Unit startActivityUnit, SootMethod sActivityCaller, String jsIntSignature) {
		this.startActivityUnit = startActivityUnit;
		this.sActivityCaller = sActivityCaller;
		this.jsIntSignature = jsIntSignature;

	}

	public void runIntentAnalysis() {
		if (InvokeStmt.class.isAssignableFrom(startActivityUnit.getClass())) {
			InvokeStmt iStmt = (InvokeStmt) startActivityUnit;
			SootMethod startActivityMethod = iStmt.getInvokeExpr().getMethod();

			if (startActivityMethod.getName().matches("startActivity.*")) {
				Local intent = (Local) iStmt.getInvokeExpr().getArg(0);
				
				if(isRelevant(sActivityCaller)) {
					
					doBackTrack(intent, startActivityUnit, sActivityCaller);
					
				} else {
					//System.out.println(startActivityUnit);
					//System.out.println(sActivityCaller);
					PostAnalysisResults.getResults().addIntentVuln("FP", "");
				}
			}
		}
	}

	private void doBackTrack(Local intent, Unit unit, SootMethod context) {
		if(!isRelevant(context)) return;
		
		UnitGraph cfg = new ExceptionalUnitGraph(context.retrieveActiveBody());
		SimpleLocalDefs slocalDef = new SimpleLocalDefs(cfg);
		for (Unit defUnit : slocalDef.getDefsOfAt(intent, unit)) {
			
			if(defUnit.equals(unit)) break;
			
			if (AssignStmt.class.isAssignableFrom(defUnit.getClass())) {
				
				doAssignStmtAnalysis((AssignStmt) defUnit, context, cfg);
			} else if (IdentityStmt.class.isAssignableFrom(defUnit.getClass())) {
				
				doIdentityStmtAnalysis((IdentityStmt) defUnit, context);
			} else {
				System.out.println(defUnit.getClass());
			}
		}
	}

	private void doIdentityStmtAnalysis(IdentityStmt idStmt, SootMethod context) {
		
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edges = cg.edgesInto(context);
		ParameterRef pRef = (ParameterRef) idStmt.getRightOp();
		int index = pRef.getIndex();

		while (edges.hasNext()) {
			Edge e = edges.next();
			SootMethod caller = e.src();
			Unit callingUnit = e.srcUnit();
			Local intent = (Local) e.srcStmt().getInvokeExpr().getArg(index);

			doBackTrack(intent, callingUnit, caller);

		}
		

	}
	
	private boolean isRelevant(SootMethod caller) {
		
		return isRelevant(caller, new ArrayList<>(), 50);
	}
	
	/**
	 * A method is relevant if somewhere ends to be the js-interface!
	 * @param caller
	 * @return
	 */
	private boolean isRelevant(SootMethod caller, List<SootMethod> explored, int counter) {
		
		if(caller.getSignature().equals(jsIntSignature)) return true;
		
		//if(counter == 0) { 
		//	PostAnalysisResults.getResults().addIntentVuln(STAR);
		//	return false;
		//}
		explored.add(caller);
		
		CallGraph cg = Scene.v().getCallGraph();

		Iterator<Edge> edges = cg.edgesInto(caller);
		while (edges.hasNext()) {
			Edge edge = edges.next();
			SootMethod sMethod = edge.src();

			if (sMethod.getSignature().equals(jsIntSignature))
				return true;
			else {
				
				if(!explored.contains(sMethod)) {
					isRelevant(sMethod, explored, counter - 1);
				}
			}
		}
		
		return false;
		
		
		
		
	}

	private void doAssignStmtAnalysis(AssignStmt aStmt, SootMethod context, UnitGraph cfg) {
		Value rightOp = aStmt.getRightOp();
		Value leftOp = aStmt.getLeftOp();

		if (NewExpr.class.isAssignableFrom(rightOp.getClass())) {
			// we won't backtrack anymore for this unit as we reached the
			// constructor
			
			doIntentConstructorAnalysis(aStmt, cfg);

		} else if (InvokeExpr.class.isAssignableFrom(rightOp.getClass())) {
			
			InvokeExpr iExpr = (InvokeExpr) rightOp;
			
			if(iExpr.getMethod().getName().equals("getLaunchIntentForPackage")) {
				PostAnalysisResults.getResults().addIntentVuln(iExpr.getMethod().getName(), jsIntSignature);
			} else if (leftOp.getType().toString().equals(BabelViewType.INTENT_TYPE)) {
				Local intent = (Local) leftOp;
				
				doBackTrack(intent, aStmt, context);
				// System.out.println(aStmt);
				//System.out.println(context);

			} 
				
			

		} else {
			System.out.println("LETS JUST FINISH THE ANALYSIS");
		}

	}

	private void doIntentConstructorAnalysis(Unit defUnit, UnitGraph cfg) {
		Queue<Unit> explored = new LinkedBlockingQueue<>();
		explored.add(defUnit);
		
		while (!explored.isEmpty()) {
			Unit current = explored.poll();
			if (InvokeStmt.class.isAssignableFrom(current.getClass())) {
				InvokeStmt iStmt = (InvokeStmt) current;
				
				if (SpecialInvokeExpr.class.isAssignableFrom(iStmt.getInvokeExpr().getClass())) {
					SpecialInvokeExpr sInvExpr = (SpecialInvokeExpr) iStmt.getInvokeExpr();
					if (sInvExpr.getBase().getType().toString().equals(BabelViewType.INTENT_TYPE)) {
						
						if(sInvExpr.getArgs().isEmpty()) {
							// Here we have a plain constructor, so we need a more precise analysis
							
							doSetActionAnalysis((Local) sInvExpr.getBase(), defUnit, cfg);
						} else {
							Value intentType = iStmt.getInvokeExpr().getArg(0);

							if (StringConstant.class.isAssignableFrom(intentType.getClass())) {
	
								PostAnalysisResults.getResults().addIntentVuln(intentType.toString().replaceAll("\"", ""),jsIntSignature);
							} else {
								// We should follow the variable passed as parameter, but we do not do it
								
								PostAnalysisResults.getResults().addIntentVuln(STAR, jsIntSignature);
							}
							
						}
						
						break;
					}
					
				}
			}

			for (Unit succ : cfg.getSuccsOf(current)) {
				if (!explored.contains(succ)) {
					explored.add(succ);
				}
			}
		}
	}

	// This is NOT an iter-method analysis
	private void doSetActionAnalysis(Local intent, Unit defUnit, UnitGraph cfg) {
		Queue<Unit> explored = new LinkedBlockingQueue<>();
		explored.add(defUnit);
		String actStr = null;
		
		while(!explored.isEmpty()) {
			Unit curr = explored.poll();
			if(InvokeStmt.class.isAssignableFrom(curr.getClass())) {
				InvokeStmt iStmt = (InvokeStmt) curr;
				InstanceInvokeExpr iExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
				
				if(iExpr.getBase().equals(intent)) {
					SootMethod sMethod = iExpr.getMethod();
					if(sMethod.getSignature().equals(SET_ACTION)) {
						Value action = iExpr.getArg(0);
						if(StringConstant.class.isAssignableFrom(action.getClass())) {
							actStr = ((StringConstant) action).value;
							
						}
						
					}
				}
			}
		
			for(Unit succ : cfg.getSuccsOf(curr)) {
				if(!explored.contains(succ)) explored.add(succ);
			}
		}
		
		// now we add the results
		if(actStr == null) {
			
			PostAnalysisResults.getResults().addIntentVuln(STAR, jsIntSignature);
		} else {
		
			PostAnalysisResults.getResults().addIntentVuln(actStr, jsIntSignature);
			
		}
		
		
		
	}
}
