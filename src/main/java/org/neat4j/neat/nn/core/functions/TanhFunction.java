/*
 * Created on Sep 30, 2004
 *
 */
package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 * @fixed Filippov
 */
public class TanhFunction extends ActivationFunctionImpl {


	public TanhFunction(double factor) {
		this.factor = factor;
	}

	public TanhFunction() {
		this.factor = 1;
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.ActivationFunction#activate(double)
	 */
	public double activate(double neuronIp) {
		double op;
		/*if (neuronIp < -20) {
			op = -1;
		} else if (neuronIp > 20) {
			op = 1;
		} else {
			op = (1 - Math.exp(-2 * neuronIp)) / (1 + Math.exp(-2 * neuronIp));
		}*/
		op = th(neuronIp/this.factor);
		return (op);
	}

	public double th(double neuronIp){
		return (Math.exp(neuronIp)-Math.exp(-neuronIp))/(Math.exp(neuronIp)+Math.exp(-neuronIp));
	}

	public double derivative(double neuronIp) {
		double deriv = 0;
		/*deriv = (1 - Math.pow(neuronIp, 2));*/
		deriv = 1 - Math.pow(th(neuronIp/this.factor), 2);
		return (deriv);
	}

	@Override
	public ActivationFunction newInstance() {
		return new TanhFunction(this.factor);
	}

	public static String getFunctionName(){
		return "tanh(x)";
	}
}
