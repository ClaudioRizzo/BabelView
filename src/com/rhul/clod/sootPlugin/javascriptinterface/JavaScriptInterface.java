/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.javascriptinterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.postAnalysis.PostAnalysisResults.Vulns;

public class JavaScriptInterface implements Iterable<JavaScriptMethod> {

	private List<JavaScriptMethod> exposedMethods;
	private String type;
	private BabelConfig config = BabelConfig.getConfigs();

	/**
	 * It is the name the developer gave to this interface in order to use it in the
	 * javascript section.
	 */
	private List<String> names;

	/**
	 * Type of the WebView adding this interface
	 */
	private List<String> webViewTypes;

	public JavaScriptInterface(String type) {
		this.exposedMethods = new ArrayList<JavaScriptMethod>();
		this.type = type;
		this.names = new ArrayList<>();
		this.webViewTypes = new ArrayList<>();

	}

	public void addWebViewBind(String webViewType) {
		if (!webViewTypes.contains(webViewType)) {
			webViewTypes.add(webViewType);
		}
	}

	public List<String> getWebViewTypesBinded() {
		List<String> toReturn = new ArrayList<>(webViewTypes);
		return toReturn;
	}

	public JavaScriptInterface(String type, JavaScriptMethod... javaScriptMethods) {
		this(type);
		this.exposedMethods = Arrays.asList(javaScriptMethods);
	}

	@Override
	public Iterator<JavaScriptMethod> iterator() {
		return exposedMethods.iterator();
	}

	public String getType() {
		return this.type;
	}

	public void addMethod(JavaScriptMethod jsMethod) {
		if (!exposedMethods.contains(jsMethod)) {
			exposedMethods.add(jsMethod);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		JavaScriptInterface other = (JavaScriptInterface) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public List<String> getNames() {
		List<String> jsNames = new ArrayList<>(names);
		return Collections.unmodifiableList(jsNames);
	}

	public void addName(String name) {
		if (!this.names.contains(name)) {
			this.names.add(name);
		}
	}

	public void toJson(boolean all) {
		for (JavaScriptMethod jsMethod : exposedMethods) {
			if (jsMethod.isVulnerable() || all) {
				jsMethod.toJson();
			}
		}
	}

	public void classToFile(int id) {
		File dir = new File(config.getLibraryFolder() + "/" + config.getApkName());
		Map<Vulns, String> vulnerabilities = new HashMap<>();
		List<String> intentVulns = new ArrayList<>();
		for (JavaScriptMethod jsMethod : exposedMethods) {
			if (jsMethod.isVulnerable()) {
				for (Vulns v : jsMethod.vulnTo()) {
					vulnerabilities.put(v, jsMethod.getsMethod().getSubSignature());
				}

				intentVulns.addAll(jsMethod.getIntentVulns());
			}
		}

		if (dir.mkdirs())
			System.out.println("Created directory: " + dir.getAbsolutePath());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject jobj = new JsonObject();

		jobj.add("library", gson.toJsonTree(getType()));
		jobj.add("vulns", gson.toJsonTree(vulnerabilities));
		jobj.add("ivulns", gson.toJsonTree(intentVulns));

		String path = dir.getAbsolutePath() + "/" + id + "_" + config.getApkName() + ".libs";

		try (FileWriter file = new FileWriter(path)) {
			file.write(gson.toJson(jobj));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
