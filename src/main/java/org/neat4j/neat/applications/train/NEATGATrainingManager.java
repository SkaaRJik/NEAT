package org.neat4j.neat.applications.train;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.*;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.core.fitness.InvalidFitnessFunction;
import org.neat4j.neat.core.fitness.MSENEATFitnessFunction;
import org.neat4j.neat.core.mutators.NEATMutator;
import org.neat4j.neat.core.pselectors.InvalidParentSelectorFunction;
import org.neat4j.neat.core.pselectors.TournamentSelector;
import org.neat4j.neat.core.xover.NEATCrossover;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.ga.core.*;
import org.neat4j.neat.nn.core.LearningEnvironment;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.utils.MathUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Training control for a NEAT network based on given configuration.
 * @author MSimmerson
 *
 */
public class NEATGATrainingManager {
	private static final Category cat = Category.getInstance(NEATGATrainingManager.class);
	protected GeneticAlgorithm ga;
	protected AIConfig config;
	protected Random random;
	protected InnovationDatabase innovationDatabase;


	public GeneticAlgorithm getGeneticAlgorithm() {
		return (this.ga);
	}
	/**
	 *
	 */
	public void initialise(AIConfig config) throws InitialisationFailedException {
		MathUtils.setSeed(Long.parseLong(config.configElement("GENERATOR.SEED")));
		config.updateConfig("NATURAL.ORDER.STRATEGY", "true");
		this.random = MathUtils.getRand();
		GADescriptor gaDescriptor = this.createDescriptor(config);
		this.assigGA(this.createGeneticAlgorithm(gaDescriptor));
		try {
			this.assignConfig(config);
			innovationDatabase = new InnovationDatabase(this.random, this.ga.pluginAllowedActivationFunctions(config));
			this.ga.pluginFitnessFunction(this.createFunction());
			this.ga.pluginCrossOver(new NEATCrossover());
			this.ga.pluginMutator(new NEATMutator(this.random, innovationDatabase));
			this.ga.pluginParentSelector(new TournamentSelector(this.random));
			this.ga.createPopulation(innovationDatabase);
		} catch (InvalidFitnessFunction e) {
			throw new InitialisationFailedException(e.getMessage());
		}  catch (Exception e) {
			e.printStackTrace();
			throw new InitialisationFailedException(e.getMessage());
		}
	}

	public void assigGA(GeneticAlgorithm ga) {
		this.ga = ga;
	}
	
	public void assignConfig(AIConfig config) {
		this.config = config;
	}

	/**
	 * Initiates an evaluation and evolution cycle.
	 *
	 */
	public void evolve() {
		int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
		double errorValueToTerminate = ((NEATGADescriptor)this.ga.getDescriptor()).getErrorTerminationValue();
		boolean nOrder = this.ga.getDescriptor().isNaturalOrder();
		boolean terminate = false;
		int i = 0;
		
		while (i < epochs /*&& !terminate*/) {
			cat.info("Running Epoch[" + i + "]\r");
			this.ga.runEpoch();
			this.saveBest();
			if ((this.ga.discoverdBestMember().fitness() >= errorValueToTerminate && !nOrder) || (this.ga.discoverdBestMember().fitness() <= errorValueToTerminate && nOrder)) {
				terminate = true;
			}
			i++;
		}
		cat.debug("Innovation Database Stats - Hits:" + innovationDatabase.getTotalHits() + " - totalMisses:" + innovationDatabase.getTotalMisses());
	}
	
	/**
	 * Saves the best candidate of the generation
	 *
	 */
	public void saveBest() {
		String pathToSave = config.configElement("SAVE.LOCATION");
		Chromosome chromosome = this.ga.discoverdBestMember();
		chromosome.setInputs(Integer.parseInt(config.configElement("INPUT.NODES")));
		chromosome.setOutputs(Integer.parseInt(config.configElement("OUTPUT.NODES")));
		this.save(pathToSave, chromosome);
	}
	
