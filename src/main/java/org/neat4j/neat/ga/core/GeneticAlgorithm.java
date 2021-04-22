/*
 * Created on Oct 13, 2004
 *
 */
package org.neat4j.neat.ga.core;

import org.neat4j.core.AIConfig;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.nn.core.functions.ActivationFunctionContainer;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface GeneticAlgorithm extends Serializable {
	public GADescriptor getDescriptor();
	public void createPopulation(InnovationDatabase innovationDatabase);
	public void runEpoch();
	public Chromosome discoverdBestMember();
	public void pluginMutator(Mutator mut);
	public void pluginFitnessFunction(FitnessFunction func);
	public void pluginParentSelector(ParentSelector selector);
	public void pluginCrossOver(CrossOver xOver);
	public void savePopulationState(String file);
	public Population population();

    ActivationFunctionContainer pluginAllowedActivationFunctions(AIConfig config);

}
