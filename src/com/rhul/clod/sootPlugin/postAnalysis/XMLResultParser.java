/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin.postAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLResultParser {

	private Document xmlDoc;
	private Set<Flow> flows = new HashSet<>();

	public XMLResultParser(String xmlFilePath) {
		File xmlFile = new File(xmlFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			this.xmlDoc = dBuilder.parse(xmlFile);
			// normalize the tree
			this.xmlDoc.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Generates and returns a set of flows.
	 * 
	 * @return A set of Flows from {@link XMLResultParser#xmlDoc}
	 */
	public Set<Flow> getFlowsSet() {
		if (!flows.isEmpty()) {
			return flows;
		}

		NodeList results = xmlDoc.getElementsByTagName(XmlConstant.Tags.result);
		for (int i = 0; i < results.getLength(); i++) {
			Node result = results.item(i);

			NodeList sourcesSinks = result.getChildNodes();
			for (int j = 0; j < sourcesSinks.getLength(); j++) {

				if (sourcesSinks.item(j).getNodeName().equals(XmlConstant.Tags.sink)) {
					Node sink = sourcesSinks.item(j);
					Node sources = sink.getNextSibling();

					NodeList sourcesList = sources.getChildNodes();
					for (int k = 0; k < sourcesList.getLength(); k++) {
						Node source = sourcesList.item(k);
						Flow flow = generateFlow(source, sink);
						if(flow.getSink().getSignature() == null || 
								flow.getSource().getSignature() == null) continue;
						flows.add(flow);
					}
				}

			}
		}

		return flows;
	}

	private Flow generateFlow(Node source, Node sink) {

		BabelSource bSource = generateBabelSource(source);
		BabelSink bSink = generateBabelSink(sink);

		return new Flow(bSource, bSink);
	}

	private BabelSink generateBabelSink(Node sink) {

		String stmtString = sink.getAttributes().getNamedItem(XmlConstant.Attributes.statement).getNodeValue();
		String signature = getMethodSignature(stmtString);
		String callerSignature = sink.getAttributes().getNamedItem(XmlConstant.Attributes.method).getNodeValue();

		return new BabelSink(signature, callerSignature, stmtString);

	}

	private String getMethodSignature(String statment) {
		Pattern p = Pattern.compile("<.*?>");
		Matcher m = p.matcher(statment);

		if (m.find()) {
			return m.group();
		} else {
			return null;
		}
	}

	private BabelSource generateBabelSource(Node source) {
		String stmtString = source.getAttributes().getNamedItem(XmlConstant.Attributes.statement).getNodeValue();
		String signature = getMethodSignature(stmtString);
		String calleeSignature = source.getAttributes().getNamedItem(XmlConstant.Attributes.method).getNodeValue();
		
		return new BabelSource(signature, calleeSignature, stmtString);
	}

}
