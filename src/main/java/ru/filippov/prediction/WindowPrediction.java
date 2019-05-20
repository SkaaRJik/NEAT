package ru.filippov.prediction;

import javafx.beans.property.SimpleObjectProperty;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
import org.neat4j.neat.core.*;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.set.InputImpl;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WindowPrediction implements Runnable {
    private final static Logger logger = Logger.getLogger(WindowPrediction.class);
    DataKeeper dataKeeper;
    DataKeeper[] dataForWindow;
    DataKeeper[] dataFromWindows;
    AIConfig[] configForWindow;
    int windowsSize = 0;
    int inputs;
    int yearPrediction = 0;
    NEATTrainingForJavaFX[] trainer;
    Double[][] predictedInputDatas;
    Double[][] predictedWindowDatas;

    List<List<Double>> outputData;
    SimpleObjectProperty<Boolean> trainIsFinished = new SimpleObjectProperty<>(false);
    Double predictionError = 0.0;
    SimpleObjectProperty<Boolean>[] predictionInputEnded;
    SimpleObjectProperty<Boolean> predictionOutputEnded;


    WindowTrainThread[] inputThreads;

    public WindowPrediction(){

    }

    public NEATTrainingForJavaFX getTrainer(int index){
        return trainer[index];
    }

    public void initialise(DataKeeper dataKeeper, int windowsSize, int yearPrediction, AIConfig config) throws IOException, InitialisationFailedException {
        this.dataKeeper = dataKeeper;
        this.windowsSize = windowsSize;
        this.yearPrediction = yearPrediction;
        this.inputs = dataKeeper.getInputs();
        this.dataFromWindows = new DataKeeper[this.inputs];
        dataForWindow = new DataKeeper[this.inputs];
        config.updateConfig("INPUT.NODES", String.valueOf(windowsSize));
        config.updateConfig("OUTPUT.NODES", "1");
        configForWindow = new AIConfig[this.inputs];
        trainer = new NEATTrainingForJavaFX[inputs];
        inputThreads = new WindowTrainThread[inputs];
        predictionInputEnded = new SimpleObjectProperty[inputs];
        predictionOutputEnded = new SimpleObjectProperty<>(false);
        for (int i = 0; i < inputs; i++) {
            trainer[i] = new NEATTrainingForJavaFX();
            configForWindow[i] = new NEATConfig((NEATConfig) config);
            predictionInputEnded[i] = new SimpleObjectProperty<>(false);
        }
        predictedInputDatas = new Double[yearPrediction][inputs];
        predictedWindowDatas = new Double[dataKeeper.getData().size()-windowsSize][inputs];
    }

    public DataKeeper prepareDataForWindow(int index, DataKeeper dataKeeper){
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

            legend.add(dataKeeper.getLegend() == null ? Double.valueOf(i+1) : dataKeeper.getLegend().get(i+windowsSize));
            windowData.add(windowRow);
        }

        DataKeeper windowDataKeeper = new DataKeeper(windowData, dataKeeper.getDataScaler());
        windowDataKeeper.setLegend(legend);
        windowDataKeeper.setInputs(windowsSize);
        windowDataKeeper.setOutputs(1);
        windowDataKeeper.setHeaders(headers);
        windowDataKeeper.setLegendHeader(dataKeeper.getLegendHeader() == null ? "Год" : dataKeeper.getHeaders().get(index));
        windowDataKeeper.calculateIndex(0.75);
        return windowDataKeeper;
    }

    @Override
    public void run() {
        trainIsFinished.setValue(false);
        Thread[] threads = new Thread[this.inputs];
        for (int i = 0; i < this.inputs; i++) {
            threads[i] = train(i, configForWindow[i] );
        }

        try {
            for (int i = 0; i < this.inputs; i++) {
                    threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        trainIsFinished.setValue(true);

    }

    public Thread train(int index, AIConfig config){
        Thread thread = new Thread(() -> {
            predictionInputEnded[index].setValue(false);
            config.updateConfig("INPUT.NODES", String.valueOf(windowsSize));
            config.updateConfig("OUTPUT.NODES", "1");

            try {
                trainer[index].initialise(config, this.dataKeeper, null);
                if(dataForWindow[index] == null) dataForWindow[index] = prepareDataForWindow(index, dataKeeper);

                inputThreads[index] = new WindowTrainThread(index, trainer[index], config, dataForWindow[index]);

                inputThreads[index].startTraining();
                predictFactorSign(index, config);

                predictionInputEnded[index].setValue(true);

            } catch (InitialisationFailedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }

    private void predictFactorSign(int index, AIConfig config) throws InitialisationFailedException {
        //List<Chromosome> bestEverChromosomes = ;
        Chromosome bestChromosome = trainer[index].getBestEverChromosomes().get(trainer[index].getBestEverChromosomes().size()-1);
        List<Double> inputs = new ArrayList<>(windowsSize+yearPrediction);

        for (int i = this.dataKeeper.getData().size()-windowsSize; i < this.dataKeeper.getData().size(); i++) {
            inputs.add(this.dataKeeper.getData().get(i).get(index));

        }

        NEATNeuralNet neatNeuralNet = this.initNet(config, bestChromosome);
        //double[] inputPattern = new double[windowsSize];
        NetworkInput input = null;
        NetworkOutputSet execute = null;
        Double[] inputTemp  = new Double[windowsSize];
        for (int i = 0; i < yearPrediction; i++) {
            for (int j = 0; j < windowsSize; j++) {
                inputTemp[j] = inputs.get(i+j);
            }
            input = new InputImpl(inputTemp);
            execute = neatNeuralNet.execute(input);
            inputs.add(execute.nextOutput().getNetOutputs().get(0));
            predictedInputDatas[i][index] = execute.nextOutput().getNetOutputs().get(0);
        }

        for (int i = 0 ; i < bestChromosome.getOutputValues().size() ; i++) {
            predictedWindowDatas[i][index] = bestChromosome.getOutputValues().get(i).get(0);
        }
    }

    public List<Double> getPredictedInputs(int index){
        List<Double> list = new ArrayList<>(predictedInputDatas.length);
        for (int j = 0; j < predictedInputDatas.length; j++) {
            list.add(predictedWindowDatas[j][index]);
        }
        return list;
    }



    public void predict(AIConfig config) throws InitialisationFailedException, IOException, ClassNotFoundException {


        Chromosome chromo = (Chromosome) NEATChromosome.readObject(config.configElement("AI.SOURCE"));
        int configInputs = Integer.parseInt(config.configElement("INPUT.NODES"));
        int configOutputs = Integer.parseInt(config.configElement("OUTPUT.NODES"));
        if(configInputs != dataKeeper.getInputs() || configOutputs != dataKeeper.getOutputs()){
            throw new IllegalArgumentException("Data and Model Mismatch!\n Trained model: Inputs = " +configInputs + " Outputs = " + configOutputs + "\n" +
                    "Data: Inputs = " +dataKeeper.getInputs() + " Outputs = " + dataKeeper.getOutputs());
        }
        NEATNeuralNet neatNeuralNet = this.initNet(config, chromo);
        predict(neatNeuralNet);


    }

    private void predict(NEATNeuralNet neatNeuralNet) throws InitialisationFailedException {

        predictionOutputEnded.setValue(false);

        for (int i = 0; i < predictedInputDatas.length; i++) {
            for (int j = 0; j < predictedInputDatas[i].length; j++) {
                if(predictedInputDatas[i][j] == null || predictedWindowDatas[i][j] == null){
                    throw new InitialisationFailedException("WindowPrediction model should be trained firstly");
                }
            }
        }

        NetworkInput input = null;
        NetworkOutputSet os = null;
        this.outputData = new ArrayList<>(predictedInputDatas.length + yearPrediction + yearPrediction);

        double error = 0;
        int n = 0;
        List<Double> netOutputs = null;


        for (int i = 0; i < yearPrediction; i++) {
            input = new InputImpl(dataKeeper.getData().get(i));
            os = neatNeuralNet.execute(input);
            netOutputs = os.nextOutput().getNetOutputs();
            this.outputData.add(netOutputs);
            for (int j = 0; j < netOutputs.size(); j++) {
                error += Math.pow(netOutputs.get(j)-dataKeeper.getData().get(i+windowsSize).get(dataKeeper.getInputs()+j), 2);
                n++;
            }
        }


        for (int i = 0; i < predictedWindowDatas.length; i++) {
            input = new InputImpl(predictedWindowDatas[i]);
            os = neatNeuralNet.execute(input);
            netOutputs = os.nextOutput().getNetOutputs();
            this.outputData.add(netOutputs);
            for (int j = 0; j < netOutputs.size(); j++) {
                error += Math.pow(netOutputs.get(j)-dataKeeper.getData().get(i+windowsSize).get(dataKeeper.getInputs()+j), 2);
                n++;
            }
        }
        error = Math.sqrt(error/n);
        this.predictionError = error;


        for (int i = 0; i < yearPrediction; i++) {
            input = new InputImpl(predictedInputDatas[i]);
            os = neatNeuralNet.execute(input);
            this.outputData.add(os.nextOutput().getNetOutputs());
        }

        predictionOutputEnded.setValue(true);
    }


    /*public void predict(Chromosome trainedModel, AIConfig config) throws InitialisationFailedException {
        int confInputs = Integer.parseInt(config.configElement("INPUT.NODES"));
        int confOutputs = Integer.parseInt(config.configElement("OUTPUT.NODES"));

        if(confInputs != dataKeeper.getInputs() || confOutputs != dataKeeper.getOutputs()){
            throw new IllegalArgumentException("Data and Model Mismatch!\n Trained model: Inputs = " +confInputs + " Outputs = " + confInputs + "\n" +
                    "Data: Inputs = " +dataKeeper.getInputs() + " Outputs = " + dataKeeper.getOutputs());
        }

        NEATNeuralNet neatNeuralNet = this.initNet(config, trainedModel);
        predict(neatNeuralNet);

    }*/



    public List<List<Double>> getOutputData() {
        return outputData;
    }

    public Boolean getTrainIsFinished() {
        return trainIsFinished.get();
    }

    public SimpleObjectProperty<Boolean> trainIsFinishedProperty() {
        return trainIsFinished;
    }




    protected NEATNeuralNet initNet(AIConfig config, Chromosome chromo) throws InitialisationFailedException {
        NeuralNet net = null;
        // need to create a nn based on this chromo.
        net = this.createNet(config);
        ((NEATNetDescriptor)(net.netDescriptor())).updateStructure(chromo);
        ((NEATNeuralNet)net).updateNetStructure();
        return (NEATNeuralNet) net;
    }

    protected NeuralNet createNet(AIConfig config) throws InitialisationFailedException {
        NEATNetManager netManager = new NEATNetManager();
        netManager.initialise(config, false);
        return ((NEATNeuralNet)netManager.managedNet());
    }

    public SimpleObjectProperty<Boolean> getInputPredictionEndedProperty(int index){
        return this.predictionInputEnded[index];
    }

    public Double getPredictionError() {
        return predictionError;
    }

    public DataKeeper getDataKeeper() {
        return dataKeeper;
    }
}
