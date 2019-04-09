package org.neat4j.neat.applications.test;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.neat4j.neat.data.core.*;

import java.util.ArrayList;
import java.util.List;

public class NEATPredictionEngineForJavaFX extends MSENEATPredictionEngine implements Runnable {

    ObservableList<List<Double>> outs;
    ListProperty<List<Double>> outsProperty;

    public NEATPredictionEngineForJavaFX() {
        outs = FXCollections.observableArrayList();
        this.outsProperty = new SimpleListProperty<>(outs);
    }

    @Override
    public void run() {
        startTesting();
    }

    @Override
    public void startTesting() {
        NetworkDataSet dataSet = this.netData();
        NetworkInputSet ipSet = dataSet.inputSet();
        NetworkInput ip;
        NetworkOutputSet opSet = null;
        int i;


        for (i = 0; i < ipSet.size(); i++) {
            if(Thread.interrupted()) break;
            ip = ipSet.inputAt(i);
            opSet = this.net.execute(ip);
            this.outs.add(opSet.nextOutput().getNetOutputs());
        }
    }

    public ObservableList<List<Double>> getOuts() {
        return outsProperty.get();
    }

    public ListProperty<List<Double>> getOutsProperty() {
        return outsProperty;
    }
}
