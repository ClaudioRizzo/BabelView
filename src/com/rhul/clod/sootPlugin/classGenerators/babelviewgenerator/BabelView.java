/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhul.clod.sootPlugin.classGenerators.FieldFactory;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.AddJsInterfaceMethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.BabelLeakMethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.InitMethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.InputSourceMethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.LoadUrlMethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.MethodGenerator;
import com.rhul.clod.sootPlugin.classGenerators.methodGenerators.SetWebViewClientMethodGenerator;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;
import com.rhul.clod.sootPlugin.types.BabelViewType;
import com.rhul.clod.sootPlugin.webViewParser.BabelViewRecords;

import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class BabelView extends SootClass {

	/**
	 * Map type of a field with its instance. No needs for a list of fields since we
	 * require only one instance for the same type of field!
	 */
	private Map<String, SootField> fields;

	/**
	 * In thi simplementation as contructors we mean the superclass constructors!
	 * (Useful to instrument)
	 */
	private Map<String, SootMethod> supeClassConstructors;

	private Map<String, SootMethod> toOverrideMethods;

	private List<SootMethod> babelConstructors;

	private List<JavaScriptInterface> jsInterfaces;

	public static final String BABEL_VIEW = "BabelView";
	private static final FieldFactory fFactory = FieldFactory.getInstance();
	private static final BabelViewRecords bViewRecords = BabelViewRecords.getInstance();

	public BabelView() {
		this(Scene.v().getSootClass(BabelViewType.WEB_VIEW_CLASS));
	}

	public BabelView(SootClass superClass) {
		this(superClass, BABEL_VIEW);
	}

	public BabelView(SootClass superClass, String name) {
		super(name, Modifier.PUBLIC);
		this.jsInterfaces = new ArrayList<>();
		setSuperclass(superClass);
		Scene.v().addClass(this);
		fields = new HashMap<>();
		supeClassConstructors = new HashMap<>();
		toOverrideMethods = new HashMap<>();
		babelConstructors = new ArrayList<>();
		initFileds();

	}

	private void initFileds() {
		// adding webClient field
		SootField webClientField = fFactory.generateField(BabelViewType.WEBVIEW_CLIENT_TYPE);
		fields.put(BabelViewType.WEBVIEW_CLIENT_TYPE, webClientField);
		addField(webClientField);

		// adding tree set field
		SootField treeField = fFactory.generateField(BabelViewType.TREE_SET_TYPE);
		fields.put(BabelViewType.TREE_SET_TYPE, treeField);
		addField(treeField);

		// adding js-interfaces fields

		this.jsInterfaces = bViewRecords.getAllInterfaceForWebViewType(getSuperclass().getType().toString());

		// if(jsInterfaces.isEmpty()) {

		// }

		for (JavaScriptInterface jsInterface : jsInterfaces) {
			String type = jsInterface.getType();
			SootField field = fFactory.generateField(type);
			fields.put(type, field);
			addField(field);
		}
	}

	public SootField getBabelField(String type) {
		if (fields.containsKey(type)) {
			return fields.get(type);
		} else {
			throw new RuntimeException("Field never added!");
		}
	}

	/**
	 * Returns all the super class constructors of this BabelView.
	 * 
	 * 
	 * @return A list of all the constructors of this BabelView
	 */
	public List<SootMethod> getAllSuperClassConstructors() {
		return Collections.unmodifiableList(new ArrayList<SootMethod>(supeClassConstructors.values()));
	}

	/**
	 * Return the super class constructor corresponding to the signature given
	 * 
	 * @param signature
	 * @return a constructor
	 */
	public SootMethod getSuperClassConstructor(String signature) {
		return supeClassConstructors.get(signature);
	}

	public void addSuperClassConstructor(String signature, SootMethod constructor) {

		if (!supeClassConstructors.containsKey(signature)) {
			this.supeClassConstructors.put(signature, constructor);
		}
	}

	public void addMethodToOverride(String name, SootMethod sMethod) {
		if (!toOverrideMethods.containsKey(name)) {
			this.toOverrideMethods.put(name, sMethod);
		}
	}

	public List<SootMethod> getBabelContructors() {
		return Collections.unmodifiableList(this.babelConstructors);
	}

	/**
	 * Takes as parameters the superSignature of: setWebViewClient,
	 * addJavascriptInterface and loadUrl methods. Notice that the order has to be
	 * the one mentioned above!
	 **/
	public void createMethodsBodies(String... superSignatures) {
		MethodGenerator inputSourceGen = new InputSourceMethodGenerator(this);
		MethodGenerator initGen = new InitMethodGenerator(this);
		MethodGenerator setWebViewClientGen = new SetWebViewClientMethodGenerator(this, superSignatures[0]);
		MethodGenerator addJsGen = new AddJsInterfaceMethodGenerator(this, superSignatures[1]);
		MethodGenerator loadUrlGen = new LoadUrlMethodGenerator(this, superSignatures[2]);
		MethodGenerator headerLoadUrlGen = new LoadUrlMethodGenerator(this, superSignatures[3]);
		MethodGenerator leakGen = new BabelLeakMethodGenerator(this);

		MethodGenerator postUrl = new LoadUrlMethodGenerator(this, superSignatures[4]);
		MethodGenerator loadData = new LoadUrlMethodGenerator(this, superSignatures[5]);
		MethodGenerator loadDataWithBaseURL = new LoadUrlMethodGenerator(this, superSignatures[5]);

		inputSourceGen.generateMethod();
		leakGen.generateMethod();

		initGen.generateMethod();
		babelConstructors.addAll(((InitMethodGenerator) initGen).getAllGeneratedConstructors());
		addJsGen.generateMethod();
		setWebViewClientGen.generateMethod();

		loadUrlGen.generateMethod();
		headerLoadUrlGen.generateMethod();
		postUrl.generateMethod("postUrl");
		loadData.generateMethod("loadData");
		loadDataWithBaseURL.generateMethod("loadDataWithBaseURL");

	}

	public List<JavaScriptInterface> getJsInterfaces() {

		return this.jsInterfaces;
	}

}
