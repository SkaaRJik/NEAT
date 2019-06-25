/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.neat.data.set;

import org.neat4j.neat.data.core.NetworkInput;

import java.util.List;

/**
 * @author MSimmerson
 *
 */
public class InputImpl implements NetworkInput {
	private double[] inputPattern;
	
	public InputImpl(double[] input) {
		this.inputPattern = new double[input.length];
		System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}

	public InputImpl(Double[] input) {
		this.inputPattern = new double[input.length];
		for (int i = 0; i < this.inputPattern.length; i++) {
			this.inputPattern[i] = new Double(input[i]);
		}
		//System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}

	public InputImpl(Double[] input, Integer inputs) {
		this.inputPattern = new double[inputs];
		for (int i = 0; i < inputs; i++) {
			this.inputPattern[i] = new Double(input[i]);
		}
		//System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}


	public InputImpl(List<Double> input) {
		this.inputPattern = new double[input.size()];
		for (int i = 0; i < this.inputPattern.length; i++) {
			this.inputPattern[i] = new Double(input.get(i));
		}
		//System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}

	public InputImpl(List<Double> input, Integer inputs) {
		this.inputPattern = new double[inputs];
		for (int i = 0; i < inputs; i++) {
			this.inputPattern[i] = new Double(input.get(i));
		}
		//System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}


	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInput#pattern()
	 */
	public double[] pattern() {
		return (this.inputPattern);
	}
	
	public String toString() {
		int i;
		StringBuffer sBuff = new StringBuffer();
		for (i = 0; i < this.inputPattern.length; i++) {
			sBuff.append(this.inputPattern[i]);
			sBuff.append(",");
		}
		
		return (sBuff.toString());
	}
}
