/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.exceptions.NoUnitFoundException;
import com.rhul.clod.sootPlugin.forwardAnalysis.StringFoldingAnalysis;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptInterface;
import com.rhul.clod.sootPlugin.javascriptinterface.JavaScriptMethod;
import com.rhul.clod.sootPlugin.postAnalysis.PostAnalysisResults.Vulns;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Singleton Class which wraps the methods for BabelView post analysis.
 * 
 * @author clod
 *
 */
public class BabelPostAnalysis {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static BabelPostAnalysis INSTANCE;

	private Set<Flow> flowDroidFlows;

	private PostAnalysisResults results = PostAnalysisResults.getResults();

	private List<Flow> furterAnalysisList;
	

	
	private String prefKeyFilePath;
	
	private Map<String, List<String>> prefMap;

	private BabelPostAnalysis() {
		this.furterAnalysisList = new ArrayList<>();
		this.flowDroidFlows = new HashSet<>();
		this.prefMap = new HashMap<>();
	}



	/**
	 * Return an instance of the analysis
	 * 
	 * @return
	 */
	public static BabelPostAnalysis getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BabelPostAnalysis();

		}
		return INSTANCE;
	}

	

	public void addFlow(BabelSource bSource, BabelSink bSink) {
		this.flowDroidFlows.add(new Flow(bSource, bSink));
	}

	/**
	 * Perform an anlysis ONLY AFTER the flowdroid analysis has been completed
	 */
	public void doAnalysis() {

		doAnalysis(this.flowDroidFlows);
	}

	public void doAnalysis(Set<Flow> flows) {
		
		this.prefKeyFilePath = createPrefFolderAndFile();
		
		for (Flow flow : flows) {	
			doAnalysis(flow, flows);
		}
			
		
		if (BabelConfig.getConfigs().getIntent()) {
			if (results.isVulnerableTo(Vulns.INTENT_CONTROL))
				try {
					runIntentParameterAnalysis();
				} catch (NoUnitFoundException e) {

					// We skip this intent...
					LOGGER.warning("[INTENT_WARNING] " + BabelConfig.getConfigs().getApkName());
				}

		}
		
		prefKeyToFile(this.prefKeyFilePath);
		
		if(BabelConfig.getConfigs().isJsinterface()) {
			serializeJsInterfaces();
			BabelExploitGen eGen = new BabelExploitGen(results.getVulnJsObject());
			eGen.toFile();
		}
		
		if(BabelConfig.getConfigs().getLibrary()) {
			
			List<JavaScriptInterface> vulnIfaces = results.getVulnJsObject();
			
			if(vulnIfaces.isEmpty()) {
				LOGGER.info("[NO_LIB] app vulnerable due to low API");
			}
			
			int i=0;
			for(JavaScriptInterface jsObject : vulnIfaces) {
				jsObject.classToFile(i);
				i++;
			}
		}
		
		results.toJsonFile(
				BabelConfig.getConfigs().getReportFolder() + "/" + BabelConfig.getConfigs().getApkName() + ".json");
	}

	private void serializeJsInterfaces() {
		
		
		for (JavaScriptMethod jsMethod : results.getVulInfaces()) {
			jsMethod.toJson();

		}
		
	}



	private void doAnalysis(Flow flow, Set<Flow> flows) {
		try {
			apiLevel();
			infoLeak(flow, false);
			fileTampering(flow);
			intentControl(flow);
			preferencesLeak(flow, flows);
			smsAndSocket(flow);
			farmeConfusion(flow);
			rce(flow);
		} catch (NoUnitFoundException noUnitExcept) {

		}

	}
	
	/**
	 * Currently this method only logs if we find interesting flows regarding reflection
	 * @param flow
	 */
	private void rce(Flow flow) {
		AbstractSourceSink source = flow.getSource();
		AbstractSourceSink sink = flow.getSink();
		if(source.getSignature().matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
			if(sink.getSignature().matches("<java.lang.Class: java.lang.Class forName\\(.*\\)>")) {
				addResult(Vulns.FORNAME, source.getInvolvedIface());
			}
			if(sink.getSignature().matches("<java.lang.Class: java.lang.reflect.Method getMethod\\(.*\\)>")) {
				addResult(Vulns.FETCH_METHOD, source.getInvolvedIface());
			}
			if(sink.getSignature().matches("<java.lang.reflect.Method: java.lang.Object invoke\\(.*\\)>")) {
				addResult(Vulns.METHOD_PARAM, source.getInvolvedIface());
			}
			if(sink.getSignature().matches("<java.lang.Class: java.lang.reflect.Constructor getConstructor\\(.*\\)>")) {
				addResult(Vulns.FETCH_CONSTR, source.getInvolvedIface());
			}
			if(sink.getSignature().matches("<java.lang.reflect.Constructor: java.lang.Object newInstance\\(.*\\)>")) {
				addResult(Vulns.NEW_INSTANCE, source.getInvolvedIface());
			}
		}
		
	}

	



	private void farmeConfusion(Flow flow) {
		AbstractSourceSink source = flow.getSource();
		AbstractSourceSink sink = flow.getSink();

		if(source.getSignature().matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>") &&
				sink.getSignature().matches("<android.webkit.WebView:.*")) {
			addResult(Vulns.FRAME_CONFUSION, flow.getSource().getInvolvedIface());
		}
		
	}



	private void apiLevel() {
		if (Scene.v().getAndroidAPIVersion() < 17) {
			results.addResult(Vulns.API);
		}
	}

	private void smsAndSocket(Flow flow) {
		String sourceSig = flow.getSource().getSignature();
		String sinkSig = flow.getSink().getSignature();

		if (sourceSig.matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
			smsAndSocket(sinkSig, flow.getSource().getInvolvedIface());
		}

	}

	private void smsAndSocket(String sinkSig, String vulnIface) {
		if (sinkSig.matches("<android\\.telephony\\.SmsManager:.*")) {
			addResult(Vulns.SMS, vulnIface);
		} else if (sinkSig.matches("<java\\.net\\.Socket.*")) {
			addResult(Vulns.SOCK, vulnIface);
		}

	}

	private void fileTampering(Flow flow) {
		String sourceSig = flow.getSource().getSignature();
		String sinkSig = flow.getSink().getSignature();

		if (sourceSig.matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
			fileTampering(sinkSig, flow.getSource().getInvolvedIface());
		} else if (sourceSig.matches("<java\\.io\\.BufferedReader:.*")) {
			fileRead(flow.getSink());

		}

	}

	private void fileRead(AbstractSourceSink sink) {
		if (sink.getSignature().matches("<BabelView[0-9]*:.*babelLeak\\(.*\\)>")) {
			String vulnIface = sink.getInvolvedIface();
			addResult(Vulns.READ_FILE, vulnIface);
		}

	}

	private void fileTampering(String sinkSig, String vulnIface) {

		if (sinkSig.matches("<java\\.io\\.Writer:.*|<java\\.io\\.OutputStream:.*|"
				+ "<java\\.io\\.FileOutputStream:.*|<java\\.io\\.OutputStreamWriter:.*")) {

			addResult(Vulns.WRITE_FILE, vulnIface);

		} else if (sinkSig.matches("<java\\.io\\.File:.*")) {
			addResult(Vulns.OPEN_FILE, vulnIface);

		}

	}

	private void intentControl(Flow flow) {
		String sourceSig = flow.getSource().getSignature();
		String sinkSig = flow.getSink().getSignature();

		if (sinkSig.matches(".*startActivit.*")) {

			if (sourceSig.matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
				addResult(Vulns.INTENT_CONTROL, flow.getSource().getInvolvedIface());
				this.furterAnalysisList.add(flow);

			}
		}

	}

	private void runIntentParameterAnalysis() throws NoUnitFoundException {
		List<AbstractSourceSink> done = new ArrayList<>();
		for (Flow result : furterAnalysisList) {
			AbstractSourceSink sink = result.getSink();
			AbstractSourceSink source = result.getSource();

			// InputSource is the unit we generate with BabelView.
			// The argument of inputSource will be the signature of the js-interface to
			// analyse
			Unit inputSourceUnit = source.getCallingUnit();
			AssignStmt inputSourceAssgn = (AssignStmt) inputSourceUnit;
			InvokeExpr inputSourceInv = (InvokeExpr) inputSourceAssgn.getRightOp();
			String jsSignature = ((StringConstant) inputSourceInv.getArg(0)).value;

			Unit callingUnit = sink.getCallingUnit();
			if (InvokeStmt.class.isAssignableFrom(callingUnit.getClass())) {
				InvokeStmt iStmt = (InvokeStmt) callingUnit;

				if (iStmt.getInvokeExpr().getMethod().getName().matches("startActivity.*")) {
					IntentAnalysis ia = new IntentAnalysis(callingUnit, sink.getCallerSootMethod(), jsSignature);
					ia.runIntentAnalysis();
					done.add(sink);
				}

			}

		}
	}

	private void preferencesLeak(Flow flow, Set<Flow> flows) throws NoUnitFoundException {
		String sourceSig = flow.getSource().getSignature();
		String sinkSig = flow.getSink().getSignature();

		if (sourceSig.matches("<android\\.content\\.SharedPreferences:.*")) {
			String key = getPreferenceKey(flow.getSource().getCallingUnit(), flow.getSource().getCallerSootMethod());
			
			if (sinkSig.matches("<BabelView[0-9]*:.*babelLeak\\(.*\\)>")) {
				String involvedIface = flow.getSink().getInvolvedIface();
				
				mapToPreference(involvedIface, key);
				
				//this.prefKeyToFile(involvedIface, key, prefKeyFilePath);
				String merging = sourceSig.split(" ")[2].split("\\(")[0].substring(3);
				preferenceLeak(flows, merging, key, flow.getSink().getInvolvedIface());
			}
		}

	}
	
	private void mapToPreference(String iface, String prefKey) {
		String mapKey = iface.substring(1, iface.length()-1);
		if(this.prefMap.containsKey(mapKey)) {
			prefMap.get(mapKey).add(prefKey);
		} else {
			List<String> keys = new ArrayList<>();
			keys.add(prefKey);
			prefMap.put(mapKey, keys);
		}
		
	}

	private String getPreferenceKey(Unit callingUnit, SootMethod sMethod) {

		Body body = sMethod.retrieveActiveBody();

		if (AssignStmt.class.isAssignableFrom(callingUnit.getClass())) {
			AssignStmt aStmt = (AssignStmt) callingUnit;
			Value right = aStmt.getRightOp();

			if (InvokeExpr.class.isAssignableFrom(right.getClass())) {
				InvokeExpr iStmt = (InvokeExpr) right;
				Value key = iStmt.getArg(0);
				return getKeyValue(key, body, callingUnit);
			}

		} else if (InvokeStmt.class.isAssignableFrom(callingUnit.getClass())) {
			InvokeStmt iStmt = (InvokeStmt) callingUnit;
			Value key = iStmt.getInvokeExpr().getArg(0);
			return getKeyValue(key, body, callingUnit);
		}

		return StringFoldingAnalysis.TOP;

	}

	private String getKeyValue(Value key, Body body, Unit callingUnit) {
		if (StringConstant.class.isAssignableFrom(key.getClass())) {
			return ((StringConstant) key).value;
		} else {
			StringFoldingAnalysis sFolding = new StringFoldingAnalysis(new ExceptionalUnitGraph(body),
					body.getLocals());
			if (Local.class.isAssignableFrom(key.getClass())) {
				Local kLocal = (Local) key;
				
				return sFolding.getStringtAt(kLocal, callingUnit);
			}
		}

		return StringFoldingAnalysis.TOP;
	}

	private void preferenceLeak(Set<Flow> flows, String merging, String key, String vulnIface) throws NoUnitFoundException {
		for (Flow flow : flows) {
			String source = flow.getSource().getSignature();
			String sink = flow.getSink().getSignature();
			

			
			if (sink.matches("<android\\.content\\.SharedPreferences\\$Editor:.*put" + merging + ".*")) {
				String putKey = getPreferenceKey(flow.getSink().getCallingUnit(), flow.getSink().getCallerSootMethod());

				// here we are saying: if I can't retrieve the key (so it is empty)
				// then consider it a flow anyway
				if (key.equals(putKey) || putKey.equals(StringFoldingAnalysis.TOP)
						|| key.equals(StringFoldingAnalysis.TOP)) {
					infoLeak(source, vulnIface, true);
				}
			}
		}
	} //

	private void infoLeak(Flow flow, boolean pref) {
		String sourceSig = flow.getSource().getSignature();
		String sinkSig = flow.getSink().getSignature();

		if (sinkSig.matches("<BabelView[0-9]*:.*babelLeak\\(.*\\)>")) {
			infoLeak(sourceSig, flow.getSink().getInvolvedIface(), pref);
		} else if (sourceSig.matches("<BabelView[0-9]*:.*inputSource\\(.*\\)>")) {
			queryExec(sinkSig, flow, pref);
		}

	}

	private void queryExec(String sinkSig, Flow flow, boolean pref) {
		if (sinkSig.matches("<android\\.database\\.sqlite\\.SQLiteDatabase:.*")) {
			if(pref) { 
				results.addResult(Vulns.PREFERENCES_LEAK_DB);
			} else {
				results.addResult(Vulns.QUERY_EXEC);
			}
		}

	}

	private void infoLeak(String sourceSig, String vulnIface, boolean pref) {
		if (sourceSig.matches("<android\\..*\\.SQLiteDatabase:.*|<android\\.database\\.Cursor:.*")) {
			if(pref) {
				addResult(Vulns.PREF_DATA_BASE_LEAK, vulnIface);
			} else {
				addResult(Vulns.DATA_BASE_LEAK, vulnIface);
			}
		} else if (sourceSig.matches("<android\\.telephony\\.TelephonyManager:.*")) {
			if(pref) {
				addResult(Vulns.PREF_TM_LEAK, vulnIface);
			} else {
				addResult(Vulns.TM_LEAK, vulnIface);
			}
		} else if (sourceSig
				.matches("<android\\.location\\.Location:.*|<android\\.telephony\\.gsm\\.GsmCellLocation:.*")) {
			if(pref) {
				addResult(Vulns.PREF_LOCATION_LEAKS, vulnIface);
			} else {
				addResult(Vulns.LOCATION_LEAKS, vulnIface);
			}
		} else if (sourceSig
				.matches("<android\\.bluetooth\\.BluetoothAdapter:.*|<android\\.net\\.wifi\\.WifiInfo:.*")) {
			if(pref) {
				addResult(Vulns.PREF_INT_CON_LEAKS, vulnIface);
			} else {
				addResult(Vulns.INT_CON_LEAKS, vulnIface);
			}
			
		}

	}
	
	private void addResult(Vulns vuln, String vulnIface) {
		results.addResult(vuln);
		results.addVulnerableIface(vuln, vulnIface);
	}
	
	
	private String createPrefFolderAndFile() {
		File dir = new File("pref_keys");
		if (dir.mkdirs())
			System.out.println("Created directory: " + dir.getAbsolutePath());
		
		return dir.getAbsolutePath()+"/"+ BabelConfig.getConfigs().getApkName() + ".pref";
	}
	
	private void prefKeyToFile(String path) {
		
		
		try (FileWriter file = new FileWriter(path)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			
			file.write(gson.toJson(this.prefMap));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	

}
