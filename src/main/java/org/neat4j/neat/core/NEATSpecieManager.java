/*
 * Created on 24-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.apache.log4j.Logger;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;



/**
 * @author MSimmerson
 *
 * Manages speicie compatablity
 */
public class NEATSpecieManager {
	Logger logger = Logger.getLogger(NEATSpecieManager.class);

	private static final NEATSpecieManager manager = new NEATSpecieManager();
	
	private NEATSpecieManager() {
	}
	
	public static NEATSpecieManager specieManager() {
		return (manager);
	}
	
	public double compatibilityScore(Chromosome specieApplicant, Chromosome specieRepresentative, double excessCoeff, double disjointCoeff, double weightCoeff) {
		int disjoints = 0;
		int excess = 0;
		boolean genesToProcess = true;
		int applicantIdx = 0;
		boolean applicantIdxEnded = false;
		int repIdx = 0;
		boolean repIdxEnded = false;
		double avWeightDiff = 0;
		double weightDiffTotal = 0;
		int N;
		double compatabilityScore = Integer.MAX_VALUE;
		Gene[] applicantGenes;
		Gene[] repGenes;
		
		applicantGenes = specieApplicant.genes();
		repGenes = specieRepresentative.genes();
		N = specieApplicant.size() > specieRepresentative.size() ? specieApplicant.size() : specieRepresentative.size();
		int counter = 0;
		while (genesToProcess) {
			//logger.debug(counter++);
			// find disjoints and excess
			if (((NEATGene)applicantGenes[applicantIdx]).getInnovationNumber() == ((NEATGene)repGenes[repIdx]).getInnovationNumber()) {
				// find average weight diff
				if (applicantGenes[applicantIdx] instanceof NEATLinkGene) {
					weightDiffTotal += (((NEATLinkGene)applicantGenes[applicantIdx]).getWeight() - ((NEATLinkGene)repGenes[repIdx]).getWeight());
				}
				applicantIdx++;
				repIdx++;
			} else if (((NEATGene)applicantGenes[applicantIdx]).getInnovationNumber() > ((NEATGene)repGenes[repIdx]).getInnovationNumber()) {
				if (repIdx < repGenes.length && !repIdxEnded) {
					repIdx++;
					disjoints++;
				} else {
					applicantIdx++;
					excess++;
				}
			} else if (((NEATGene)applicantGenes[applicantIdx]).getInnovationNumber() < ((NEATGene)repGenes[repIdx]).getInnovationNumber()) {
				if (applicantIdx < applicantGenes.length && !applicantIdxEnded) {
					applicantIdx++;
					disjoints++;
				} else {
					repIdx++;
					excess++;
				}
			}
						
			if (applicantIdx == N || repIdx == N) {
				genesToProcess = false;
			}

			// ensure we don't go out of range
			if (applicantIdx == applicantGenes.length) {
				applicantIdx %= applicantGenes.length;
				applicantIdxEnded = true;
			} else if (repIdx == repGenes.length) {
				repIdx %= repGenes.length;
				repIdxEnded = true;
			}
		}
		avWeightDiff = Math.abs(weightDiffTotal / (N - disjoints - excess));
		compatabilityScore = ((excessCoeff * excess) / N) + ((disjointCoeff * disjoints) / N) + weightCoeff * avWeightDiff;
		
		return (compatabilityScore);
	}
}
