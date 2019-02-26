package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

public class SinFunction extends ActivationFunctionImpl {

    public SinFunction(double factor) {
        super(factor);
    }

    @Override
    public double activate(double neuronIp) {
        return 0;
    }

    @Override
    public double derivative(double neuronIp) {
        return 0;
    }

    @Override
    public ActivationFunction newInstance() {
        return new SinFunction(this.factor);
    }


}
