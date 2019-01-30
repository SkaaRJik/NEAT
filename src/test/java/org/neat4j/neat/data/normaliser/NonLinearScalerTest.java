package org.neat4j.neat.data.normaliser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neat4j.neat.data.utilsdata.DataCreator;
import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class NonLinearScalerTest {

    @Test
    void normalize() {
        List<List<Double>> data = DataCreator.getRandomDataSet(5,5, 1000);
        DataScaler dataScaler = new NonLinearScaler(new SigmoidFunction());
        List<List<Double>> normalized = dataScaler.normalize(data);
        for (List<Double> rows : normalized){
            for(Double value : rows){
                Assertions.assertTrue(value <= 1);
            }
        }
    }

    @Test
    void tanghDerivative(){
        ActivationFunction activationFunction = new TanhFunction(3);
        double x = 0.54;


        //Assertions.assertEquals(derivative(x), activationFunction.derivative(x));
        Assertions.assertEquals((3 * Math.exp(3 * x))/Math.pow(1.0 + Math.exp(-3 * x),2), Math.exp(-x*3)/Math.pow(1+Math.exp(-x*3),2));
    }

    @Test
    void TanghWithParametres(){
        ActivationFunction activationFunction = new TanhFunction(3);
        double x = 0.54;
        Assertions.assertEquals(activationFunction.activate(x), th(x));


    }

    public double th(double neuronIp){
        return (Math.exp(neuronIp/3)-Math.exp(-neuronIp/3))/(Math.exp(neuronIp/3)+Math.exp(-neuronIp/3));
    }

    public double derivative(double neuronIp){

        return 1/Math.pow((Math.exp(-neuronIp/3)+Math.exp(neuronIp/3)),2)
                *(Math.exp(neuronIp/3)-Math.exp(-neuronIp/3))
                *(Math.exp(-neuronIp/3)/3-Math.exp(neuronIp/3)/3)
                +((Math.exp(-neuronIp/3)/3-Math.exp(neuronIp/3)/3)/((Math.exp(-neuronIp/3)+Math.exp(neuronIp/3))));
    }
}