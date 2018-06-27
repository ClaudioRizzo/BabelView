/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhul.clod.sootPlugin.exceptions.ClassNeverAddedException;
import com.rhul.clod.sootPlugin.exceptions.ExceptionMessages;

import soot.Modifier;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

public class SootCreatorHelper extends Helper {

	private static SootCreatorHelper INSTANCE;
	public static final String OBJECT_CLASS = "java.lang.Object";

	public static final String ADDJS_METHOD_NAME = "addJavascriptInterface";

	/**
	 * Map containing all the new classes created by this helper
	 */
	private Map<String, SootClass> addedClass;
	

	

	private SootCreatorHelper() {
		addedClass = new HashMap<String, SootClass>();
	}

	protected static SootCreatorHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SootCreatorHelper();
		}
		return INSTANCE;
	}
	
	
	/**
	 * Create a class with the given name and superClass. By default, Object is
	 * the superclass of the created class.
	 * 
	 * @param name
	 * @param superclass
	 * @return The class created
	 */
	public SootClass createSootClass(String name, String superclass) {
		SootClass sClass = new SootClass(name, Modifier.PUBLIC);

		if (superclass != null) {
			sClass.setSuperclass(Scene.v().getSootClass(superclass));
		} else {
			sClass.setSuperclass(Scene.v().getSootClass(OBJECT_CLASS));
		}

		Scene.v().addClass(sClass);
		addedClass.put(name, sClass);

		return sClass;
	}

	/**
	 * This method create a general public method with the signature
	 * corresponding to the given parameters
	 * 
	 * @param The
	 *            name of the class you want to add the method to
	 * @param The
	 *            name of the method you want to add
	 * @param List
	 *            of Types of the parameters
	 * @param Type
	 *            the method has to return
	 * @throws ClassNeverAddedException
	 *             thrown in case the class has never been created.
	 */
	public SootMethod addPublicMethodSignatureToClass(String cName, String methodName, List<Type> parameters,
			Type returnType) throws ClassNeverAddedException {
		SootClass sClass = this.getAddedClass(cName);
		SootMethod method = new SootMethod(methodName, parameters, returnType, Modifier.PUBLIC);
		sClass.addMethod(method);
		return method;
	}

	

	/**
	 * This method prints the class given as parameter to a file in the
	 * sootOutput directory
	 * 
	 * @param className
	 */
	public void printClassToFile(String className) {

		try {

			SootClass sClass = Scene.v().getSootClass(className);
			String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
			OutputStream streamOut;
			// streamOut = new FileOutputStream(fileName);
			streamOut = new JasminOutputStream(new FileOutputStream(fileName));
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
			jasminClass.print(writerOut);
			// Printer.v().printTo(sClass, writerOut);
			writerOut.flush();
			streamOut.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * This method prints the class given as parameter to a file in the
	 * sootOutput directory in a Jimple format
	 * 
	 * @param className
	 */
	public void printJimpleToFile(String className) {
		try {

			SootClass sClass = Scene.v().getSootClass(className);
			String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_jimple);
			OutputStream streamOut;
			streamOut = new FileOutputStream(fileName);
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(sClass, writerOut);
			writerOut.flush();
			streamOut.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SootClass getAddedClass(String cName) throws ClassNeverAddedException {
		SootClass sClass = this.addedClass.get(cName);

		if (sClass == null) {
			throw new ClassNeverAddedException(cName + ": " + ExceptionMessages.CLASS_NOT_ADDED);
		} else {
			return sClass;
		}
	}
}
