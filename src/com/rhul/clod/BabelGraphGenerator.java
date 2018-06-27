/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod;

import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.dot.DotGraph;

public class BabelGraphGenerator {

	private DotGraph sDotGraph;
	private JimpleBasedInterproceduralCFG cfg;

	private String DOT_NAME = "BabelViewGraph";

	public BabelGraphGenerator() {
		sDotGraph = new DotGraph(DOT_NAME);
		cfg = new JimpleBasedInterproceduralCFG();
		
	}

	public void generateGraph() {
		sDotGraph.plot(DOT_NAME + ".dot");
	}

	public void generateGraph(String name) {
		sDotGraph.plot(name + ".dot");
	}

	public void addNodeToPlot(SootMethod node) {
		Set<Unit> calls = cfg.getCallsFromWithin(node);

		if (!checkSignature(node.getSignature())) {
			for (Unit u : calls) {
				if (u instanceof InvokeStmt) {
					InvokeStmt iStm = (InvokeStmt) u;
					SootMethod target = iStm.getInvokeExpr().getMethod();
					if (target != null) {
						System.out.println("ADDING: " + node.getSignature() + "->" + target.getSignature());
						sDotGraph.drawEdge(node.getSignature(), target.getSignature());

					} 
				} else if(u instanceof AssignStmt){
					AssignStmt aStm = (AssignStmt) u;
					Value right = aStm.getRightOp();
					if(right instanceof InvokeExpr) {
						InvokeExpr iExpr = (InvokeExpr) right;
						SootMethod target = iExpr.getMethod();
						sDotGraph.drawEdge(node.getSignature(),target.getSignature());
					}
				}

			}

			//sDotGraph.drawNode(node.getSignature());

		}

	}

	private boolean checkSignature(String signature) {
		return signature.startsWith("<android") || signature.startsWith("<java") || signature.startsWith("<org");

	}

	public void longGeneration() {
		for (SootClass sClass : Scene.v().getClasses()) {
			for (SootMethod sMethod : sClass.getMethods()) {
				if (sMethod.isConcrete()) {
					if(sMethod.getName().equals("onCreate")) {
						System.out.println(sMethod.retrieveActiveBody());
					}
					sMethod.retrieveActiveBody();
					addNodeToPlot(sMethod);
				}
			}
		}

		generateGraph("after");
	}

}
