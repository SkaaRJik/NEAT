/*
 * Created on 22-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.apache.log4j.Category;
import org.neat4j.neat.core.control.NEAT;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.nn.core.*;
import org.neat4j.neat.nn.core.functions.LinearFunction;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;

import java.util.*;

/**
 * @author MSimmerson
 *
 * The NEAT Neural Network
 */
public class NEATNeuralNet implements NeuralNet {
	private static final Category cat = Category.getInstance(NEATNeuralNet.class); 
	private NEATNetDescriptor descriptor;
	private Synapse[] connections;
	private Map<Integer, NEATNeuron> neurons;
	private int level = 0;
	
	public List<NEATNeuron>  getNeurons() {
		return new ArrayList<NEATNeuron>(this.neurons.values());
	}



	/**
	 * Exercises the network for the given input data set
	 */
	public NetworkOutputSet execute(NetworkInput netInput) {
		NEATNetOutputSet opSet;
		List<Double> outputs;
		this.level = 0;
		int i;
		// trawl through the graph bacwards from each output node
		Object[] outputNeurons = this.outputNeurons().toArray();
		if (outputNeurons.length == 0) {
			cat.debug("No output getNeurons");
		}
		outputs = new ArrayList<>(outputNeurons.length);
		
		for (i = 0; i < outputNeurons.length; i++) {
			outputs.add(this.neuronOutput((NEATNeuron)outputNeurons[i], netInput));
		}
		
		opSet = new NEATNetOutputSet();
		opSet.addNetworkOutput(new NEATNetOutput(outputs));
		return (opSet);
	}
	
	public List<NEATNeuron> outputNeurons() {
		List<NEATNeuron> outputNeurons = new ArrayList<>();
		int i;

		for (NEATNeuron neatNeuron : this.neurons.values()) {
			if (neatNeuron.neuronType() == NEATNodeGene.TYPE.OUTPUT) {
				outputNeurons.add(neatNeuron);
			}
		}
		return (outputNeurons);
	}
	
	private double neuronOutput(NEATNeuron neuron, NetworkInput netInput) {
		double output = 0;
		double[] inputPattern;
		// find its inputs
		Object[] sourceNodes = neuron.sourceNeurons().toArray();
		Object[] incomingSynapses = neuron.incomingSynapses().toArray();
		int i;
		
		this.level++;
		if (neuron.neuronType() == NEATNodeGene.TYPE.INPUT) {
			inputPattern = new double[1];
			// match the input column to the input node, id's start from 1
			inputPattern[0] = netInput.pattern()[neuron.id() - 1];
		} else {
			inputPattern = new double[sourceNodes.length];
			for (i = 0; i < sourceNodes.length; i++) {
				if (neuron.id() == ((NEATNeuron)sourceNodes[i]).id()) {				
					// Self Recurrent
					//cat.debug("Self Recurrent:" + neuron.id() + ":" + ((NEATNeuron)sourceNodes.get(i)).id());
					inputPattern[i] = neuron.lastActivation();
				} else if (neuron.neuronDepth() > ((NEATNeuron)sourceNodes[i]).neuronDepth()) {
					// Recurrent
					//cat.debug("Recurrent:" + neuron.id() + ":" + ((NEATNeuron)sourceNodes.get(i)).id());
					inputPattern[i] = ((NEATNeuron)sourceNodes[i]).lastActivation();
				} else {
					inputPattern[i] = this.neuronOutput((NEATNeuron)sourceNodes[i], netInput);
//					if (((Synapse)incomingSynapses[i]).isEnabled()) {
//						inputPattern[i] = this.neuronOutput((NEATNeuron)sourceNodes[i], netInput);
//					} else {
//						cat.info("Stop recursion");
//					}
				}
			}
		}
		output = neuron.activate(inputPattern);
		this.level--;
		return (output);
	}

	/**
	 * Generates a neural network structure based on the network getDescriptor
	 *
	 */
	public void updateNetStructure() {
		// use getDescriptor's chromo to create net
		Chromosome netStructure = this.descriptor.neatStructure();
		ArrayList nodes = new ArrayList();
		ArrayList links = new ArrayList();
		Gene[] genes = netStructure.genes();
		int i;
		
		for (i = 0; i < netStructure.size(); i++) {
			if (genes[i] instanceof NEATNodeGene) {					
				nodes.add(genes[i]);
			} else if (genes[i] instanceof NEATLinkGene) {	
				if (((NEATLinkGene)genes[i]).isEnabled()) {
					// only add enabled links to the net structure
					links.add(genes[i]);
				}
			}
		}
		this.neurons = this.createNeurons(nodes);
		this.connections = this.createLinks(links, this.neurons);
		this.assignNeuronDepth(this.outputNeurons(), 0);
	}
	
