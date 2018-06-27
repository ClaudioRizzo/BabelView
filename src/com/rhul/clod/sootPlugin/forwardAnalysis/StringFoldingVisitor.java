/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.forwardAnalysis;

import java.util.Map;

import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StmtSwitch;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

public class StringFoldingVisitor implements StmtSwitch {

	private static final String SB_APPEND = "<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>";
	private static final String SB_TO_STRING = "<java.lang.StringBuilder: java.lang.String toString()>";
	private static final String SB_INIT_PLAIN = "<java.lang.StringBuilder: void <init>()>";
	private static final String SB_INIT_STR = "<java.lang.StringBuilder: void <init>(java.lang.String)>";

	private Map<Local, String> setIn;
	private Map<Local, String> setOut;

	private static final String TOP = StringFoldingAnalysis.TOP;

	public StringFoldingVisitor(Map<Local, String> setIn, Map<Local, String> setOut) {
		this.setIn = setIn;
		this.setOut = setOut;
	}

	@Override
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		defaultCase(stmt);

		if (InstanceInvokeExpr.class.isAssignableFrom(stmt.getInvokeExpr().getClass())) {
			InstanceInvokeExpr iExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
			// We support append, toString, and init of the String builder.
			stringBuilder(iExpr, (Local) iExpr.getBase());
		}

	}

	private boolean stringBuilder(InstanceInvokeExpr iExpr, Local toReassign) {

		SootMethod iMethod = iExpr.getMethod();
		Local lBase = (Local) iExpr.getBase();
		if (iMethod.getSignature().equals(SB_APPEND)) {
			Value arg = iExpr.getArg(0);
			String toAppendStr = stringBuilderParamValue(arg);

			setOut.put(toReassign, concat(setIn.get(toReassign), toAppendStr));
			return true;

		} else if (iMethod.getSignature().equals(SB_TO_STRING)) {

			setOut.put(toReassign, setIn.get(lBase));
			return true;
		} else if (iMethod.getSignature().equals(SB_INIT_PLAIN)) {
			setOut.put(toReassign, "");
			return true;
		} else if (iMethod.getSignature().equals(SB_INIT_STR)) {
			setOut.put(toReassign, stringBuilderParamValue(iExpr.getArg(0)));
		}
		return false;

	}

	private String stringBuilderParamValue(Value arg) {
		String toAppendStr = TOP;

		if (StringConstant.class.isAssignableFrom(arg.getClass())) {
			toAppendStr = ((StringConstant) arg).value;
		} else if (Local.class.isAssignableFrom(arg.getClass())) {
			toAppendStr = setIn.get(arg);
		}

		return toAppendStr;

	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		defaultCase(stmt);

		Value left = stmt.getLeftOp();
		Value right = stmt.getRightOp();

		if (Local.class.isAssignableFrom(left.getClass())) {
			Local lLocal = (Local) left;
			if (setIn.keySet().contains(lLocal)) {
				// Then it is a local we care of and we need to check
				// the origin of its assignment

				if (InvokeExpr.class.isAssignableFrom(right.getClass())) {
					if (StaticInvokeExpr.class.isAssignableFrom(right.getClass())) {
						setOut.put(lLocal, TOP);
					} else {

						InstanceInvokeExpr iExpr = (InstanceInvokeExpr) right;

						if (!stringBuilder(iExpr, lLocal)) {
							// It means the there was a custom method invocation,
							// so it could be anything as we don't follow it.
							setOut.put(lLocal, TOP);
						}
					}
				} else if (Local.class.isAssignableFrom(right.getClass())) {
					Local rLocal = (Local) right;
					// we simply update the value of left with whatever there was in right.
					setOut.put(lLocal, setIn.get(rLocal));
				} else if (StringConstant.class.isAssignableFrom(right.getClass())) {
					// this time the new value of lLocal is the constant string
					setOut.put(lLocal, ((StringConstant) right).value);
				} else if (NewExpr.class.isAssignableFrom(right.getClass())) {
					NewExpr newExpr = (NewExpr) right;
					if (newExpr.getType().toString().equals(BabelViewType.STRING_BUILDER_TYPE)) {
						setOut.putAll(setIn);
					}
				} else if (InstanceFieldRef.class.isAssignableFrom(right.getClass())) {

					setOut.put(lLocal, TOP);
				} else if (FieldRef.class.isAssignableFrom(right.getClass())) {

					String str = "";
					if (StringConstant.class.isAssignableFrom(right.getClass())) {
						// We have a constant so we are done
						str = ((StringConstant) right).value;
					} else {

						FieldRef fRef = (FieldRef) right;
						str = lookUpFieldRef(fRef);
					}
					setOut.put(lLocal, str);

				} else {
					// As we don't support any other case we assign TOP

					setOut.put(lLocal, TOP);
				}
			}
		} else {
			// If left is not a local, we really don't care.
		}
	}

	private String lookUpFieldRef(FieldRef fRef) {
		SootMethod clinit = fRef.getField().getDeclaringClass().getMethod("void <clinit>()");
		for (Unit u : clinit.retrieveActiveBody().getUnits()) {
			if (AssignStmt.class.isAssignableFrom(u.getClass())) {
				AssignStmt aStmt = (AssignStmt) u;
				Value left = aStmt.getLeftOp();
				Value right = aStmt.getRightOp();

				if (FieldRef.class.isAssignableFrom(left.getClass())) {
					FieldRef aFieldRef = (FieldRef) left;
					// If the static field is the one that we are looking for...
					if (aFieldRef.getField().equals(fRef.getField())) {
						// we then retrieve its constant value if it is a string!
						if (StringConstant.class.isAssignableFrom(right.getClass())) {
							StringConstant vStr = (StringConstant) right;
							return vStr.value;
						}
					}
				}
			}
		}
		return TOP;
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		defaultCase(stmt);

		Value left = stmt.getLeftOp();
		Value right = stmt.getRightOp();

		if (Local.class.isAssignableFrom(left.getClass())) {
			Local lLocal = (Local) left;

			if (setIn.containsKey(lLocal)) {
				// It is a Local we care of
				if (IdentityRef.class.isAssignableFrom(right.getClass())) {
					// the local is not a constant and depends on external input
					setOut.put(lLocal, TOP);
				}
			}
		}

	}

	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseGotoStmt(GotoStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		defaultCase(stmt);

	}

	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseNopStmt(NopStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseRetStmt(RetStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void caseThrowStmt(ThrowStmt stmt) {
		defaultCase(stmt);
	}

	@Override
	public void defaultCase(Object obj) {
		setOut.putAll(setIn);

	}

	private String concat(String s1, String s2) {
		if (s1.equals(TOP) || s2.equals(TOP)) {
			return TOP;
		} else {
			return s1 + s2;
		}
	}

}