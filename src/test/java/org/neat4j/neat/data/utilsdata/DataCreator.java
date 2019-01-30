package org.neat4j.neat.data.utilsdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataCreator {
    public static List<List<Double>> getRandomDataSet(int sizeI, int sizeJ, int range){
        List<List<Double>> data = new ArrayList<>(sizeI);
        List<Double> row;
        Random random = new Random(2341232141534221L);
        for(int i = 0 ; i < sizeI ; i ++){
            row = new ArrayList<Double>(sizeJ);
            for (int j = 0; j < sizeJ; j++) {
                row.add(Double.valueOf(random.nextInt(range)));
            }
            data.add(row);
        }
        return data;
    }

    public static List<List<Double>> get123DataSet(){
        List<List<Double>> data = new ArrayList<>(4);
        List<Double> row;
        for(int i = 0 ; i < 4 ; i ++){
            row = new ArrayList<Double>(3);
            for (int j = 0; j < 3; j++) {
                row.add(Double.valueOf(j+1));
            }
            data.add(row);
        }
        return data;
    }
}
