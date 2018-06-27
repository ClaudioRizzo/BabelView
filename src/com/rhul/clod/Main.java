/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.rhul.clod.logging.BabelLogger;
import com.rhul.clod.sootPlugin.BabelViewSceneTransformer;
import com.rhul.clod.sootPlugin.LoadUrlSceneTransformer;
import com.rhul.clod.sootPlugin.postAnalysis.BabelPostAnalysis;
import com.rhul.clod.sootPlugin.postAnalysis.XMLResultParser;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.cmd.MainClass;
import soot.options.Options;

public class Main {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static BabelConfig config = BabelConfig.getConfigs();

	public static void main(String[] args) {
		ArgParser aParser = new ArgParser(args);
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n");

		int flowTimeout = 0;

		generateResultDirectory();

		try {
			BabelLogger.setup();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}

		try {

			final CommandLine cli = aParser.getCli();

			if (cli.hasOption("help")) {
				aParser.printHelp();
				System.exit(0);
			}

			if (cli.hasOption(ArgParser.NOTHING)) {
				config.setNothing(true);
			}

			if (cli.hasOption(ArgParser.INTENTS)) {
				config.setIntent(true);
			}

			if (cli.hasOption(ArgParser.WRAPPER)) {
				config.setWrappers(true);
			}

			if (cli.hasOption(ArgParser.JS)) {
				config.setJsinterface(true);
			}

			if (cli.hasOption(ArgParser.FLOW_TIMEOUT)) {
				flowTimeout = Integer.parseInt(cli.getOptionValue(ArgParser.FLOW_TIMEOUT));
			}

			if (cli.hasOption(ArgParser.LIBRARY)) {
				config.setLibrary(true);
			}

			if (cli.hasOption(ArgParser.TAINT_WRAPPER_FILE)) {

				config.setTaintWrapperFilePath(cli.getOptionValue(ArgParser.TAINT_WRAPPER_FILE));
			}

			if (cli.hasOption(ArgParser.SOURCE_SINKS_FILE)) {
				config.setSourceSinksFilePath(cli.getOptionValue(ArgParser.SOURCE_SINKS_FILE));
			}

			if (cli.hasOption(ArgParser.APK) && cli.hasOption(ArgParser.JARS)) {

				final String apkPath = cli.getOptionValue(ArgParser.APK);
				final String androidJars = cli.getOptionValue(ArgParser.JARS);

				config.setApkPath(cli.getOptionValue(ArgParser.APK));
				config.setAndroidJars(cli.getOptionValue(ArgParser.JARS));
				config.setInstrPath();

				if (cli.hasOption(ArgParser.FLOWDROID)) {

					String savePath = "";
					if (cli.hasOption(ArgParser.SAVEFLOWS)) {
						savePath = cli.getOptionValue(ArgParser.SAVEFLOWS);
					}

					runFlowDroid(config.getApkPath(), savePath);

				} else if (cli.hasOption(ArgParser.CHAIN)) {

					if (flowTimeout > 0) {
						ExecutorService executor = Executors.newCachedThreadPool();
						Callable<Object> task = new Callable<Object>() {
							public String call() throws IOException, InterruptedException, ExecutionException,
									TimeoutException, Exception {
								runChain(apkPath, androidJars, cli);
								return "Chain Done";
							}
						};
						Future<Object> future = executor.submit(task);
						System.out.println(future.get(flowTimeout, TimeUnit.SECONDS));
						executor.shutdown();
					} else {

						runChain(apkPath, androidJars, cli);
					}

				} else if (cli.hasOption(ArgParser.POST_ANALYSIS)) {

					runPostAnalysis(cli);

				} else {
					// We run BV as a stand alone
					runBabelView(cli);
				}

			} else {
				aParser.printHelp();
				System.exit(-1);
			}

			// soot.Main.main(args);
		} catch (TimeoutException e) {
			LOGGER.severe("[TIME_OUT] " + BabelConfig.getConfigs().getApkName());

		} catch (ParseException pEx) {
			aParser.printHelp();
			System.exit(0);
		} catch (InterruptedException iException) {
			LOGGER.severe("[INTERRUPTION] " + config.getApkName());
			Thread.currentThread().interrupt();

		} catch (Exception general) {
			LOGGER.severe("[GENERAL_ERROR] " + config.getApkName());
			general.printStackTrace();

		} finally {
			LOGGER.info("[PROCESSED] " + config.getApkName());
			System.exit(0);
		}

	}

