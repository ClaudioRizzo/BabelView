/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.visitors;

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
import soot.ShortType;
import soot.StmtAddressType;
import soot.Type;
import soot.TypeSwitch;
import soot.Unit;
import soot.UnknownType;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Jimple;

public class CastVisitor extends TypeSwitch {
	
	
	private Local castLocal;
	private Unit castUnit;
	
	private Local objectLocal;
	
	
	public CastVisitor(Local objectLocal, Body body) {
		this.objectLocal = objectLocal;
	}
	
	
	private void generateCast(Type t) {
		Local castLocal = LocalFactory.getInstance().genLocal(t);
		CastExpr cExpr = Jimple.v().newCastExpr(this.objectLocal, castLocal.getType());
		AssignStmt aStm = Jimple.v().newAssignStmt(castLocal, cExpr);
		this.castLocal = castLocal;
		this.castUnit = aStm;
	}

	@Override
	public void caseArrayType(ArrayType t) {
		generateCast(t);

	}

	@Override
	public void caseBooleanType(BooleanType t) {
		generateCast(RefType.v(BabelViewType.BOOLEAN_TYPE));

	}

	@Override
	public void caseByteType(ByteType t) {
		generateCast(RefType.v(BabelViewType.BYTE_TYPE));

	}

	@Override
	public void caseCharType(CharType t) {
		generateCast(RefType.v(BabelViewType.CHAR_TYPE));

	}

	@Override
	public void caseDoubleType(DoubleType t) {
		generateCast(RefType.v(BabelViewType.DOUBLE_TYPE));

	}

	@Override
	public void caseFloatType(FloatType t) {
		generateCast(RefType.v(BabelViewType.FLOAT_TYPE));
	}

	@Override
	public void caseIntType(IntType t) {
		generateCast(RefType.v(BabelViewType.INTEGER_TYPE));
	}	

	@Override
	public void caseLongType(LongType t) {
		generateCast(RefType.v(BabelViewType.LONG_TYPE));

	}

	@Override
	public void caseRefType(RefType t) {
		Local castLocal = LocalFactory.getInstance().genLocal(t);
		CastExpr cExpr = Jimple.v().newCastExpr(this.objectLocal, castLocal.getType());
		AssignStmt aStm = Jimple.v().newAssignStmt(castLocal, cExpr);
		this.castLocal = castLocal;
		this.castUnit = aStm;
	}

	@Override
	public void caseShortType(ShortType t) {
		generateCast(RefType.v(BabelViewType.SHORT_TYPE));

	}

	@Override
	public void caseStmtAddressType(StmtAddressType t) {
		caseDefault(t);

	}

	@Override
	public void caseUnknownType(UnknownType t) {
		caseDefault(t);
	}

	@Override
	public void caseVoidType(VoidType t) {
		caseDefault(t);
	}

	@Override
	public void caseAnySubType(AnySubType t) {
		caseDefault(t);
	}

	@Override
	public void caseNullType(NullType t) {
		caseDefault(t);
	}

	@Override
	public void caseErroneousType(ErroneousType t) {
		caseDefault(t);
	}

	@Override
	public void caseDefault(Type t) {
		
	}




	public Local getCastLocal() {
		return castLocal;
	}




	public Unit getCastUnit() {
		return castUnit;
	}


}
