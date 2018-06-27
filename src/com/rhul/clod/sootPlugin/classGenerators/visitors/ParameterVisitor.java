/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.visitors;

import java.util.List;

import com.rhul.clod.sootPlugin.classGenerators.LocalFactory;

import soot.TypeSwitch;
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
import soot.SootMethod;
import soot.StmtAddressType;
import soot.Type;
import soot.UnknownType;
import soot.jimple.Jimple;
import soot.jimple.VirtualInvokeExpr;

/**
 * Visitor handling the generation of code in case an interface has or not a
 * primitive type as parameter
 * 
 * @author clod
 *
 */
public class ParameterVisitor extends TypeSwitch {
	
	private Local castLocal;
	private List<Local> paramList;
	
	private Body body;
	
	private static final String JAVA_LANG = "java.lang";
	
	public ParameterVisitor(Body body, Local castLocal, List<Local> paramList) {
		this.castLocal = castLocal;
		this.paramList = paramList;
		this.body = body;
	}
	
	private LocalFactory localFactory = LocalFactory.getInstance();
	
	
	// Generate a call to xValue method [for example intValue..] and add the result into the parameter list
	// which will be used to make a call to the interface methods.
	private void makeCallToXValue(Type t, String xValueMethodSignature) {
		Local primLocal = localFactory.genLocal(t);
		body.getLocals().add(primLocal);
		paramList.add(primLocal);
		SootMethod xValueMethod  = Scene.v().getMethod(xValueMethodSignature);
		
		VirtualInvokeExpr vInvExp = Jimple.v().newVirtualInvokeExpr(castLocal, xValueMethod.makeRef());
		body.getUnits().add(Jimple.v().newAssignStmt(primLocal, vInvExp));
		
	}

	@Override
	public void caseArrayType(ArrayType t) {
		//TODO: Instead of casting the array, add the value to the array!
		paramList.add(castLocal);
		
	}

	@Override
	public void caseBooleanType(BooleanType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Boolean: boolean booleanValue()>");
		
	}

	@Override
	public void caseByteType(ByteType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Byte: byte byteValue()>");
		
	}

	@Override
	public void caseCharType(CharType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Char: char charValue()>");
		
	}

	@Override
	public void caseDoubleType(DoubleType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Double: double doubleValue()>");
		
	}

	@Override
	public void caseFloatType(FloatType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Float: float floatValue()>");
		
	}

	@Override
	public void caseIntType(IntType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Integer: int intValue()>");
		
	}

	@Override
	public void caseLongType(LongType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Long: long longValue()>");
		
	}

	@Override
	public void caseRefType(RefType t) {
		paramList.add(castLocal);
		
	}

	@Override
	public void caseShortType(ShortType t) {
		makeCallToXValue(t, "<"+JAVA_LANG+".Short: short shortValue()>");		
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
	public void caseVoidType(soot.VoidType t) {
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
