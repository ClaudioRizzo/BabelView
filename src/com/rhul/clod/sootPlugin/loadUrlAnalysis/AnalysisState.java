/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.loadUrlAnalysis;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class AnalysisState {
	
	private SootMethod currentMethod;
	private UnitGraph currentGraph;
	
	private UnitGraph callerCfg;
	private Unit callerUnit;
	private AbstractInvokeExpr callerExpression;
	private UnitGraph calledCfg;
	
	private AnalysisState callerState;
	private AnalysisState calledState;
	
	private SimpleLocalDefs sLocalDef;
	
	public AnalysisState(SootMethod currentMethod){
		
		this(currentMethod, null, null, null);
	}
	
	public AnalysisState(SootMethod currentMethod, UnitGraph callerCfg, Unit callerUnit) {
		this(currentMethod, callerCfg, callerUnit, null);
		this.callerCfg = callerCfg;
		this.callerUnit = callerUnit;
	}
	
	public AnalysisState(SootMethod currentMethod, UnitGraph callerCfg, Unit callerUnit, UnitGraph calledCfg) {
		this.currentMethod = currentMethod;
		this.callerCfg = callerCfg;
		this.callerUnit = callerUnit;
		this.calledCfg = calledCfg;
	}

	public SootMethod getCurrentMethod() {
		return currentMethod;
	}

	public void setCurrentMethod(SootMethod currentMethod) {
		this.currentMethod = currentMethod;
	}

	public UnitGraph getCallerCfg() {
		return callerCfg;
	}

	public void setCallerCfg(UnitGraph callerCfg) {
		this.callerCfg = callerCfg;
	}

	public Unit getCallerUnit() {
		return callerUnit;
	}

	public void setCallerUnit(Unit callerUnit) {
		this.callerUnit = callerUnit;
	}

	public UnitGraph getCalledCfg() {
		return calledCfg;
	}

	public void setCalledCfg(UnitGraph calledCfg) {
		this.calledCfg = calledCfg;
	}

	public AnalysisState getCallerState() {
		return callerState;
	}

	public void setCallerState(AnalysisState callerState) {
		this.callerState = callerState;
	}

	public AnalysisState getCalledState() {
		return calledState;
	}

	public void setCalledState(AnalysisState calledState) {
		this.calledState = calledState;
	}

	public AbstractInvokeExpr getCallerExpression() {
		return callerExpression;
	}

	public void setCallerExpression(AbstractInvokeExpr callerExpression) {
		this.callerExpression = callerExpression;
	}

	public UnitGraph getCurrentGraph() {
		return currentGraph;
	}

	public void setCurrentGraph(UnitGraph currentGraph) {
		this.currentGraph = currentGraph;
	}

	public SimpleLocalDefs getsLocalDef() {
		return sLocalDef;
	}

	public void setsLocalDef(SimpleLocalDefs sLocalDef) {
		this.sLocalDef = sLocalDef;
	}
	
	
	
	
	
}
