package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.nn.core.ActivationFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NonLinearScaler implements DataScaler {
    ActivationFunction activationFunction;

    public NonLinearScaler(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

    @Override
    public List<List<Double>> normalize(List<List<Double>> dataToNormalize) {
        int n = dataToNormalize.size() * dataToNormalize.get(0).size();
        double averrage = dataToNormalize.stream()
                .flatMap(List::stream)
                .mapToDouble(Double::doubleValue)
                .sum() / (n);

        double disp = Math.sqrt(dataToNormalize.stream().flatMap(List::stream).mapToDouble(aDouble -> Math.pow((aDouble - averrage), 2)).sum() / (n-1));
        /*List<List<Double>> normalised = dataToNormalize
                .stream()
                .flatMap(List::stream)
                .flatMapToDouble(aDouble -> DoubleStream.of((aDouble-averrage)/disp))
                .collect(() -> new ArrayList<>(), (lists, value) -> lists.add(new ArrayList<>()), (lists, lists2) -> lists.add(lists2));*/
        List<List<Double>> normalised = new ArrayList<>(dataToNormalize.size());
        List<Double> row;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            row = new ArrayList<Double>(dataToNormalize.get(i).size());
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
                row.add(activationFunction.activate((dataToNormalize.get(i).get(j)-averrage)/disp));
            }
            normalised.add(row);
        }

        return normalised;
    }
}
