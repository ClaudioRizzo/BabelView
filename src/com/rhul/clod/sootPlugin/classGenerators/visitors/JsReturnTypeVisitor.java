/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.visitors;

import java.util.ArrayList;
import java.util.List;

import com.rhul.clod.sootPlugin.classGenerators.BabelViewGenerator;
import com.rhul.clod.sootPlugin.classGenerators.LocalFactory;
import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.AnySubType;
import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.ErroneousType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.StmtAddressType;
import soot.Type;
import soot.TypeSwitch;
import soot.UnknownType;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

public class JsReturnTypeVisitor extends TypeSwitch {
	
	private Local retLocal;
	private Body body;
	private String jsSignature;
	
	public JsReturnTypeVisitor(Local retLocal, Local thisLocal, String jsSignature, Body body) {
		this.retLocal = retLocal;
		this.body = body;
		this.jsSignature = jsSignature;
	}
	
	private Local genLocal(Type t) {
		Local l = LocalFactory.getInstance().genLocal(t);
		body.getLocals().add(l);
		return l;
	}
	
	private SootMethod getToStringMethod(String type) {
		
		
		String toStringSignature = "<"+type+": java.lang.String toString()>";		
		return Scene.v().getMethod(toStringSignature);
		
	}
	
	private void leak(Local toLeak, Body body) {
		List<Value> logParams = new ArrayList<>();
		logParams.add(StringConstant.v(BabelViewType.BABEL_VIEW+": "+this.jsSignature));
		logParams.add(toLeak);
		
		// Body is the loadUrl method in BabelView.... 
		SootMethod logMethod = body.getMethod().getDeclaringClass().getMethodByName(BabelViewType.BABEL_LEAK_NAME);
		
		StaticInvokeExpr sInvExp = Jimple.v().newStaticInvokeExpr(logMethod.makeRef(), logParams);
		body.getUnits().add(Jimple.v().newInvokeStmt(sInvExp));
		
	}
	
	private void genLeakCalls(Local baseLocal, String type) {
		Local toLeak = LocalFactory.getInstance().genLocal(RefType.v(BabelViewType.STRING_TYPE));
		body.getLocals().add(toLeak);
		
		SootClass sClass = Scene.v().getSootClass(type);
		if(!sClass.declaresField("java.lang.String toString()")) {
			// If we can't find the toString method, we add it !
			BabelViewGenerator.instrumentSuperClass(sClass, "java.lang.String toString()");
			
		}
		
		VirtualInvokeExpr vInvokeExpr = Jimple.v().newVirtualInvokeExpr(baseLocal, getToStringMethod(type).makeRef());
		AssignStmt aStmt = Jimple.v().newAssignStmt(toLeak, vInvokeExpr);
		body.getUnits().add(aStmt);
		leak(toLeak, body);
		
	}
	

	
	private void genValueOfInvocation(Type baseType, String primitiveType) {
		Local refLocal = genLocal(baseType);
		SootMethod valueOf = Scene.v().getMethod("<"+baseType.toString()+": "+baseType.toString()+" valueOf("+primitiveType+")>");
		AssignStmt aStm = Jimple.v().newAssignStmt(refLocal, Jimple.v().newStaticInvokeExpr(valueOf.makeRef(), retLocal));
		body.getUnits().add(aStm);
		
		genLeakCalls(refLocal, baseType.toString());
	}
	
	
	
	@Override
	public void caseArrayType(ArrayType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseBooleanType(BooleanType t) {
		genValueOfInvocation(RefType.v(BabelViewType.BOOLEAN_TYPE), t.toString());

	}

	@Override
	public void caseByteType(ByteType t) {
		genValueOfInvocation(RefType.v(BabelViewType.BYTE_TYPE), t.toString());

	}

	@Override
	public void caseCharType(CharType t) {
		genValueOfInvocation(RefType.v(BabelViewType.CHAR_TYPE), t.toString());

	}

	@Override
	public void caseDoubleType(DoubleType t) {
		genValueOfInvocation(RefType.v(BabelViewType.DOUBLE_TYPE), t.toString());

	}

	@Override
	public void caseFloatType(FloatType t) {
		genValueOfInvocation(RefType.v(BabelViewType.FLOAT_TYPE), t.toString());
	}

	@Override
	public void caseIntType(IntType t) {
		genValueOfInvocation(RefType.v(BabelViewType.INTEGER_TYPE), t.toString());
	
	}	

	@Override
	public void caseLongType(LongType t) {
		genValueOfInvocation(RefType.v(BabelViewType.LONG_TYPE), t.toString());

	}

	@Override
	public void caseRefType(RefType t) {
		genLeakCalls(retLocal, t.toString());
	}

	@Override
	public void caseShortType(ShortType t) {
		genValueOfInvocation(RefType.v(BabelViewType.SHORT_TYPE), t.toString());

	}

	@Override
	public void caseStmtAddressType(StmtAddressType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseUnknownType(UnknownType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseVoidType(VoidType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseAnySubType(AnySubType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNullType(NullType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseErroneousType(ErroneousType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseDefault(Type t) {
		// TODO Auto-generated method stub

	}

}