	private static void runPostAnalysis(CommandLine cli) {
		File f = new File(config.getInstrPath());

		if (!f.exists() || config.isJsinterface()) {
			runBabelView(cli);
			G.reset();
			setSootOptions(config.getInstrPath(), config.getAndroidJars());
		} else {

			setSootOptions(config.getInstrPath(), config.getAndroidJars());
		}

		if (config.getIntent()) {
			// FlowDroidFacade flowDroidFacade = new FlowDroidFacade();
			// flowDroidFacade.createCallGraphForAndroid(config.getAndroidJars(),
			// config.getInstrPath());
			SetupApplication app = new SetupApplication(config.getAndroidJars(), config.getInstrPath());
			app.constructCallgraph();
		}

		Options.v().set_process_dir(Collections.singletonList(config.getInstrPath()));
		String xmlFile = cli.getOptionValue(ArgParser.POST_ANALYSIS);
		XMLResultParser parser = new XMLResultParser(xmlFile);
		BabelPostAnalysis.getInstance().doAnalysis(parser.getFlowsSet());

	}

	private static void runChain(String apkPath, String androidJars, CommandLine cli)
			throws IOException, InterruptedException, ExecutionException, TimeoutException, Exception {
		LOGGER.info("[BABELVIEW] " + config.getApkName());
		setSootOptions(apkPath, androidJars);

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.MyInstrumentation", new BabelViewSceneTransformer()));
		// PackManager.v().runPacks();

		PackManager.v().getPack("wjtp").apply();
		PackManager.v().writeOutput();

		String[] pathSplits = apkPath.split("/");
		String apkName = pathSplits[pathSplits.length - 1];

		String savePath = "";
		if (cli.hasOption(ArgParser.SAVEFLOWS)) {
			savePath = cli.getOptionValue(ArgParser.SAVEFLOWS);
		}

		runFlowDroid(config.getInstrPath(), savePath);

		LOGGER.info("[FLOWDROID] " + config.getApkName());

		System.gc();

		setSootOptions(config.getInstrPath(), config.getAndroidJars());

		Options.v().set_process_dir(Collections.singletonList("sootOutput/" + apkName));
		XMLResultParser parser = new XMLResultParser(cli.getOptionValue(ArgParser.SAVEFLOWS));
		BabelPostAnalysis.getInstance().doAnalysis(parser.getFlowsSet());

		LOGGER.info("[POST_ANALYSIS] " + config.getApkName());

	}

	private static void generateResultDirectory() {
		File file = new File("babelReport");
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("babelReport created");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

	}

	private static void runFlowDroid(String apkPath, String resultPath)
			throws IOException, InterruptedException, ExecutionException, TimeoutException, Exception {

		final String[] flowDroidArgs = getFlowDroidParams(apkPath, config.getAndroidJars(), resultPath);

		MainClass.main(flowDroidArgs);

	}

	private static String[] getFlowDroidParams(String apkPath, String jarPath, String saveFlowsPath) {

		String taintWrapperFilePath = "";
		if (config.getTaintWrapperFilePath() == null || "".equals(config.getTaintWrapperFilePath())) {
			Path currentRelativePath = Paths.get("EasyTaintWrapperSource.txt");
			taintWrapperFilePath = currentRelativePath.toAbsolutePath().toString();
		} else {
			taintWrapperFilePath = config.getTaintWrapperFilePath();
		}

		String sourcesSinksFilePath = "";
		if (config.getSourceSinksFilePath() == null || "".equals(config.getSourceSinksFilePath())) {
			Path currentRelativePath = Paths.get("SourcesAndSinks.txt");
			sourcesSinksFilePath = currentRelativePath.toAbsolutePath().toString();
		} else {
			sourcesSinksFilePath = config.getSourceSinksFilePath();
		}

		if (saveFlowsPath == null || "".equals(saveFlowsPath))
			return new String[] { "--apkfile", apkPath, "--platformsdir", jarPath, "--nostatic", "--layoutmode", "none",
					"--taintwrapperfile", taintWrapperFilePath, "--sourcessinksfile", sourcesSinksFilePath,
					"--logsourcesandsinks", "--callbacksourcemode", "sourcelist" };
		else
			return new String[] { "--apkfile", apkPath, "--platformsdir", jarPath, "--nostatic", "--layoutmode", "none",
					"--outputfile", saveFlowsPath, "--taintwrapper", "easy", "--taintwrapperfile", taintWrapperFilePath,
					"--sourcessinksfile", sourcesSinksFilePath, "--logsourcesandsinks", "--callbacksourcemode",
					"sourcelist" };
	}

	private static void runBabelView(CommandLine cli) {

		setSootOptions(config.getApkPath(), config.getAndroidJars());
		SceneTransformer transformer;

		if (cli.hasOption(ArgParser.URLS)) {
			transformer = new LoadUrlSceneTransformer();
		} else {

			transformer = new BabelViewSceneTransformer();
		}

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.MyInstrumentation", transformer));
		PackManager.v().getPack("wjtp").apply();
		// PackManager.v().runPacks();
		PackManager.v().writeOutput();

	}

	private static void setSootOptions(String apkPath, String androidPlatforms) {

		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_whole_program(true);

		Options.v().set_force_overwrite(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_android_jars(androidPlatforms);
		Options.v().set_process_dir(Collections.singletonList(apkPath));

		Options.v().set_validate(true);
		Scene.v().addBasicClass("java.lang.Math", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);
		Scene.v().loadNecessaryClasses();

	}

}