/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.forwardAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhul.clod.sootPlugin.types.BabelViewType;

import soot.Local;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;

public class StringFoldingAnalysis extends ForwardFlowAnalysis<Unit, Map<Local, String>> {

	public static final String TOP = "*";

	private List<Local> locals;

	public StringFoldingAnalysis(DirectedGraph<Unit> cfg, Chain<Local> methodLocals) {
		super(cfg);

		locals = new ArrayList<>();
		for (Local l : methodLocals) {
			if (l.getType().toString().equals(BabelViewType.STRING_TYPE)
					|| l.getType().toString().equals(BabelViewType.STRING_BUILDER_TYPE)) {
				locals.add(l);
			}
		}

		doAnalysis();

	}

	/**
	 * Return the constant value of the given local at the given unit
	 * 
	 * @param l
	 * @param u
	 * @return The constant String for the given local at the given unit.
	 */
	public String getStringtAt(Local l, Unit u) {
		return getFlowAfter(u).get(l);

	}

	@Override
	protected void flowThrough(Map<Local, String> in, Unit d, Map<Local, String> out) {
		// System.out.println(d);

		StringFoldingVisitor visitor = new StringFoldingVisitor(in, out);
		d.apply(visitor);

		// for(Local l : locals) {
		// System.out.println(l+" -> "+out.get(l));
		// }
	}

	@Override
	protected Map<Local, String> entryInitialFlow() {
		Map<Local, String> entryMap = new HashMap<>();
		for (Local l : locals) {
			entryMap.put(l, "");
		}
		return entryMap;
	}

	@Override
	protected Map<Local, String> newInitialFlow() {
		Map<Local, String> entryMap = new HashMap<>();
		for (Local l : locals) {
			entryMap.put(l, "");
		}
		return entryMap;
	}

	@Override
	protected void merge(Map<Local, String> in1, Map<Local, String> in2, Map<Local, String> out) {
		for (Local lKey : in1.keySet()) {
			String val1 = in1.get(lKey);
			String val2 = in2.get(lKey);

			if (val1.equals(val2)) {
				out.put(lKey, val1);
			} else {
				out.put(lKey, TOP);
			}
		}

	}

	@Override
	protected void copy(Map<Local, String> source, Map<Local, String> dest) {
		dest.putAll(source);

	}

}
