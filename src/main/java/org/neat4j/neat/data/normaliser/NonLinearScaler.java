package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.nn.core.ActivationFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NonLinearScaler implements DataScaler {
    ActivationFunction activationFunction;
    Double minRange;
    Double maxRange;
    List<Double> averages;
    List<Double> disps;


    public NonLinearScaler(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

    @Override
    public DataKeeper normalise(List<List<Double>> dataToNormalize, double minRange, double maxRange) {
        Double[][] dataArray = dataToNormalize.stream().map(doubles -> doubles.stream().toArray(Double[]::new)).toArray(Double[][]::new);
        Double[][] normalisedDataArray = new Double[dataArray.length][dataArray[0].length];

        this.averages = new ArrayList<>(dataToNormalize.get(0).size());
        this.disps = new ArrayList<>(dataToNormalize.get(0).size());
        this.minRange = minRange;
        this.maxRange = maxRange;

        double average = 0;
        double disp = 0;
        int countNull = 0;
        for (int j = 0; j < dataArray[0].length; j++) {

            average = 0;
            countNull = 0;
            for (int i = 0; i < dataArray.length; i++) {
                if(dataArray[i][j]!=null) {
                    average += dataArray[i][j];
                } else {
                    countNull++;
                }
            }
            average /= (dataArray.length-countNull);
            this.averages.add(average);

            disp = 0;
            for (int i = 0; i < dataArray.length; i++) {
                if(dataArray[i][j]!=null) {
                    disp += Math.pow(dataArray[i][j] - average, 2);
                }
            }
            disp = Math.sqrt(disp/(dataArray.length-countNull-1));
            disps.add(disp);
            for (int i = 0; i < dataArray.length; i++) {
                if(dataArray[i][j]!=null) {
                    normalisedDataArray[i][j] = activationFunction.activate((dataArray[i][j] - average) / disp) * (maxRange - minRange) + minRange;
                } else {
                    normalisedDataArray[i][j] = null;
                }
            }
        }

        List<List<Double>> normalised = Arrays.stream(normalisedDataArray).map(doubles -> Arrays.stream(doubles).collect(Collectors.toList())).collect(Collectors.toList());

        return new DataKeeper(normalised, this);
    }

    @Override
    public DataKeeper denormalise(List<List<Double>> dataToNormalize) {
        return null;
    }

    @Override
    public List<List<Double>> denormaliseColumns(List<List<Double>> column, List<Integer> columnIndexes) {
        return null;
    }


}
