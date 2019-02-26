package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

/**
 * @author Filippov
 */


public abstract class ActivationFunctionImpl implements ActivationFunction {
    protected double factor;

    public ActivationFunctionImpl(double factor) {
        this.factor = factor;
    }

    public ActivationFunctionImpl() { }


}
