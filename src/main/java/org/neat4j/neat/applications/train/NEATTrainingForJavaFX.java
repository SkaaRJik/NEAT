package org.neat4j.neat.applications.train;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.ga.core.Chromosome;

import java.util.ArrayList;
import java.util.List;

public class NEATTrainingForJavaFX extends NEATGATrainingManager implements Runnable{
    private static final Logger cat = Logger.getLogger(NEATTrainingForJavaFX.class);
    DoubleProperty status;
    ObservableList<Chromosome> bestEverChromosomes;
    ListProperty<Chromosome> bestEverChromosomesProperty;
    private int currentEpoch;

    @Override
    public void run() {
        this.evolve();
    }

    @Override
    public void initialise(AIConfig config) throws InitialisationFailedException {
        InnovationDatabase.refresh();
        super.initialise(config);
        this.status = new SimpleDoubleProperty(0);

        bestEverChromosomes = FXCollections.observableArrayList();
        this.bestEverChromosomesProperty = new SimpleListProperty<>(bestEverChromosomes);
    }

    public void evolve() {
        int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
        double terminateVal = ((NEATGADescriptor)this.ga.getDescriptor()).getErrorTerminationValue();
        boolean terminateEnabled = ((NEATGADescriptor)this.ga.getDescriptor()).isToggleErrorTerminationValue();
        boolean nOrder = ((NEATGADescriptor)this.ga.getDescriptor()).isNaturalOrder();
        boolean terminate = false;
        int i = 0;

        while (i < epochs) {
            currentEpoch = i+1;
            cat.info("Running Epoch[" + i + "]\r");
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
        cat.debug("Innovation Database Stats - Hits:" + InnovationDatabase.totalHits + " - totalMisses:" + InnovationDatabase.totalMisses);

    }

    public double getStatus() {
        return status.get();
    }

    public DoubleProperty statusProperty() {
        return status;
    }

    @Override
    public void saveBest() {
        String pathToSave = config.configElement("SAVE.LOCATION");
        Chromosome best = this.ga.discoverdBestMember();
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
}
