/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptMethod;
import com.rhul.clod.sootPlugin.webViewParser.BabelViewRecords;


public class PostAnalysisResults {

	public enum Vulns {
		PREFERENCES_LEAK_DB("Preferences DB Query Exec"), PREF_DATA_BASE_LEAK("Pref DB Leak"), PREF_TM_LEAK("Pref TM Leak"),
		PREF_INT_CON_LEAKS("Pref Connectivity Leaks"), PREF_LOCATION_LEAKS("Pref Location Leaks"),
		DATA_BASE_LEAK("SQL-lite Leaks"), TM_LEAK("TM Leaks"), INT_CON_LEAKS("Connectivity Leaks"), 
		LOCATION_LEAKS("Location Leaks"), QUERY_EXEC("SQL-lite Query Exec"), INTENT_CONTROL("Intent Control"), 
		OPEN_FILE("File Opening"), WRITE_FILE("File Writing"), READ_FILE("File Reading"), SMS("Send SMS"), SOCK("Open Socket"), 
		API("Reflection"), FRAME_CONFUSION("Frame Confusion"), FORNAME("Fetch Class"), FETCH_METHOD("Fetch method"), 
		NEW_INSTANCE("Constructor Instance"), METHOD_PARAM("Method Parameter"), FETCH_CONSTR("Fetch Constructor");

		private final String name;

		private Vulns(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		


	}
	
	enum IntentVulns {
		ACTION_VIEW("android.intent.action.VIEW"), ACTION_CALL("android.intent.action.CALL"), ACTION_SEND("android.intent.action.SEND"),
		ACTION_SENDTO("android.intent.action.SENDTO"), EXTERNAL_APP("getLaunchIntentForPackage");
		
		private final String action;
		
		private IntentVulns(final String action) {
			this.action = action;
		}
		
		@Override
		public String toString() {
			return this.action;
		}
		
		
	}

	private static PostAnalysisResults INSTANCE;
	
	private Map<Vulns, Integer> result;
	private List<String> intentVulns;
	
	/**
	 * A list of vulnerable js-interface methods
	 */
	private List<JavaScriptMethod> vulnJsIfaces;
	private List<JavaScriptInterface> vulnJsObjects;
	
	private BabelViewRecords bRecord = BabelViewRecords.getInstance();
	
	private PostAnalysisResults() {
		result = new HashMap<>();
		intentVulns = new ArrayList<>();
		vulnJsIfaces = new ArrayList<>();
		vulnJsObjects = new ArrayList<>();
		initResult();
	}
	
	private void initResult() {
		for(Vulns v : Vulns.values()) {
			result.put(v, 0);
		}
		
	}

	public static PostAnalysisResults getResults() {
		if(INSTANCE == null)
			INSTANCE = new PostAnalysisResults();
		
		return INSTANCE;
	}
	
	/**
	 * Add the given vulnerability to the map
	 * @param vuln
	 */
	public void addResult(Vulns vuln) {
		result.put(vuln, 1);
	}
	
	/**
	 * Given the vulnerability {@code vuln}, it returns true if the analysed APK is vulnerable to {@code vuln}
	 * @param vuln
	 * @return true if the APK is vulnerable to {@code vuln}, false otherwise
	 */
	public boolean isVulnerableTo(Vulns vuln) {
		return result.get(vuln) == 1;
	}
	
	public void addIntentVuln(String vuln, String jsIfaceSignature) {
		
		if(!this.intentVulns.contains(vuln)) {
			this.intentVulns.add(vuln);
			
			for(JavaScriptMethod jsIface : getVulInfaces()) {
				
				if(jsIface.getSignature().equals(jsIfaceSignature)) {
					jsIface.addIntentVuln(vuln);
				}
			}
		}
		
		
	}
	
	
	public void toJsonFile(String path) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject jobj = new JsonObject();
		
		if(BabelConfig.getConfigs().isIntent() && isVulnerableTo(Vulns.INTENT_CONTROL)) {
			for(Vulns v : Vulns.values()) {
				
				if(!v.equals(Vulns.INTENT_CONTROL)) {
					jobj.add(v.toString(), gson.toJsonTree(result.get(v)));
				} else {
					
					jobj.add(v.toString(), gson.toJsonTree(intentVulns));
				}
			}
		}
			
		
		
		try (FileWriter file = new FileWriter(path)) {
			if(BabelConfig.getConfigs().isIntent() && isVulnerableTo(Vulns.INTENT_CONTROL))
				file.write(gson.toJson(jobj));
			else
				file.write(gson.toJson(result));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void printResults() {
		for(Vulns v : Vulns.values()) {
			System.out.println(v + ": "+result.get(v));
		}
	}
	
	public void addVulnerableIface(Vulns vuln, String jsSignature) {
		
		if (!bRecord.isParsed())
			throw new RuntimeException("I can't retrieve js-methods without parsing the app first!");

		findAndAddVulnerable(vuln, jsSignature);
		
	}

	private void findAndAddVulnerable(Vulns vuln, String jsSignature) {
		for(JavaScriptInterface jsIface : bRecord.getAllJsInterfaces()) {
			for(JavaScriptMethod jsMethod : jsIface) {
			
				if(jsMethod.getSignature().equals(jsSignature)) {
					jsMethod.setVulnerable(true);
					jsMethod.addVuln(vuln);
					this.vulnJsIfaces.add(jsMethod);
					
					JavaScriptInterface jsObject = jsMethod.getBelogToInterface();
					if(!this.vulnJsObjects.contains(jsObject)) {
						this.vulnJsObjects.add(jsObject);
					}
				} 
			}
		}
		
	}
	
	public List<JavaScriptMethod> getVulInfaces() {
		return this.vulnJsIfaces;
	}
	
	public List<JavaScriptInterface> getVulnJsObject() {
		return this.vulnJsObjects;
	}
 	
}
