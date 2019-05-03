package org.neat4j.neat.data.normalizator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.data.normaliser.DataScaler;
import org.neat4j.neat.data.normaliser.LinearScaler;
import org.neat4j.neat.data.utilsdata.DataCreator;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;

import java.util.List;

class LinearScalerTest {

    @Test
    void normalize() {
        List<List<Double>> data = DataCreator.getRandomDataSet(5,5, 1000);
        DataScaler dataScaler = new LinearScaler();
        DataKeeper normalized = dataScaler.normalise(data, 0.0, 1.0);
        for (List<Double> rows : normalized.getData()){
            for(Double value : rows){
                Assertions.assertTrue(value <= 1);
            }
        }
    }

    @Test
    void foo(){
        double x = 0.5;
        double bar = (Math.exp(-x) / Math.pow(Math.exp(-x)+1, 2));

        Assertions.assertEquals(new SigmoidFunction().derivative(x), bar);
    }

    @Test
    void bar(){
        double a = Double.parseDouble("0,5");
    }
}