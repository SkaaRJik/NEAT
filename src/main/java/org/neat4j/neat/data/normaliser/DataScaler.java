package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.data.core.DataKeeper;

import java.io.Serializable;
import java.util.List;

public interface DataScaler extends Serializable {
    DataKeeper normalise(List<List<Double>> dataToNormalize, double minRange, double maxRange);
    DataKeeper denormalise(List<List<Double>> dataToNormalize);

    List<List<Double>> denormaliseColumns(List<List<Double>> column, List<Integer> columnIndexes);
}
