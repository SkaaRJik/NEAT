package org.neat4j.neat.core;

import org.neat4j.core.AIConfig;

public class DefaultConfig {
    static private AIConfig defaultConfig;

    public static  AIConfig getDefaultConfig(){
        
        
        
        
        if(defaultConfig == null){
            defaultConfig = new NEATConfig();
            defaultConfig.updateConfig("PROBABILITY.MUTATION", "0.25");
            defaultConfig.updateConfig("PROBABILITY.CROSSOVER", "0.5");
            defaultConfig.updateConfig("PROBABILITY.ADDLINK", "0.1");
            defaultConfig.updateConfig("PROBABILITY.ADDNODE", "0.03");
            defaultConfig.updateConfig("PROBABILITY.NEWACTIVATIONFUNCTION", "0.1");
            defaultConfig.updateConfig("PROBABILITY.MUTATEBIAS", "0.3");
            defaultConfig.updateConfig("PROBABILITY.TOGGLELINK", "0.1");
            defaultConfig.updateConfig("PROBABILITY.WEIGHT.REPLACED", "0.05");
            defaultConfig.updateConfig("GENERATOR.SEED", "1548235723799");
            defaultConfig.updateConfig("EXCESS.COEFFICIENT", "1");
            defaultConfig.updateConfig("DISJOINT.COEFFICIENT", "1");
            defaultConfig.updateConfig("WEIGHT.COEFFICIENT", "0.4");
            defaultConfig.updateConfig("COMPATABILITY.THRESHOLD", "0.5");
            defaultConfig.updateConfig("COMPATABILITY.CHANGE", "0.1");
            defaultConfig.updateConfig("SPECIE.COUNT", "3");
            defaultConfig.updateConfig("SURVIVAL.THRESHOLD", "0.2");
            defaultConfig.updateConfig("SPECIE.AGE.THRESHOLD", "80");
            defaultConfig.updateConfig("SPECIE.YOUTH.THRESHOLD", "10");
            defaultConfig.updateConfig("SPECIE.OLD.PENALTY", "1.2");
            defaultConfig.updateConfig("SPECIE.YOUTH.BOOST", "0.7");
            defaultConfig.updateConfig("SPECIE.FITNESS.MAX", "15");
            defaultConfig.updateConfig("MAX.PERTURB", "0.5");
            defaultConfig.updateConfig("MAX.BIAS.PERTURB", "0.1");
            defaultConfig.updateConfig("FEATURE.SELECTION", "false");
            defaultConfig.updateConfig("RECURRENCY.ALLOWED", "false");
            defaultConfig.updateConfig("INPUT.ACTIVATIONFUNCTIONS", "org.neat4j.neat.nn.core.functions.LinearFunction");
            defaultConfig.updateConfig("OUTPUT.ACTIVATIONFUNCTIONS", "org.neat4j.neat.nn.core.functions.SigmoidFunction");
            defaultConfig.updateConfig("HIDDEN.ACTIVATIONFUNCTIONS", "org.neat4j.neat.nn.core.functions.TanhFunction");
            defaultConfig.updateConfig("ELE.EVENTS", "false");
            defaultConfig.updateConfig("ELE.SURVIVAL.COUNT", "0.1");
            defaultConfig.updateConfig("ELE.EVENT.TIME", "1000");
            defaultConfig.updateConfig("KEEP.BEST.EVER", "true");
            defaultConfig.updateConfig("EXTRA.FEATURE.COUNT", "0");
            defaultConfig.updateConfig("POP.SIZE", "150");
            defaultConfig.updateConfig("NUMBER.EPOCHS", "100");
            defaultConfig.updateConfig("TERMINATION.VALUE.TOGGLE", "false");
            defaultConfig.updateConfig("TERMINATION.VALUE", "0.00001");
            defaultConfig.updateConfig("NATURAL.ORDER.STRATEGY", "true");
        }
        return defaultConfig;
    }


}
