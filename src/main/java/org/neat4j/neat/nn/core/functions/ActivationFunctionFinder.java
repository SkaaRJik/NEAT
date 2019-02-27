package org.neat4j.neat.nn.core.functions;

import org.neat4j.neat.nn.core.ActivationFunction;

public class ActivationFunctionFinder {
    public  static String getFunctionClassNameByName(String name){
        if(ArctgFunction.getStaticFunctionName().equalsIgnoreCase(name)) return ArctgFunction.class.getName();
        if(LinearFunction.getStaticFunctionName().equalsIgnoreCase(name)) return LinearFunction.class.getName();
        if(SigmoidFunction.getStaticFunctionName().equalsIgnoreCase(name)) return SigmoidFunction.class.getName();
        if(TanhFunction.getStaticFunctionName().equalsIgnoreCase(name)) return TanhFunction.class.getName();
        return null;
    }
}
