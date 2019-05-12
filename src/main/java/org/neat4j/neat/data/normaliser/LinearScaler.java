package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.data.core.DataKeeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinearScaler implements DataScaler {

    List<Double> mins;
    List<Double> maxs;

    @Override
    public DataKeeper normalise(List<List<Double>> dataToNormalize, double minRange, double maxRange) {
        double min = 0;
        double max = 0;
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        mins = new ArrayList<>(dataToNormalize.get(0).size());
        maxs = new ArrayList<>(dataToNormalize.get(0).size());
        for (int i = 0; i < dataToNormalize.get(0).size(); i++) {
            for(int j = 0 ; j < dataToNormalize.size(); j++){
                if(i==0){
                    output.add(new ArrayList<>(dataToNormalize.get(0).size()));
                }
                if(j == 0){
                    if(dataToNormalize.get(j).get(i) != null) {
                        min = dataToNormalize.get(j).get(i);
                        max = dataToNormalize.get(j).get(i);
                    }
                    continue;
                }
                if(dataToNormalize.get(j).get(i) != null) {
                    min = Double.min(min, dataToNormalize.get(j).get(i));
                    max = Double.max(max, dataToNormalize.get(j).get(i));
                }
            }
            mins.add(min);
            maxs.add(max);
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

        return new DataKeeper(output, this);
    }

    @Override
    public DataKeeper denormalise(List<List<Double>> dataToNormalize) {
        double min = 0;
        double max = 0;
        List<List<Double>> output = new ArrayList<>(dataToNormalize.size());
        for (int i = 0; i < dataToNormalize.get(0).size(); i++) {
            for(int j = 0 ; j < dataToNormalize.size(); j++){
                if(i==0){
                    output.add(new ArrayList<>(dataToNormalize.get(0).size()));
                }
                if(j == 0){
                    if(dataToNormalize.get(j).get(i) != null) {
                        min = dataToNormalize.get(j).get(i);
                        max = dataToNormalize.get(j).get(i);
                    }
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

                output.get(j).add(((dataToNormalize.get(j).get(i)-min)/(max-min))*(maxs.get(i)-mins.get(i))+mins.get(i));

            }

        }

        return new DataKeeper(output, this);
    }

}
