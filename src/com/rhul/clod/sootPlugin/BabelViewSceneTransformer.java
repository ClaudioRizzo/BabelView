/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.rhul.clod.BabelConfig;
import com.rhul.clod.sootPlugin.classGenerators.BabelViewGenerator;
import com.rhul.clod.sootPlugin.classGenerators.babelviewgenerator.BabelView;
import com.rhul.clod.sootPlugin.exceptions.NoJavaScriptInterfacesButAPIException;
import com.rhul.clod.sootPlugin.exceptions.NoJavaScriptInterfacesException;
import com.rhul.clod.sootPlugin.instrumenters.BabelInstrumenter;
import com.rhul.clod.sootPlugin.postAnalysis.BabelExploitGen;
import com.rhul.clod.sootPlugin.postAnalysis.PostAnalysisResults;
import com.rhul.clod.sootPlugin.postAnalysis.PostAnalysisResults.Vulns;
import com.rhul.clod.sootPlugin.webViewParser.BabelViewParser;

import soot.SceneTransformer;

public class BabelViewSceneTransformer extends SceneTransformer {

	private BabelInstrumenter babelIntsr = BabelInstrumenter.getInstance();// BabelViewInstrumenter.getBabelViewInstrumenter();
	private BabelViewParser babelParser;
	private boolean nothing = BabelConfig.getConfigs().isNothing();

	private BabelConfig config = BabelConfig.getConfigs();

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public BabelViewSceneTransformer() {

		babelParser = BabelViewParser.getBabelViewParser();

	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		if (!nothing) {
			try {

				babelParser.deepParse();

				if (!config.isJsinterface() || true) {
					generateAndInstrument();
				}

			} catch (NoJavaScriptInterfacesException nJsInterfaceException) {
				nJsInterfaceException.printStackTrace();
				LOGGER.severe("[NO_JS_INTERFACE] " + BabelConfig.getConfigs().getApkName());
				LOGGER.info("[PROCESSED] " + BabelConfig.getConfigs().getApkName());
				System.exit(0);

			} catch (NoJavaScriptInterfacesButAPIException nJsButApiExc) {
				PostAnalysisResults.getResults().addResult(Vulns.API);
				PostAnalysisResults.getResults().toJsonFile(BabelConfig.getConfigs().getReportFolder() + "/"
						+ BabelConfig.getConfigs().getApkName() + ".json");
				LOGGER.severe("[ALL_PUBLIC_METHODS] " + BabelConfig.getConfigs().getApkName());
				LOGGER.info("[PROCESSED] " + BabelConfig.getConfigs().getApkName());

				// We write a basic exploit to chek if the app is at leas injectable
				if (BabelConfig.getConfigs().isJsinterface()) {

					BabelExploitGen eGen = new BabelExploitGen(new ArrayList<>());
					eGen.toFile();
				}

				System.exit(0);

			}

		} else {
			System.out.println("I AM TRASFORMING DOING ABSOLUTELY NOTHING!");

		}
	}

	private void generateAndInstrument() {
		BabelViewGenerator gen = new BabelViewGenerator();

		List<BabelView> babelViewList = gen.generate();

		babelIntsr.initInstrumenter(babelViewList);

		for (BabelView babelView : babelViewList) {
			babelView.setApplicationClass();
			// SootClass main =
			// Scene.v().getSootClass("dummyMainClass");
			// Scene.v().removeClass(main);
			babelIntsr.instrument();

		}
	}

}
