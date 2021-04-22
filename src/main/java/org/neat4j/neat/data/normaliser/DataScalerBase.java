package org.neat4j.neat.data.normaliser;

import java.util.ArrayList;
import java.util.List;

public abstract class DataScalerBase implements DataScaler {
    Boolean enableLogTransform;

    @Override
    public List<List<Double>> logTransform(List<List<Double>> dataToNormalize) {
        List<List<Double>> logData = new ArrayList<>(dataToNormalize.size());

        for (List<Double> row : dataToNormalize){
            ArrayList<Double> logRow = new ArrayList<>(row.size());
            logData.add(logRow);
            for(Double value : row){
                if(value <= 0){
                    return dataToNormalize;
                }
                logRow.add(Math.log(value));
            }
        }

        return logData;
    }

    @Override
    public List<List<Double>> expTransform(List<List<Double>> logTransformedData) {
        List<List<Double>> data = new ArrayList<>(logTransformedData.size());

        for (List<Double> row : logTransformedData){
            ArrayList<Double> logRow = new ArrayList<>(row.size());
            data.add(logRow);
            for(Double value : row){
                logRow.add(Math.exp(value));
            }
        }

        return data;
    }
}
