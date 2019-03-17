package org.neat4j.neat.data.normaliser;

import java.util.ArrayList;
import java.util.List;

public class LinearScaler implements DataScaler {
    @Override
    public List<List<Double>> normalize(List<List<Double>> dataToNormalize) {
        double min = 0;
        double max = 0;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
                if(i == 0 && j == 0){
                    min = dataToNormalize.get(i).get(j);
                    max = dataToNormalize.get(i).get(j);
                    continue;
                }
                min = Double.min(min, dataToNormalize.get(i).get(j));
                max = Double.max(max, dataToNormalize.get(i).get(j));
            }
        }
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        List<Double> row;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            row = new ArrayList<Double>(dataToNormalize.get(i).size());
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
               row.add((dataToNormalize.get(i).get(j)-min)/(max-min));
            }
            output.add(row);
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
                min = Double.min(min, dataToNormalize.get(i).get(j));
                max = Double.max(max, dataToNormalize.get(i).get(j));
            }
        }
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        List<Double> row;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            row = new ArrayList<Double>(dataToNormalize.get(i).size());
            for(int j = 0 ; j < dataToNormalize.get(i).size(); j++){
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
