package org.neat4j.neat.applications.train;


import javafx.beans.property.*;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;

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

    private SimpleObjectProperty<DataKeeper> dataKeeper = new SimpleObjectProperty<>(null);

    private DoubleProperty status = new SimpleDoubleProperty(0);
    private BooleanProperty isEnded = new SimpleBooleanProperty(false);
    private List<Chromosome> bestEverChromosomes;
    private SimpleObjectProperty<Chromosome> bestEverChromosomeProperty = new SimpleObjectProperty<>(null);

    private Integer currentEpoch = 0;
    private Double lastTrainError = 0.0;
    private Double lastValidationError = null;




    String pathToSave;
    @Override
    public void run() {
        this.evolve();
    }

    @Override
    public void initialise(AIConfig config) throws InitialisationFailedException {

        super.initialise(config);

        this.status.setValue(0);
        this.isEnded.setValue(false);
        bestEverChromosomes = new ArrayList<>(Integer.parseInt(config.configElement("NUMBER.EPOCHS")));

        bestEverChromosomeProperty.setValue(null);
        currentEpoch = 0;
        lastTrainError = 0.0;
        lastValidationError = null;








        
        
        
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
        this.dataKeeper.setValue(trainDataSet);
        this.initialise(config);

    }

    public void evolve() {
        int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
        double terminateVal = ((NEATGADescriptor)this.ga.getDescriptor()).getErrorTerminationValue();
        boolean terminateEnabled = ((NEATGADescriptor)this.ga.getDescriptor()).isToggleErrorTerminationValue();
        boolean nOrder = ((NEATGADescriptor)this.ga.getDescriptor()).isNaturalOrder();
        boolean terminate = false;


        pathToSave = config.configElement("SAVE.LOCATION");
        int i = 0;
        while (i < epochs) {
            if(Thread.interrupted()) {
                break;
            }
            logger.info("Running Epoch[" + i + "]\r");
            this.ga.runEpoch();
            this.saveBest();

            if ((this.ga.discoverdBestMember().fitness() >= terminateVal && !nOrder) || (this.ga.discoverdBestMember().fitness() <= terminateVal && nOrder)) {
                terminate = true;
            }

            i++;
            status.setValue(((double)i)/epochs);
            this.saveDataForGUI(i);
            if(terminate && terminateEnabled) {
                status.setValue(1);
                break;
            }

        }
        this.status.setValue(1.0);
        this.isEnded.setValue(true);
        logger.debug("Innovation Database Stats - Hits:" + innovationDatabase.totalHits + " - totalMisses:" + innovationDatabase.totalMisses);

    }

    private void saveDataForGUI(int i) {
        Chromosome best = this.ga.discoverdBestMember();
        logger.info("Save GUI[" + i + "]\r");

        this.lastTrainError = best.getTrainError();
        this.lastValidationError = best.getValidationError();
        currentEpoch = i+1;


        /*this.errorData.add();
        if(best.getValidationError()!=null){
            this.validationErrorData.add(this.createXYChart(i, best.getValidationError()));
        }*/



    }


    private XYChart.Data<Number, Number> createXYChart(Number i, Double value){
        if(value == null) return null;
        XYChart.Data<Number, Number> xyData = new XYChart.Data<>(i, value);
        xyData.setNode(new StackPane());
        Tooltip.install(xyData.getNode(), new Tooltip(String.valueOf(value)));
        return xyData;
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

        this.bestEverChromosomeProperty.setValue(best);
        if(pathToSave != null)
            this.save(pathToSave, best);
        bestEverChromosomes.add(best);
    }

    public List<Chromosome> getBestEverChromosomes() {
        return bestEverChromosomes;
    }

    public List<Chromosome> getBestEverChromosomesList() {
        return bestEverChromosomes;
    }

    public SimpleObjectProperty<Chromosome> getBestChromosomeProperty() {
        return bestEverChromosomeProperty;
    }

    public boolean isIsEnded() {
        return isEnded.get();
    }

    public BooleanProperty isEndedProperty() {
        return isEnded;
    }

    public Double getLastTrainError() {
        return lastTrainError;
    }

    public Double getLastValidationError() {
        return lastValidationError;
    }

    public Integer getCurrentEpoch() {
        return currentEpoch;
    }

    public SimpleObjectProperty<DataKeeper> getDataKeeperProperty() {
        return dataKeeper;
    }

    public DataKeeper getDataKeeper(){
        return dataKeeper.getValue();
    }
}