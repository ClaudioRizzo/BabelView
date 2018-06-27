/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * Collection of methods useful to understand the real nature of flows
 * @author clod
 *
 */
public class PostAnalysisUtils {
	
	private PostAnalysisUtils() { }
	
	public static void printMethodCalls(SootMethod sMethod) {
		printMethodCalls(sMethod, new ArrayList<>());
	}
	
	
	private static void printMethodCalls(SootMethod sMethod, List<SootMethod> discovered) {
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> iter = cg.edgesOutOf(sMethod);
		discovered.add(sMethod);
		System.out.println(sMethod);
		while(iter.hasNext()) {
			SootMethod caller = iter.next().src();
			if(!discovered.contains(caller)) {
				printMethodCalls(caller, discovered);
			}
			System.out.println("++++++++");
		}
		System.out.println("==============");
	}

	public static void printMethodCallers(SootMethod sMethod) {
		printMethodCallers(sMethod, new ArrayList<>());
	}
	
	private static void printMethodCallers(SootMethod sMethod, List<SootMethod> discovered) {
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> iter = cg.edgesInto(sMethod);
		discovered.add(sMethod);
		System.out.println(sMethod);
		while(iter.hasNext()) {
			SootMethod caller = iter.next().src();
			if(!discovered.contains(caller)) {
				printMethodCallers(caller, discovered);
			}
			System.out.println("++++++++");
		}
		System.out.println("==============");
		
	}

}
