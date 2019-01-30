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
        return 0;
    }

    @Override
    public double derivative(double neuronIp) {
        return 0;
    }
}
