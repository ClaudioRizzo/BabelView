/* Copyright (c) Royal Holloway, University of London | Contact Claudio Rizzo (claudio.rizzo.90@gmail.com), Johannes Kinder (johannes.kinder@rhul.ac.uk) or Lorenzo Cavallaro (Lorenzo.Cavallaro@rhul.ac.uk) for details or support | LICENSE.md for license details */
package com.rhul.clod.sootPlugin;

import java.util.Map;

import com.rhul.clod.sootPlugin.loadUrlAnalysis.VLoadUrlAnalysis;

import soot.Body;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

public class LoadUrlSceneTransformer extends SceneTransformer {


	public LoadUrlSceneTransformer() {
		
	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		VLoadUrlAnalysis urlAnalysis = new VLoadUrlAnalysis();
		
		
		for (SootClass sClass : Scene.v().getClasses()) {
			for (SootMethod sMethod : sClass.getMethods()) {

				if (sMethod.isConcrete()) {
					Body body = sMethod.retrieveActiveBody();
					if(sClass.getName().equals("clearpass.bank.mabclient.OOjiBOMain$27")) {
						//System.out.println(body);
					}
					

					for (Unit u : body.getUnits()) {
						String url = urlAnalysis.getLoadUrlStrings(sMethod, body, u);
						if(!url.equals(""))
							System.out.println("Result: "+url);
						//fWebVIew.flowAnalysis(sMethod, u, "A");
							
						

					}
				}
			}
		}

		
	}

}
