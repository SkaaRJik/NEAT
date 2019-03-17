package org.neat4j.neat.data.normaliser;

import java.util.List;

public interface DataScaler {
    List<List<Double>> normalize(List<List<Double>> dataToNormalize);
    List<List<Double>> normalize(List<List<Double>> dataToNormalize, double minRange, double maxRange);
}
