package ru.filippov.prediction;

import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
import org.neat4j.neat.data.core.DataKeeper;

import java.io.IOException;

public class WindowTrainThread implements Runnable{
    final static Logger logger = Logger.getLogger(WindowTrainThread.class);
    int index;
    NEATTrainingForJavaFX neatTrainingForJavaFX;
    AIConfig configForWindow;


    public WindowTrainThread(int index, NEATTrainingForJavaFX neatTraining, AIConfig configForWindow, DataKeeper dataKeeper) throws IOException, InitialisationFailedException {
        this.index = index;
        this.neatTrainingForJavaFX = neatTraining;
        this.configForWindow = configForWindow;
        this.neatTrainingForJavaFX.initialise(configForWindow, dataKeeper, null);
        logger.info("Thread " + index + " initted! AIConfig INFO - " + this.configForWindow );
    }

    public void startTraining(){
        neatTrainingForJavaFX.evolve();
    }


    @Override
    public void run() {
        /*Thread thread = new Thread(neatTrainingForJavaFX);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        startTraining();
    }
}
