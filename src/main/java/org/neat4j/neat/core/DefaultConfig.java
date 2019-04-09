package org.neat4j.neat.core;

import java.util.ArrayList;
import java.util.List;

public class DefaultConfig {
    static private List<String> defaultConfig;

    public static  List<String> getDefaultConfig(){
        if(defaultConfig == null){
            defaultConfig = new ArrayList<>(50);
            defaultConfig.add("PROBABILITY.MUTATION=0.25");
            defaultConfig.add("PROBABILITY.CROSSOVER=0.5");
            defaultConfig.add("PROBABILITY.ADDLINK=0.1");
            defaultConfig.add("PROBABILITY.ADDNODE=0.03");
            defaultConfig.add("PROBABILITY.NEWACTIVATIONFUNCTION=0.1");
            defaultConfig.add("PROBABILITY.MUTATEBIAS=0.3");
            defaultConfig.add("PROBABILITY.TOGGLELINK=0.1");
            defaultConfig.add("PROBABILITY.WEIGHT.REPLACED=0.0");
            defaultConfig.add("GENERATOR.SEED=1548235723799");
            defaultConfig.add("EXCESS.COEFFICIENT=1");
            defaultConfig.add("DISJOINT.COEFFICIENT=1");
            defaultConfig.add("WEIGHT.COEFFICIENT=0.4");
            defaultConfig.add("COMPATABILITY.THRESHOLD=0.5");
            defaultConfig.add("COMPATABILITY.CHANGE=0.1");
            defaultConfig.add("SPECIE.COUNT=3");
            defaultConfig.add("SURVIVAL.THRESHOLD=0.2");
            defaultConfig.add("SPECIE.AGE.THRESHOLD=80");
            defaultConfig.add("SPECIE.YOUTH.THRESHOLD=10");
            defaultConfig.add("SPECIE.OLD.PENALTY=1.2");
            defaultConfig.add("SPECIE.YOUTH.BOOST=0.7");
            defaultConfig.add("SPECIE.FITNESS.MAX=15");
            defaultConfig.add("MAX.PERTURB=0.5");
            defaultConfig.add("MAX.BIAS.PERTURB=0.1");
            defaultConfig.add("FEATURE.SELECTION=false");
            defaultConfig.add("RECURRENCY.ALLOWED=false");
            defaultConfig.add("INPUT.ACTIVATIONFUNCTIONS=org.neat4j.neat.nn.core.functions.LinearFunction");
            defaultConfig.add("OUTPUT.ACTIVATIONFUNCTIONS=org.neat4j.neat.nn.core.functions.SigmoidFunction");
            defaultConfig.add("HIDDEN.ACTIVATIONFUNCTIONS=org.neat4j.neat.nn.core.functions.TanhFunction");
            defaultConfig.add("ELE.EVENTS=false");
            defaultConfig.add("ELE.SURVIVAL.COUNT=0.1");
            defaultConfig.add("ELE.EVENT.TIME=1000");
            defaultConfig.add("KEEP.BEST.EVER=true");
            defaultConfig.add("EXTRA.FEATURE.COUNT=0");
            defaultConfig.add("POP.SIZE=150");
            defaultConfig.add("NUMBER.EPOCHS=100");
            defaultConfig.add("TERMINATION.VALUE.TOGGLE=false");
            defaultConfig.add("TERMINATION.VALUE=0.00001");
            defaultConfig.add("NATURAL.ORDER.STRATEGY=true");
        }
        return defaultConfig;
    }


}
