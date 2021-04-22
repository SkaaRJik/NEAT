package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.data.core.DataKeeper;

import java.util.ArrayList;
import java.util.List;

public class LinearScaler extends DataScalerBase {

    List<Double> mins;
    List<Double> maxs;
    Double d1;
    Double d2;

    @Override
    public DataKeeper normalise(List<List<Double>> dataToNormalize, double minRange, double maxRange, boolean enableLogTransform) {

        List<List<Double>> data = dataToNormalize;
        this.enableLogTransform = enableLogTransform;
        
        if(this.enableLogTransform){
            data = this.logTransform(data);
            if(data == dataToNormalize){
                this.enableLogTransform = false;
            }
        }
        
        double min = 0;
        double max = 0;
        d1 = minRange;
        d2 = maxRange;
        List<List<Double>> output = new ArrayList<>(data.size());
        mins = new ArrayList<>(data.get(0).size());
        maxs = new ArrayList<>(data.get(0).size());
        for (int i = 0; i < data.get(0).size(); i++) {
            for(int j = 0 ; j < data.size(); j++){
                if(i==0){
                    output.add(new ArrayList<>(data.get(0).size()));
                }
                if(j == 0){
                    if(data.get(j).get(i) != null) {
                        min = data.get(j).get(i);
                        max = data.get(j).get(i);
                    }
                    continue;
                }
                if(data.get(j).get(i) != null) {
                    min = Double.min(min, data.get(j).get(i));
                    max = Double.max(max, data.get(j).get(i));
                }
            }
            mins.add(min);
            maxs.add(max);
            for(int j = 0 ; j < data.size(); j++){
                if(data.get(j).get(i) == null) {
                    output.get(j).add(null);
                    continue;
                }
                if(minRange >= 0)
                    output.get(j).add(((data.get(j).get(i)-min)/(max-min))*(maxRange-minRange)+minRange);
                else
                    output.get(j).add(((data.get(j).get(i)-min)/(max-min))*(maxRange*2) + minRange);
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
                if(dataToNormalize.get(j).get(i) == null) {
                    output.get(j).add(null);
                    continue;
                }
                output.get(j).add(((dataToNormalize.get(j).get(i)-d1)*(maxs.get(i)-mins.get(i)))/(d2-d1)+mins.get(i));
                //output.get(j).add(((dataToNormalize.get(j).get(i)-mins.get(i))*(max-min))/(maxs.get(i)-mins.get(i))+min);
                //output.get(j).add(((dataToNormalize.get(j).get(i)-min)/(max-min))*(maxs.get(i)-mins.get(i))+mins.get(i));
            }
        }

        if(this.enableLogTransform){
            output = this.expTransform(output);
        }

        return new DataKeeper(output, this);
    }

    @Override
    public List<List<Double>> denormaliseColumns(List<List<Double>> columns, List<Integer> columnIndexes) {
        double min = 0;
        double max = 0;
        List<List<Double>> output = new ArrayList<>(columns.size());
        for (int i = 0; i < columns.get(0).size(); i++) {
            for(int j = 0; j < columns.size(); j++){
                if(i==0){
                    output.add(new ArrayList<>(columns.get(0).size()));
                }
                if(j == 0){
                    if(columns.get(j).get(i) != null) {
                        min = columns.get(j).get(i);
                        max = columns.get(j).get(i);
                    }
                    continue;
                }
                if(columns.get(j).get(i) != null) {
                    min = Double.min(min, columns.get(j).get(i));
                    max = Double.max(max, columns.get(j).get(i));
                }
            }
            for(int j = 0 ; j < columns.size(); j++){
                if(columns.get(j).get(i) == null) {
                    output.get(j).add(null);
                    continue;
                }
                output.get(j).add(((columns.get(j).get(i)-d1)*(maxs.get(columnIndexes.get(i))-mins.get(columnIndexes.get(i))))/(d2-d1)+mins.get(columnIndexes.get(i)));
                //output.get(j).add(((columns.get(j).get(i)-mins.get(columnIndexes.get(i)))*(max-min))/(maxs.get(columnIndexes.get(i))-mins.get(columnIndexes.get(i)))+min);
                //output.get(j).add(((columns.get(j).get(i)-min)/(max-min))*(maxs.get(columnIndexes.get(i))-mins.get(columnIndexes.get(i)))+mins.get(columnIndexes.get(i)));
            }
        }
        if(this.enableLogTransform){
            output = this.expTransform(output);
        }
        return output;
    }


}
