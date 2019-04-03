/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;


import org.neat4j.neat.nn.core.ActivationFunction;

/**
 * @author MSImmerson
 *
 * Gene that describes a NEAT node (neuron)
 */
public class NEATNodeGene implements NEATGene {

	public enum TYPE {
		HIDDEN, OUTPUT, INPUT
	}

	private int innovationNumber;
	private int id;
	private double sigmoidFactor = -1.0;
	private TYPE type;
	private double depth;
	private double bias;
	private ActivationFunction activationFunction;
	private String label;

	public NEATNodeGene(int innovationNumber, int id, double sigmoidF, TYPE type, String label,double bias, ActivationFunction activationFunction) {
		this.innovationNumber = innovationNumber;
		this.id = id;
		this.sigmoidFactor = sigmoidF;
		this.type = type;
		this.bias = bias;
		this.initialiseDepth();
		this.activationFunction = activationFunction;
		this.label = label;
	}
	
	private void initialiseDepth() {
		if (this.type == TYPE.INPUT) {
			this.depth = 0;
		} else if (this.type == TYPE.OUTPUT) {
			this.depth = 1;
		}
	}
	
	/**
	 * @return Returns the depth.
	 */
	public double getDepth() {
		return depth;
	}
	/**
	 * @param depth The depth to set.
	 */
	public void setDepth(double depth) {
		this.depth = depth;
	}

	public void setSigmoidFactor(double bias) {
		this.sigmoidFactor = bias;
	}
	
	public TYPE getType() {
		return type;
	}

	public int getInnovationNumber() {
		return (this.innovationNumber);
	}

	public int id() {
		return (this.id);
	}

	public double sigmoidFactor() {
		return (this.sigmoidFactor);
	}
	
	public Number geneAsNumber() {
		return (new Integer(this.innovationNumber));
	}

	public String geneAsString() {
		return (this.innovationNumber + ":" + this.id + ":" + this.sigmoidFactor);
	}

	public double bias() {
		return bias;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	public ActivationFunction getActivationFunction() {
		return activationFunction;
	}

	public void setActivationFunction(ActivationFunction activationFunction) {
		this.activationFunction = activationFunction;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
