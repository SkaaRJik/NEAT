package org.neat4j.neat.applications.train;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.NEATSpecieManager;
import org.neat4j.neat.ga.core.Specie;

public class NEATTrainingForJavaFX extends NEATGATrainingManager implements Runnable{
    private static final Logger cat = Logger.getLogger(NEATTrainingForJavaFX.class);
    DoubleProperty status;

    @Override
    public void run() {
        this.evolve();
    }

    @Override
    public void initialise(AIConfig config) throws InitialisationFailedException {
        InnovationDatabase.refresh();
        super.initialise(config);
        this.status = new SimpleDoubleProperty(0);
    }

    public void evolve() {
        int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
        double terminateVal = ((NEATGADescriptor)this.ga.getDescriptor()).getTerminationValue();
        boolean nOrder = ((NEATGADescriptor)this.ga.getDescriptor()).isNaturalOrder();
        boolean terminate = false;
        int i = 0;

        while (i < epochs /*&& !terminate*/) {
            cat.info("Running Epoch[" + i + "]\r");
            this.ga.runEpoch();
            this.saveBest();
            if ((this.ga.discoverdBestMember().fitness() >= terminateVal && !nOrder) || (this.ga.discoverdBestMember().fitness() <= terminateVal && nOrder)) {
                terminate = true;
            }
            i++;
            status.setValue(((double)i)/epochs);
        }
        cat.debug("Innovation Database Stats - Hits:" + InnovationDatabase.hits + " - misses:" + InnovationDatabase.misses);
    }

    public double getStatus() {
        return status.get();
    }

    public DoubleProperty statusProperty() {
        return status;
    }
}
