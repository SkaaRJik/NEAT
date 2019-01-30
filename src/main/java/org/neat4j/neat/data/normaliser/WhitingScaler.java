package org.neat4j.neat.data.normaliser;

import java.util.ArrayList;
import java.util.List;

public class WhitingScaler implements DataScaler {
    @Override
    public List<List<Double>> normalize(List<List<Double>> dataToNormalize) {
        int columns = dataToNormalize.get(0).size();
        int rows = dataToNormalize.size();
        List<Double>[] average = new List[2];
        average[0] = new ArrayList<>(columns);
        average[1] = new ArrayList<>(rows);
        double sum;
        //Count average value of each columns
        for (int i = 0; i < dataToNormalize.get(0).size(); i++) {
            sum = 0;
            for (int j = 0; j < dataToNormalize.size(); j++) {
                sum += dataToNormalize.get(j).get(i);
            }
            average[0].add(sum/dataToNormalize.size());
        }
        //Count average value of each rows
        double totalSum = 0;
        for (int i = 0; i < dataToNormalize.size(); i++) {
            sum = 0;
            for (int j = 0; j < dataToNormalize.get(i).size(); j++) {
                sum += dataToNormalize.get(i).get(j);
            }
            average[1].add(sum/dataToNormalize.size());
            for (int j = 0; j < dataToNormalize.get(i).size(); j++) {
                totalSum += (dataToNormalize.get(i).get(j) - average[1].get(i))*(dataToNormalize.get(i).get(j) - average[0].get(j));
            }
            totalSum /= (columns*rows-1);
        }
        return null;




    }
}
