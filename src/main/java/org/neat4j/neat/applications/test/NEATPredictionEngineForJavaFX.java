package org.neat4j.neat.applications.test;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.neat4j.neat.data.core.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class NEATPredictionEngineForJavaFX extends MSENEATPredictionEngine implements Runnable {

    ObservableList<List<Double>> outs;
    ListProperty<List<Double>> outsProperty;
    SimpleObjectProperty<Double> errorProperty = new SimpleObjectProperty<>();
    BooleanProperty isFinished = new SimpleBooleanProperty(false);

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
        ExpectedOutputSet eOpSet = dataSet.expectedOutputSet();
        NetworkInput ip;
        NetworkOutputSet opSet = null;
        List<Double> op;
        List<Double> eOp;
        double error = 0;
        int i;
        int j;
        try (FileWriter writer = new FileWriter("outputs.csv", false)) {
            writer.write("Expected;Output\n");
            for (i = 0; i < eOpSet.size(); i++) {
                if (Thread.interrupted()) break;
                ip = ipSet.inputAt(i);
                opSet = this.net.execute(ip);

                op = opSet.nextOutput().getNetOutputs();
                this.outs.add(op);
                eOp = eOpSet.nextOutput().getNetOutputs();

                for (j = 0; j < eOp.size(); j++) {
                    if (eOp.get(j) != null) {
                        writer.append(String.valueOf(eOp.get(j)).replace(".", ",")+";"+String.valueOf(op.get(j)).replace(".", ",")+"\n");
                        error += Math.pow(eOp.get(j) - op.get(j), 2);
                    }
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        errorProperty.set(Math.sqrt(error / eOpSet.size()));
        this.isFinished.setValue(true);

    }

    public ObservableList<List<Double>> getOuts() {
        return outsProperty.get();
    }

    public ListProperty<List<Double>> getOutsProperty() {
        return outsProperty;
    }

    public Double getError() {
        return errorProperty.get();
    }

    public SimpleObjectProperty<Double> getErrorProperty() {
        return errorProperty;
    }

    public BooleanProperty getIsFinished(){
        return this.isFinished;
    }
}