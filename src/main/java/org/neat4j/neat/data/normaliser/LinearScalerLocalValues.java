package org.neat4j.neat.data.normaliser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinearScalerLocalValues implements DataScaler {
    @Override
    public List<List<Double>> normalize(List<List<Double>> dataToNormalize) {


        Double[][] array = dataToNormalize.stream().map(doubles -> { return doubles.stream().toArray(Double[]::new);}).toArray(Double[][]::new);



        double min = 0;
        double max = 0;
        for (int i = 0; i < array[0].length; i++) {
            for(int j = 0 ; j < array.length; j++){
                if(j == 0){
                    min = array[j][i];
                    max = array[j][i];
                    continue;
                }
                if(array[j][i] != null) {
                    min = Double.min(min, array[j][i]);
                    max = Double.max(max, array[j][i]);
                }
            }

            for(int j = 0 ; j < array.length; j++) {
                if (array[j][i] != null) {
                    array[j][i] = (array[j][i] - min) / (max - min);
                } else {
                    array[j][i] = null;
                }
            }


        }
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        for (int i = 0; i < array.length; i++) {
            output.add(Arrays.asList(array[i]));
        }
        return output;
    }

    @Override
    public List<List<Double>> normalize(List<List<Double>> dataToNormalize, double minRange, double maxRange) {
        double min = 0;
        double max = 0;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
                if(i == 0 && j == 0){
                    min = dataToNormalize.get(i).get(j);
                    max = dataToNormalize.get(i).get(j);
                    continue;
                }
                if(dataToNormalize.get(i).get(j) != null) {
                    min = Double.min(min, dataToNormalize.get(i).get(j));
                    max = Double.max(max, dataToNormalize.get(i).get(j));
                }
            }
        }
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        List<Double> row;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            row = new ArrayList<Double>(dataToNormalize.get(i).size());
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
                if(dataToNormalize.get(i).get(j) == null) {
                    row.add(null);
                    continue;
                }
                if(minRange >= 0)
                    row.add(((dataToNormalize.get(i).get(j)-min)/(max-min))*(maxRange-minRange)+minRange);
                else
                    row.add(((dataToNormalize.get(i).get(j)-min)/(max-min))*(maxRange*2) + minRange);
            }
            output.add(row);
        }
        return output;
    }
}