	/**
	 *
	 */
	public GADescriptor createDescriptor(AIConfig config) {

		NEATGADescriptor descriptor = new NEATGADescriptor();
		descriptor.setPAddLink(Double.parseDouble(config.configElement("PROBABILITY.ADDLINK")));
		descriptor.setPAddNode(Double.parseDouble(config.configElement("PROBABILITY.ADDNODE")));
		descriptor.setPToggleLink(Double.parseDouble(config.configElement("PROBABILITY.TOGGLELINK")));
		descriptor.setPMutateBias(Double.parseDouble(config.configElement("PROBABILITY.MUTATEBIAS")));
		descriptor.setPNewActivationFunction(Double.parseDouble(config.configElement("PROBABILITY.NEWACTIVATIONFUNCTION")));
		//descriptor.setPXover(Double.parseDouble(config.configElement("PROBABILITY.CROSSOVER")));
		descriptor.setPMutation(Double.parseDouble(config.configElement("PROBABILITY.MUTATION")));
		descriptor.setInputNodes(Integer.parseInt(config.configElement("INPUT.NODES")));
		descriptor.setOutputNodes(Integer.parseInt(config.configElement("OUTPUT.NODES")));
		descriptor.setNaturalOrder(Boolean.valueOf((config.configElement("NATURAL.ORDER.STRATEGY"))).booleanValue());
		descriptor.setPopulationSize(Integer.parseInt(config.configElement("POP.SIZE")));
		descriptor.setDisjointCoeff(Double.parseDouble(config.configElement("DISJOINT.COEFFICIENT")));
		descriptor.setExcessCoeff(Double.parseDouble(config.configElement("EXCESS.COEFFICIENT")));
		descriptor.setWeightCoeff(Double.parseDouble(config.configElement("WEIGHT.COEFFICIENT")));
		descriptor.setThreshold(Double.parseDouble(config.configElement("COMPATABILITY.THRESHOLD")));
		descriptor.setCompatabilityChange(Double.parseDouble(config.configElement("COMPATABILITY.CHANGE")));
		descriptor.setMaxSpecieAge(Integer.parseInt(config.configElement("SPECIE.FITNESS.MAX")));
		descriptor.setSpecieAgeThreshold(Integer.parseInt(config.configElement("SPECIE.AGE.THRESHOLD")));
		descriptor.setSpecieYouthThreshold(Integer.parseInt(config.configElement("SPECIE.YOUTH.THRESHOLD")));
		descriptor.setAgePenalty(Double.parseDouble(config.configElement("SPECIE.OLD.PENALTY")));
		descriptor.setYouthBoost(Double.parseDouble(config.configElement("SPECIE.YOUTH.BOOST")));
		descriptor.setSpecieCount(Integer.parseInt(config.configElement("SPECIE.COUNT")));
		descriptor.setPWeightReplaced(Double.parseDouble(config.configElement("PROBABILITY.WEIGHT.REPLACED")));
		descriptor.setSurvivalThreshold(Double.parseDouble(config.configElement("SURVIVAL.THRESHOLD")));
		descriptor.setFeatureSelection(Boolean.valueOf(config.configElement("FEATURE.SELECTION")).booleanValue());
		descriptor.setExtraFeatureCount(Integer.parseInt(config.configElement("EXTRA.FEATURE.COUNT")));
		descriptor.setEleEvents(Boolean.valueOf(config.configElement("ELE.EVENTS")).booleanValue());
		descriptor.setEleSurvivalCount(Double.parseDouble(config.configElement("ELE.SURVIVAL.COUNT")));
		descriptor.setEleEventTime(Integer.parseInt(config.configElement("ELE.EVENT.TIME")));
		descriptor.setRecurrencyAllowed(Boolean.valueOf(config.configElement("RECURRENCY.ALLOWED")).booleanValue());
		descriptor.setKeepBestEver(Boolean.valueOf(config.configElement("KEEP.BEST.EVER")).booleanValue());
		descriptor.setErrorTerminationValue(Double.parseDouble(config.configElement("TERMINATION.VALUE")));
		descriptor.setMaxPerturb(Double.parseDouble(config.configElement("MAX.PERTURB")));
		descriptor.setMaxBiasPerturb(Double.parseDouble(config.configElement("MAX.BIAS.PERTURB")));
		descriptor.setToggleErrorTerminationValue(Boolean.parseBoolean(config.configElement("TERMINATION.VALUE.TOGGLE")));
		
		return (descriptor);
	}


