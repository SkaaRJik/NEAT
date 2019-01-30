/*
 * Created on Sep 27, 2004
 *
 */
package org.neat4j.core;

import java.io.File;
import java.io.IOException;

/**
 * @author MSimmerson
 *
 */
public interface AIConfig {
	/**
	 * Allows access to given configuration values
	 * @param elementKey
	 * @return The configuration value for the given key
	 */
	public String configElement(String elementKey);
	/**
	 * Allows updating of configuration values
	 * @param elementKey
	 * @param elementValue
	 */
	public void updateConfig(String elementKey, String elementValue);

	boolean saveConfig(File file) throws IOException;
}
