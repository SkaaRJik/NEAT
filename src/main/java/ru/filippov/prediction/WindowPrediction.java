package ru.filippov.prediction;

import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WindowPrediction implements Runnable {
    private final static Logger logger = Logger.getLogger(WindowPrediction.class);
    DataKeeper dataKeeper;
    DataKeeper[] dataForWindow;
    DataKeeper[] dataFromWindows;
    AIConfig[] configForWindows;
    int windowsSize = 0;
    int inputs;
    int yearPrediction = 0;
    NEATTrainingForJavaFX[] trainer;





    WindowTrainThread[] inputThreads;


    public WindowPrediction(DataKeeper dataKeeper, int windowsSize, int yearPrediction, AIConfig config) throws IOException, InitialisationFailedException {
        this.dataKeeper = dataKeeper;
        this.windowsSize = windowsSize;
        this.yearPrediction = yearPrediction;
        this.inputs = dataKeeper.getInputs();
        this.dataFromWindows = new DataKeeper[this.inputs];
        dataForWindow = new DataKeeper[this.inputs];
        config.updateConfig("INPUT.NODES", String.valueOf(windowsSize));
        config.updateConfig("OUTPUT.NODES", "1");
        configForWindows = new AIConfig[inputs];
        trainer = new NEATTrainingForJavaFX[inputs];
        inputThreads = new WindowTrainThread[inputs];




        for (int i = 0; i < inputs; i++) {
            configForWindows[i] = new NEATConfig((NEATConfig)config);
            trainer[i] = new NEATTrainingForJavaFX();
        }
    }

    public DataKeeper prepareDataForWindow(int index){
        List<List<Double>> windowData = new ArrayList<>(dataKeeper.getData().size()- windowsSize);
        List<Double> legend = new ArrayList<>(dataKeeper.getData().size()- windowsSize);
        List<String> headers = new ArrayList<>(windowsSize +1);
        List<Double> windowRow;
        for (int i = 0; i < dataKeeper.getData().size()- windowsSize; i++) {
            windowRow = new ArrayList<>(windowsSize +1);
            for (int j = i; j <= i+ windowsSize; j++) {
                if(i == 0) {
                    headers.add(String.valueOf(j+1));
                }
                windowRow.add(dataKeeper.getData().get(j).get(index));
            }
            legend.add(Double.valueOf(i+1));
            windowData.add(windowRow);
        }

        DataKeeper windowDataKeeper = new DataKeeper(windowData, dataKeeper.getDataScaler());
        windowDataKeeper.setLegend(legend);
        windowDataKeeper.setInputs(windowsSize);
        windowDataKeeper.setOutputs(1);
        windowDataKeeper.setHeaders(headers);
        windowDataKeeper.setLegendHeader(dataKeeper.getHeaders() == null ? "Окно" : dataKeeper.getHeaders().get(index));
        return windowDataKeeper;
    }

    @Override
    public void run() {


        /*Thread[] threads = new Thread[inputs];
        for (int i = 0; i < this.inputs; i++) {
            int finalI = i;
            threads[finalI] = new Thread(() -> {
                try {
                    dataForWindow[finalI] = prepareDataForWindow(finalI);
                    inputThreads[finalI] = new WindowTrainThread(finalI, trainer[finalI], configForWindows[finalI], dataForWindow[finalI]);
                    new Thread(inputThreads[finalI]).start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InitialisationFailedException e) {
                    e.printStackTrace();
                }
            });
            threads[finalI].start();
        }
        try {
        for (int i = 0; i < inputs; i++) {
                threads[i].join();
        }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        Thread[] threads = new Thread[this.inputs];

        for (int i = 0; i < this.inputs; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    dataForWindow[finalI] = prepareDataForWindow(finalI);
                    inputThreads[finalI] = new WindowTrainThread(finalI, trainer[finalI], configForWindows[finalI], dataForWindow[finalI]);
                    inputThreads[finalI].startTraining();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InitialisationFailedException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        try {
            for (int i = 0; i < this.inputs; i++) {
                    threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        List<Chromosome> bestEverChromosomes;
        for (int i = 0; i < this.inputs; i++) {

            bestEverChromosomes = inputThreads[i].neatTrainingForJavaFX.getBestEverChromosomes();
            logger.info("fitness = " + bestEverChromosomes.get(bestEverChromosomes.size()-1).fitness());


        }
        logger.info(this.getClass().getName() + " Потоки закончили работу");


        /*Thread[] threads = new Thread[this.inputs];
        for (int i = 0; i < inputThreads.length; i++) {
            threads[i] = new Thread(inputThreads[i]);
            threads[i].start();
        }
        for (int i = 0; i < inputThreads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}
