package org.neat4j.neat.nn.core;

import java.io.Serializable;

/**
 * @author msimmerson
 *
 */
public interface ActivationFunction extends Serializable
{
	public double activate(double neuronIp);
	public double derivative(double neuronIp);
	ActivationFunction newInstance();
	String getFunctionName();
}
