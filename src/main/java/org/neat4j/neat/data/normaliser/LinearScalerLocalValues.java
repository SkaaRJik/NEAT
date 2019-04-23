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

        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        List<Double> row = null;

        for (int i = 0; i < dataToNormalize.get(0).size(); i++) {
            for(int j = 0 ; j < dataToNormalize.size(); j++){
                if(i==0){
                    output.add(new ArrayList<>(dataToNormalize.get(0).size()));
                }
                if(j == 0){
                    min = dataToNormalize.get(j).get(i);
                    max = dataToNormalize.get(j).get(i);
                    continue;
                }
                if(dataToNormalize.get(j).get(i) != null) {
                    min = Double.min(min, dataToNormalize.get(j).get(i));
                    max = Double.max(max, dataToNormalize.get(j).get(i));
                }
            }

            for(int j = 0 ; j < dataToNormalize.size(); j++){
                if(dataToNormalize.get(j).get(i) == null) {
                    output.get(j).add(null);
                    continue;
                }
                if(minRange >= 0)
                    output.get(j).add(((dataToNormalize.get(j).get(i)-min)/(max-min))*(maxRange-minRange)+minRange);
                else
                    output.get(j).add(((dataToNormalize.get(j).get(i)-min)/(max-min))*(maxRange*2) + minRange);
            }

        }
        return output;
    }
}
