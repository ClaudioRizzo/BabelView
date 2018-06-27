/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.loadUrlAnalysis;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class StringBuilderModel {

	public StringBuilderModel() {
	}

	private List<Unit> current = new ArrayList<>();

	//TODO: while loop not supported
	public List<List<Unit>> getStringBuilderUnits(UnitGraph cfg, Unit sUnit) {

		List<List<Unit>> results = new ArrayList<>();

		for (Unit pred : cfg.getPredsOf(sUnit)) {

			if (pred.toString().contains("<java.lang.StringBuilder: void <init>")
					&& !pred.toString().contains("goto")) {
				// Base case
				current.add(pred);

				results.add(current);
				current = new ArrayList<>();

			} else if (pred.toString().contains("<java.lang.StringBuilder: java.lang.StringBuilder append")
					&& !pred.toString().contains("goto")) {

				current.add(pred);

				results.addAll(getStringBuilderUnits(cfg, pred));

			} else {
				// We go on
				results.addAll(getStringBuilderUnits(cfg, pred));
			}

		}

		return results;
	}
	
	

}
