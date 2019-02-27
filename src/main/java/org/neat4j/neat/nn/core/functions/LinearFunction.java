package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 * @fixed Filippov
 */
public class LinearFunction extends ActivationFunctionImpl {

	public LinearFunction(double factor) { this.factor = factor; }
	public LinearFunction() {this.factor = 1; }
	public double activate(double neuronIp) {
		return (neuronIp*this.factor);
	}
	public double derivative(double neuronIp) {
		return (this.factor);
	}

	@Override
	public ActivationFunction newInstance() {
		return new LinearFunction(this.factor);
	}

	public static String getStaticFunctionName(){
		return "y=x";
	}
	public String getFunctionName(){
		return "y=x";
	}
}
