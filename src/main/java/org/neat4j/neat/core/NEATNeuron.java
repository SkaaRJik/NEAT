/*
 * Created on 23-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.Neuron;
import org.neat4j.neat.nn.core.Synapse;

import java.util.ArrayList;

/**
 * @author MSimmerson
 *
 * Specific NEAT neuron
 */
public class NEATNeuron implements Neuron {
	private double lastActivation;
	private double bias;
	private double[] weights;
	private ActivationFunction activationFunction;
	private int id;
	private NEATNodeGene.TYPE type;
	private int depth;
	String label;
	private ArrayList<NEATNeuron> sourceNeurons;
	private ArrayList<Synapse> incomingSynapses;
	private ArrayList<Synapse> outSynapses;

	public NEATNeuron(ActivationFunction function, int id, NEATNodeGene.TYPE type, String label) {
		this.activationFunction = function;
		this.id = id;
		this.type = type;
		this.sourceNeurons = new ArrayList<>();
		this.incomingSynapses = new ArrayList<>();
		this.outSynapses = new ArrayList<>();
		this.depth = -1;
		this.label = label;
	}
	
	public void addSourceNeuron(NEATNeuron neuron) {
		this.sourceNeurons.add(neuron);
	}
	
	public void addIncomingSynapse(Synapse synapse) {
		this.incomingSynapses.add(synapse);
	}
	
	public ArrayList<Synapse> incomingSynapses() {
		return (this.incomingSynapses);
	}
	
	public ArrayList<NEATNeuron> sourceNeurons() {
		return (this.sourceNeurons);
	}
	
	public double lastActivation() {
		return (this.lastActivation);
	}
	
	/**
	 * If it is an input neuron, returns the input, else will run through the specified activation function.
	 * 
	 */
	public double activate(double[] nInputs) {
		double neuronIp = 0;
		int i = 0;
		double weight;
		double input;
		Synapse synapse;
		Object[] incoming = this.incomingSynapses.toArray();
		// acting as a bias neuron
		this.lastActivation = -1;

		if (this.type != NEATNodeGene.TYPE.INPUT) {
			if (nInputs.length > 0) {
				for (i = 0; i < nInputs.length; i++) {
					input = nInputs[i];
					synapse = (Synapse)incoming[i];
					if (synapse.isEnabled()) {
						weight = synapse.getWeight();
						neuronIp += (input * weight);
					}
				}
				neuronIp += (-1 * this.bias);
				this.lastActivation = this.activationFunction.activate(neuronIp);
			}
		} else {
			//neuronIp = nInputs[0];
			this.lastActivation = nInputs[0];
		}
		
		return (this.lastActivation);
	}

	public ActivationFunction function() {
		return (this.activationFunction);
	}

	public void modifyWeights(double[] weightMods, double[] momentum, boolean mode) {
		System.arraycopy(weightMods, 0, this.weights, 0, this.weights.length);

	}

	public void modifyBias(double biasMod, double momentum, boolean mode) {
		this.bias = biasMod;
	}

	public double[] weights() {
		return (this.weights);
	}

	public double bias() {
		return (this.bias);
	}

	public double[] lastWeightDeltas() {
		return null;
	}

	public double lastBiasDelta() {
		return 0;
	}

	@Override
	public int getID() {
		return this.id;
	}

	public int id() {
		return (this.id);
	}
	
	public NEATNodeGene.TYPE neuronType() {
		return (this.type);
	}
	
	public int neuronDepth() {
		return (this.depth);
	}

	public void setNeuronDepth(int depth) {
		this.depth = depth;
	}

	public void setActivationFunction(ActivationFunction activationFunction) {
		this.activationFunction = activationFunction;
	}

	public ActivationFunction getActivationFunction() {
		return activationFunction;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "NEATNeuron{" +
				"\n  id=" + id +
				"\n  type=" + type +
				"\n  label='" + label  +
				"\n  activationFunction=" + activationFunction.getFunctionName() +
				"\n  bias=" + bias +
				"\n}";
	}


	public ArrayList<Synapse> getOutSynapses() {
		return outSynapses;
	}

	public void addOutSynapse(Synapse synapse){
		this.outSynapses.add(synapse);
	}
}
