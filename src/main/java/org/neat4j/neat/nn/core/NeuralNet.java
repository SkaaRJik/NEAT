package org.neat4j.neat.nn.core;

import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;

import java.io.Serializable;
import java.util.List;


/**
 * @author msimmerson
 *
 */
public interface NeuralNet extends Serializable
{
	public void createNetStructure(NeuralNetDescriptor descriptor);
	public NeuralNetDescriptor netDescriptor();
	List<NEATNeuron> hiddenLayers();
	List<NEATNeuron> outputLayer();
	List<NEATNeuron> inputLayer();
	public void seedNet(double[] weights);
	public int requiredWeightCount();
	public int netID();
	public NetworkOutputSet execute(NetworkInput netInput);
	public Neuron neuronAt(int x, int y);
}
