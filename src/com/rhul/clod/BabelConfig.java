/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod;

public class BabelConfig {

	private static BabelConfig instance;

	private boolean nothing;
	private boolean intent;
	private boolean wrappers;
	private boolean jsinterface;

	private String apkPath;
	private String apkName;
	private String instrPath;

	private String sourceSinksFilePath;
	private String taintWrapperFilePath;


	private String androidJars;

	private String reportFolder = "babelReport";
	private String interfaceFolder = "interfaces";
	private String libraryFolder = "ifaceLibs";

	private boolean library;

	private BabelConfig() {
	}

	public static BabelConfig getConfigs() {
		if (instance == null)
			instance = new BabelConfig();
		return instance;
	}

	public String getApkName() {
		return this.apkName.split("\\.")[0];
	}

	public boolean getIntent() {
		return intent;
	}

	public boolean isNothing() {
		return nothing;
	}

	public void setNothing(boolean nothing) {
		this.nothing = nothing;
	}

	public boolean isIntent() {
		return intent;
	}

	public void setIntent(boolean intent) {
		this.intent = intent;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
		if (apkName == null) {
			String[] pathSplits = apkPath.split("/");
			String apkName = pathSplits[pathSplits.length - 1];
			this.apkName = apkName;
		}
	}

	public String getInstrPath() {
		return instrPath;
	}

	public void setInstrPath() {
		if (apkName == null) {
			String[] pathSplits = apkPath.split("/");
			String apkName = pathSplits[pathSplits.length - 1];
			this.apkName = apkName;
		}

		this.instrPath = "sootOutput/" + apkName;
	}

	public String getAndroidJars() {
		return androidJars;
	}

	public void setAndroidJars(String androidJars) {
		this.androidJars = androidJars;
	}

	public boolean isWrappers() {
		return wrappers;
	}

	public void setWrappers(boolean wrappers) {
		this.wrappers = wrappers;
	}

	public String getReportFolder() {
		return this.reportFolder;
	}

	public void setReportFolder(String reportFolder) {
		this.reportFolder = reportFolder;

	}

	public boolean isJsinterface() {
		return jsinterface;
	}

	public void setJsinterface(boolean jsinterface) {
		this.jsinterface = jsinterface;
	}


	public String getInterfaceFolder() {

		return interfaceFolder;
	}

	public void setInterfaceFolder(String interfaceFolder) {
		this.interfaceFolder = interfaceFolder;
	}

	public void setLibrary(boolean library) {
		this.library = library;

	}

	public boolean getLibrary() {
		return this.library;
	}

	public String getLibraryFolder() {
		return libraryFolder;
	}

	public void setLibraryFolder(String libraryFolder) {
		this.libraryFolder = libraryFolder;
	}

	/**
	 * @return the sourceSinksFilePath
	 */
	public String getSourceSinksFilePath() {
		return sourceSinksFilePath;
	}

	/**
	 * @param sourceSinksFilePath
	 *            the sourceSinksFilePath to set
	 */
	public void setSourceSinksFilePath(String sourceSinksFilePath) {
		this.sourceSinksFilePath = sourceSinksFilePath;
	}

	/**
	 * @return the taintWrapperFilePath
	 */
	public String getTaintWrapperFilePath() {
		return taintWrapperFilePath;
	}

	/**
	 * @param taintWrapperFilePath
	 *            the taintWrapperFilePath to set
	 */
	public void setTaintWrapperFilePath(String taintWrapperFilePath) {
		this.taintWrapperFilePath = taintWrapperFilePath;
	}
}
