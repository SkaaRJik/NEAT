/*
 * Created on Oct 13, 2004
 *
 */
package org.neat4j.neat.ga.core;

import org.neat4j.neat.data.core.NetworkOutputSet;

import java.io.Serializable;

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

	void setOutputValues(NetworkOutputSet opSet);
	NetworkOutputSet getOutputValues();
}
