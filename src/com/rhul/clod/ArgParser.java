/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ArgParser {

	public static final String JS = "js";
	private Options options;
	private String[] args;

	public static final String APK = "apk";
	public static final String JARS = "jars";
	public static final String NOTHING = "nothing";
	public static final String URLS = "urls";
	public static final String WRAPPER = "luw";
	public static final String FLOWDROID = "flowdroid";
	public static final String SAVEFLOWS = "saveflows";
	public static final String CHAIN = "chain";
	public static final String INTENTS = "intents";
	public static final String POST_ANALYSIS = "pa";
	public static final String FLOW_TIMEOUT = "ftimeout";
	public static final String TAINT_WRAPPER_FILE = "tw";
	public static final String SOURCE_SINKS_FILE = "s";
	
	public static final String LIBRARY = "lib";

	public ArgParser(String[] args) {
		options = new Options();
		this.args = args;
		addBoleanOptions();
		addArgumentOptions();
	}

	public CommandLine getCli() throws ParseException {
		CommandLineParser parser = new DefaultParser();

		// parse the command line arguments
		CommandLine line;
		try {
			line = parser.parse(options, args);
			return line;
		} catch (MissingOptionException mExc) {
			throw new ParseException("You must provide the required arguments");
		}

	}

	public void printHelp() {
		HelpFormatter hFormatter = new HelpFormatter();
		hFormatter.printHelp("BabelView", options, true);
	}

	private void addArgumentOptions() {
		Option apk = Option.builder("apk").argName("apk").hasArg().desc("path to the apk to analize").required()
				.build();
		Option jars = Option.builder("jars").argName("jars").hasArg().desc("path to android jars folder").required()
				.build();
		Option urls = Option.builder("urls").argName("urls")
				.desc("Generates a list of all the loadUrls and the URLs they load").hasArg()
				.desc("path where the list of loadUrls is saved").build();

		Option fDroid = Option.builder("flowdroid").argName("flowdroid")
				.desc("Run FlowDroid on the given apk with the given android jars").build();
		Option fSave = Option.builder("saveflows").argName("saveflows")
				.desc("save the flow analysis results in a XML file").hasArg()
				.desc("paht to file where to save the results").build();

		Option chain = Option.builder("chain").argName("chain")
				.desc("Execute BabelView, FlowDroid and PostAnalysis in this order").build();

		Option pa = Option.builder("pa").argName("pa").desc("Run the post analysis on the give xml file").hasArg()
				.desc("path to xml file to analyze").build();

		Option flowTimeOut = Option.builder("ftimeout").argName("ftimeout")
				.desc("Set a time out for the flow analysis of N seconds").hasArg()
				.desc("Seconds before the timeout is triggered").build();


		
		Option taintWrapperFile = Option.builder("tw").argName("tw")
				.desc("Specify taint wrapper file location").hasArg()
				.desc("Specify taint wrapper file location").build();
		
		Option sourceAndSinksFile = Option.builder("s").argName("s")
				.desc("Specify sources and sinks file location").hasArg()
				.desc("Specify sources and sinks file location").build();
		
		options.addOption(apk);
		options.addOption(jars);
		options.addOption(urls);
		options.addOption(fDroid);
		options.addOption(fSave);
		options.addOption(chain);
		options.addOption(pa);
		options.addOption(flowTimeOut);
		options.addOption(taintWrapperFile);
		options.addOption(sourceAndSinksFile);

	}

	private void addBoleanOptions() {
		Option help = new Option("help", "print this message");
		Option nothing = new Option("nothing", "if set the application will not be instrumented with BabelView");
		Option wrapper = new Option("luw",
				"perform the analysis to see if there is any wrapper of loadUrl calling it like super.loadUrl");

		Option intent = new Option("intents",
				"If active, the intent analysis will provide precises information of the type of Intent vulnerability we found");
		
		Option lib = new Option("lib", "Generates a list with all the library -- i.e package name -- implementing vulnerable interfaces");
		
		Option jsInterface = new Option("js", "Generates json files for vulnerable interfaces");
				

		// Option urls = new Option("urls", "Use BabelView to generate a list of
		// all the loadUrl and the URL they load");
		options.addOption(help);
		options.addOption(nothing);
		options.addOption(wrapper);
		options.addOption(intent);
		options.addOption(lib);
		options.addOption(jsInterface);
		// options.addOption(urls);
	}

}
