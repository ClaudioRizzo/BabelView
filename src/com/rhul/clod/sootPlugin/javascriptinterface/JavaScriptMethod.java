/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.javascriptinterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.exceptions.NoParametersException;
import com.rhul.clod.sootPlugin.postAnalysis.PostAnalysisResults.Vulns;

import soot.SootMethod;
import soot.Type;

/**
 * Class representing a method in the JavaScript interface.
 * 
 * @author clod
 *
 */
public class JavaScriptMethod {

	private List<Type> parameters;
	private Type returnType;
	private String signature;
	private List<Vulns> vulns;
	private List<String> intentVulns;

	private JavaScriptInterface belogToInterface;

	/**
	 * Soot method representing this interface
	 */
	private SootMethod sMethod;

	/**
	 * True if this interface method is vulnerable
	 */
	private boolean vulnerable;

	private BabelConfig config = BabelConfig.getConfigs();

	public JavaScriptMethod(SootMethod sMethod, JavaScriptInterface belogToInterface) {
		this.parameters = new ArrayList<Type>();
		this.setsMethod(sMethod);
		this.setBelogToInterface(belogToInterface);
		this.signature = sMethod.getSignature();
		this.vulns = new ArrayList<>();
		this.intentVulns = new ArrayList<>();
	}

	public JavaScriptMethod(SootMethod sMethod, JavaScriptInterface belogToInterface, Type returnType) {
		this(sMethod, belogToInterface);
		this.returnType = returnType;

	}

	public JavaScriptMethod(SootMethod sMethod, JavaScriptInterface belogToInterface, Type returnType, Type... params) {
		this(sMethod, belogToInterface, returnType);
		this.parameters = Arrays.asList(params);
	}

	public void addParameter(Type parameter) {
		parameters.add(parameter);
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	public Type getReturnType() {
		return this.returnType;
	}

	public String getSignature() {
		return this.signature;
	}

	public List<Type> getParameters() throws NoParametersException {
		if (parameters.isEmpty())
			throw new NoParametersException("");

		return Collections.unmodifiableList(parameters);
	}

	public SootMethod getsMethod() {
		return sMethod;
	}

	public void setsMethod(SootMethod sMethod) {
		this.sMethod = sMethod;
	}

	public boolean isVulnerable() {
		return vulnerable;
	}

	public void setVulnerable(boolean vulnerable) {
		this.vulnerable = vulnerable;
	}

	public JavaScriptInterface getBelogToInterface() {
		return belogToInterface;
	}

	public void setBelogToInterface(JavaScriptInterface belogToInterface) {
		this.belogToInterface = belogToInterface;
	}

	public void toJson() {
		File dir = new File(config.getInterfaceFolder() + "/" + config.getApkName());

		if (dir.mkdirs())
			System.out.println("Created directory: " + dir.getAbsolutePath());

		String path = dir.getAbsolutePath() + "/" + getsMethod().getName() + ".json";

		try (FileWriter file = new FileWriter(path)) {
			file.write(getJsonObject());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getJsonObject() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject jobj = new JsonObject();

		jobj.add("name", gson.toJsonTree(belogToInterface.getNames()));
		jobj.add("methodName", gson.toJsonTree(getsMethod().getName()));
		jobj.add("rType", gson.toJsonTree(getReturnType().toString()));

		try {
			jobj.add("paramTypes",
					gson.toJsonTree(getParameters().stream().map(x -> x.toString()).collect(Collectors.toList())));
		} catch (NoParametersException e) {
			jobj.add("paramTypes", gson.toJsonTree(new ArrayList<String>()));
		}

		return gson.toJson(jobj);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaScriptMethod other = (JavaScriptMethod) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	public List<Vulns> vulnTo() {
		if (vulns.isEmpty()) {
			throw new RuntimeException("This method has not been found vulnerable yet");
		}

		return vulns;

	}

	public void addVuln(Vulns vuln) {
		if (!vulns.contains(vuln))
			this.vulns.add(vuln);
	}

	public void addIntentVuln(String iVulns) {
		if (!intentVulns.contains(iVulns)) {
			this.intentVulns.add(iVulns);
		}
	}

	public List<String> getIntentVulns() {
		return intentVulns;
	}

}
