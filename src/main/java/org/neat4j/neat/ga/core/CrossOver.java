package org.neat4j.neat.ga.core;

import java.util.Random;

/**
 * @author MSimmerson
 *
 */
public interface CrossOver extends Operator {
	public void setProbability(double prob);
	public ChromosomeSet crossOver(ChromosomeSet parents, Random rand);
}
