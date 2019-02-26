package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ActivationFunctionContainer {
   private static List<ActivationFunctionImpl> inputActivationFunctions = new ArrayList<>();
   private static List<ActivationFunctionImpl> hiddenActivationFunctions = new ArrayList<>();
   private static List<ActivationFunctionImpl> outputActivationFunctions = new ArrayList<>();

   public static void refresh(){
       inputActivationFunctions.clear();
       outputActivationFunctions.clear();
       hiddenActivationFunctions.clear();
   }

    public static List<ActivationFunctionImpl> getInputActivationFunctions() {
        return inputActivationFunctions;
    }

    public static List<ActivationFunctionImpl> getHiddenActivationFunctions() {
        return hiddenActivationFunctions;
    }

    public static List<ActivationFunctionImpl> getOutputActivationFunctions() {
        return outputActivationFunctions;
    }

    public static ActivationFunction getRandomInputActivationFunction(Random random){
        return inputActivationFunctions.get(random.nextInt(inputActivationFunctions.size())).newInstance();
    }

    public static ActivationFunction getRandomOutputActivationFunction(Random random){
        return outputActivationFunctions.get(random.nextInt(outputActivationFunctions.size())).newInstance();
    }

    public static ActivationFunction getRandomHiddenActivationFunction(Random random){
        return hiddenActivationFunctions.get(random.nextInt(hiddenActivationFunctions.size())).newInstance();
    }
}
