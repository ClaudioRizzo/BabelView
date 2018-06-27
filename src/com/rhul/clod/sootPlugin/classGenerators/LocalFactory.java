/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Type;
import soot.jimple.Jimple;

//TODO: make mode use safe! if sequential mode is selected, only sequential mode can be used along the whole program. Same for the Dedicated.
public class LocalFactory {

	public final static String SEQUENTIAL_MODE = "sequential";
	public final static String DEDICATED_MODE = "dedicated";

	public final static String PRIMITIVE = "primitive";
	public final static String REFERENCE = "reference";
	public final static String GENERAL = "general";

	private final static String GENERAL_NAME = "$g";

	/**
	 * For each type of locals (primitive, reference or general) we save a list of
	 * the Local assigned. Each new local name will be incremented by 1 Sequential
	 * use only
	 */
	private Map<String, List<Local>> localNamesSequentialMap;

	private static LocalFactory instance = null;

	private LocalFactory() {
		this.localNamesSequentialMap = new HashMap<>();
	}

	public static LocalFactory getInstance() {
		if (instance == null) {
			instance = new LocalFactory();
		}
		return instance;
	}

	/**
	 * Generates a local. The name of the local follows this logic:
	 * 
	 * primitive type: $pn where n is a number from 0 to N, reference type: $rn
	 * where n is a number from 0 to N, general type: $gn where n is a number from 0
	 * to N general type are all the types not present in the BabelTypeEnum
	 * 
	 * Sequential mode will increase sequentially the number of the local even if in
	 * different methods Dedicated mode will restart the counting for each method
	 * 
	 * @param type
	 * @param mode:
	 *            sequential mode or dedicated mode: use the strings provided by
	 *            this factory
	 * @return a local of that type
	 */

	public Local genLocal(Type type) {
		List<Local> generalLocals = this.localNamesSequentialMap.get(GENERAL);
		if (generalLocals == null) {
			generalLocals = new ArrayList<>();
			this.localNamesSequentialMap.put(GENERAL, generalLocals);
		}
		String name = GENERAL_NAME + generalLocals.size();
		Local local = Jimple.v().newLocal(name, type);
		generalLocals.add(local);
		return local;
	}

}
