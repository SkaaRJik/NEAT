/*
 * Created on Oct 13, 2004
 *
 */
package org.neat4j.neat.ga.core;

import org.neat4j.neat.data.core.NetworkOutputSet;

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

	void setOutputValues(List<List<Double>> opSet);
	List<List<Double>> getOutputValues();
}