	/**
	 * Creates a GA for NEAT evolution based on the getDescriptor
	 * @param gaDescriptor
	 * @return created GA
	 */
	public GeneticAlgorithm createGeneticAlgorithm(GADescriptor gaDescriptor) {
		GeneticAlgorithm ga = new NEATGeneticAlgorithm((NEATGADescriptor)gaDescriptor, this.random, 5);
		return (ga);
	}

	/**
	 *
	 */
	public FitnessFunction createFunction() throws InvalidFitnessFunction {
		FitnessFunction function = null;
		AIConfig nnConfig;
		NEATNetManager netManager;
		NeuralNet net = null;
		NetworkDataSet dataSet = null;
		NetworkDataSet testSet = null;
		LearningEnvironment env;

		try {

				/*nnConfig = new NEATConfig();
				//nnConfig  = new NEATLoader().loadConfig(nnConfigFile);
				nnConfig.updateConfig("INPUT_SIZE", config.configElement("INPUT.NODES"));
				nnConfig.updateConfig("OUTPUT_SIZE", config.configElement("OUTPUT.NODES"));
				nnConfig.updateConfig("LEARNABLE", config.configElement("LEARNABLE"));*/
				/*if(!config.configElement("TRAINING.SET").matches("/"))
					config.updateConfig("TRAINING.SET", config.configElement("CONFIGURATION.FILEPATH")+"/"+config.configElement("TRAINING.SET"));
				else
					config.updateConfig("TRAINING.SET", config.configElement("TRAINING.SET"));
				*/
				netManager = new NEATNetManager();
				netManager.initialise(config, true);
				net = netManager.managedNet();
				env = net.netDescriptor().learnable().learningEnvironment();
				dataSet = (NetworkDataSet)env.learningParameter("TRAINING.SET");
				testSet = (NetworkDataSet)env.learningParameter("TEST.SET");
				function = new MSENEATFitnessFunction(net, dataSet, testSet);

		}  catch (IllegalArgumentException e) {
			throw new InvalidFitnessFunction("Invalid function class, " + function.getClass() + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
		}  catch (InitialisationFailedException e) {
			e.printStackTrace();
			throw new InvalidFitnessFunction("Could not create Firness function, configuration was invalid:" + e.getMessage());
		}
		
		return (function);
	}


	public ParentSelector createParentSelector(AIConfig config) throws InvalidParentSelectorFunction {
		String pSelectorClass = config.configElement("OPERATOR.PSELECTOR");
		ParentSelector pSelector;
		
		if (pSelectorClass != null) {
			try {
				pSelector = (ParentSelector)Class.forName(pSelectorClass).newInstance();
			} catch (InstantiationException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (Exception e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			}
		} else {
			throw new InvalidParentSelectorFunction("Parent Selector class was null");
		}
		
		return (pSelector);
	}








	public boolean save(String fileName, Chromosome genoType) {
		boolean saveOk = false;
		ObjectOutputStream s = null;
		FileOutputStream out = null;
		try {
			if (fileName != null)
			//System.out.println("Saving Best Chromosome to " + fileName);
			out = new FileOutputStream(fileName);
			s = new ObjectOutputStream(out);
			s.writeObject(genoType);
			s.flush();
			saveOk = true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (s != null) {
					s.close();
				}
				
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		//System.out.println("Saving Best Chromosome...Done");
		return (saveOk);
	}


}
