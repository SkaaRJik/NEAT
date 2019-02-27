/*
 * Created on Sep 29, 2004
 *
 */
package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 * @fixed Filippov
 */
public class SigmoidFunction extends ActivationFunctionImpl {
	
	public SigmoidFunction() { this.factor = 1; }
	
	public SigmoidFunction(double factor) {
		this.factor = factor;
	}
	
	/**
	 * Returns +/- 1
	 * @see org.neat4j.ailibrary.nn.core.ActivationFunction#activate(double)
	 */
	public double activate(double neuronIp) {
		return (1.0 / ( 1.0 + Math.exp(-this.factor * neuronIp)));
	}

	public double derivative(double neuronIp) {
		if(this.factor == 1)
			return (activate(neuronIp) * (1 - activate(neuronIp)));
		else
			return (this.factor * Math.exp(-this.factor * neuronIp))/Math.pow(1.0 + Math.exp(-this.factor * neuronIp),2);
	}

	@Override
	public ActivationFunction newInstance() {
		return new SigmoidFunction(this.factor);
	}

	public void setFactor(double mod) {
		this.factor = mod;
	}

	public double getFactor(){
		return this.factor;
	}

	public static String getStaticFunctionName(){
		return "sigmoid(x)";
	}

	public  String getFunctionName(){
		return "sigmoid(x)";
	}
}
