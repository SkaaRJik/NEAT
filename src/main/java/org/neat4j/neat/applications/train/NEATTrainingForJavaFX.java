package org.neat4j.neat.applications.train;


import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NEATTrainingForJavaFX extends NEATGATrainingManager implements Runnable{
    private static final Logger logger = Logger.getLogger(NEATTrainingForJavaFX.class);
    public static final File TEMP_DIRECTORY_PATH = new File( Paths.get("").toAbsolutePath().toString()+"\\temp");
    static {
        if(!TEMP_DIRECTORY_PATH.exists()){
            TEMP_DIRECTORY_PATH.mkdir();
        }
    }


    DoubleProperty status;
    BooleanProperty isEnded;
    ObservableList<Chromosome> bestEverChromosomes;
    ListProperty<Chromosome> bestEverChromosomesProperty;
    private int currentEpoch;
    String pathToSave;
    @Override
    public void run() {
        this.evolve();
    }

    @Override
    public void initialise(AIConfig config) throws InitialisationFailedException {

        super.initialise(config);
        this.status = new SimpleDoubleProperty(0);
        this.isEnded = new SimpleBooleanProperty(false);
        bestEverChromosomes = FXCollections.observableArrayList();
        this.bestEverChromosomesProperty = new SimpleListProperty<>(bestEverChromosomes);
    }

    public void initialise(AIConfig config, DataKeeper trainDataSet, String pathToSave) throws InitialisationFailedException, IOException {

        config.updateConfig("TRAINING.SET", TEMP_DIRECTORY_PATH.getAbsolutePath()+"\\"+ UUID.randomUUID()+".tmp");
        trainDataSet.saveSet(config.configElement("TRAINING.SET"), trainDataSet.getTrainData());


        List<List<Double>> testData = trainDataSet.getTestData();
        if(testData!=null) {
            config.updateConfig("TEST.SET", TEMP_DIRECTORY_PATH.getAbsolutePath() + "\\" + UUID.randomUUID() + ".tmp");
            trainDataSet.saveSet(config.configElement("TEST.SET"), trainDataSet.getTestData());
        }
        config.updateConfig("SAVE.LOCATION", pathToSave);



        logger.debug("trainModel() : tempDataset name " + config.configElement("TRAINING.SET"));

        this.initialise(config);

    }

    public void evolve() {
        int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
        double terminateVal = ((NEATGADescriptor)this.ga.getDescriptor()).getErrorTerminationValue();
        boolean terminateEnabled = ((NEATGADescriptor)this.ga.getDescriptor()).isToggleErrorTerminationValue();
        boolean nOrder = ((NEATGADescriptor)this.ga.getDescriptor()).isNaturalOrder();
        boolean terminate = false;

        int i = 0;
        pathToSave = config.configElement("SAVE.LOCATION");
        while (i < epochs) {
            if(Thread.interrupted()) {
                break;
            }
            currentEpoch = i+1;
            logger.info("Running Epoch[" + i + "]\r");
            this.ga.runEpoch();
            this.saveBest();

            if ((this.ga.discoverdBestMember().fitness() >= terminateVal && !nOrder) || (this.ga.discoverdBestMember().fitness() <= terminateVal && nOrder)) {
                terminate = true;
            }
            i++;
            status.setValue(((double)i)/epochs);
            if(terminate && terminateEnabled) {
                status.setValue(1);
                break;
            }

        }
        this.status.setValue(1.0);
        this.isEnded.setValue(true);
        logger.debug("Innovation Database Stats - Hits:" + innovationDatabase.totalHits + " - totalMisses:" + innovationDatabase.totalMisses);

    }

    public double getStatus() {
        return status.get();
    }

    public DoubleProperty statusProperty() {
        return status;
    }

    @Override
    public void saveBest() {
        Chromosome best = this.ga.discoverdBestMember();
        best.setInputs(Integer.parseInt(config.configElement("INPUT.NODES")));
        best.setOutputs(Integer.parseInt(config.configElement("OUTPUT.NODES")));
        if(pathToSave != null)
            this.save(pathToSave, best);
        bestEverChromosomes.add(best);
    }

    public List<Chromosome> getBestEverChromosomes() {
        return bestEverChromosomes;
    }

    public int getCurrentEpoch() {
        return this.currentEpoch;
    }

    public ObservableList<Chromosome> getBestEverChromosomesProperty() {
        return bestEverChromosomesProperty.get();
    }

    public ListProperty<Chromosome> bestEverChromosomesPropertyProperty() {
        return bestEverChromosomesProperty;
    }

    public boolean isIsEnded() {
        return isEnded.get();
    }

    public BooleanProperty isEndedProperty() {
        return isEnded;
    }
}