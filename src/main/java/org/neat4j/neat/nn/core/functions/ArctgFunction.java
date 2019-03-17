package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;


/**
 * @author Filippov
 */

public class ArctgFunction extends ActivationFunctionImpl {

    public ArctgFunction(double factor) { this.factor = factor; }

    public ArctgFunction() { this.factor = 1; }

    @Override
    public double activate(double neuronIp) {
        return Math.atan(neuronIp*this.factor);
    }

    @Override
    public double derivative(double neuronIp) {

        return this.factor / (Math.pow(this.factor, 2)*Math.pow(neuronIp,2)+1);
    }

    @Override
    public ActivationFunction newInstance() {
        return new ArctgFunction(this.factor);
    }

    public static String getStaticFunctionName(){
        return "arctg(x)";
    }

    public String getFunctionName(){
        return "arctg(x)";
    }
}
