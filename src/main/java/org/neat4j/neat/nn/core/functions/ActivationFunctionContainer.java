package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

import java.util.List;
import java.util.Random;

public class ActivationFunctionContainer {
   private  List<ActivationFunctionImpl> inputActivationFunctions;
   private  List<ActivationFunctionImpl> hiddenActivationFunctions;
   private  List<ActivationFunctionImpl> outputActivationFunctions;

    public void setInputActivationFunctions(List<ActivationFunctionImpl> inputActivationFunctions) {
        this.inputActivationFunctions = inputActivationFunctions;
    }

    public void setHiddenActivationFunctions(List<ActivationFunctionImpl> hiddenActivationFunctions) {
        this.hiddenActivationFunctions = hiddenActivationFunctions;
    }

    public void setOutputActivationFunctions(List<ActivationFunctionImpl> outputActivationFunctions) {
        this.outputActivationFunctions = outputActivationFunctions;
    }

    public List<ActivationFunctionImpl> getInputActivationFunctions() {
        return inputActivationFunctions;
    }

    public List<ActivationFunctionImpl> getHiddenActivationFunctions() {
        return hiddenActivationFunctions;
    }

    public List<ActivationFunctionImpl> getOutputActivationFunctions() {
        return outputActivationFunctions;
    }

    public ActivationFunction getRandomInputActivationFunction(Random random){
        return inputActivationFunctions.get(random.nextInt(inputActivationFunctions.size())).newInstance();
    }

    public ActivationFunction getRandomOutputActivationFunction(Random random){
        return outputActivationFunctions.get(random.nextInt(outputActivationFunctions.size())).newInstance();
    }

    public ActivationFunction getRandomHiddenActivationFunction(Random random){
        return hiddenActivationFunctions.get(random.nextInt(hiddenActivationFunctions.size())).newInstance();
    }
}
