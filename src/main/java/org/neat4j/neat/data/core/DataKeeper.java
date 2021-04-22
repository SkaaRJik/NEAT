package org.neat4j.neat.data.core;

import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.data.normaliser.DataScalerBase;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataKeeper implements Serializable{


    DataScalerBase dataScaler;
    List<List<Double>> data;


    List<String> headers;
    String legendHeader;
    List<Double> legend;
    int inputs;
    int outputs;
    Integer trainIndexEnd;


    public static DataKeeper loadDataset(File datasetName) throws IOException {

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(datasetName));
        try {
            return (DataKeeper) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            objectInputStream.close();
        }
        return null;

    }


    public DataKeeper(List<List<Double>> data, DataScalerBase dataScaler) {
        this.data = data;
        this.dataScaler = dataScaler;

    }





    public DataKeeper(List<List<Double>> data, List<String> headers, List<Double> legend) {
        this.data = data;
        this.headers = headers;
        this.legend = legend;
    }

    public DataKeeper(List<List<Double>> data, List<String> headers, String legendHeader, List<Double> legend) {
        this.data = data;
        this.headers = headers;
        this.legend = legend;
        this.legendHeader = legendHeader;
    }

    public List<List<Double>> getData() {
        return data;
    }

    public void setData(List<List<Double>> data) {
        this.data = data;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Double> getLegend() {
        return legend;
    }

    public void setLegend(List<Double> legend) {
        this.legend = legend;
    }

    public List<String> getHeadersForTableView(){
        List<String> newHeaders = new ArrayList<>(this.headers.size()+1);

        newHeaders.add(legendHeader);
        newHeaders.addAll(headers);
        return newHeaders;

    }

    public List<List<Double>> getDataForTableView(){
        List<List<Double>> dataForTableView = new ArrayList<>(this.data.size()+1);
        for (int i = 0 ; i < data.size(); i++) {
            List<Double> row = new ArrayList<>(data.get(i).size()+1);
            row.add(legend.get(i));
            for (int j = 0; j < data.get(i).size(); j++) {
                row.add(data.get(i).get(j));
            }
            dataForTableView.add(row);
        }
        return dataForTableView;
    }

    public String getLegendHeader() {
        return legendHeader;
    }

    public void setLegendHeader(String legendHeader) {
        this.legendHeader = legendHeader;
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


    public boolean writeDataIntoFile(File dest) throws IOException{

        boolean saveOk = false;
        ObjectOutputStream s = null;
        FileOutputStream out = null;
        try {
            if (dest != null)
                //System.out.println("Saving Best Chromosome to " + fileName);
                out = new FileOutputStream(dest);
            s = new ObjectOutputStream(out);
            s.writeObject(this);
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

    public void setTrainIndexEnd(Integer index){
        this.trainIndexEnd = index;

    }

    public DataScalerBase getDataScaler() {
        return dataScaler;
    }

    public List<List<Double>> getTrainData(){

        if(trainIndexEnd == null) return this.data;
        List<List<Double>> trainData = new ArrayList<>(trainIndexEnd);
        for (int i = 0; i < trainIndexEnd; i++) {
            trainData.add(this.data.get(i));
        }
        return trainData;
    }

    public List<List<Double>> getTestData(){
        if(trainIndexEnd == null) return null;
        List<List<Double>> testData =null;
        for (int i = trainIndexEnd; i < this.data.size(); i++) {
            if(i == trainIndexEnd) testData = new ArrayList<>(this.data.size() - trainIndexEnd);
            testData.add(this.data.get(i));
        }
        return testData;
    }


    public File saveSet(String filePath, List<List<Double>> set) throws IOException {
        if (set == null) return null;
        File file = new File(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
        Double value;
        for (int i = 0; i < this.headers.size(); i++) {
            bufferedWriter.write(this.headers.get(i));
            if(i != this.headers.size() - 1) bufferedWriter.write(";");
        }
        bufferedWriter.append("\n");
        for(List<Double> list : set){
            for (int i = 0; i < list.size(); i++) {
                value = list.get(i);

                bufferedWriter.write(String.valueOf(value));
                if(i != list.size()-1) bufferedWriter.write(";");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();
        return file;
    }

    public void calculateIndex(double percent){
        this.trainIndexEnd = (int) Math.round(this.data.size() * percent);
    }

    public DataKeeper createDataKeeperForChromosome(Chromosome chromosome) throws ExceptionInInitializerError{
        List<List<Double>> newData = new ArrayList<>(data.size());



        try {
            NeuralNet net = NEATNeuralNet.createNet(chromosome);
            for (int i = 0; i < data.size(); i++) {
                newData.add(new ArrayList<>(chromosome.getInputs()+getOutputs()));
            }
            List<String> newHeaders = new ArrayList<>(chromosome.getInputs()+getOutputs());


            putDataByLayers(newData, newHeaders, net);

            DataKeeper dataKeeper = new DataKeeper(newData, newHeaders, this.legendHeader, this.legend);
            dataKeeper.setInputs(net.inputLayer().size());
            dataKeeper.setOutputs(net.outputLayer().size());
            dataKeeper.setDataScaler(dataScaler);
            return dataKeeper;

        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void putDataByLayers(List<List<Double>> newData, List<String> newHeaders, NeuralNet neatNeurons) throws ExceptionInInitializerError{
        int counter = 0;
        for (int i = 0; i < this.inputs; i++) {
            for (int j = 0; j < neatNeurons.inputLayer().size(); j++) {
                if (this.headers.get(i).equals(neatNeurons.inputLayer().get(j).getLabel())) {
                    counter++;
                    for (int k = 0; k < data.size(); k++) {
                        newData.get(k).add(data.get(k).get(i));
                    }
                    newHeaders.add(this.headers.get(i));
                    break;
                }
            }
        }
        if(counter == 0) throw new ExceptionInInitializerError("Не соответствие моделей: \nНе удалось подобрать данные для входного слоя.");
        counter = 0;
        for (int i = this.inputs; i < this.headers.size(); i++) {
            for (int j = 0; j < neatNeurons.outputLayer().size(); j++) {
                if (this.headers.get(i).equals(neatNeurons.outputLayer().get(j).getLabel())) {
                    counter++;
                    for (int k = 0; k < data.size(); k++) {
                        newData.get(k).add(data.get(k).get(i));
                    }
                    newHeaders.add(this.headers.get(i));
                    break;
                }
            }
        }
        if(counter == 0) throw new ExceptionInInitializerError("Не соответствие моделей: \nНе удалось подобрать данные для выходного слоя.");
    }

    public void setDataScaler(DataScalerBase dataScaler) {
        this.dataScaler = dataScaler;
    }



    public DataKeeper denormaliseData(){
       return this.dataScaler.denormalise(this.getData());
    }



    public List<List<Double>> denormaliseColumns(List<List<Double>> column, List<Integer> columnIndexes){
        return this.dataScaler.denormaliseColumns(column, columnIndexes);
    }
}
