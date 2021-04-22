/*
 * Created on Oct 13, 2004
 *
 */
package org.neat4j.neat.ga.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public interface Chromosome extends Comparable, Serializable {
	Gene[] genes();
	int size();
	void updateChromosome(Gene[] newGenes);
	void updateFitness(double fitness);
	double fitness();

	int getInputs();
	void setInputs(int inputs);
	int getOutputs();
	void setOutputs(int outputs);

	Double getValidationError();
	void setValidationError(Double error);
	double getTrainError();
	void setTrainError(double trainError);


	void setOutputValues(List<List<Double>> opSet);
	List<List<Double>> getOutputValues();

}
