package org.neat4j.neat.core.fitness;

import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.core.NEATNodeGene;
import org.neat4j.neat.data.core.ExpectedOutputSet;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.nn.core.NeuralNet;

import java.util.ArrayList;
import java.util.List;

public class MSENEATFitnessFunction extends NEATFitnessFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public MSENEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		super(net, dataSet);
	}

	public MSENEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet, NetworkDataSet testSet) {
		super(net, dataSet, testSet);
	}


	private double calculateError(NetworkDataSet networkDataSet, List<List<Double>> outputs){


		NetworkOutputSet opSet;
		NetworkInput ip;
		ExpectedOutputSet eOpSet = networkDataSet.expectedOutputSet();
		List<Double> op;
		List<Double> eOp;
		double error = 0;

		// execute net over data set
		for (int i = 0; i < eOpSet.size(); i++) {
			ip = this.evaluationData().inputSet().nextInput();
			opSet = this.net().execute(ip);
			op = opSet.nextOutput().getNetOutputs();
			eOp = eOpSet.nextOutput().getNetOutputs();
			if(i == 0) ((ArrayList<List<Double>>) outputs).ensureCapacity(op.size());
			List<Double> outputValues = new ArrayList<>(eOpSet.size());
			outputs.add(outputValues);
			for (int j = 0; j < op.size(); j++) {
				outputValues.add(op.get(j));
				error += Math.pow(eOp.get(j) - op.get(j), 2);
			}
		}
		return (Math.sqrt(error / eOpSet.size()));
	}


	public double evaluate(Chromosome genoType) {
		int i;
		int j;


		List<List<Double>> outputs = new ArrayList<>();
		// need to create a net based on this chromo
		this.createNetFromChromo(genoType);
		double error = calculateError(this.evaluationData(), outputs);


		if(this.testData() != null) {
			genoType.setValidationError(calculateError(this.testData(), outputs));
		}
		genoType.setOutputValues(outputs);


		NEATNodeGene nodeGene;
		Gene[] genes = genoType.genes();
		int inputs = 0;
		int outputsIndex = 0;
		for (i = 0; i < genes.length; i++) {
			if (genes[i] instanceof NEATNodeGene) {
				nodeGene = (NEATNodeGene)genes[i];
				if(nodeGene.getType() == NEATNodeGene.TYPE.INPUT) nodeGene.setLabel(this.evaluationData().inputSet().getHeaders().get(inputs++));
				else if(nodeGene.getType() == NEATNodeGene.TYPE.OUTPUT) nodeGene.setLabel(this.evaluationData().expectedOutputSet().getHeaders().get(outputsIndex++));;
			}
		}
		return error;
	}
}
