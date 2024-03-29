/*
 * Created on Sep 29, 2004
 *
 */
package org.neat4j.neat.core;

import org.neat4j.core.AIConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Holds an object representation of the NEAT config
 * @author MSimmerson
 *
 */
public class NEATConfig implements AIConfig {
	private HashMap<String, String> config;


	//Make copy of config
	public NEATConfig (NEATConfig originalConfig){
		this.config = new HashMap(originalConfig.config);
	}

	
	public NEATConfig(HashMap config) {
		this.config = new HashMap(config);
	}	
		
	// default
	public NEATConfig() {
		this.config = new HashMap();
	}

	public NEATConfig(String filePath) {
		this.config = new HashMap();
	}
	/**
	 * @see org.neat4j.ailibrary.core.AIConfig#configElement(java.lang.String)
	 */
	public String configElement(String elementKey) {
		return ((String)this.config.get(elementKey));
	}

	public void updateConfig(String elementKey, String elementValue) {
		this.config.put(elementKey, elementValue);
	}

	@Override
	public boolean saveConfig(File dest) throws IOException {
		if(dest != null) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
			writer.write("");
			this.config.entrySet().stream().forEach(o -> {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
				try {
					writer.append(entry.getKey() +"="+entry.getValue()+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public HashMap<String,String> getMap() {
		return this.config;
	}

	public List<String> getActivationFunctionsByElementKey(String elementKey){
		StringTokenizer stringTokenizer = new StringTokenizer(this.configElement(elementKey), ";");
		List<String> activationFunctions = new ArrayList<>(stringTokenizer.countTokens());
		while (stringTokenizer.hasMoreTokens()){
			activationFunctions.add(stringTokenizer.nextToken());
		}
		return activationFunctions;

	}
}
