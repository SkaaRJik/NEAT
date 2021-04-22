/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * NEAT specific chromosome
 * @author MSimmerson
 *
 */
public class NEATChromosome implements Chromosome {
	private Gene[] genes;
	private double fitness;
	private double trainError;
	private Double validationError;
	private int specieId = -1;
	private boolean nOrder = false;
	private int inputs;
	private int outputs;
	private List<List<Double>> outputValues;
	
	public NEATChromosome(Gene[] genes) {
		this.updateChromosome(genes);
	}

	
	/**
	 * @return Returns the specieId.
	 */
	public int getSpecieId() {
		return specieId;
	}

	/**
	 * @param specieId The specieId to set.
	 */
	public void setSpecieId(int specieId) {
		this.specieId = specieId;
	}

	public Gene[] genes() {
		return (this.genes);
	}

	public int size() {
		return (this.genes.length);
	}

	public void updateChromosome(Gene[] newGenes) {
		this.genes = new NEATGene[newGenes.length];
		System.arraycopy(newGenes, 0, this.genes, 0, this.genes.length);
	}

	public void updateFitness(double fitness) {
		this.fitness = fitness;
	}

	public double fitness() {
		return (this.fitness);
	}

	@Override
	public void setOutputValues(List<List<Double>> opSet) {
		this.outputValues = opSet;
	}

	@Override
	public List<List<Double>> getOutputValues() {
		return outputValues;
	}

	public void setNaturalOrder(boolean nOrder) {
		this.nOrder = nOrder;
	}

	public int compareTo(Object o) {
		int returnVal = 0;
		NEATChromosome test = (NEATChromosome)o;
		// sorts with highest first
		if (this.fitness > test.fitness()) {
			if (this.nOrder) {
				returnVal = 1;
			} else {
				returnVal = -1;
			}
		} else if (this.fitness < test.fitness()) {
			if (this.nOrder) {
				returnVal = -1;
			} else {
				returnVal = 1;
			}
		}
		
		return (returnVal);
	}

	/**
	 * Reads a saved chromosome from fileName
	 * @param fileName - location of chromosome
	 * @return Recreate object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	static public Object readObject(String fileName) throws IOException, ClassNotFoundException {
		Object o = null;
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		o = ois.readObject();
		ois.close();
		fis.close();


		return (o);
	}

	public int getInputs() {
		return inputs;
	}

	public void setInputs(int inputs) {
		this.inputs = inputs;
	}

	public int getOutputs() {
		return outputs;
	}

	public void setOutputs(int outputs) {
		this.outputs = outputs;
	}

	@Override
	public Double getValidationError() {
		return this.validationError;
	}

	@Override
	public void setValidationError(Double error) {
		this.validationError = error;
	}

	public double getTrainError() {
		return trainError;
	}

	public void setTrainError(double trainError) {
		this.trainError = trainError;
	}
}
