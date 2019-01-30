/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.ga.core.Population;
import org.neat4j.neat.utils.MathUtils;

import java.util.Random;

/**
 * @author MSimmerson
 *
 * Contains a population of NEAT chromosomes that are used to describe a NEAT neural network.
 */
public class NEATPopulation implements Population {
	private Chromosome[] chromosomes;
	private int popSize;
	private int initialChromoSize;
	private int inputs;
	private int outputs;
	private boolean featureSelection;
	private int extraFeatureCount = 0;
	private final Random random;

	public NEATPopulation(int popSize, int initialChromoSize, int inputs, int outputs, boolean featureSelection, int extraFeaturecount, Random random) {
		this.popSize = popSize;
		this.initialChromoSize = initialChromoSize;
		this.inputs = inputs;
		this.outputs = outputs;
		this.featureSelection = featureSelection;
		this.extraFeatureCount = extraFeaturecount;
		this.random = random;
	}

	public NEATPopulation(int popSize, int initialChromoSize, int inputs, int outputs, boolean featureSelection, Random random) {
		this(popSize, initialChromoSize, inputs, outputs, featureSelection, 0, random);
	}
	
	public Chromosome[] genoTypes() {
		return (this.chromosomes);
	}

	/**
	 * Creates an intial population
	 */
	public void createPopulation() {
		this.chromosomes = new Chromosome[this.popSize];
		int i;
		// use the innovation database to create the initial population
		Chromosome[] templates = InnovationDatabase.getInstance(this.random).initialiseInnovations(this.popSize, this.inputs, this.outputs, this.featureSelection, this.extraFeatureCount);
		
		for (i = 0; i < this.popSize; i++) {
			this.chromosomes[i] = this.individualFromTemplate(templates[i]);
		}
	}
	
	private Chromosome individualFromTemplate(Chromosome template) {
		int i;
		Gene[] templateGenes = template.genes();
		Gene[] individualGenes = new Gene[templateGenes.length]; 
		NEATNodeGene nodeGene;
		NEATLinkGene linkGene;
		NEATFeatureGene featureGene;
		
		for (i = 0; i < templateGenes.length; i++) {
			if (templateGenes[i] instanceof NEATNodeGene) {
				nodeGene = (NEATNodeGene)templateGenes[i];
				individualGenes[i] = new NEATNodeGene(nodeGene.getInnovationNumber(), nodeGene.id(), MathUtils.nextPlusMinusOne(), nodeGene.getType(), MathUtils.nextDouble());				
			} else if (templateGenes[i] instanceof NEATLinkGene) {
				linkGene = (NEATLinkGene)templateGenes[i];
				individualGenes[i] = new NEATLinkGene(linkGene.getInnovationNumber(), true, linkGene.getFromId(), linkGene.getToId(), MathUtils.nextPlusMinusOne());
			} else if (templateGenes[i] instanceof NEATFeatureGene) {
				featureGene = (NEATFeatureGene)templateGenes[i];
				individualGenes[i] = new NEATFeatureGene(featureGene.getInnovationNumber(), MathUtils.nextDouble());
			}
		}

		return (new NEATChromosome(individualGenes));
	}

	/** 
	 * @see org.neat4j.ailibrary.ga.core.Population#updatePopulation(org.neat4j.ailibrary.ga.core.Chromosome[])
	 */
	public void updatePopulation(Chromosome[] newGenoTypes) {
		if (newGenoTypes.length == this.popSize) {
			System.arraycopy(newGenoTypes, 0, this.chromosomes, 0, this.popSize);
		} else {
			System.out.println(this.getClass().getName() + ".updatePopulation() incompatable newGenoTypes length");
		}
	}

}