	private void assignNeuronDepth(List<NEATNeuron> neurons, int depth) {
		int i;
		NEATNeuron neuron;
		
		for (i = 0; i < neurons.size(); i++) {
			neuron = (NEATNeuron)neurons.get(i);
			if (neuron.neuronType() == NEATNodeGene.TYPE.OUTPUT) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);
				}
			} else if (neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);				
				}
			} else if (neuron.neuronType() == NEATNodeGene.TYPE.INPUT) {
				neuron.setNeuronDepth(Integer.MAX_VALUE);
			}
		}
	}
	
	private Map<Integer, NEATNeuron> createNeurons(List<NEATNodeGene> nodes) {


		Map<Integer, NEATNeuron> tempNeurons = new HashMap<>(nodes.size());

		NEATNeuron neuron;
		for (NEATNodeGene gene : nodes) {
			neuron = new NEATNeuron(this.createActivationFunction(gene), gene.id(), gene.getType(), gene.getLabel());
			neuron.setActivationFunction(gene.getActivationFunction());
			neuron.modifyBias(gene.bias(), 0, true);
			tempNeurons.put(neuron.id(), neuron);
		}
		return tempNeurons;
	}
	
	private ActivationFunction createActivationFunction(NEATNodeGene gene) {
		ActivationFunction function = null;
		// inputs are passed through
		if (gene.getType() == NEATNodeGene.TYPE.INPUT) {
			function = new LinearFunction();
		} else if (gene.getType() == NEATNodeGene.TYPE.OUTPUT){
			function = new SigmoidFunction(gene.sigmoidFactor());
		} else {
			function = new TanhFunction();
		}
		
		return (function);
	}
	
	private Synapse[] createLinks(ArrayList links, Map<Integer, NEATNeuron> neurons) {
		NEATLinkGene gene;
		Synapse[] synapses = new Synapse[links.size()];
		int i;
		NEATNeuron from;
		NEATNeuron to;
		
		for (i = 0; i < links.size(); i++) {
			gene = (NEATLinkGene)links.get(i);
			from = neurons.get(gene.getFromId());
			to = neurons.get(gene.getToId());
			to.addSourceNeuron(from);
			synapses[i] = new Synapse(from, to, gene.getWeight());
			synapses[i].setEnabled(gene.isEnabled());
			to.addIncomingSynapse(synapses[i]);
			from.addOutSynapse(synapses[i]);
		}
		
		return (synapses);
	}

	/**
	 * Updates the internal network structure
	 */
	public void createNetStructure(NeuralNetDescriptor descriptor) {
		this.descriptor = (NEATNetDescriptor)descriptor;
	}

	public NeuralNetDescriptor netDescriptor() {
		return (this.descriptor);
	}

	public List<NEATNeuron> hiddenLayers() {
		List<NEATNeuron> outputLayer = new ArrayList<>() ;
		for(NEATNeuron neatNeuron : this.neurons.values()){
			if(neatNeuron.neuronType() == NEATNodeGene.TYPE.HIDDEN){
				outputLayer.add(neatNeuron);
			}
		}
		return outputLayer;
	}

	public List<NEATNeuron> outputLayer() {
		List<NEATNeuron> outputLayer = new ArrayList<>() ;
		for(NEATNeuron neatNeuron : this.neurons.values()){
			if(neatNeuron.neuronType() == NEATNodeGene.TYPE.OUTPUT){
				outputLayer.add(neatNeuron);
			}
		}
		return outputLayer;
	}

	@Override
	public List<NEATNeuron> inputLayer() {
		List<NEATNeuron> inputNeurons = new ArrayList<>(this.neurons.size());
		for(NEATNeuron neuron : this.neurons.values()){
			if(neuron.neuronType() == NEATNodeGene.TYPE.INPUT){
				inputNeurons.add(neuron);
			}
		}
		return inputNeurons;
	}

	public void seedNet(double[] weights) {
	}

	public int requiredWeightCount() {
		return 0;
	}

	public int netID() {
		return 0;
	}

	public Neuron neuronAt(int x, int y) {
		return null;
	}

	public Synapse[] getConnections() {
		return connections;
	}
}
