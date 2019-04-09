/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.neat.data.csv;

import org.neat4j.neat.data.core.NetworkOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public class CSVExpectedOutput implements NetworkOutput {
	private List<Double> values;
	
	public CSVExpectedOutput(List<Double> eOut) {
		this.values = new ArrayList<>(eOut);

		//System.arraycopy(eOut, 0, this.values, 0, this.values.length);
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutput#values()
	 */
	public List<Double> getNetOutputs() {
		return (this.values);
	}

	public String toString() {
		int i;
		StringBuffer sBuff = new StringBuffer();
		for (i = 0; i < this.values.size(); i++) {
			sBuff.append(this.values.get(i));
			sBuff.append(",");
		}
		
		return (sBuff.toString());
	}
}
