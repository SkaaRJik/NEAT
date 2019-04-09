/*
 * Created on 21-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.functions.ActivationFunctionContainer;
import org.neat4j.neat.utils.MathUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Provides the NEAT innovation instance as described in Kenneth Stanley's NEAT papers
 * The innovations are kept for the entire life of the run.
 * @author MSimmerson
 *
 */
public class InnovationDatabase {
	private final Random ran;
	private HashMap innovations;
	private int innovationId = 1;
	private int neuronId = 1;
	public  int totalHits = 0;
	public  int totalMisses = 0;

	public InnovationDatabase(Random ran){
		this.ran = ran;
		innovationId = 1;
		neuronId = 1;
		totalHits = 0;
		totalMisses = 0;
		this.innovations = new HashMap();
	}

	private int nextInnovationNumber() {
		return (innovationId++);
	}
	
	private int nextNodeNumber() {
		return (neuronId++);
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */

	/**
	 * Creates popsize chromosomes ready for creating a NEAT network. 
	 * @param popSize - size of the population
	 * @param inputs - number of network inputs
	 * @param outputs - number of network outputs
	 * @param featureSelection - if true assigns one connection to each output to a random input.  If false, all inputs are connected to all outputs
	 * @param extraFeatureCount - creates chromosome features that are non structural, ie they have no bearing on the creation of the net they represent
	 *  evolving data inputs.
	 * @return Created chromosome population
	 */
	public Chromosome[] initialiseInnovations(int popSize, int inputs, int outputs, boolean featureSelection, int extraFeatureCount) {	
		int i;
		int j;
		int popIdx;
		NEATNodeGene[] nodes = new NEATNodeGene[inputs + outputs]; 
		//NEATLinkGene[] links = new NEATLinkGene[totalNumConnections] ;
		NEATLinkGene[] links;
		NEATFeatureGene[] features = new NEATFeatureGene[extraFeatureCount];
		
		Chromosome[] templates = new Chromosome[popSize];
		
		for (i = 0; i < extraFeatureCount; i++) {
			features[i] = this.createFeatureGene();
		}
		
		for (i = 0; i < inputs; i++) {
			nodes[i] = this.createNewNodeGene(NEATNodeGene.TYPE.INPUT);
		}

		for (i = inputs; i < nodes.length; i++) {
			nodes[i] = this.createNewNodeGene(NEATNodeGene.TYPE.OUTPUT);
		}

//		for (i = inputs + outputs; i < nodes.length; i++) {
//			nodes[i] = this.createNewNodeGene(NEATNodeGene.HIDDEN);
//		}
		
		for (popIdx = 0; popIdx < templates.length; popIdx++) {
			// outputs are last nodes.
			if (featureSelection) {
				// start with one link to each output, allows feature selection
				links = new NEATLinkGene[outputs] ;
				for (i = 0; i < outputs; i++) {
					links[i] = this.submitLinkInnovation(nodes[this.ran.nextInt(inputs)].id(), nodes[inputs + i].id());
					links[i].setWeight(MathUtils.nextPlusMinusOne());
				}
			} else {
				// start with each input to each output, doesn't allow feature selection
				links = new NEATLinkGene[inputs * outputs] ;
				for (i = 0; i < outputs; i++) {
					for (j = 0; j < inputs; j++) {
						links[(i * outputs) + j] = this.submitLinkInnovation(nodes[j].id(), nodes[inputs + i].id());
						links[(i * outputs) + j].setWeight(MathUtils.nextPlusMinusOne());
					}
				}
			}
//			links[2] = this.submitLinkInnovation(nodes[0].id(), nodes[3].id());
//			links[2].setWeight(MathUtils.nextPlusMinusOne());
//			links[3] = this.submitLinkInnovation(nodes[1].id(), nodes[3].id());
//			links[3].setWeight(MathUtils.nextPlusMinusOne());
//			links[3] = this.submitLinkInnovation(nodes[3].id(), nodes[2].id());
//			links[3].setWeight(MathUtils.nextPlusMinusOne());
			templates[popIdx] = this.createNEATChromosome(nodes, links, features);
		}
		
		
		return (templates);
	}
	
	private Chromosome createNEATChromosome(NEATNodeGene[] nodes, NEATLinkGene[] links, NEATFeatureGene[] features) {
		Gene[] genes = new Gene[nodes.length + links.length + features.length];
		System.arraycopy(features, 0, genes, 0, features.length);
		System.arraycopy(nodes, 0, genes, features.length, nodes.length);
		System.arraycopy(links, 0, genes, features.length + nodes.length, links.length);
		
		return (new NEATChromosome(genes));
	}

	private NEATFeatureGene createFeatureGene() {
		int innovationNumber = this.nextInnovationNumber();
		NEATInnovation databaseEntry = new NEATFeatureInnovation();
		databaseEntry.setInnovationId(innovationNumber);
		this.innovations.put(new Integer(innovationNumber), databaseEntry);
		
		return (new NEATFeatureGene(innovationNumber, MathUtils.nextDouble()));
	}
	
	private NEATNodeGene createNewNodeGene(NEATNodeGene.TYPE type) {
		int innovationNumber = this.nextInnovationNumber();
		NEATInnovation databaseEntry = new NEATNodeInnovation();
		databaseEntry.setInnovationId(innovationNumber);
		((NEATNodeInnovation)databaseEntry).setNodeId(this.nextNodeNumber());
		this.innovations.put(new Integer(innovationNumber), databaseEntry);
		ActivationFunction activationFunction = null;

		if (type == NEATNodeGene.TYPE.HIDDEN){
			activationFunction = ActivationFunctionContainer.getRandomHiddenActivationFunction(ran);
		} else if (type == NEATNodeGene.TYPE.INPUT) {
			activationFunction = ActivationFunctionContainer.getRandomInputActivationFunction(ran);
		} else if(type == NEATNodeGene.TYPE.OUTPUT) {
			activationFunction = ActivationFunctionContainer.getRandomOutputActivationFunction(ran);
		}

		NEATNodeGene nodeGene = new NEATNodeGene(innovationNumber, ((NEATNodeInnovation)databaseEntry).getNodeId(), MathUtils.nextDouble(), type, "",MathUtils.nextPlusMinusOne(), activationFunction);
		
		return (nodeGene);
	}
	
	private NEATInnovation findNodeInnovation(int linkInnovationId) {
		NEATNodeInnovation nodeInnovation = new NEATNodeInnovation(linkInnovationId);
		NEATInnovation databaseEntry = null;
		boolean found = false;
		Set keySet;
		Integer key;
		Iterator it;
		
		if (this.innovations.containsValue(nodeInnovation)) {
			keySet = this.innovations.keySet();
			it = keySet.iterator();
			
			while (it.hasNext() && !found) {
				key = (Integer)it.next();
				databaseEntry = (NEATInnovation)this.innovations.get(key);				
				if ((databaseEntry instanceof NEATNodeInnovation) && 
					(databaseEntry != null) && 
					((((NEATNodeInnovation)databaseEntry)).getLinkInnovationId() == linkInnovationId)) {
					found = true;
				}
			}
		}
		
		return (databaseEntry);
	}
	
	/**
	 * Submits a node insertion mutation to the instance.  If it does not exist, it creates it and adds it to
	 * the instance.  It returns the instance entry
	 * @param linkGene - link to add node to
	 * @return - Created node gene
	 */
	public NEATNodeGene submitNodeInnovation(NEATLinkGene linkGene) {
		int innovationNumber;
		NEATInnovation databaseEntry = this.findNodeInnovation(linkGene.getInnovationNumber());
		NEATNodeGene gene = null;

		if (databaseEntry == null) {
			totalMisses++;

			innovationNumber = this.nextInnovationNumber();
			databaseEntry = new NEATNodeInnovation(linkGene.getInnovationNumber());
			((NEATNodeInnovation)databaseEntry).setNodeId(this.nextNodeNumber());
			databaseEntry.setInnovationId(innovationNumber);
			this.innovations.put(new Integer(innovationNumber), databaseEntry);
		} else {

			totalHits++;
		}

		gene = new NEATNodeGene(databaseEntry.innovationId(), ((NEATNodeInnovation)databaseEntry).getNodeId(), MathUtils.nextDouble(), NEATNodeGene.TYPE.HIDDEN, "",MathUtils.nextPlusMinusOne(), ActivationFunctionContainer.getRandomHiddenActivationFunction(ran));

		return (gene);
	}
	
	/**
	 * Submits a link insertion mutation to the instance.  If it does not exist, it creates it and adds it to
	 * the instance.  It returns the instance entry
	 * @param from - from node identifier
	 * @param to - to node identifier
	 * @return - Created link gene
	 */
	public NEATLinkGene submitLinkInnovation(int fromId, int toId) {
		int innovationNumber;
		NEATInnovation databaseEntry = this.findLinkInnovation(fromId, toId);

		if (databaseEntry == null) {
			totalMisses++;

			innovationNumber = this.nextInnovationNumber();
			databaseEntry = new NEATLinkInnovation(fromId, toId);
			databaseEntry.setInnovationId(innovationNumber);
			this.innovations.put(new Integer(innovationNumber), databaseEntry);
		} else {
			totalHits++;

		}
		// the 0 weight is a place holder
		NEATLinkGene gene = new NEATLinkGene(databaseEntry.innovationId(), true, fromId, toId, 0);
		
		return (gene);
	}
	
	private NEATInnovation findLinkInnovation(int fromId, int toId) {
		NEATLinkInnovation linkInnovation = new NEATLinkInnovation(fromId, toId);
		NEATInnovation databaseEntry = null;
		boolean found = false;
		Set keySet;
		Integer key;
		Iterator it;
		
		if (this.innovations.containsValue(linkInnovation)) {
			keySet = this.innovations.keySet();
			it = keySet.iterator();
			
			while (it.hasNext() && !found) {
				key = (Integer)it.next();
				databaseEntry = (NEATInnovation)this.innovations.get(key);				
				if ((databaseEntry instanceof NEATLinkInnovation) && 
					(databaseEntry != null) && 
					(((NEATLinkInnovation)databaseEntry).getFromId() == fromId) && 
					(((NEATLinkInnovation)databaseEntry).getToId() == toId)) {
					found = true;
				}
			}
		}
		
		return (databaseEntry);
	}

	public int getTotalHits() {
		return totalHits;
	}

	public int getTotalMisses() {
		return totalMisses;
	}
}
