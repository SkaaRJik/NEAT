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

	public double evaluate(Chromosome genoType) {
		int i;
		int j;
		NetworkOutputSet opSet;
		NetworkInput ip;
		ExpectedOutputSet eOpSet = this.evaluationData().expectedOutputSet();
		List<Double> op;
		List<Double> eOp;
		double error = 0;


		// need to create a net based on this chromo
		this.createNetFromChromo(genoType);
		List<List<Double>> outputs = new ArrayList<>();

		// execute net over data set
		for (i = 0; i < eOpSet.size(); i++) {
			ip = this.evaluationData().inputSet().nextInput();
			opSet = this.net().execute(ip);
			op = opSet.nextOutput().getNetOutputs();
			eOp = eOpSet.nextOutput().getNetOutputs();
			if(i == 0) ((ArrayList<List<Double>>) outputs).ensureCapacity(op.size());
			List<Double> outputValues = new ArrayList<>(eOpSet.size());
			outputs.add(outputValues);
			for (j = 0; j < op.size(); j++) {
				outputValues.add(op.get(j));
				error += Math.pow(eOp.get(j) - op.get(j), 2);
			}
		}
		error = (Math.sqrt(error / eOpSet.size()));
		genoType.setTrainError(error);
		genoType.setOutputValues(outputs);

		if(this.testData() != null) {
			double valError = 0;
			eOpSet = this.testData().expectedOutputSet();

			for (i = 0; i < eOpSet.size(); i++) {
				ip = this.testData().inputSet().nextInput();
				opSet = this.net().execute(ip);
				op = opSet.nextOutput().getNetOutputs();
				eOp = eOpSet.nextOutput().getNetOutputs();
				List<Double> outputValues = new ArrayList<>(eOpSet.size());
				outputs.add(outputValues);
				for (j = 0; j < op.size(); j++) {
					outputValues.add(op.get(j));
					valError += Math.pow(eOp.get(j) - op.get(j), 2);
				}
			}
			valError = (Math.sqrt(valError / eOpSet.size()));
			genoType.setValidationError(valError);
		}


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
		//System.out.println(genoType.getTrainError() + " " + );
		return genoType.getValidationError() == null ? genoType.getTrainError() : genoType.getTrainError() + genoType.getValidationError();
		//return genoType.getValidationError() == null ? genoType.getTrainError() :  genoType.getTrainError() + genoType.getValidationError();
		//return genoType.getValidationError() == null ? genoType.getTrainError() :   genoType.getValidationError();
	}
}
