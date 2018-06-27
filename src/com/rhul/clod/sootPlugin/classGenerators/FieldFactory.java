/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators;

import java.util.ArrayList;
import java.util.List;

import soot.Modifier;
import soot.RefType;
import soot.SootField;

/**
 * This factory generates fields in a sequential way having $fn as name, where n
 * is an incremental number for each field added.
 * 
 * @author clod
 *
 */
public class FieldFactory {

	private static FieldFactory instance = null;

	private static final String FIELD_NAME = "$f";

	private List<SootField> fields;

	private FieldFactory() {
		fields = new ArrayList<>();
	}

	public static FieldFactory getInstance() {
		if (instance == null) {
			instance = new FieldFactory();
		}

		return instance;
	}

	public SootField generateField(String fieldType) {
		SootField sField = new SootField(FIELD_NAME + fields.size(), RefType.v(fieldType), Modifier.PRIVATE);
		fields.add(sField);
		return sField;
	}

}
