/*
 * Created on Oct 6, 2004
 *
 */
package org.neat4j.neat.core;

import org.neat4j.neat.data.core.NetworkOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MSimmerson
 *
 * Contains the output getNetOutputs of a NEAT neural net
 */
public class NEATNetOutput implements NetworkOutput {
	private List<Double> netOutputs;
	
	public NEATNetOutput(List<Double> outputs) {
		this.netOutputs = new ArrayList<>(outputs);
		//System.arraycopy(outputs, 0, this.netOutputs, 0, this.netOutputs.length);
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutput#values()
	 */
	public List<Double> getNetOutputs() {
		return (this.netOutputs);
	}

}
