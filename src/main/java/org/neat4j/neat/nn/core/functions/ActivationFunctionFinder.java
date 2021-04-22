package org.neat4j.neat.nn.core.functions;

public class ActivationFunctionFinder {
    public  static String getFunctionClassNameByName(String name){
        if(ArctgFunction.getStaticFunctionName().equalsIgnoreCase(name)) return ArctgFunction.class.getName();
        if(LinearFunction.getStaticFunctionName().equalsIgnoreCase(name)) return LinearFunction.class.getName();
        if(SigmoidFunction.getStaticFunctionName().equalsIgnoreCase(name)) return SigmoidFunction.class.getName();
        if(TanhFunction.getStaticFunctionName().equalsIgnoreCase(name)) return TanhFunction.class.getName();
        return null;
    }


    public  static String getFunctionNameByClassName(String name){
        if(ArctgFunction.class.getName().equalsIgnoreCase(name)) return ArctgFunction.getStaticFunctionName();
        if(LinearFunction.class.getName().equalsIgnoreCase(name)) return LinearFunction.getStaticFunctionName();
        if(SigmoidFunction.class.getName().equalsIgnoreCase(name)) return SigmoidFunction.getStaticFunctionName();
        if(TanhFunction.class.getName().equalsIgnoreCase(name)) return TanhFunction.getStaticFunctionName();
        return null;
    }

    public static String getConcatFunctions(String functions){
        StringBuilder stringBuilder = new StringBuilder();
        String[] split = functions.split(";");
        for (int i = 0; i < split.length; i++) {
            stringBuilder.append(ActivationFunctionFinder.getFunctionNameByClassName(split[i]));
            if(i+1 < split.length)
                stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }

}
